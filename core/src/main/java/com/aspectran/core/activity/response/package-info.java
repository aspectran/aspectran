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
 * Provides classes and interfaces for modeling and handling various types of responses
 * within the Aspectran framework.
 *
 * <p>This package defines the core components responsible for generating a response
 * after an {@link com.aspectran.core.activity.Activity} has processed a request.
 * It supports diverse response strategies, including:</p>
 * <ul>
 *   <li>Transforming activity results into specific output formats (e.g., JSON, XML, APON).</li>
 *   <li>Redirecting the client to a new URL, potentially with FlashMap attributes.</li>
 *   <li>Forwarding the request to another internal resource on the server (e.g., another translet or a view).</li>
 *   <li>Dispatching to a view technology for rendering (e.g., JSP, Thymeleaf).</li>
 *   <li>Providing dynamic, programmatic control over the response (e.g., direct {@code OutputStream} writing, setting HTTP status codes).</li>
 * </ul>
 * <p>The foundational {@link com.aspectran.core.activity.response.Response} interface establishes
 * the contract for all response types, ensuring a consistent approach to output generation.</p>
 */
package com.aspectran.core.activity.response;
