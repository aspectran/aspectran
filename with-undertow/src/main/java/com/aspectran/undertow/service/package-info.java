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
 * Provides the core service for handling requests in an embedded Undertow environment.
 * This package contains the primary service interface, {@code com.aspectran.undertow.service.TowService},
 * and its implementations. These components form the heart of the request processing pipeline when
 * running Aspectran on an embedded Undertow server without the overhead of the full Servlet API.
 * This lightweight, non-servlet approach offers a high-performance alternative for handling
 * HTTP requests directly.
 */
package com.aspectran.undertow.service;
