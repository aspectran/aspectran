/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.core.component.bean.event.EventListenerRegistry;
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
 * Base implementation of the Aspectran {@link BeanRegistry}.
 * <p>
 * Provides retrieval and lifecycle management of beans across supported
 * scopes (singleton, request, session). Delegates instantiation to the
 * underlying {@link AbstractBeanFactory}, coordinates FactoryBean handling,
 * and interacts with {@link BeanRuleRegistry} to resolve bean definitions.
 * </p>
 */
abstract class AbstractBeanRegistry extends AbstractBeanFactory implements BeanRegistry {

    private static final Logger logger = LoggerFactory.getLogger(AbstractBeanRegistry.class);

    private final SingletonScope singletonScope = new SingletonScope();

    private final EventListenerRegistry eventListenerRegistry = new  EventListenerRegistry();

    private final BeanRuleRegistry beanRuleRegistry;

    /**
     * Instantiates a new Abstract bean registry.
     * @param context the activity context
     * @param beanRuleRegistry the bean rule registry
     */
    AbstractBeanRegistry(ActivityContext context, BeanRuleRegistry beanRuleRegistry) {
        super(context);
        this.beanRuleRegistry = beanRuleRegistry;
    }

    /**
     * Retrieves the {@link EventListenerRegistry} instance associated with this bean registry.
     * The {@code EventListenerRegistry} is responsible for managing the registration of
     * event listener methods and enabling efficient event dispatching.
     * @return the {@link EventListenerRegistry} instance
     */
    public EventListenerRegistry getEventListenerRegistry() {
        return eventListenerRegistry;
    }

    /**
     * Retrieves the {@link BeanRuleRegistry} instance associated with this bean registry.
     * The {@code BeanRuleRegistry} contains and manages the definitions of bean rules
     * and their associated metadata, enabling the framework to create and manage beans
     * in accordance with these rules.
     * @return the {@link BeanRuleRegistry} instance
     */
    public BeanRuleRegistry getBeanRuleRegistry() {
        return beanRuleRegistry;
    }

    @Override
    public Set<String> getBasePackages() {
        return beanRuleRegistry.getBasePackages();
    }

    /**
     * Returns the bean instance for the given bean rule.
     * @param beanRule the bean rule
     * @param <V> the type of the bean
     * @return the bean instance
     */
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
        BeanInstance instance = getScopedBean(singletonScope, beanRule);
        if (beanRule.isFactoryProductionRequired()) {
            return resolveFactoryProducedBean(beanRule, instance, singletonScope);
        } else {
            return instance.getBean();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getPrototypeScopeBean(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        Object bean = createBean(beanRule, null);
        if (bean != null && beanRule.isFactoryProductionRequired()) {
            bean = getFactoryProducedObject(beanRule, bean, null);
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
        BeanInstance instance = getScopedBean(scope, beanRule);
        if (beanRule.isFactoryProductionRequired()) {
            return resolveFactoryProducedBean(beanRule, instance, scope);
        } else {
            return instance.getBean();
        }
    }

    private Object getSessionScopeBean(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        Scope scope = getSessionScope();
        if (scope == null) {
            throw new UnsupportedBeanScopeException(ScopeType.SESSION, beanRule);
        }
        BeanInstance instance = getScopedBean(scope, beanRule);
        if (beanRule.isFactoryProductionRequired()) {
            return resolveFactoryProducedBean(beanRule, instance, scope);
        } else {
            return instance.getBean();
        }
    }

    private Object resolveFactoryProducedBean(@NonNull BeanRule beanRule, @NonNull BeanInstance instance, @Nullable Scope scope) {
        Object beanToReturn;
        if (instance.getFactory() != null) {
            // Factory exists, check if product is cached
            if (instance.getBean() != null) {
                // Product is cached, return it
                beanToReturn = instance.getBean();
            } else {
                // Product not cached (isSingleton()=false), create new product
                beanToReturn = getFactoryProducedObject(beanRule, instance.getFactory(), scope);
            }
        } else {
            // No factory, instance.getBean() is the candidate
            beanToReturn = getFactoryProducedObject(beanRule, instance.getBean(), scope);
        }
        return beanToReturn;
    }

    private BeanInstance getScopedBean(@NonNull Scope scope, BeanRule beanRule) {
        ReadWriteLock scopeLock = scope.getScopeLock();
        if (scopeLock == null) {
            BeanInstance instance = scope.getBeanInstance(beanRule);
            if (instance == null) {
                Object bean = createBean(beanRule, scope);
                if (bean != null && beanRule.isFactoryProductionRequired()) {
                    // If it's a factory bean, getFactoryProducedObject will put the BeanInstance(product, factory) into scope
                    getFactoryProducedObject(beanRule, bean, scope);
                    instance = scope.getBeanInstance(beanRule); // Retrieve the updated BeanInstance
                } else {
                    instance = BeanInstance.forProduct(bean);
                    scope.putBeanInstance(beanRule, instance);
                }
            }
            return instance;
        } else {
            boolean readLocked = true;
            scopeLock.readLock().lock();
            BeanInstance instance;
            try {
                instance = scope.getBeanInstance(beanRule);
                if (instance == null) {
                    readLocked = false;
                    scopeLock.readLock().unlock();
                    scopeLock.writeLock().lock();
                    try {
                        // Double-check inside the write lock
                        instance = scope.getBeanInstance(beanRule);
                        if (instance == null) {
                            Object bean = createBean(beanRule, scope);
                            if (bean != null && beanRule.isFactoryProductionRequired()) {
                                // Synchronize on the factory bean instance to ensure thread-safe object production.
                                synchronized (bean) {
                                    getFactoryProducedObject(beanRule, bean, scope);
                                    instance = scope.getBeanInstance(beanRule); // Retrieve the updated BeanInstance
                                }
                            } else {
                                instance = BeanInstance.forProduct(bean);
                                scope.putBeanInstance(beanRule, instance);
                            }
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
            return instance;
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
    protected Object createBean(@NonNull BeanRule beanRule, Scope scope) {
        Object bean = super.createBean(beanRule, scope);
        if (beanRule.isSingleton()) {
            eventListenerRegistry.registerListener(bean);
        }
        return bean;
    }

    @Override
    public boolean hasSingleton(Object bean) {
        ReadWriteLock scopeLock = singletonScope.getScopeLock();
        scopeLock.readLock().lock();
        try {
            return singletonScope.hasInstance(bean);
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
            if (singletonScope.hasInstance(bean)) {
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
