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
 * Defines the core components and abstractions for handling requests within an
 * {@link com.aspectran.core.activity.Activity}.
 * <p>
 * This package encapsulates the mechanisms by which input data is captured,
 * adapted, and made available to activities in the Aspectran framework. Responsibilities
 * typically include:
 * <ul>
 *   <li>Representing and abstracting incoming request data (parameters, headers, bodies, etc.)</li>
 *   <li>Providing adapters to access request-related information (such as request parameters, session data, cookies, or file uploads)</li>
 *   <li>Decoupling activities from low-level transport details by offering unified APIs to query request context</li>
 *   <li>Allowing for custom extension or overriding of request-handling logic via adapters or pluggable components</li>
 * </ul>
 * </p>
 *
 * <p>
 * By centralizing request adaptation logic in this package, Aspectran ensures
 * consistent behavior across activities, regardless of how the request was received
 * (e.g., web, command-line, or batch environments). This design promotes portability,
 * testability, and modularity in how activities consume inputs.
 * </p>
 */
package com.aspectran.core.activity.request;
