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

/**
 * Defines a contract for objects that can create a deep copy (replica) of themselves.
 *
 * <p>This interface is primarily implemented by rule objects within the Aspectran context.
 * Replication is crucial for ensuring that the original, cached rule definitions remain
 * immutable. At runtime, when a rule is needed for a specific request, a mutable replica
 * is created and used instead. This prevents any state modifications during a single
 * request from affecting the shared, canonical rule definition, which is essential for
 * thread safety and context integrity.</p>
 *
 * <p>Created: 2016. 2. 2.</p>
 *
 * @since 2.0.0
 * @param <T> the type of the object to be replicated
 */
public interface Replicable<T> {

    /**
     * Creates and returns a new, deep-copied instance of this object.
     * The returned replica should be an independent object, meaning that modifications
     * to the replica should not affect the original object.
     *
     * @return a new, replicated instance of the object
     */
    T replicate();

}
