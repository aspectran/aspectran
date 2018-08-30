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
package com.aspectran.core.component.bean.scope;

import com.aspectran.core.component.bean.InstantiatedBean;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
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

    protected final Map<BeanRule, InstantiatedBean> scopedBeanMap = new HashMap<>();

    private final ScopeType scopeType;

    public AbstractScope(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public ReadWriteLock getScopeLock() {
        return scopeLock;
    }

    @Override
    public InstantiatedBean getInstantiatedBean(BeanRule beanRule) {
        return scopedBeanMap.get(beanRule);
    }

    @Override
    public void putInstantiatedBean(BeanRule beanRule, InstantiatedBean instantiatedBean) {
        scopedBeanMap.put(beanRule, instantiatedBean);
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            if (!scopedBeanMap.isEmpty()) {
                log.debug("Destroy the " + scopeType + " scoped beans in the " + this);
            }
        }

        for (Map.Entry<BeanRule, InstantiatedBean> entry : scopedBeanMap.entrySet()) {
            BeanRule beanRule = entry.getKey();
            InstantiatedBean instantiatedBean = entry.getValue();
            Object bean = instantiatedBean.getBean();
            if (bean != null) {
                doDestroy(beanRule, bean);
            }
        }

        scopedBeanMap.clear();
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
                log.error("Could not destroy the " + scopeType + " scoped bean " + beanRule, e);
            }
        }
    }

}
