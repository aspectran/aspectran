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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getBasePackages() {
        return beanRuleRegistry.getBasePackages();
    }

    /**
     * Returns the bean instance for the given bean rule, dispatching to the
     * appropriate scope handler.
     * @param beanRule the bean rule
     * @param <V> the type of the bean
     * @return the bean instance
     * @throws BeanCreationException if the bean could not be created
     * @throws UnsupportedBeanScopeException if the specified scope is not supported
     */
    @SuppressWarnings("unchecked")
    protected <V> V getBean(@NonNull BeanRule beanRule) {
        return switch (beanRule.getScopeType()) {
            case SINGLETON -> (V)getSingletonScopeBean(beanRule);
            case PROTOTYPE ->
                // Does not manage the complete lifecycle of a prototype bean.
                // In particular, Aspectran does not manage destruction phase of prototype-scoped beans.
                getPrototypeScopeBean(beanRule);
            case REQUEST -> (V)getRequestScopeBean(beanRule);
            case SESSION -> (V)getSessionScopeBean(beanRule);
            case null -> throw new BeanCreationException("Scope type is not set", beanRule);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <V> V getPrototypeScopeBean(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        Object bean = createBean(beanRule, null);
        if (bean != null && beanRule.isFactoryProductionRequired()) {
            bean = produceObjectFromFactory(beanRule, bean, null);
        }
        return (V)bean;
    }

    /**
     * Gets a singleton-scoped bean instance.
     * @param beanRule the bean rule
     * @return the singleton bean instance
     */
    private Object getSingletonScopeBean(BeanRule beanRule) {
        return getScopedBean(beanRule, singletonScope);
    }

    /**
     * Gets a request-scoped bean instance.
     * @param beanRule the bean rule
     * @return the request-scoped bean instance
     */
    private Object getRequestScopeBean(BeanRule beanRule) {
        return getScopedBean(beanRule, getRequestScope());
    }

    /**
     * Gets a session-scoped bean instance.
     * @param beanRule the bean rule
     * @return the session-scoped bean instance
     */
    private Object getSessionScopeBean(BeanRule beanRule) {
        return getScopedBean(beanRule, getSessionScope());
    }

    /**
     * Retrieves a bean from the specified scope, creating it if it does not exist.
     * Also handles FactoryBean resolution.
     * @param beanRule the rule for the bean to retrieve
     * @param scope the scope from which to retrieve the bean
     * @return the bean instance
     * @throws UnsupportedBeanScopeException if the scope is not available
     */
    private Object getScopedBean(@NonNull BeanRule beanRule, @Nullable Scope scope) {
        if (scope == null) {
            throw new UnsupportedBeanScopeException(beanRule.getScopeType(), beanRule);
        }
        BeanInstance instance = getScopedBeanFromScope(scope, beanRule);
        if (beanRule.isFactoryProductionRequired()) {
            return resolveFactoryProducedBean(beanRule, instance, scope);
        } else {
            return instance.getBean();
        }
    }

    /**
     * Resolves a bean produced by a FactoryBean, handling caching for singleton products.
     * @param beanRule the bean rule for the factory
     * @param instance the bean instance, which may contain the factory and/or a cached product
     * @param scope the scope in which the bean resides
     * @return the final bean product
     */
    private Object resolveFactoryProducedBean(
            @NonNull BeanRule beanRule, @NonNull BeanInstance instance, @Nullable Scope scope) {
        Object beanToReturn;
        if (instance.getFactory() != null) {
            // Factory exists, check if product is cached
            if (instance.getBean() != null) {
                // Product is cached, return it
                beanToReturn = instance.getBean();
            } else {
                // Product not cached (isSingleton()=false), create new product
                beanToReturn = produceObjectFromFactory(beanRule, instance.getFactory(), scope);
            }
        } else {
            // No factory, instance.getBean() is the candidate
            beanToReturn = produceObjectFromFactory(beanRule, instance.getBean(), scope);
        }
        return beanToReturn;
    }

    /**
     * Retrieves a bean instance from the given scope, creating and registering it if not found.
     * This method implements double-checked locking for thread-safe scopes.
     * @param scope the scope to search in
     * @param beanRule the rule for the bean to create
     * @return the existing or newly created bean instance
     */
    private BeanInstance getScopedBeanFromScope(@NonNull Scope scope, BeanRule beanRule) {
        ReadWriteLock scopeLock = scope.getScopeLock();
        if (scopeLock == null) {
            // Not a thread-safe scope (e.g., request scope)
            BeanInstance instance = scope.getBeanInstance(beanRule);
            if (instance == null) {
                instance = createAndRegisterBean(scope, beanRule);
            }
            return instance;
        } else {
            // Thread-safe scope (e.g., singleton, session)
            scopeLock.readLock().lock();
            try {
                BeanInstance instance = scope.getBeanInstance(beanRule);
                if (instance != null) {
                    return instance;
                }
            } finally {
                scopeLock.readLock().unlock();
            }

            scopeLock.writeLock().lock();
            try {
                // Double-check inside the write lock
                BeanInstance instance = scope.getBeanInstance(beanRule);
                if (instance == null) {
                    instance = createAndRegisterBean(scope, beanRule);
                }
                return instance;
            } finally {
                scopeLock.writeLock().unlock();
            }
        }
    }

    /**
     * Creates a bean instance and registers it with the given scope.
     * @param scope the scope to register the bean with
     * @param beanRule the rule for the bean to create
     * @return the newly created bean instance
     */
    private BeanInstance createAndRegisterBean(@NonNull Scope scope, @NonNull BeanRule beanRule) {
        Object bean = createBean(beanRule, scope);
        BeanInstance instance;
        if (bean != null && beanRule.isFactoryProductionRequired()) {
            // For FactoryBeans, getFactoryProducedObject handles registration.
            // Synchronize on the factory bean instance to ensure thread-safe object production.
            synchronized (bean) {
                produceObjectFromFactory(beanRule, bean, scope);
                instance = scope.getBeanInstance(beanRule); // Retrieve the updated BeanInstance
            }
        } else {
            instance = BeanInstance.forProduct(bean);
            scope.putBeanInstance(beanRule, instance);
        }
        return instance;
    }

    /**
     * Gets the current request scope, if available.
     * @return the request scope, or {@code null} if not in a request context
     */
    @Nullable
    private RequestScope getRequestScope() {
        Activity activity = getActivityContext().getAvailableActivity();
        if (activity != null  && activity.getRequestAdapter() != null) {
            return activity.getRequestAdapter().getRequestScope();
        } else {
            return null;
        }
    }

    /**
     * Gets the current session scope, creating one if it doesn't exist.
     * @return the session scope, or {@code null} if not in a session context
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSingleton(@NonNull Object bean) {
        ReadWriteLock scopeLock = singletonScope.getScopeLock();
        scopeLock.readLock().lock();
        try {
            return singletonScope.hasInstance(bean);
        } finally {
            scopeLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSingleton(@NonNull Class<?> type) {
        return hasSingleton(type, null);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroySingleton(@NonNull Object bean) throws Exception {
        ReadWriteLock scopeLock = singletonScope.getScopeLock();
        scopeLock.writeLock().lock();
        try {
            if (singletonScope.hasInstance(bean)) {
                singletonScope.destroy(bean);
            }
        } finally {
            scopeLock.writeLock().unlock();
        }
    }

    /**
     * Instantiates all non-lazy-init singleton beans.
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

    /**
     * Instantiates a single bean if it is a non-lazy singleton and has not been
     * instantiated yet.
     * @param beanRule the bean rule to process
     */
    private void instantiateSingleton(@NonNull BeanRule beanRule) {
        if (beanRule.isSingleton() && !beanRule.isLazyInit()
                && !singletonScope.containsBeanRule(beanRule)) {
            getSingletonScopeBean(beanRule);
        }
    }

    /**
     * Destroys all cached singleton beans.
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
