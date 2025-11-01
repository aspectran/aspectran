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
package com.aspectran.core.component.bean.ablility;

/**
 * Contract for objects that are factories for other beans.
 * A FactoryBean is used to encapsulate complex creation logic and
 * allows the container to obtain the product object via {@link #getObject()}.
 *
 * @param <T> the type of object produced by this factory
 */
public interface FactoryBean<T> {

    /** The conventional factory method name. */
    String FACTORY_METHOD_NAME = "getObject";

    /**
     * Return an instance of the bean this factory manages.
     * @return the created object
     * @throws Exception if creation fails
     */
    T getObject() throws Exception;

    /**
     * Is the object managed by this factory a singleton?
     * <p>That is, will {@link #getObject()} always return the same object
     * (a reference that can be cached)?</p>
     * <p>The default implementation returns {@code true}.</p>
     * @return whether the exposed object is a singleton
     */
    default boolean isSingleton() {
        return true;
    }

}
