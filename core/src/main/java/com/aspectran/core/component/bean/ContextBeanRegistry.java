/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.component.bean;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.scope.Scope;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.BeanProxifierType;
import com.aspectran.core.context.rule.type.ScopeType;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class ContextBeanRegistry.
 * 
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public class ContextBeanRegistry extends AbstractBeanRegistry {

    private final ReadWriteLock singletonScopeLock = new ReentrantReadWriteLock();

    public ContextBeanRegistry(ActivityContext context, BeanRuleRegistry beanRuleRegistry,
                               BeanProxifierType beanProxifierType) {
        super(context, beanRuleRegistry, beanProxifierType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(BeanRule beanRule) {
        if (beanRule.getScopeType() == ScopeType.PROTOTYPE) {
            // Does not manage the complete lifecycle of a prototype bean.
            return (T)getPrototypeScopeBean(beanRule);
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

    private Object getPrototypeScopeBean(BeanRule beanRule) {
        Object bean = createBean(beanRule);
        if (bean != null && beanRule.isFactoryProductionRequired()) {
            bean = getFactoryProducedObject(beanRule, bean);
        }
        return bean;
    }

    private Object getSingletonScopeBean(BeanRule beanRule) {
        boolean readLocked = true;
        singletonScopeLock.readLock().lock();
        Object bean;
        try {
            InstantiatedBean instantiatedBean = beanRule.getInstantiatedBean();
            if (instantiatedBean == null) {
                readLocked = false;
                singletonScopeLock.readLock().unlock();
                singletonScopeLock.writeLock().lock();
                try {
                    instantiatedBean = beanRule.getInstantiatedBean();
                    if (instantiatedBean == null) {
                        bean = createBean(beanRule);
                    } else {
                        bean = instantiatedBean.getBean();
                    }
                    if (bean != null && beanRule.isFactoryProductionRequired()) {
                        bean = getFactoryProducedObject(beanRule, bean);
                    }
                } finally {
                    singletonScopeLock.writeLock().unlock();
                }
            } else {
                instantiatedBean = beanRule.getInstantiatedBean();
                bean = instantiatedBean.getBean();
                if (bean != null && beanRule.isFactoryProductionRequired()) {
                    readLocked = false;
                    singletonScopeLock.readLock().unlock();
                    singletonScopeLock.writeLock().lock();

                    try {
                        bean = getFactoryProducedObject(beanRule, bean);
                    } finally {
                        singletonScopeLock.writeLock().unlock();
                    }
                }
            }
        } finally {
            if (readLocked) {
                singletonScopeLock.readLock().unlock();
            }
        }
        return bean;
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
        Object bean;
        try {
            InstantiatedBean instantiatedBean = scope.getInstantiatedBean(beanRule);
            if (instantiatedBean == null) {
                readLocked = false;
                scopeLock.readLock().unlock();
                scopeLock.writeLock().lock();
                try {
                    instantiatedBean = scope.getInstantiatedBean(beanRule);
                    if (instantiatedBean == null) {
                        bean = createBean(beanRule);
                        scope.putInstantiatedBean(beanRule, new InstantiatedBean(bean));
                    } else {
                        bean = instantiatedBean.getBean();
                    }
                    if (beanRule.isFactoryProductionRequired()) {
                        bean = getFactoryProducedObject(beanRule, bean);
                    }
                } finally {
                    scopeLock.writeLock().unlock();
                }
            } else {
                bean = instantiatedBean.getBean();
                if (bean != null && beanRule.isFactoryProductionRequired()) {
                    readLocked = false;
                    scopeLock.readLock().unlock();
                    scopeLock.writeLock().lock();
                    try {
                        bean = getFactoryProducedObject(beanRule, bean);
                    } finally {
                        scopeLock.writeLock().unlock();
                    }
                }
            }
        } finally {
            if (readLocked) {
                scopeLock.readLock().unlock();
            }
        }
        return bean;
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
        return context.getEnvironment().getApplicationAdapter().getApplicationScope();
    }

}
