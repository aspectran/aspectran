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

import com.aspectran.core.context.rule.BeanRule;

/**
 * Exception to be thrown from a FactoryBean's getObject() method
 * if the bean is not fully initialized yet,
 * for example because it is involved in a circular reference.
 */
public class FactoryBeanNotInitializedException extends BeanRuleException {

    /** @serial */
    private static final long serialVersionUID = 5961471681939634699L;

    /**
     * Instantiates a new FactoryBeanNotInitializedException.
     *
     * @param beanRule the bean rule
     */
    public FactoryBeanNotInitializedException(BeanRule beanRule) {
        super("FactoryBean is not fully initialized yet", beanRule);
    }

    /**
     * Instantiates a new FactoryBeanNotInitializedException.
     *
     * @param msg the detail message
     * @param beanRule the bean rule
     */
    public FactoryBeanNotInitializedException(String msg, BeanRule beanRule) {
        super(msg, beanRule);
    }

}
