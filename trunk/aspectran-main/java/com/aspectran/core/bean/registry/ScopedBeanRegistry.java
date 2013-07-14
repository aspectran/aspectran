package com.aspectran.core.bean.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aspectran.base.adapter.ApplicationAdapter;
import com.aspectran.base.adapter.SessionAdapter;
import com.aspectran.base.rule.BeanRule;
import com.aspectran.base.rule.BeanRuleMap;
import com.aspectran.base.rule.ItemRule;
import com.aspectran.base.rule.ItemRuleMap;
import com.aspectran.base.token.expression.ValueExpression;
import com.aspectran.base.token.expression.ValueExpressor;
import com.aspectran.base.type.ScopeType;
import com.aspectran.base.util.MethodUtils;
import com.aspectran.base.variable.ValueMap;
import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.bean.BeansException;
import com.aspectran.core.bean.ScopeBean;
import com.aspectran.core.bean.ScopeBeanMap;
import com.aspectran.core.bean.ablility.Disposable;
import com.aspectran.core.bean.scope.ApplicationScope;
import com.aspectran.core.bean.scope.ContextScope;
import com.aspectran.core.bean.scope.RequestScope;
import com.aspectran.core.bean.scope.Scope;
import com.aspectran.core.bean.scope.SessionScope;
import com.aspectran.core.translet.SuperTranslet;

