/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class AbstractScope.
 *
 * @since 2011. 3. 12.
 */
public class AbstractScope implements Scope {

    private static final Log log = LogFactory.getLog(AbstractScope.class);

    private final ReadWriteLock scopeLock = new ReentrantReadWriteLock();

    private final Map<BeanRule, BeanInstance> scopedBeanInstanceMap = new LinkedHashMap<>();

    private final ScopeType scopeType;

    AbstractScope(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public ReadWriteLock getScopeLock() {
        return scopeLock;
    }

    @Override
    public BeanInstance getBeanInstance(BeanRule beanRule) {
        return scopedBeanInstanceMap.get(beanRule);
    }

    @Override
    public void putBeanInstance(BeanRule beanRule, BeanInstance beanInstance) {
        scopedBeanInstanceMap.put(beanRule, beanInstance);
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            if (!scopedBeanInstanceMap.isEmpty()) {
                log.debug("Destroy " + scopeType + " scoped beans from " + this);
            }
        }

        List<BeanRule> beanRules = new ArrayList<>(scopedBeanInstanceMap.keySet());
        ListIterator<BeanRule> iterator = beanRules.listIterator(beanRules.size());
        while (iterator.hasPrevious()) {
            BeanRule beanRule = iterator.previous();
            BeanInstance instance = scopedBeanInstanceMap.get(beanRule);
            Object bean = instance.getBean();
            if (bean != null) {
                doDestroy(beanRule, bean);
            }
        }
        scopedBeanInstanceMap.clear();
    }

    private void doDestroy(BeanRule beanRule, Object bean) {
        if (bean != null) {
            try {
                if (beanRule.isDisposableBean()) {
                    ((DisposableBean)bean).destroy();
                } else if (beanRule.getDestroyMethodName() != null) {
                    Method destroyMethod = beanRule.getDestroyMethod();
                    destroyMethod.invoke(bean, MethodUtils.EMPTY_OBJECT_ARRAY);
                }
            } catch (Exception e) {
                log.error("Could not destroy " + scopeType + " scoped bean " + beanRule, e);
            }
        }
    }

}
