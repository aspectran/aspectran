/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

/**
 * The Class NoSuchBeanException.
 */
public class NoSuchBeanException extends BeanException {

    /** @serial */
    private static final long serialVersionUID = 1866105813455720749L;

    private final String id;

    private final Class<?> type;

    /**
     * Instantiates a new NoSuchBeanException.
     *
     * @param id the bean id
     */
    public NoSuchBeanException(String id) {
        super("No bean named '" + id + "' available");
        this.id = id;
        this.type = null;
    }

    /**
     * Instantiates a new NoSuchBeanException.
     *
     * @param type the required type of the missing bean
     */
    public NoSuchBeanException(Class<?> type) {
        super("No qualifying bean of type '" + type + "' available");
        this.type = type;
        this.id = null;
    }

    /**
     * Instantiates a new NoSuchBeanException.
     *
     * @param type the required type
     * @param id the bean id
     */
    public NoSuchBeanException(Class<?> type, String id) {
        super("No qualifying bean with name '" + id + "' of type '" + type + "' available");
        this.type = type;
        this.id = id;
    }

    /**
     * Returns the id of the missing bean.
     *
     * @return the bean id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the type required type of the missing bean.
     *
     * @return the required type
     */
    public Class<?> getType() {
        return type;
    }

}
