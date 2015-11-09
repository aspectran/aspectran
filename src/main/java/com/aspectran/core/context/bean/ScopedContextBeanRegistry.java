/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.bean;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.bean.scope.RequestScope;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.bean.scope.ScopedBean;
import com.aspectran.core.context.bean.scope.ScopedBeanMap;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.BeanRuleMap;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.ClassUtils;

/**
 * 
 * The Class ScopedContextBeanRegistry.
 * 
 * <p>Created: 2009. 03. 09 오후 23:48:09</p>
 */
public class ScopedContextBeanRegistry extends AbstractContextBeanRegistry {

	private final Object singletonScopeLock = new Object();

	private final Object requestScopeLock = new Object();
	
	private final Object sessionScopeLock = new Object();
	
	private final Object applicationScopeLock = new Object();
	
	public ScopedContextBeanRegistry(ActivityContext context, BeanRuleMap beanRuleMap, BeanProxifierType beanProxifierType) {
		super(context, beanRuleMap, beanProxifierType);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.context.bean.BeanRegistry#getBean(java.lang.String)
	 */
	public <T> T getBean(String id) {
		BeanRule beanRule = beanRuleMap.get(id);

		if(beanRule == null)
			throw new BeanNotFoundException(id);
		
		return getBean(beanRule);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.context.bean.BeanRegistry#getBean(java.lang.Class)
	 */
	public <T> T getBean(Class<T> classType) {
		BeanRule beanRule = classBeanRuleMap.get(classType);
		
		if(beanRule == null)
			throw new BeanNotFoundException(classType.getName());
		
		return getBean(beanRule);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.context.bean.BeanRegistry#getBean(java.lang.String, java.lang.Class)
	 */
	public <T> T getBean(String id, Class<T> requiredType) {
		BeanRule beanRule = beanRuleMap.get(id);

		if(beanRule == null)
			throw new BeanNotFoundException(id);

		if(!ClassUtils.isAssignable(beanRule.getBeanClass(), requiredType)) {
			throw new BeanNotOfRequiredTypeException(beanRule, requiredType);
		}
		
		return getBean(beanRule);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getBean(BeanRule beanRule) {
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
		Activity activity = context.getCurrentActivity();
		
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
		Activity activity = context.getCurrentActivity();

		if(activity == null || activity.getSessionAdapter() == null)
			return null;

		return activity.getSessionAdapter().getSessionScope();
	}
	
	private Scope getApplicationScope() {
		return context.getApplicationAdapter().getApplicationScope();
	}
	
}