/**
 * SINGLETON: 모든 singleton 빈은context 생성시 초기화 된다.
 * APPLICATION: 최초 참조시 초기화 된다.
 * 초기화 시점이 다르지만, 소멸 시점은 동일하다.(context 소멸시) 
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class ScopedBeanRegistry extends AbstractBeanRegistry implements BeanRegistry {

	private BeanRuleMap beanRuleMap;
	
	private ContextScope contextScope = new ContextScope();
	
	private Lock sessionScopeLock = new ReentrantLock(true);

	private Lock applicationScopeLock = new ReentrantLock(true);

	public ScopedBeanRegistry(BeanRuleMap beanRuleMap) {
		this.beanRuleMap = beanRuleMap;

		for(BeanRule br : beanRuleMap) {
			ScopeType scope = br.getScopeType();

			if(scope == ScopeType.SINGLETON) {
				if(!br.isRegistered()) {
					Object bean = createBean(br);

					br.setBean(bean);
					br.setRegistered(true);
				}
			}
		}
	}

	public Object getBean(String id, AspectranActivity activity) {
		BeanRule beanRule = beanRuleMap.get(id);
		
		if(beanRule == null)
			throw new BeanNotFoundException(id);
		
		if(beanRule.getScopeType() == ScopeType.SINGLETON)
			return beanRule.getBean();

		if(activity == null)
			return createBean(beanRule);
			
		if(beanRule.getScopeType() == ScopeType.PROTOTYPE) {
			return createBean(beanRule, activity);
		} else if(beanRule.getScopeType() == ScopeType.REQUEST) {
			return getRequestScopeBean(beanRule, activity);
		} else if(beanRule.getScopeType() == ScopeType.SESSION) {
			return getSessionScopeBean(beanRule, activity);
		} else if(beanRule.getScopeType() == ScopeType.CONTEXT) {
			return getContextScopeBean(beanRule, activity);
		} else if(beanRule.getScopeType() == ScopeType.APPLICATION) {
			return getApplicationScopeBean(beanRule, activity);
		}
		
		throw new BeansException();
	}
	
	private Object getRequestScopeBean(BeanRule beanRule, AspectranActivity activity) {
		synchronized(this) {
			RequestScope scope = activity.getRequestScope();
			
			if(scope == null) {
				scope = new RequestScope();
				activity.setRequestScope(scope);
			}
			
			return getScopeBean(scope, beanRule, activity);
		}
	}

	private Object getSessionScopeBean(BeanRule beanRule, AspectranActivity activity) {
		SuperTranslet translet = (SuperTranslet)activity.getTransletInstance();
		SessionAdapter session = translet.getSessionAdapter();
		
		if(session == null) {
			throw new BeansException("This package does not supported for session scope. The specified session adapter is not exists.");
		}
		
		sessionScopeLock.lock();
		
		try {
			SessionScope scope = (SessionScope)session.getAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE);

			if(scope == null) {
				scope = new SessionScope();
				session.setAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE, scope);
			}
			
			return getScopeBean(scope, beanRule, activity);
		} finally {
			sessionScopeLock.unlock();
		}
	}

	private Object getContextScopeBean(BeanRule beanRule, AspectranActivity activity) {
		synchronized (contextScope) {
			return getScopeBean(contextScope, beanRule, activity);
		}
	}
	
	private Object getApplicationScopeBean(BeanRule beanRule, AspectranActivity activity) {
		ApplicationAdapter application = activity.getApplicationAdapter();

		if(application == null) {
			throw new BeansException("This package does not supported for application scope. The specified application adapter is not exists.");
		}

		applicationScopeLock.lock();

		try {
			ApplicationScope scope = (ApplicationScope)application.getAttribute(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE);

			if(scope == null) {
				scope = new ApplicationScope();
				application.setAttribute(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE, scope);
			}
			
			return getScopeBean(scope, beanRule, activity);
		} finally {
			applicationScopeLock.unlock();
		}
	}
	
	private Object getScopeBean(Scope scope, BeanRule beanRule, AspectranActivity activity) {
		ScopeBeanMap sbm = scope.getScopeBeanMap();
		ScopeBean scopeBean = sbm.get(beanRule.getId());
			
		if(scopeBean != null)
			return scopeBean.getBean();

		Object bean = createBean(beanRule, activity);
		
		scopeBean = new ScopeBean(beanRule);
		scopeBean.setBean(bean);
		
		sbm.putScopeBean(scopeBean);
		
		return bean;

	}
	
	private Object createBean(BeanRule beanRule) {
		ValueExpressor expressor = new ValueExpression(this);

		return createBean(beanRule, expressor);
	}
	
	private Object createBean(BeanRule beanRule, AspectranActivity activity) {
		ValueExpressor expressor = new ValueExpression(activity);

		return createBean(beanRule, expressor);
	}

	private Object createBean(BeanRule beanRule, ValueExpressor expressor) {
		try {
			ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
			ValueMap valueMap = expressor.express(constructorArgumentItemRuleMap);

			int parameterSize = constructorArgumentItemRuleMap.size();
			Object[] args = new Object[parameterSize];
			
			Iterator<ItemRule> iter = constructorArgumentItemRuleMap.iterator();
			int i = 0;
			
			while(iter.hasNext()) {
				ItemRule ir = iter.next();
				Object o = valueMap.get(ir.getName());
				args[i] = o;
				
				i++;
			}
			
			return newInstance(beanRule, args);
		} catch(Exception e) {
			throw new BeanCreationException(beanRule, e);
		}
	}
	
	public void destoryScopeBean(ScopeBeanMap scopeBeanMap) throws Exception {
		for(Disposable scopeBean : scopeBeanMap) {
			scopeBean.destroy();
		}
	}
	
	public void destoryScopeBean(Disposable scopeBean) throws Exception {
		scopeBean.destroy();
	}
	
	public void destroy() throws Exception {
		for(BeanRule br : beanRuleMap) {
			ScopeType scopeType = br.getScopeType();

			if(scopeType == ScopeType.SINGLETON) {
				if(br.isRegistered()) {
					String destroyMethodName = br.getDestroyMethod();
					
					if(destroyMethodName != null)
						MethodUtils.invokeMethod(br.getBean(), destroyMethodName, null);
					
					br.setBean(null);
					br.setRegistered(false);
				}
			}
		}
	}
	
	public void destoryBean(BeanRule beanRule, SuperTranslet translet) throws InvocationTargetException {
		if(beanRule.getScopeType() == ScopeType.SINGLETON) {
			String destroyMethodName = beanRule.getDestroyMethod();
			
			//if(destroyMethodName != null)
			//	MethodUtils.invokeMethod(beanRule.getBean(), destroyMethodName, translet);
		}
	}
	
}
