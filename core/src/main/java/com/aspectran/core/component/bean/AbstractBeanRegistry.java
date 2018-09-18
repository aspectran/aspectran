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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.BeanProxifierType;

/**
 * The Class AbstractBeanRegistry.
 * 
 * <p>Created: 2009. 03. 09 PM 23:48:09</p>
 */
public abstract class AbstractBeanRegistry extends AbstractBeanFactory implements BeanRegistry {

    public AbstractBeanRegistry(ActivityContext context, BeanRuleRegistry beanRuleRegistry,
                                BeanProxifierType beanProxifierType) {
        super(context, beanRuleRegistry, beanProxifierType);
    }

    @Override
    public <T> T getBean(String id) {
        BeanRule beanRule = beanRuleRegistry.getBeanRule(id);
        if (beanRule == null) {
            throw new BeanNotFoundException(id);
        }
        return getBean(beanRule);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) {
        BeanRule[] beanRules = beanRuleRegistry.getBeanRules(requiredType);
        if (beanRules == null) {
            BeanRule configBeanRule = beanRuleRegistry.getConfigBeanRule(requiredType);
            if (configBeanRule != null) {
                return getBean(configBeanRule);
            } else {
                throw new RequiredTypeBeanNotFoundException(requiredType);
            }
        }
        if (beanRules.length > 1) {
            throw new NoUniqueBeanException(requiredType, beanRules);
        }
        return getBean(beanRules[0]);
    }

    @Override
    public <T> T getBean(String id, Class<T> requiredType) {
        BeanRule beanRule = beanRuleRegistry.getBeanRule(id);
        if (beanRule == null) {
            throw new BeanNotFoundException(id);
        }
        if (requiredType != null && !requiredType.isAssignableFrom(beanRule.getTargetBeanClass())) {
            throw new BeanNotOfRequiredTypeException(requiredType, beanRule);
        }
        return getBean(beanRule);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, String id) {
        BeanRule[] beanRules = beanRuleRegistry.getBeanRules(requiredType);
        if (beanRules == null) {
            throw new RequiredTypeBeanNotFoundException(requiredType);
        }
        if (beanRules.length == 1) {
            return getBean(beanRules[0]);
        } else if (id != null) {
            for (BeanRule beanRule : beanRules) {
                if (id.equals(beanRule.getId())) {
                    return getBean(beanRule);
                }
            }
        }
        throw new NoUniqueBeanException(requiredType, beanRules);
    }

    @Override
    public <T> T getConfigBean(Class<T> requiredType) {
        BeanRule beanRule = beanRuleRegistry.getConfigBeanRule(requiredType);
        if (beanRule == null) {
            throw new RequiredTypeBeanNotFoundException(requiredType);
        }
        return getBean(beanRule);
    }

    @Override
    public boolean containsBean(String id) {
        return beanRuleRegistry.containsBeanRule(id);
    }

    @Override
    public boolean containsBean(Class<?> requiredType) {
        return beanRuleRegistry.containsBeanRule(requiredType);
    }

    abstract protected <T> T getBean(BeanRule beanRule);

}
