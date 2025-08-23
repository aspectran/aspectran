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

import java.io.Serial;

/**
 * Thrown when a bean instance cannot be found.
 */
public class NoSuchBeanException extends BeanException {

    @Serial
    private static final long serialVersionUID = 1866105813455720749L;

    private final String id;

    private final Class<?> type;

    /**
     * Create a new NoSuchBeanException.
     * @param id the id of the missing bean
     */
    public NoSuchBeanException(String id) {
        super("No bean named '" + id + "' available");
        this.id = id;
        this.type = null;
    }

    /**
     * Create a new NoSuchBeanException.
     * @param type the required type of the missing bean
     */
    public NoSuchBeanException(Class<?> type) {
        super("No qualifying bean of type '" + type + "' available");
        this.type = type;
        this.id = null;
    }

    /**
     * Create a new NoSuchBeanException.
     * @param type the required type of the missing bean
     * @param id the id of the missing bean
     */
    public NoSuchBeanException(Class<?> type, String id) {
        super("No qualifying bean with name '" + id + "' of type '" + type + "' available");
        this.type = type;
        this.id = id;
    }

    /**
     * Returns the id of the missing bean.
     * @return the id of the missing bean
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the required type of the missing bean.
     * @return the required type of the missing bean
     */
    public Class<?> getType() {
        return type;
    }

}
