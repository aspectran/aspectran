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
 * Contains classes for representing and evaluating various types of values.
 * <p>This package provides a structured way to handle values that can be simple
 * literals, collections (lists, maps), or complex expressions involving AsEL tokens.
 * These classes are used to encapsulate value definitions from rules and resolve
 * them at runtime.
 *
 * <p>Notably, AsEL expressions (typically OGNL) can embed AsEL tokens. In such
 * cases, the tokens are evaluated first to retrieve their corresponding objects,
 * which are then used as part of the larger OGNL expression evaluation.</p>
 *
 * <p>For example, in the expression:
 * <pre>
 *   #{myBean}.calculate(1 + ${someNumber})
 * </pre>
 * The <code>${someNumber}</code> token is resolved to a numeric value first. Then, the OGNL
 * expression is evaluated, calling the <code>calculate</code> method on the bean retrieved
 * via the <code>#{myBean}</code> token.</p>
 */
package com.aspectran.core.context.asel.value;
