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
 * Core infrastructure for Aspectran's bean model.
 * <p>
 * Aspectran beans are similar to Spring Beans and support:
 * </p>
 * <ul>
 *   <li>Automatic component scanning</li>
 *   <li>Annotation- and XML-based configuration</li>
 *   <li>Proxy beans for AOP and interception</li>
 *   <li>Multiple bean scopes (singleton, session, request, etc.)</li>
 * </ul>
 * This package provides the registry, factory utilities, and exceptions used
 * by the container.
 */
package com.aspectran.core.component.bean;
