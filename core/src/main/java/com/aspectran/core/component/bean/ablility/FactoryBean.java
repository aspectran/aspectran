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
package com.aspectran.core.component.bean.ablility;

/**
 * The Interface FactoryBean.
 *
 * @param <T> the generic type
 * 
 * @since 2015. 4. 2.
 */
public interface FactoryBean<T> {

    String FACTORY_METHOD_NAME = "getObject";

    /**
     * Return an instance (possibly shared or independent) of the object managed by this factory.
     * As with a BeanFactory, this allows support for both the Singleton and Prototype design pattern.
     *
     * @return an instance of the bean (can be null)
     * @throws Exception in case of creation errors
     */
    T getObject() throws Exception;

}
