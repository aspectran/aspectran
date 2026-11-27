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
 * Provides service implementations and request handler components for running Aspectran
 * in a Java Servlet environment.
 * <p>The central interface is {@link com.aspectran.web.servlet.service.ServletWebService},
 * which specializes the core web service to manage application lifecycle, request dispatching,
 * and integration with {@link jakarta.servlet.ServletContext}.
 */
package com.aspectran.web.servlet.service;
