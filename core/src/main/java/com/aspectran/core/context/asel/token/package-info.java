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
 * Provides the core components for parsing, representing, and evaluating Aspectran Expression Language (AsEL) tokens.
 *
 * <p>This package is the foundation of AsEL, a simple yet powerful expression language used within the Aspectran
 * framework to dynamically resolve values at runtime. It includes:
 * <ul>
 *   <li>{@link com.aspectran.core.context.asel.token.Token}: Represents a parsed unit, which can be either a literal
 *       text segment or a special expression (e.g., {@code #{beanId}}, {@code ${paramName}}).</li>
 *   <li>{@link com.aspectran.core.context.asel.token.Tokenizer}: A low-level scanner that breaks down a raw string
 *       into a sequence of {@link com.aspectran.core.context.asel.token.Token}s.</li>
 *   <li>{@link com.aspectran.core.context.asel.token.TokenParser}: A high-level parser that orchestrates the
 *       tokenization process and provides utility methods for handling common expression patterns.</li>
 *   <li>{@link com.aspectran.core.context.asel.token.TokenEvaluator}: An interface that defines the contract for
 *       resolving tokens into their actual values within a given
 *       {@link com.aspectran.core.activity.Activity} context.</li>
 *   <li>{@link com.aspectran.core.context.asel.token.TokenEvaluation}: The default implementation of the
 *       {@code TokenEvaluator}, containing the logic to fetch values from beans, parameters, attributes, etc.</li>
 * </ul>
 *
 * <p>Together, these components enable flexible and dynamic configuration and data access throughout the application.
 */
package com.aspectran.core.context.asel.token;
