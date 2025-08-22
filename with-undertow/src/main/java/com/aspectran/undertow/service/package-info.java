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
 * Provides a service implementation for running Aspectran on top of the
 * Undertow web server.
 * <p>This package acts as a bridge between the generic Aspectran core and the
 * specific Undertow server API, allowing Aspectran to handle web requests
 * directly using Undertow's native {@code HttpServerExchange} objects.
 */
package com.aspectran.undertow.service;
