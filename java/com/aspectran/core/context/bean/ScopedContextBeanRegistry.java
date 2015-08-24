package com.aspectran.core.context.bean;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.ScopedBean;
import com.aspectran.core.context.bean.scope.ScopedBeanMap;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.rule.type.BeanProxyModeType;
import com.aspectran.core.context.rule.type.ScopeType;

/**
 * SINGLETON: 모든 singleton 빈은context 생성시 초기화 된다.
 * APPLICATION: 최초 참조시 초기화 된다.
 * 초기화 시점이 다르지만, 소멸 시점은 동일하다.(context 소멸시) 
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class ScopedContextBeanRegistry extends AbstractContextBeanRegistry {

	private final Object singletonScopeLock = new Object();

	private final Object requestScopeLock = new Object();
	
	private final Object sessionScopeLock = new Object();
	
	private final Object applicationScopeLock = new Object();
	
	public ScopedContextBeanRegistry(ActivityContext context, BeanRuleMap beanRuleMap, BeanProxyModeType beanProxyMode) {
		super(context, beanRuleMap, beanProxyMode);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(String id) {
		BeanRule beanRule = beanRuleMap.get(id);
		
		if(beanRule == null)
			throw new BeanNotFoundException(id);
		
		if(beanRule.getScopeType() == ScopeType.PROTOTYPE) {
			return (T)createBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.SINGLETON) {
			return (T)getSingletonScopeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.REQUEST) {
			return (T)getRequestScopeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.SESSION) {
			return (T)getSessionScopeBean(beanRule);
		} else if(beanRule.getScopeType() == ScopeType.APPLICATION) {
			return (T)getApplicationScopeBean(beanRule);
		}
		
		throw new BeanException();
	}
	
	private Object getSingletonScopeBean(BeanRule beanRule) {
		synchronized(singletonScopeLock) {
			if(beanRule.isRegistered())
				return beanRule.getBean();

			Object bean = createBean(beanRule);

			beanRule.setBean(bean);
			beanRule.setRegistered(true);

			return bean;
		}
	}

	private Object getRequestScopeBean(BeanRule beanRule) {
		synchronized(requestScopeLock) {
			Scope scope = getRequestScope();
			
			if(scope == null)
				throw new UnsupportedBeanScopeException(ScopeType.REQUEST, beanRule);
			
			return getScopedBean(scope, beanRule);
		}
	}
	
	private Object getSessionScopeBean(BeanRule beanRule) {
		Scope scope = getSessionScope();

		if(scope == null)
			throw new UnsupportedBeanScopeException(ScopeType.SESSION, beanRule);
		
		synchronized(sessionScopeLock) {
			return getScopedBean(scope, beanRule);
		}
	}

	private Object getApplicationScopeBean(BeanRule beanRule) {
		Scope scope = getApplicationScope();

		if(scope == null)
			throw new UnsupportedBeanScopeException(ScopeType.APPLICATION, beanRule);

		synchronized(applicationScopeLock) {
			return getScopedBean(scope, beanRule);
		}
	}
	
	private Object getScopedBean(Scope scope, BeanRule beanRule) {
		ScopedBeanMap scopedBeanMap = scope.getScopedBeanMap();
		ScopedBean scopeBean = scopedBeanMap.get(beanRule.getId());
			
		if(scopeBean != null)
			return scopeBean.getBean();

		Object bean = createBean(beanRule);
		
		scopeBean = new ScopedBean(beanRule);
		scopeBean.setBean(bean);
		
		scopedBeanMap.putScopeBean(scopeBean);
		
		return bean;
	}
	
	private Scope getRequestScope() {
		Activity activity = context.getLocalActivity();
		
		if(activity == null)
			return null;
		
		Scope requestScope = activity.getRequestScope();
		
		if(requestScope == null) {
			requestScope = new RequestScope();
			activity.setRequestScope(requestScope);
		}
		
		return requestScope;
	}

	private Scope getSessionScope() {
		Activity activity = context.getLocalActivity();

		if(activity == null)
			return null;

		return activity.getSessionAdapter().getScope();
	}
	
	private Scope getApplicationScope() {
		return context.getApplicationAdapter().getScope();
	}
	
}
