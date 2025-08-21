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
 * Contains classes for representing and evaluating AsEL expressions.
 * <p>This package provides a structured way to handle the evaluation of complex
 * AsEL expressions, which combine Aspectran's token expressions with the full power
 * of OGNL. This corresponds to the dynamic value evaluation context of AsEL.
 *
 * <p>The evaluation process is two-staged: first, any token expressions
 * (e.g., <code>#{...}</code>, <code>${...}</code>) are resolved to their respective
 * objects. Then, the entire expression is evaluated by the OGNL engine.
 *
 * <p>For example, in the expression:
 * <pre>
 *   #{myBean^calculate}(1 + ${someNumber})
 * </pre>
 * The <code>${someNumber}</code> token is resolved to a numeric value first. Then, the OGNL
 * expression is evaluated, calling the <code>calculate</code> method on the bean retrieved
 * via the <code>#{myBean}</code> token.</p>
 */
package com.aspectran.core.context.asel.value;
