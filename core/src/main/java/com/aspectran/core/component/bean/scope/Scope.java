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

import com.aspectran.core.component.bean.BeanInstance;
import com.aspectran.core.component.session.NonPersistent;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    @NonNull
    ScopeType getScopeType();

    /**
     * Returns the lock for this scope, if any.
     * <p>Scopes that are not thread-safe may return {@code null}.</p>
     * @return the scope lock, or {@code null} if not applicable
     */
    @Nullable
    ReadWriteLock getScopeLock();

    /**
     * Returns the bean instance associated with the given bean rule.
     * @param beanRule the bean rule to retrieve the instance for
     * @return the bean instance, or {@code null} if not found
     */
    @Nullable
    BeanInstance getBeanInstance(@NonNull BeanRule beanRule);

    /**
     * Registers a bean instance with this scope.
     * @param beanRule the bean rule defining the instance
     * @param beanInstance the bean instance to register
     */
    void putBeanInstance(@NonNull BeanRule beanRule, @NonNull BeanInstance beanInstance);

    /**
     * Returns the bean rule associated with the given bean instance.
     * @param bean the bean instance to find the rule for
     * @return the corresponding bean rule, or {@code null} if not found
     */
    @Nullable
    BeanRule getBeanRuleByInstance(@NonNull Object bean);

    /**
     * Checks if the specified bean instance exists within the scope.
     * @param bean the bean instance to check
     * @return {@code true} if the bean instance exists in the scope, {@code false} otherwise
     */
    boolean hasInstance(@NonNull Object bean);

    /**
     * Checks if a bean defined by the given rule is present in this scope.
     * @param beanRule the bean rule to check for
     * @return {@code true} if a bean with the given rule exists, {@code false} otherwise
     */
    boolean containsBeanRule(@NonNull BeanRule beanRule);

    /**
     * Destroys the specified bean instance within this scope.
     * @param bean the bean instance to destroy
     * @throws Exception if destruction fails
     */
    void destroy(@NonNull Object bean) throws Exception;

    /**
     * Destroys all beans held in this scope.
     */
    void destroy();

}
