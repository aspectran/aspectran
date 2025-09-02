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
package com.aspectran.utils.nodelet;

/**
 * An interface for classes that add a set of {@link Nodelet}s to a {@link NodeletGroup}.
 * This allows for the encapsulation and reuse of a common set of parsing rules.
 * Implementations of this interface define a suite of nodelets that can be added
 * to a group in a single operation using {@link NodeletGroup#with(NodeletAdder)}.
 *
 * <p>Created: 2025-08-29</p>
 */
public interface NodeletAdder {

    /**
     * Adds a collection of nodelets to the given {@link NodeletGroup}.
     * @param group the nodelet group to which the nodelets will be added
     */
    void addTo(NodeletGroup group);

}
