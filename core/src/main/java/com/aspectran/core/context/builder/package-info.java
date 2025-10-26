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
 * Provides the core strategy for building an {@link com.aspectran.core.context.ActivityContext}.
 *
 * <p>This package contains the classes responsible for parsing Aspectran's
 * configuration (from XML, APON, or annotated classes) and constructing a
 * fully initialized {@code ActivityContext}. The main entry point is the
 * {@link com.aspectran.core.context.builder.ActivityContextBuilder} interface,
 * with {@link com.aspectran.core.context.builder.HybridActivityContextBuilder}
 * being the primary implementation.
 *
 * <p>The building process involves several key steps:
 * <ul>
 *   <li>Configuring the builder with paths to rule files, packages to scan, or other parameters.</li>
 *   <li>Parsing the configuration into internal rule objects (e.g., BeanRule, TransletRule, AspectRule).</li>
 *   <li>Creating and populating registries for all core components.</li>
 *   <li>Validating the integrity of the configuration, such as bean references.</li>
 *   <li>Initializing the {@code ActivityContext} and all its singleton components.</li>
 * </ul>
 *
 * <p>It also includes support for automatic context reloading on configuration changes.
 */
package com.aspectran.core.context.builder;
