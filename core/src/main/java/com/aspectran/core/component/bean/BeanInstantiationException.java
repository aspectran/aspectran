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

import org.jspecify.annotations.NonNull;

import java.io.Serial;

/**
 * Thrown when a bean class cannot be instantiated via reflection.
 * Provides context about the failing bean class.
 */
public class BeanInstantiationException extends BeanException {

    @Serial
    private static final long serialVersionUID = 387409430536237392L;

    private final Class<?> beanClass;

    /**
     * Instantiates a new BeanInstantiationException.
     * @param beanClass the bean class
     * @param cause the root cause
     */
    public BeanInstantiationException(Class<?> beanClass, Throwable cause) {
        this(beanClass, cause.getMessage(), cause);
    }

    /**
     * Instantiates a new BeanInstantiationException.
     * @param beanClass the bean class
     * @param msg the detail message
     * @param cause the root cause
     */
    public BeanInstantiationException(@NonNull Class<?> beanClass, String msg, Throwable cause) {
        super("Could not instantiate bean class [" + beanClass.getName() + "]: " + msg, cause);
        this.beanClass = beanClass;
    }

    /**
     * Instantiates a new BeanInstantiationException.
     * @param beanClass the bean class
     * @param msg the detail message
     */
    public BeanInstantiationException(@NonNull Class<?> beanClass, String msg) {
        super("Could not instantiate bean class [" + beanClass.getName() + "]: " + msg);
        this.beanClass = beanClass;
    }

    /**
     * Returns the bean class.
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

}
