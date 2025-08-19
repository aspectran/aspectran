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
 * Contains classes that model the executable process flow of a translet.
 *
 * <p>This package defines the hierarchical structure of actions that are executed by the
 * {@link com.aspectran.core.activity.Activity} engine. The structure is composed of:
 * <ul>
 *   <li>{@link com.aspectran.core.activity.process.ContentList}: A top-level container
 *       that holds groups of action lists, representing a major section of a translet's
 *       executable content (e.g., {@code <contents>}).</li>
 *   <li>{@link com.aspectran.core.activity.process.ActionList}: An ordered collection of
 *       {@link com.aspectran.core.activity.process.action.Executable} actions that are
 *       executed sequentially.</li>
 * </ul>
 * This structured model allows the process flow to be represented hierarchically,
 * much like an XML document, providing a clear and organized definition of the work
 * to be performed during a request.
 */
package com.aspectran.core.activity.process;
