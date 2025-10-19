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
package com.aspectran.core.activity;

/**
 * A functional interface that represents a command to be executed by an
 * {@link InstantActivity}.
 *
 * <p>This interface encapsulates a piece of work that can be run within the
 * context of an activity, providing access to beans and other framework components.
 * It is designed to be used with lambda expressions.
 *
 * <p>Created: 2020/05/19</p>
 *
 * @param <V> the type of the result returned by the action
 */
@FunctionalInterface
public interface InstantAction<V> {

    /**
     * Executes the action within the context of an {@code InstantActivity}.
     * @return the result of the execution, of type {@code V}
     * @throws Exception if an error occurs during execution
     */
    V execute() throws Exception;

}
