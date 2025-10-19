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
 * <p>Key actions include:
 * <ul>
 *   <li><b>Business Logic:</b> {@link com.aspectran.core.activity.process.action.InvokeAction}
 *       for invoking methods on beans.</li>
 *   <li><b>Flow Control:</b> {@code IncludeAction} for executing other translets and
 *       {@code ChooseAction} for conditional branching.</li>
 *   <li><b>Response Handling:</b> {@code EchoAction} for writing data directly to the
 *       response and {@code HeaderAction} for setting response headers.</li>
 *   <li><b>AOP:</b> {@link com.aspectran.core.activity.process.action.AdviceAction} for
 *       executing AOP advice beans.</li>
 * </ul>
 */
package com.aspectran.core.activity.process.action;
