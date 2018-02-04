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
 * The Class BeanCreationException.
 */
public class BeanCreationException extends BeanRuleException {

    /** @serial */
    private static final long serialVersionUID = -4711272699122321571L;

    /**
     * Instantiates a new BeanCreationException.
     *
     * @param beanRule the bean rule
     */
    public BeanCreationException(BeanRule beanRule) {
        super("Cannot create a bean", beanRule);
    }

    /**
     * Instantiates a new BeanCreationException.
     *
     * @param msg The detail message
     * @param beanRule the bean rule
     */
    public BeanCreationException(String msg, BeanRule beanRule) {
        super(msg, beanRule);
    }

    /**
     * Instantiates a new BeanCreationException.
     *
     * @param beanRule the bean rule
     * @param cause the root cause
     */
    public BeanCreationException(BeanRule beanRule, Throwable cause) {
        super("Cannot create a bean", beanRule, cause);
    }

    /**
     * Instantiates a new BeanCreationException.
     *
     * @param msg the detail message
     * @param beanRule the bean rule
     * @param cause the root cause
     */
    public BeanCreationException(String msg, BeanRule beanRule, Throwable cause) {
        super(msg, beanRule, cause);
    }

}
