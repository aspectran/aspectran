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
 * Provides concrete implementations of the
 * {@link com.aspectran.core.activity.process.action.Executable} interface.
 *
 * <p>This package contains the standard, built-in actions available in Aspectran.
 * Each action represents a command that performs a specific, well-defined operation
 * during the processing of a request. These operations include, but are not limited to:
 * <ul>
 *   <li>Invoking methods on user-defined beans (e.g., {@link InvokeAction}, {@link AnnotatedAction})</li>
 *   <li>Manipulating the HTTP response (e.g., {@link HeaderAction})</li>
 *   <li>Controlling the execution flow (e.g., {@link ChooseAction}, {@link IncludeAction})</li>
 *   <li>Rendering data back to the client (e.g., {@link EchoAction})</li>
 *   <li>Executing AOP advice (e.g., {@link AdviceAction}, {@link AnnotatedAdviceAction})</li>
 * </ul>
 * These actions are fundamental building blocks for defining the behavior of translets.</p>
 */
package com.aspectran.core.activity.process.action;
