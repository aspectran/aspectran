package com.aspectran.core.context.bean;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.SuperTranslet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.ablility.DisposableBean;
import com.aspectran.core.context.bean.scope.ApplicationScope;
import com.aspectran.core.context.bean.scope.ContextScope;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.ScopedBean;
import com.aspectran.core.context.bean.scope.ScopedBeanMap;
import com.aspectran.core.context.bean.scope.SessionScope;
import com.aspectran.core.rule.BeanRule;
import com.aspectran.core.rule.BeanRuleMap;
import com.aspectran.core.rule.ItemRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.token.expression.ItemTokenExpression;
import com.aspectran.core.token.expression.ItemTokenExpressor;
import com.aspectran.core.type.ScopeType;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.var.ValueMap;

/**
 * SINGLETON: 모든 singleton 빈은context 생성시 초기화 된다.
 * APPLICATION: 최초 참조시 초기화 된다.
 * 초기화 시점이 다르지만, 소멸 시점은 동일하다.(context 소멸시) 
 * <p>
 * Created: 2009. 03. 09 오후 23:48:09
 * </p>
 */
public class ScopedBeanRegistry extends AbstractBeanRegistry implements BeanRegistry {

	private final BeanRuleMap beanRuleMap;
	
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
			
			return getScopedBean(scope, beanRule, activity);
		}
	}

	private Object getSessionScopeBean(BeanRule beanRule, AspectranActivity activity) {
		SessionAdapter session = activity.getSessionAdapter();
		
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
			
			return getScopedBean(scope, beanRule, activity);
		} finally {
			sessionScopeLock.unlock();
		}
	}

	private Object getContextScopeBean(BeanRule beanRule, AspectranActivity activity) {
		synchronized (contextScope) {
			return getScopedBean(contextScope, beanRule, activity);
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
			
			return getScopedBean(scope, beanRule, activity);
		} finally {
			applicationScopeLock.unlock();
		}
	}
	
	private Object getScopedBean(Scope scope, BeanRule beanRule, AspectranActivity activity) {
		ScopedBeanMap sbm = scope.getScopeBeanMap();
		ScopedBean scopeBean = sbm.get(beanRule.getId());
			
		if(scopeBean != null)
			return scopeBean.getBean();

		Object bean = createBean(beanRule, activity);
		
		scopeBean = new ScopedBean(beanRule);
		scopeBean.setBean(bean);
		
		sbm.putScopeBean(scopeBean);
		
		return bean;

	}
	
	private Object createBean(BeanRule beanRule) {
		ItemTokenExpressor expressor = new ItemTokenExpression(this);

		return createBean(beanRule, expressor);
	}
	
	private Object createBean(BeanRule beanRule, AspectranActivity activity) {
		ItemTokenExpressor expressor = new ItemTokenExpression(activity);

		return createBean(beanRule, expressor);
	}

	private Object createBean(BeanRule beanRule, ItemTokenExpressor expressor) {
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
	
	public void destoryScopeBean(ScopedBeanMap scopeBeanMap) throws Exception {
		for(DisposableBean scopeBean : scopeBeanMap) {
			scopeBean.destroy();
		}
	}
	
	public void destoryScopeBean(DisposableBean scopeBean) throws Exception {
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
