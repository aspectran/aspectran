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
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

/**
 * Central registry and access point for Aspectran beans.
 * Provides lookup operations by id and type, as well as lifecycle
 * and scanning-related utilities used by the container.
 */
public interface BeanRegistry {

    /**
     * Returns the base packages for scanning.
     * @return a set of base packages
     */
    Set<String> getBasePackages();

    /**
     * Returns an instance of the bean that matches the given id.
     * @param <V> the type of bean object retrieved
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     */
    <V> V getBean(String id);

    /**
     * Returns the bean instance that uniquely matches the given object type.
     * @param <V> the type of bean object retrieved
     * @param type the type the bean must match; can be an interface or superclass
     * @return an instance of the bean
     * @since 1.3.1
     */
    <V> V getBean(Class<V> type);

    /**
     * Returns an instance of the bean that matches the given object type.
     * If more than one matching bean is found, we pick a bean that matches the given id.
     * @param <V> the type of bean object retrieved
     * @param type type the bean must match; can be an interface or superclass
     * @param id the id of the bean to retrieve
     * @return an instance of the bean
     * @since 2.0.0
     */
    <V> V getBean(Class<V> type, String id);

    /**
     * Returns instances of beans that match the given object type.
     * @param <V> the type of bean object retrieved
     * @param type type the bean must match; can be an interface or superclass
     * @return an array of instances of beans
     */
    <V> V[] getBeansOfType(Class<V> type);

    /**
     * Returns a prototype-scoped bean instance for the given bean rule.
     * @param <V> the type of bean object retrieved
     * @param beanRule the bean rule
     * @return an instance of the bean
     */
    <V> V getPrototypeScopeBean(BeanRule beanRule);

    /**
     * Return whether a bean with the specified id is present.
     * @param id the id of the bean to query
     * @return whether a bean with the specified id is present
     */
    boolean containsBean(String id);

    /**
     * Return whether a bean with the specified object type is present.
     * @param type the object type of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> type);

    /**
     * Returns whether the bean corresponding to the specified object type and ID exists.
     * @param type the object type of the bean to query
     * @param id the id of the bean to query
     * @return whether a bean with the specified type is present
     */
    boolean containsBean(Class<?> type, String id);

    /**
     * Returns whether a single bean of the given type is present.
     * @param type the object type of the bean to query
     * @return whether a single bean of the given type is present
     */
    boolean containsSingleBean(Class<?> type);

    /**
     * Finds all bean classes that have the specified annotation.
     * @param annotationType the annotation type to look for
     * @return a collection of bean classes
     */
    Collection<Class<?>> findConfigBeanClassesWithAnnotation(Class<? extends Annotation> annotationType);

    /**
     * Returns whether the given bean is a singleton.
     * @param bean the bean instance to check
     * @return whether the given bean is a singleton
     */
    boolean hasSingleton(Object bean);

    /**
     * Returns whether a singleton bean of the given type is present.
     * @param type the object type of the bean to query
     * @return whether a singleton bean of the given type is present
     */
    boolean hasSingleton(@NonNull Class<?> type);

    /**
     * Returns whether a singleton bean of the given type and id is present.
     * @param type the object type of the bean to query
     * @param id the id of the bean to query
     * @return whether a singleton bean of the given type and id is present
     */
    boolean hasSingleton(Class<?> type, String id);

    /**
     * Destroys the given singleton bean.
     * @param bean the bean instance to destroy
     * @throws Exception if an error occurs during destruction
     */
    void destroySingleton(Object bean) throws Exception;

    /**
     * Returns whether this bean registry is available.
     * @return whether this bean registry is available
     */
    boolean isAvailable();

}
