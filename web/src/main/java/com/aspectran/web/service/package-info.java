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
 * Provides a service implementation for running Aspectran in a Java Servlet-based
 * web environment.
 * <p>This package acts as a bridge between the generic Aspectran core and the
 * specific world of {@link jakarta.servlet.http.HttpServletRequest},
 * {@link jakarta.servlet.http.HttpServletResponse}, and {@link jakarta.servlet.ServletContext}.
 * The central component is the {@link com.aspectran.web.service.WebService},
 * which handles incoming HTTP requests and dispatches them to Aspectran's processing pipeline.</p>
 */
package com.aspectran.web.service;
