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
/**
 * Provides classes for representing the structured results of action processing.
 *
 * <p>This package contains the data model for capturing the output of an
 * {@link com.aspectran.core.activity.Activity}'s execution in a hierarchical manner.
 * The core structure is as follows:
 * <ul>
 *   <li>{@link com.aspectran.core.activity.process.result.ProcessResult}: The top-level
 *       container holding the aggregated results for the entire activity lifecycle.</li>
 *   <li>{@link com.aspectran.core.activity.process.result.ContentResult}: A container for
 *       the results of a logical group of actions, typically corresponding to a
 *       {@code <contents>} or {@code <response>} block within a translet definition.</li>
 *   <li>{@link com.aspectran.core.activity.process.result.ActionResult}: A simple object
 *       that holds the result of a single action execution, including its ID and value.</li>
 *   <li>{@link com.aspectran.core.activity.process.result.ResultValueMap}: A specialized map
 *       for storing nested or complex action result values.</li>
 * </ul>
 * This structured model allows for a detailed and organized representation of the activity's
 * outcome, which is useful for subsequent processing, view rendering, or debugging.</p>
 *
 * @since 2008
 */
package com.aspectran.core.activity.process.result;
