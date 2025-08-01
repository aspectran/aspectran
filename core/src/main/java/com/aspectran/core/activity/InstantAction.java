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
 * An action to execute on Instant Activity.
 *
 * <p>Created: 2020/05/19</p>
 */
public interface InstantAction<V> {

    /**
     * Executes an instant action and throw an exception
     * if an error occurs during execution.
     * @return the result of the execution
     * @throws Throwable if an error occurs
     */
    V execute() throws Throwable;

}
