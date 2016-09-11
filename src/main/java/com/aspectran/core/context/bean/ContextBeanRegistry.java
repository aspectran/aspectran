/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.bean;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.bean.scope.Scope;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.ScopeType;

/**
 * The Class ContextBeanRegistry.
 * 
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public class ContextBeanRegistry extends AbstractBeanRegistry {

	private final ReadWriteLock singletonScopeLock = new ReentrantReadWriteLock();

	public ContextBeanRegistry(BeanRuleRegistry beanRuleRegistry, BeanProxifierType beanProxifierType) {
		super(beanRuleRegistry, beanProxifierType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getBean(BeanRule beanRule) {
		if (beanRule.getScopeType() == ScopeType.PROTOTYPE) {
			return (T)createBean(beanRule);
		} else if (beanRule.getScopeType() == ScopeType.SINGLETON) {
			return (T)getSingletonScopeBean(beanRule);
		} else if (beanRule.getScopeType() == ScopeType.REQUEST) {
			return (T)getRequestScopeBean(beanRule);
		} else if (beanRule.getScopeType() == ScopeType.SESSION) {
			return (T)getSessionScopeBean(beanRule);
		} else if (beanRule.getScopeType() == ScopeType.APPLICATION) {
			return (T)getApplicationScopeBean(beanRule);
		}
		
		throw new BeanException();
	}

	private Object getSingletonScopeBean(BeanRule beanRule) {
		boolean readLocked = true;
		singletonScopeLock.readLock().lock();

		try {
			if (!beanRule.isRegistered()) {
				readLocked = false;
				singletonScopeLock.readLock().unlock();
				singletonScopeLock.writeLock().lock();

				try {
					if (!beanRule.isRegistered()) {
						beanRule.setBean(createBean(beanRule));
						beanRule.setRegistered(true);
					}
				} finally {
					singletonScopeLock.writeLock().unlock();
				}
			}
		} finally {
			if (readLocked) {
				singletonScopeLock.readLock().unlock();
			}
		}

		return beanRule.getBean();
	}

	private Object getRequestScopeBean(BeanRule beanRule) {
		Scope scope = getRequestScope();
		if (scope == null) {
			throw new UnsupportedBeanScopeException(ScopeType.REQUEST, beanRule);
		}
		return getScopedBean(scope, beanRule);
	}
	
	private Object getSessionScopeBean(BeanRule beanRule) {
		Scope scope = getSessionScope();
		if (scope == null) {
			throw new UnsupportedBeanScopeException(ScopeType.SESSION, beanRule);
		}
		return getScopedBean(scope, beanRule);
	}

	private Object getApplicationScopeBean(BeanRule beanRule) {
		Scope scope = getApplicationScope();
		if (scope == null) {
			throw new UnsupportedBeanScopeException(ScopeType.APPLICATION, beanRule);
		}
		return getScopedBean(scope, beanRule);
	}
	
	private Object getScopedBean(Scope scope, BeanRule beanRule) {
		ReadWriteLock scopeLock = scope.getScopeLock();

		boolean readLocked = true;
		scopeLock.readLock().lock();

		try {
			if (!beanRule.isRegistered()) {
				readLocked = false;
				scopeLock.readLock().unlock();
				scopeLock.writeLock().lock();

				try {
					if (!beanRule.isRegistered()) {
						beanRule.setBean(createBean(beanRule));
						beanRule.setRegistered(true);
					}
				} finally {
					scopeLock.writeLock().unlock();
				}
			}
		} finally {
			if (readLocked) {
				scopeLock.readLock().unlock();
			}
		}

		return beanRule.getBean();
	}

	private Scope getRequestScope() {
		Activity activity = context.getCurrentActivity();
		if (activity != null) {
			RequestAdapter requestAdapter = activity.getRequestAdapter();
			if (requestAdapter != null) {
				return requestAdapter.getRequestScope();
			}
		}
		return null;
	}

	private Scope getSessionScope() {
		Activity activity = context.getCurrentActivity();
		if (activity != null) {
			SessionAdapter sessionAdapter = activity.getSessionAdapter();
			if (sessionAdapter != null) {
				return sessionAdapter.getSessionScope();
			}
		}
		return null;
	}
	
	private Scope getApplicationScope() {
		return context.getApplicationAdapter().getApplicationScope();
	}
	
}
