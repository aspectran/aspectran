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
 * Provides the core classes for the Aspectran execution context.
 * <p>This package contains the {@link com.aspectran.core.context.ActivityContext} interface,
 * which is the central container for an Aspectran application. It manages the lifecycle
 * of all components, provides access to configuration, and handles the execution
 * of activities in a multi-threaded environment.
 *
 * <p>The main components of this package include:
 * <ul>
 *     <li>{@link com.aspectran.core.context.ActivityContext}: The central interface
 *         for the application context.</li>
 *     <li>{@link com.aspectran.core.context.DefaultActivityContext}: The default
 *         implementation of the ActivityContext.</li>
 *     <li>com.aspectran.core.context.builder.ActivityContextBuilder: The builder
 *         responsible for creating and initializing the ActivityContext.</li>
 * </ul>
 */
package com.aspectran.core.context;
