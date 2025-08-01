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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.bean.BeanInstance;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.utils.Assert;
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
 * Base class for {@link Scope} implementations.
 *
 * @since 2011. 3. 12.
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
    public void putBeanInstance(Activity activity, BeanRule beanRule, BeanInstance beanInstance) {
        Assert.notNull(activity, "activity must not be null");
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

    @Override
    public void destroy() {
        if (logger.isDebugEnabled()) {
            if (!scopedBeanInstances.isEmpty()) {
                logger.debug("Destroy {} scoped beans from {}", getScopeType(), this);
            }
        }

        List<BeanRule> targets = new ArrayList<>(scopedBeanInstances.keySet());
        List<BeanRule> leftBehinds = new ArrayList<>();
        for (ListIterator<BeanRule> iter = targets.listIterator(targets.size()); iter.hasPrevious();) {
            BeanRule beanRule = iter.previous();
            BeanInstance instance = scopedBeanInstances.get(beanRule);
            Object bean = instance.getBean();
            if (bean != null) {
                if (beanRule.isLazyDestroy()) {
                    leftBehinds.add(beanRule);
                } else {
                    try {
                        doDestroy(beanRule, bean);
                    } catch (Exception e) {
                        logger.error("Could not destroy {} scoped bean {}", getScopeType(), beanRule, e);
                    }
                }
            }
        }
        for (BeanRule beanRule : leftBehinds) {
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

    private void doDestroy(BeanRule beanRule, Object bean) throws Exception {
        if (bean != null) {
            if (beanRule.getDestroyMethod() != null) {
                Method destroyMethod = beanRule.getDestroyMethod();
                try {
                    destroyMethod.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof Exception) {
                        throw (Exception)e.getCause();
                    } else {
                        throw e;
                    }
                }
            }
            if (beanRule.isDisposableBean()) {
                ((DisposableBean)bean).destroy();
            }
        }
    }

}
