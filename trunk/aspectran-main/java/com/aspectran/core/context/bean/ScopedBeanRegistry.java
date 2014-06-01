package com.aspectran.core.context.bean;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.ScopedBean;
import com.aspectran.core.context.bean.scope.ScopedBeanMap;
import com.aspectran.core.var.rule.BeanRule;
import com.aspectran.core.var.rule.BeanRuleMap;
import com.aspectran.core.var.token.ItemTokenExpression;
import com.aspectran.core.var.token.ItemTokenExpressor;
import com.aspectran.core.var.type.ScopeType;

/**
 * SINGLETON: 모든 singleton 빈은context 생성시 초기화 된다.
 * APPLICATION: 최초 참조시 초기화 된다.
 * 초기화 시점이 다르지만, 소멸 시점은 동일하다.(context 소멸시) 
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class ScopedBeanRegistry extends AbstractBeanRegistry implements LocalBeanRegistry {

	private final Object singletonScopeLock = new Object();

	private final Object requestScopeLock = new Object();
	
	private final Object sessionScopeLock = new Object();
	
	private final Object applicationScopeLock = new Object();
	
	public ScopedBeanRegistry(BeanRuleMap beanRuleMap) {
		super(beanRuleMap);
	}

	public Object getBean(String id) {
		BeanRule beanRule = beanRuleMap.get(id);
		
		if(beanRule == null)
			throw new BeanNotFoundException(id);
		
		if(beanRule.getScopeType() == ScopeType.PROTOTYPE) {
			return getPrototypeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.SINGLETON) {
			return getSingletonScopeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.REQUEST) {
			return getRequestScopeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.SESSION) {
			return getSessionScopeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.APPLICATION) {
			return getApplicationScopeBean(beanRule);
		}
		
		throw new BeanException();
	}
	
	private Object getPrototypeBean(BeanRule beanRule) {
		CoreActivity activity = ActivityContext.getCoreActivity();
		Object bean;
		
		if(activity == null)
			bean = createBean(beanRule);
		else
			bean = createBean(beanRule, activity);

		return bean;
	}
	
	private Object getSingletonScopeBean(BeanRule beanRule) {
		CoreActivity activity = ActivityContext.getCoreActivity();

		synchronized(singletonScopeLock) {
			if(beanRule.isRegistered())
				return beanRule.getBean();

			Object bean;
			
			if(activity == null)
				bean = createBean(beanRule);
			else
				bean = createBean(beanRule, activity);

			beanRule.setBean(bean);
			beanRule.setRegistered(true);

			return bean;
		}
	}

	private Object getRequestScopeBean(BeanRule beanRule) {
		CoreActivity activity = ActivityContext.getCoreActivity();
		
		synchronized(requestScopeLock) {
			if(activity == null)
				throw new UnsupportedBeanScopeException(ScopeType.REQUEST, beanRule);
			
			Scope scope = activity.getRequestScope();
			
			if(scope == null) {
				scope = new RequestScope();
				activity.setRequestScope(scope);
			}
			
			return getScopedBean(scope, beanRule, activity);
		}
	}
	
	private Object getSessionScopeBean(BeanRule beanRule) {
		CoreActivity activity = ActivityContext.getCoreActivity();

		SessionAdapter session = null;
		
		if(activity != null)
			session = activity.getSessionAdapter();

		if(session == null)
			throw new UnsupportedBeanScopeException(ScopeType.SESSION, beanRule);
		
		synchronized(sessionScopeLock) {
			Scope scope = session.getScope();
			return getScopedBean(scope, beanRule, activity);
		}
	}

	private Object getApplicationScopeBean(BeanRule beanRule) {
		CoreActivity activity = ActivityContext.getCoreActivity();

		ApplicationAdapter application = null;
		
		if(activity != null)
			application = activity.getApplicationAdapter();

		if(application == null)
			throw new UnsupportedBeanScopeException(ScopeType.APPLICATION, beanRule);

		synchronized(applicationScopeLock) {
			Scope scope = application.getScope();
			return getScopedBean(scope, beanRule, activity);
		}
	}
	
	private Object getScopedBean(Scope scope, BeanRule beanRule, CoreActivity activity) {
		ScopedBeanMap scopedBeanMap = scope.getScopedBeanMap();
		ScopedBean scopeBean = scopedBeanMap.get(beanRule.getId());
			
		if(scopeBean != null)
			return scopeBean.getBean();

		Object bean;
		
		if(activity == null)
			bean = createBean(beanRule);
		else
			bean = createBean(beanRule, activity);
		
		scopeBean = new ScopedBean(beanRule);
		scopeBean.setBean(bean);
		
		scopedBeanMap.putScopeBean(scopeBean);
		
		return bean;
	}
	
	protected Object createBean(BeanRule beanRule) {
		ItemTokenExpressor expressor = new ItemTokenExpression(this);
		return createBean(beanRule, expressor);
	}
	
	protected Object createBean(BeanRule beanRule, CoreActivity activity) {
		ItemTokenExpressor expressor = new ItemTokenExpression(activity);
		return createBean(beanRule, expressor);
	}
	
}
