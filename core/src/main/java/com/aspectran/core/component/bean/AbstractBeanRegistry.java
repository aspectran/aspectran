/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.component.bean.scope.RequestScope;
import com.aspectran.core.component.bean.scope.Scope;
import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.component.bean.scope.SingletonScope;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * The Class AbstractBeanRegistry.
 *
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
abstract class AbstractBeanRegistry extends AbstractBeanFactory implements BeanRegistry {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBeanRegistry.class);

    private final SingletonScope singletonScope = new SingletonScope();

    private final BeanRuleRegistry beanRuleRegistry;

    AbstractBeanRegistry(ActivityContext context, BeanRuleRegistry beanRuleRegistry) {
        super(context);
        this.beanRuleRegistry = beanRuleRegistry;
    }

    protected BeanRuleRegistry getBeanRuleRegistry() {
        return beanRuleRegistry;
    }

    @SuppressWarnings("unchecked")
    protected <V> V getBean(@NonNull BeanRule beanRule) {
        if (beanRule.getScopeType() == ScopeType.SINGLETON) {
            return (V)getSingletonScopeBean(beanRule);
        } else if (beanRule.getScopeType() == ScopeType.PROTOTYPE) {
            // Does not manage the complete lifecycle of a prototype bean.
            // In particular, Aspectran does not manage destruction phase of prototype-scoped beans.
            return getPrototypeScopeBean(beanRule);
        } else if (beanRule.getScopeType() == ScopeType.REQUEST) {
            return (V)getRequestScopeBean(beanRule);
        } else if (beanRule.getScopeType() == ScopeType.SESSION) {
            return (V)getSessionScopeBean(beanRule);
        }
        throw new BeanCreationException(beanRule);
    }

    private Object getSingletonScopeBean(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        return getScopedBean(singletonScope, beanRule);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getPrototypeScopeBean(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        Object bean = createBean(beanRule);
        if (bean != null && beanRule.isFactoryProductionRequired()) {
            bean = getFactoryProducedObject(beanRule, bean);
        }
        return (V)bean;
    }

    private Object getRequestScopeBean(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        Scope scope = getRequestScope();
        if (scope == null) {
            throw new UnsupportedBeanScopeException(ScopeType.REQUEST, beanRule);
        }
        return getScopedBean(scope, beanRule);
    }

    private Object getSessionScopeBean(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        Scope scope = getSessionScope();
        if (scope == null) {
            throw new UnsupportedBeanScopeException(ScopeType.SESSION, beanRule);
        }
        return getScopedBean(scope, beanRule);
    }

    private Object getScopedBean(@NonNull Scope scope, BeanRule beanRule) {
        ReadWriteLock scopeLock = scope.getScopeLock();
        if (scopeLock == null) {
            Object bean;
            BeanInstance instance = scope.getBeanInstance(beanRule);
            if (instance == null) {
                bean = createBean(beanRule, scope);
            } else {
                bean = instance.getBean();
            }
            if (bean != null && beanRule.isFactoryProductionRequired()) {
                bean = getFactoryProducedObject(beanRule, bean);
            }
            return bean;
        } else {
            boolean readLocked = true;
            scopeLock.readLock().lock();
            Object bean;
            try {
                BeanInstance instance = scope.getBeanInstance(beanRule);
                if (instance == null) {
                    readLocked = false;
                    scopeLock.readLock().unlock();
                    scopeLock.writeLock().lock();
                    try {
                        instance = scope.getBeanInstance(beanRule);
                        if (instance == null) {
                            bean = createBean(beanRule, scope);
                        } else {
                            bean = instance.getBean();
                        }
                        if (bean != null && beanRule.isFactoryProductionRequired()) {
                            bean = getFactoryProducedObject(beanRule, bean);
                        }
                    } finally {
                        scopeLock.writeLock().unlock();
                    }
                } else {
                    bean = instance.getBean();
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
    }

    @Nullable
    private RequestScope getRequestScope() {
        Activity activity = getActivityContext().getAvailableActivity();
        if (activity != null  && activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getRequestScope();
        } else {
            return null;
        }
    }

    @Nullable
    private SessionScope getSessionScope() {
        Activity activity = getActivityContext().getAvailableActivity();
        if (activity != null && activity.hasSessionAdapter()) {
            return activity.getSessionAdapter().getSessionScope(true);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasSingleton(Object bean) {
        ReadWriteLock scopeLock = singletonScope.getScopeLock();
        scopeLock.readLock().lock();
        try {
            return (singletonScope.getBeanRuleByInstance(bean) != null);
        } finally {
            scopeLock.readLock().unlock();
        }
    }

    @Override
    public boolean hasSingleton(@NonNull Class<?> type) {
        return hasSingleton(type, null);
    }

    @Override
    public boolean hasSingleton(@NonNull Class<?> type, @Nullable String id) {
        ReadWriteLock scopeLock = singletonScope.getScopeLock();
        scopeLock.readLock().lock();
        try {
            BeanRule[] beanRules = getBeanRuleRegistry().getBeanRules(type);
            if (beanRules == null) {
                BeanRule beanRule = getBeanRuleRegistry().getBeanRuleForConfig(type);
                if (beanRule != null) {
                    return singletonScope.containsBeanRule(beanRule);
                } else {
                    return false;
                }
            }
            if (beanRules.length == 1) {
                if (id != null) {
                    if (id.equals(beanRules[0].getId())) {
                        return singletonScope.containsBeanRule(beanRules[0]);
                    } else {
                        return false;
                    }
                } else {
                    return singletonScope.containsBeanRule(beanRules[0]);
                }
            } else {
                if (id != null) {
                    for (BeanRule beanRule : beanRules) {
                        if (id.equals(beanRule.getId())) {
                            return singletonScope.containsBeanRule(beanRule);
                        }
                    }
                } else {
                    for (BeanRule beanRule : beanRules) {
                        if (singletonScope.containsBeanRule(beanRule)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } finally {
            scopeLock.readLock().unlock();
        }
    }

    @Override
    public void destroySingleton(Object bean) throws Exception {
        ReadWriteLock scopeLock = singletonScope.getScopeLock();
        boolean readLocked = true;
        scopeLock.readLock().lock();
        try {
            BeanRule beanRule = singletonScope.getBeanRuleByInstance(bean);
            if (beanRule != null) {
                readLocked = false;
                scopeLock.readLock().unlock();
                scopeLock.writeLock().lock();
                try {
                    singletonScope.destroy(bean);
                } finally {
                    scopeLock.writeLock().unlock();
                }
            }
        } finally {
            if (readLocked) {
                scopeLock.readLock().unlock();
            }
        }
    }

    /**
     * Instantiate all singletons(non-lazy-init).
     */
    private void instantiateSingletons() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing singletons in {}", this);
        }
        for (BeanRule beanRule : beanRuleRegistry.getIdBasedBeanRules()) {
            instantiateSingleton(beanRule);
        }
        for (Set<BeanRule> beanRuleSet : beanRuleRegistry.getTypeBasedBeanRules()) {
            for (BeanRule beanRule : beanRuleSet) {
                instantiateSingleton(beanRule);
            }
        }
        for (BeanRule beanRule : beanRuleRegistry.getConfigurableBeanRules()) {
            instantiateSingleton(beanRule);
        }
    }

    private void instantiateSingleton(@NonNull BeanRule beanRule) {
        if (beanRule.isSingleton() && !beanRule.isLazyInit()
                && !singletonScope.containsBeanRule(beanRule)) {
            createBean(beanRule, singletonScope);
        }
    }

    /**
     * Destroy all cached singletons.
     */
    private void destroySingletons() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying singletons in {}", this);
        }
        singletonScope.destroy();
    }

    @Override
    protected void doInitialize() throws Exception {
        instantiateSingletons();
    }

    @Override
    protected void doDestroy() throws Exception {
        destroySingletons();
    }

}
