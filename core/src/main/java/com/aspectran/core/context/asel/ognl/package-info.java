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
 * Provides the OGNL (Object-Graph Navigation Language) integration for AsEL.
 * <p>This package is responsible for the second stage of AsEL evaluation. After
 * Aspectran's own token expressions (e.g., <code>#{...}</code>, <code>${...}</code>)
 * are resolved into objects, the classes in this package parse and evaluate the
 * resulting OGNL expression.
 *
 * <p>This two-stage process allows for powerful "AsEL Expressions".
 * For example, an expression used for conditional logic might look like:
 * <pre>
 *   #{user^getRole()} == 'ADMIN' &amp;&amp; ${loginAttempts} &lt; 5
 * </pre>
 * In this case, the token expressions <code>#{user^getRole()}</code> and
 * <code>${loginAttempts}</code> are resolved first. Then, the OGNL engine in this
 * package evaluates the entire expression to produce a final boolean value.
 * This mechanism is fundamental to features like conditional actions
 * (e.g., <code>&lt;if&gt;</code>, <code>&lt;choose&gt;</code>) in Aspectran.
 */
package com.aspectran.core.context.asel.ognl;
