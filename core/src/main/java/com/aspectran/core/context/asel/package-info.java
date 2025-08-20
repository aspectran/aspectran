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
 * Contains the Aspectran Expression Language (AsEL) classes.
 * <p>AsEL is a powerful expression language that supports querying and manipulating
 * an object graph at runtime. It has its own token expression format for simple
 * value retrieval and internally uses OGNL (Object-Graph Navigation Language)
 * to handle more complex expressions.
 *
 * <p>The Aspectran Token expression provides a simple and consistent way to access
 * various data within the application context. Tokens are identified by special
 * symbols and can be embedded within strings to be resolved at runtime.
 * The supported token types are:</p>
 * <ul>
 *   <li><code>${parameterName}</code>: Accesses a request parameter.</li>
 *   <li><code>@{attributeName}</code>: Accesses an attribute from the current activity context.</li>
 *   <li><code>%{propertyName}</code>: Accesses a property from the environment.</li>
 *   <li><code>#{beanId}</code>: Accesses a bean from the bean registry.</li>
 *   <li><code>~{templateId}</code>: Accesses a compiled template.</li>
 * </ul>
 * <p>These tokens are parsed and evaluated to dynamically insert values into
 * strings or to retrieve objects for further processing. For more complex logic,
 * such as method calls or conditional operations, AsEL delegates the evaluation
 * to OGNL.</p>
 */
package com.aspectran.core.context.asel;
