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
 * <p>This package contains the necessary components to parse and evaluate complex
 * OGNL expressions, which form the basis of Aspectran's "Value Expressions" and
 * "Boolean Expressions". A key feature is the two-stage evaluation process: AsEL
 * tokens (like <code>${...}</code> or <code>#{...}</code>) embedded within an OGNL
 * expression are resolved first. The resulting objects are then used as inputs for
 * the final OGNL evaluation.
 *
 * <p>A "Boolean Expression" is a specific type of Value Expression that resolves to
 * a boolean result, often used for conditional logic. For example:
 * <pre>
 *   #{userBean.role} == 'ADMIN' &amp;&amp; ${loginAttempts} &lt; 5
 * </pre>
 * In this case, the AsEL tokens <code>#{userBean.role}</code> and
 * <code>${loginAttempts}</code> are resolved first. Then, the OGNL engine evaluates
 * the entire expression to produce a final boolean value. This mechanism is
 * fundamental to features like conditional actions (e.g., <code>&lt;if&gt;</code>,
 * <code>&lt;choose&gt;</code>) in Aspectran.
 */
package com.aspectran.core.context.asel.ognl;