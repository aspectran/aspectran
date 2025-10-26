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
 * Provides adapter interfaces and implementations for different runtime environments.
 *
 * <p>This package defines the SPI that allows the core framework to interact with a
 * hosting container (e.g., web server, shell) without depending on container-specific
 * APIs. The primary contracts are:
 * <ul>
 *   <li>{@link com.aspectran.core.adapter.ApplicationAdapter}: Manages application-level
 *       context, such as base path resolution and application-scoped attributes.</li>
 *   <li>{@link com.aspectran.core.adapter.RequestAdapter}: Provides access to request
 *       headers, parameters, attributes, and other request data.</li>
 *   <li>{@link com.aspectran.core.adapter.ResponseAdapter}: Handles response headers,
 *       status, content type, and the response body.</li>
 *   <li>{@link com.aspectran.core.adapter.SessionAdapter}: Manages session identity,
 *       lifecycle, attributes, and session-scoped storage.</li>
 * </ul>
 */
package com.aspectran.core.adapter;
