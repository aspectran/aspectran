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
 * Strategy interface for a bean scope in Aspectran.
 * <p>Defines operations to store, retrieve, and destroy scoped bean instances,
 * as well as providing metadata such as the {@link ScopeType} and an optional
 * scope lock for thread safety.</p>
 */
public interface Scope extends NonPersistent {

    /**
     * Returns the type of this scope.
     * @return the scope type
     */
    ScopeType getScopeType();

    /**
     * Returns the lock for this scope, if any.
     * <p>Scopes that are not thread-safe may return {@code null}.</p>
     * @return the scope lock, or {@code null} if not applicable
     */
    ReadWriteLock getScopeLock();

    /**
     * Returns the bean instance associated with the given bean rule.
     * @param beanRule the bean rule to retrieve the instance for
     * @return the bean instance, or {@code null} if not found
     */
    BeanInstance getBeanInstance(BeanRule beanRule);

    /**
     * Registers a bean instance with this scope.
     * @param activity the current activity
     * @param beanRule the bean rule defining the instance
     * @param beanInstance the bean instance to register
     */
    void putBeanInstance(Activity activity, BeanRule beanRule, BeanInstance beanInstance);

    /**
     * Returns the bean rule associated with the given bean instance.
     * @param bean the bean instance to find the rule for
     * @return the corresponding bean rule, or {@code null} if not found
     */
    BeanRule getBeanRuleByInstance(Object bean);

    /**
     * Checks if a bean defined by the given rule is present in this scope.
     * @param beanRule the bean rule to check for
     * @return {@code true} if a bean with the given rule exists, {@code false} otherwise
     */
    boolean containsBeanRule(BeanRule beanRule);

    /**
     * Destroys the specified bean instance within this scope.
     * @param bean the bean instance to destroy
     * @throws Exception if destruction fails
     */
    void destroy(Object bean) throws Exception;

    /**
     * Destroys all beans held in this scope.
     */
    void destroy();

}
