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

import com.aspectran.core.context.rule.BeanRule;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;

/**
 * Thrown when a bean class cannot be instantiated via reflection.
 * Provides context about the failing bean class.
 */
public class BeanInstantiationException extends BeanException {

    @Serial
    private static final long serialVersionUID = 387409430536237392L;

    private final Class<?> beanClass;

    private final BeanRule beanRule;

    /**
     * Instantiates a new BeanInstantiationException.
     * @param beanRule the bean rule
     * @param msg the detail message
     * @param cause the root cause
     */
    public BeanInstantiationException(@NonNull BeanRule beanRule, String msg, Throwable cause) {
        super("Could not instantiate bean " + beanRule + ": " + msg, cause);
        this.beanClass = beanRule.getBeanClass();
        this.beanRule = beanRule;
    }

    /**
     * Instantiates a new BeanInstantiationException.
     * @param beanRule the bean rule
     * @param msg the detail message
     */
    public BeanInstantiationException(@NonNull BeanRule beanRule, String msg) {
        super("Could not instantiate bean " + beanRule + ": " + msg);
        this.beanClass = beanRule.getBeanClass();
        this.beanRule = beanRule;
    }

    /**
     * Returns the bean class.
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Returns the bean rule.
     * @return the bean rule
     */
    @Nullable
    public BeanRule getBeanRule() {
        return beanRule;
    }

}
