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
 * Adapter interfaces and base implementations that encapsulate application, request,
 * response, and session details for different runtime environments.
 * <p>
 * This package defines the SPI used by the core activity/Translet layer to interact with
 * the hosting container without depending on container-specific APIs. The primary
 * contracts are:
 * </p>
 * <ul>
 *   <li>{@link com.aspectran.core.adapter.ApplicationAdapter} – application-level context
 *       such as base-path resolution and application-scoped attributes</li>
 *   <li>{@link com.aspectran.core.adapter.RequestAdapter} – access to request headers,
 *       parameters, files, attributes, locales, etc.</li>
 *   <li>{@link com.aspectran.core.adapter.ResponseAdapter} – response headers, status,
 *       content type/encoding, and output handling</li>
 *   <li>{@link com.aspectran.core.adapter.SessionAdapter} – session identity, lifecycle,
 *       attributes, and session-scoped storage</li>
 * </ul>
 */
package com.aspectran.core.adapter;
