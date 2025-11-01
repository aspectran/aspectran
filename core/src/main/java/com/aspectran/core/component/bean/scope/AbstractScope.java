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
package com.aspectran.core.component.bean.scope;

import com.aspectran.core.component.bean.BeanInstance;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Abstract base class for {@link Scope} implementations.
 * <p>Provides the core infrastructure for managing the lifecycle of scoped
 * bean instances, including storage and destruction logic.</p>
 */
public abstract class AbstractScope implements Scope {

    private static final Logger logger = LoggerFactory.getLogger(AbstractScope.class);

    private final Map<BeanRule, BeanInstance> scopedBeanInstances = new LinkedHashMap<>();

    public AbstractScope() {
    }

    protected Map<BeanRule, BeanInstance> getScopedBeanInstances() {
        return scopedBeanInstances;
    }

    @Override
    public BeanInstance getBeanInstance(BeanRule beanRule) {
        return scopedBeanInstances.get(beanRule);
    }

    @Override
    public void putBeanInstance(BeanRule beanRule, BeanInstance beanInstance) {
        Assert.notNull(beanRule, "beanRule must not be null");
        Assert.notNull(beanInstance, "beanInstance must not be null");
        scopedBeanInstances.put(beanRule, beanInstance);
    }

    @Override
    public BeanRule getBeanRuleByInstance(Object bean) {
        Assert.notNull(bean, "bean must not be null");
        for (Map.Entry<BeanRule, BeanInstance> entry : scopedBeanInstances.entrySet()) {
            if (entry.getValue().getBean() == bean) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public boolean containsBeanRule(BeanRule beanRule) {
        return scopedBeanInstances.containsKey(beanRule);
    }

    @Override
    public void destroy(Object bean) throws Exception {
        BeanRule beanRule = getBeanRuleByInstance(bean);
        if (beanRule != null) {
            doDestroy(beanRule, bean);
            scopedBeanInstances.remove(beanRule);
        }
    }

    /**
     * Destroys all beans in this scope.
     * <p>This method ensures that beans marked with {@code lazy-destroy="true"}
     * are destroyed after all other beans. This is crucial for resources like
     * database connections that must remain open until all dependent beans
     * have been destroyed. Beans within each group (non-lazy and lazy) are
     * destroyed in the reverse order of their creation.</p>
     */
    @Override
    public void destroy() {
        if (logger.isDebugEnabled()) {
            if (!scopedBeanInstances.isEmpty()) {
                logger.debug("Destroy {} scoped beans from {}", getScopeType(), this);
            }
        }

        List<BeanRule> beanRulesToDestroy = new ArrayList<>(scopedBeanInstances.keySet());
        List<BeanRule> lazyDestroyBeans = new ArrayList<>();

        // Step 1: Destroy non-lazy beans in reverse creation order.
        for (ListIterator<BeanRule> iter = beanRulesToDestroy.listIterator(beanRulesToDestroy.size()); iter.hasPrevious();) {
            BeanRule beanRule = iter.previous();
            BeanInstance instance = scopedBeanInstances.get(beanRule);
            Object bean = instance.getBean();
            if (bean != null) {
                if (beanRule.isLazyDestroy()) {
                    lazyDestroyBeans.add(beanRule);
                } else {
                    try {
                        doDestroy(beanRule, bean);
                    } catch (Exception e) {
                        logger.error("Could not destroy {} scoped bean {}", getScopeType(), beanRule, e);
                    }
                }
            }
        }

        // Step 2: Destroy the lazy-destroy beans.
        for (BeanRule beanRule : lazyDestroyBeans) {
            BeanInstance instance = scopedBeanInstances.get(beanRule);
            Object bean = instance.getBean();
            if (bean != null) {
                try {
                    doDestroy(beanRule, bean);
                } catch (Exception e) {
                    logger.error("Could not destroy {} scoped bean {}", getScopeType(), beanRule, e);
                }
            }
        }

        scopedBeanInstances.clear();
    }

    /**
     * Performs the actual destruction of a bean instance.
     * <p>Invokes the custom destroy method and/or the {@link DisposableBean}
     * interface method.</p>
     * @param beanRule the rule for the bean to be destroyed
     * @param bean the bean instance to be destroyed
     * @throws Exception if the destruction process fails
     */
    private void doDestroy(BeanRule beanRule, Object bean) throws Exception {
        if (bean != null) {
            Method destroyMethod = beanRule.getDestroyMethod();
            if (destroyMethod != null) {
                try {
                    destroyMethod.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
                } catch (InvocationTargetException e) {
                    throw ExceptionUtils.getCause(e);
                }
            }

            if (beanRule.isDisposableBean() && bean instanceof DisposableBean disposableBean) {
                disposableBean.destroy();
            }
        }
    }

}
