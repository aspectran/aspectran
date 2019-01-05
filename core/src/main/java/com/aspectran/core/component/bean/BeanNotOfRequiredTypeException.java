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
package com.aspectran.core.component.bean;

import com.aspectran.core.context.rule.BeanRule;

/**
 * Thrown when a bean doesn't match the expected type
 */
public class BeanNotOfRequiredTypeException extends BeanRuleException {

    /** @serial */
    private static final long serialVersionUID = -6483506119764294512L;

    private Class<?> requiredType;

    /**
     * Create a new BeanNotOfRequiredTypeException.
     *
     * @param requiredType the required type
     * @param beanRule the bean rule
     */
    public BeanNotOfRequiredTypeException(Class<?> requiredType, BeanRule beanRule) {
        super("Bean named '" + beanRule.getId() + "' must be of type [" + requiredType.getName() + "]", beanRule);
        this.requiredType = requiredType;
    }

    /**
     * Gets the required type.
     *
     * @return the required type
     */
    public Class<?> getRequiredType() {
        return requiredType;
    }

}
