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
 * The Interface BeanRegistry.
 *
 * @since 2012. 11. 9.
 */
public interface BeanRegistry {

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

    boolean containsSingleBean(Class<?> type);

    Collection<Class<?>> findConfigBeanClassesWithAnnotation(Class<? extends Annotation> annotationType);

    boolean hasSingleton(Object bean);

    boolean hasSingleton(@NonNull Class<?> type);

    boolean hasSingleton(Class<?> type, String id);

    void destroySingleton(Object bean) throws Exception;

    boolean isAvailable();

}
