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
 * <p>This package, in conjunction with the {@link com.aspectran.core.context.asel.item.ItemEvaluator},
 * forms the basis of Aspectran's "ValueEvaluator" concept, which handles both AsEL tokens
 * and OGNL expressions. While the {@code ItemEvaluator} handles the resolution of AsEL
 * tokens within item rules, the classes in this package are responsible for parsing and
 * evaluating complex OGNL expressions that may use the results of that token evaluation.
 *
 * <p>This two-stage process allows for powerful "Value Expressions".
 * For example, an expression used for conditional logic might look like:
 * <pre>
 *   #{userBean.role} == 'ADMIN' &amp;&amp; ${loginAttempts} &lt; 5
 * </pre>
 * In this case, the AsEL tokens <code>#{userBean.role}</code> and
 * <code>${loginAttempts}</code> are resolved first by the token evaluation mechanism.
 * Then, the OGNL engine in this package evaluates the entire expression to produce a
 * final value. This mechanism is fundamental to features like conditional
 * actions (e.g., <code>&lt;if&gt;</code>, <code>&lt;choose&gt;</code>) in Aspectran.
 */
package com.aspectran.core.context.asel.ognl;