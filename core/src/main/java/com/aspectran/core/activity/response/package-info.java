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
 * Contains classes and interfaces for modeling and handling various types of responses.
 *
 * <p>This package provides the core components for generating a response after an
 * {@link com.aspectran.core.activity.Activity} has processed a request. It supports
 * different response strategies, such as:
 * <ul>
 *   <li>Transforming content into a specific format (e.g., JSON, XML)</li>
 *   <li>Redirecting the client to a new URL</li>
 *   <li>Forwarding the request to another resource on the server</li>
 *   <li>Dispatching to a view technology (e.g., JSP)</li>
 * </ul>
 * The base {@link com.aspectran.core.activity.response.Response} interface defines the
 * contract for all response types.
 */
package com.aspectran.core.activity.response;