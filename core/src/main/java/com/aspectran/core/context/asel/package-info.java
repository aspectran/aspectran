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
 * <p>AsEL is a powerful expression language based on OGNL (Object-Graph Navigation Language)
 * that allows querying and manipulating an object graph at runtime.
 *
 * <p>AsEL is composed of two main components:
 * <ol>
 *   <li><b>Token Expression</b>: Aspectran's own simple expression syntax for direct
 *       access to framework components. Tokens are identified by special symbols.</li>
 *   <li><b>AsEL Expression</b>: A full-featured expression that combines Token Expressions
 *       with OGNL's capabilities, such as method calls, property navigation, and
 *       conditional logic.</li>
 * </ol>
 *
 * <p>The supported token types are:</p>
 * <ul>
 *   <li><code>#{beanId}</code>: Accesses a bean from the bean registry.</li>
 *   <li><code>@{attributeName}</code>: Accesses an attribute from the current activity context.</li>
 *   <li><code>${parameterName}</code>: Accesses a request parameter.</li>
 *   <li><code>%{propertyName}</code>: Accesses a property from the environment.</li>
 *   <li><code>~{templateId}</code>: Renders a template and includes its output.</li>
 * </ul>
 */
package com.aspectran.core.context.asel;
