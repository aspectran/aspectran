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
package com.aspectran.core.component.bean.scope;

import com.aspectran.core.component.bean.InstantiatedBean;
import com.aspectran.core.context.rule.BeanRule;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * The Interface Scope.
 *
 * @since 2011. 3. 12.
 */
public interface Scope {

    /**
     * Returns the scope lock.
     *
     * @return the scope lock
     */
    ReadWriteLock getScopeLock();

    /**
     * Returns an instance of the bean that matches the given bean rule.
     *
     * @param beanRule the bean rule of the bean to retrieve
     * @return an instance of the bean
     */
    InstantiatedBean getInstantiatedBean(BeanRule beanRule);

    /**
     * Saves an instantiated bean with the given bean rule into the scope.
     *
     * @param beanRule the bean rule of the bean to save
     * @param instantiatedBean an instance of the bean
     */
    void putInstantiatedBean(BeanRule beanRule, InstantiatedBean instantiatedBean);

    /**
     * Destroy all scoped beans in this scope.
     */
    void destroy();

}
