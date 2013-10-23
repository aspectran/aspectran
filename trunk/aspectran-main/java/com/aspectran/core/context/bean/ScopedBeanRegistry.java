package com.aspectran.core.context.bean;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.activity.SuperTranslet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.SessionAdapter;
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
	
	private final ContextScope contextScope = new ContextScope();
	
	private final Object singletonScopeLock = new Object();

	private final Object requestScopeLock = new Object();
	
	private final Object contextScopeLock = new Object();
	
	private final Object sessionScopeLock = new Object();
	
	private final Object applicationScopeLock = new Object();

	public ScopedBeanRegistry(BeanRuleMap beanRuleMap) {
		this.beanRuleMap = beanRuleMap;

		for(BeanRule beanRule : beanRuleMap) {
			ScopeType scope = beanRule.getScopeType();

			if(scope == ScopeType.SINGLETON) {
				if(!beanRule.isRegistered() && !beanRule.isLazyInit()) {
					Object bean = createBean(beanRule);

					beanRule.setBean(bean);
					beanRule.setRegistered(true);
				}
			}
		}
	}

	public Object getBean(String id, AspectranActivity activity) {
		BeanRule beanRule = beanRuleMap.get(id);
		
		if(beanRule == null)
			throw new BeanNotFoundException(id);
		
		if(beanRule.getScopeType() == ScopeType.SINGLETON) {
			return getSingletonScopeBean(beanRule, activity);
		} else if(beanRule.getScopeType() == ScopeType.PROTOTYPE) {
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
		
		throw new BeanException();
	}
	
	private Object getSingletonScopeBean(BeanRule beanRule, AspectranActivity activity) {
		synchronized(singletonScopeLock) {
			if(beanRule.isRegistered())
				return beanRule.getBean();

			Object bean = createBean(beanRule);

			beanRule.setBean(bean);
			beanRule.setRegistered(true);

			return bean;
		}
	}

	private Object getRequestScopeBean(BeanRule beanRule, AspectranActivity activity) {
		synchronized(requestScopeLock) {
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
			throw new BeanException("This package does not supported for session scope. The specified session adapter is not exists.");
		}
		
		synchronized(sessionScopeLock) {
			SessionScope scope = (SessionScope)session.getAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE);

			if(scope == null) {
				scope = new SessionScope();
				session.setAttribute(SessionScope.SESSION_SCOPE_ATTRIBUTE, scope);
			}
			
			return getScopedBean(scope, beanRule, activity);
		}
	}

	private Object getContextScopeBean(BeanRule beanRule, AspectranActivity activity) {
		synchronized(contextScopeLock) {
			return getScopedBean(contextScope, beanRule, activity);
		}
	}
	
	private Object getApplicationScopeBean(BeanRule beanRule, AspectranActivity activity) {
		ApplicationAdapter application = activity.getApplicationAdapter();

		if(application == null) {
			throw new BeanException("This package does not supported for application scope. The specified application adapter is not exists.");
		}

		synchronized(applicationScopeLock) {
			ApplicationScope scope = (ApplicationScope)application.getAttribute(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE);

			if(scope == null) {
				scope = new ApplicationScope();
				application.setAttribute(ApplicationScope.APPLICATION_SCOPE_ATTRIBUTE, scope);
			}
			
			return getScopedBean(scope, beanRule, activity);
		}
	}
	
	private Object getScopedBean(Scope scope, BeanRule beanRule, AspectranActivity activity) {
		ScopedBeanMap scopedBeanMap = scope.getScopeBeanMap();
		ScopedBean scopeBean = scopedBeanMap.get(beanRule.getId());
			
		if(scopeBean != null)
			return scopeBean.getBean();

		Object bean = createBean(beanRule, activity);
		
		scopeBean = new ScopedBean(beanRule);
		scopeBean.setBean(bean);
		
		scopedBeanMap.putScopeBean(scopeBean);
		
		return bean;
	}
	
	private Object createBean(BeanRule beanRule) {
		return createBean(beanRule, null);
	}
	
	private Object createBean(BeanRule beanRule, AspectranActivity activity) {
		try {
			ItemTokenExpressor expressor = null;
			
			if(activity != null)
				expressor = new ItemTokenExpression(activity);
			else
				expressor = new ItemTokenExpression(this);
			
			ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
			
			if(constructorArgumentItemRuleMap != null) {
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
			} else {
				return newInstance(beanRule, new Object[0]);
			}
		} catch(Exception e) {
			throw new BeanCreationException(beanRule, e);
		}
	}
	
//	public void destoryScopeBean(ScopedBeanMap scopeBeanMap) throws Exception {
//		for(DisposableBean scopeBean : scopeBeanMap) {
//			scopeBean.destroy();
//		}
//	}
//	
//	public void destoryScopeBean(DisposableBean scopeBean) throws Exception {
//		scopeBean.destroy();
//	}
	
	public void destroy() {
		for(BeanRule beanRule : beanRuleMap) {
			try {
				ScopeType scopeType = beanRule.getScopeType();
	
				if(scopeType == ScopeType.SINGLETON) {
					if(beanRule.isRegistered()) {
						String destroyMethodName = beanRule.getDestroyMethod();
						
						if(destroyMethodName != null)
							MethodUtils.invokeMethod(beanRule.getBean(), destroyMethodName, null);
						
						beanRule.setBean(null);
						beanRule.setRegistered(false);
					}
				}
			} catch(Exception e) {
				throw new BeanDestroyFailedException(beanRule);
			}
		}
	}
	
	public void destoryBean(BeanRule beanRule, SuperTranslet translet) throws InvocationTargetException {
		if(beanRule.getScopeType() == ScopeType.SINGLETON) {
			//String destroyMethodName = beanRule.getDestroyMethod();
			
			//if(destroyMethodName != null)
			//	MethodUtils.invokeMethod(beanRule.getBean(), destroyMethodName, translet);
		}
	}
	
}
