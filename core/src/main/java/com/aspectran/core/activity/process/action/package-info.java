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
 * <p>This package contains the standard, built-in actions that form the core of
 * Aspectran's execution logic. Each action is an {@code Executable} command that
 * performs a specific operation during the processing of a translet. These actions
 * serve as the fundamental building blocks for defining application behavior.
 *
 * <p>Key actions and components include:
 * <ul>
 *   <li><b>Business Logic & Method Invocation:</b>
 *     <ul>
 *       <li>{@link com.aspectran.core.activity.process.action.InvokeAction} -
 *           for dynamically or statically invoking methods on beans based on XML/configuration rules.</li>
 *       <li>{@link com.aspectran.core.activity.process.action.MethodAction} -
 *           for directly executing action methods with parameter binding resolved from the current activity.</li>
 *       <li>{@link com.aspectran.core.activity.process.action.ActionMethodInvoker} -
 *           a utility engine providing the core logic for parameter binding and method invocation.</li>
 *     </ul>
 *   </li>
 *   <li><b>Flow Control:</b>
 *     <ul>
 *       <li>{@link com.aspectran.core.activity.process.action.IncludeAction} -
 *           for executing other translets and incorporating their results.</li>
 *       <li>{@link com.aspectran.core.activity.process.action.ChooseAction} -
 *           for conditional branching based on dynamic evaluations.</li>
 *     </ul>
 *   </li>
 *   <li><b>Response Handling:</b>
 *     <ul>
 *       <li>{@link com.aspectran.core.activity.process.action.EchoAction} -
 *           for writing data directly to the response context.</li>
 *       <li>{@link com.aspectran.core.activity.process.action.HeaderAction} -
 *           for manipulating and setting HTTP response headers.</li>
 *     </ul>
 *   </li>
 *   <li><b>AOP Support:</b>
 *     <ul>
 *       <li>{@link com.aspectran.core.activity.process.action.AdviceAction} -
 *           for executing rule-defined AOP advice beans.</li>
 *       <li>{@link com.aspectran.core.activity.process.action.MethodAdviceAction} -
 *           for executing annotation-based advice methods within the AOP aspect context.</li>
 *     </ul>
 *   </li>
 * </ul>
 */
package com.aspectran.core.activity.process.action;
