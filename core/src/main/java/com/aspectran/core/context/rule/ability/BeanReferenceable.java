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
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.context.rule.type.BeanRefererType;

/**
 * Defines a contract for rule classes that can reference a managed bean.
 *
 * <p>This interface is used to identify which rules hold a reference to a bean
 * and to understand how that bean is being referenced (e.g., as a property of an
 * action, as an advice bean, etc.). This is useful for introspection and for
 * analyzing the dependency relationships within the application context.</p>
 *
 * <p>Created: 2016. 2. 20.</p>
 *
 * @since 2.0.0
 */
public interface BeanReferenceable {

    /**
     * Returns the type of the bean referrer, indicating how the bean is being used.
     * @return the {@link BeanRefererType} which categorizes the reference
     */
    BeanRefererType getBeanRefererType();

}
