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
package com.aspectran.core.component.bean.scope;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.component.bean.BeanInstance;
import com.aspectran.core.component.session.NonPersistent;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * The Interface Scope.
 *
 * @since 2011. 3. 12.
 */
public interface Scope extends NonPersistent {

    /**
     * Returns the type of this scope.
     * @return the scope type
     */
    ScopeType getScopeType();

    /**
     * Returns the lock of this scope.
     * @return the scope lock
     */
    ReadWriteLock getScopeLock();

    /**
     * Returns an instance of the bean that matches the given bean rule.
     * @param beanRule the bean rule of the bean to retrieve
     * @return an instance of the bean
     */
    BeanInstance getBeanInstance(BeanRule beanRule);

    /**
     * Saves an instantiated bean with the given bean rule into the scope.
     * @param activity the current activity
     * @param beanRule the bean rule of the bean to save
     * @param beanInstance an instance of the bean
     */
    void putBeanInstance(Activity activity, BeanRule beanRule, BeanInstance beanInstance);

    /**
     * Returns the bean rule corresponding to the bean object.
     * @param bean the bean object to find
     * @return the bean rule
     */
    BeanRule getBeanRuleByInstance(Object bean);

    /**
     * Returns whether the bean rule exists in this scope.
     * @param beanRule the bean rule to find
     * @return {@code true} if the bean rule exists in this scope,  {@code false} otherwise
     */
    boolean containsBeanRule(BeanRule beanRule);

    /**
     * Destroy the bean that matches the given object in this scope.
     * @param bean the bean object to destroy
     * @throws Exception if the bean cannot be destroyed
     */
    void destroy(Object bean) throws Exception;

    /**
     * Destroy all scoped beans in this scope.
     */
    void destroy();

}
