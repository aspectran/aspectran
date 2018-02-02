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

/**
 * The Interface BeanRegistry.
 *
 * @since 2012. 11. 9.
 */
public interface BeanRegistry {

    /**
     * Return an instance of the bean that matches the given id.
     *
     * @param <T> the generic type
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     */
    <T> T getBean(String id);

    /**
     * Return an instance of the bean that matches the given object type.
     *
     * @param <T> the generic type
     * @param requiredType the type the bean must match; can be an interface or superclass. {@code null} is disallowed.
     * @return an instance of the bean
     * @since 1.3.1
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * Return an instance of the bean that matches the given id.
     * If the bean is not of the required type then throw a BeanNotOfRequiredTypeException.
     *
     * @param <T> the generic type
     * @param id the id of the bean to retrieve
     * @param requiredType type the bean must match; can be an interface or superclass. {@code null} is disallowed.
     * @return an instance of the bean
     * @since 1.3.1
     */
    <T> T getBean(String id, Class<T> requiredType);

    /**
     * Return an instance of the bean that matches the given object type.
     * If the bean is not exists ,retrieve the bean with the specified id.
     *
     * @param <T> the generic type
     * @param requiredType type the bean must match; can be an interface or superclass. {@code null} is allowed.
     * @param id the id of the bean to retrieve; if requiredType is {@code null}.
     * @return an instance of the bean
     * @since 2.0.0
     */
    <T> T getBean(Class<T> requiredType, String id);

    /**
     * Return the bean instance that matches the specified object type.
     * If the bean is not of the required type then throw a {@code BeanNotOfRequiredTypeException}.
     *
     * @param <T> the generic type
     * @param requiredType type the bean must match; can be an interface or superclass. {@code null} is disallowed.
     * @return an instance of the bean
     * @since 2.0.0
     */
    <T> T getConfigBean(Class<T> requiredType);

    /**
     * Return whether a bean with the specified id is present.
     *
     * @param id the id of the bean to query
     * @return whether a bean with the specified id is present
     */
    boolean containsBean(String id);

    /**
     * Return whether a bean with the specified object type is present.
     *
     * @param requiredType the object type of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> requiredType);

}
