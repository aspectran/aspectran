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
 * Provides classes related to dispatching the response to a view layer for rendering.
 *
 * <p>This package defines the mechanism Aspectran employs to integrate with various
 * view technologies (e.g., JSP, Thymeleaf, FreeMarker). It facilitates the forwarding
 * of control to a view resource for generating the final client response.</p>
 *
 * <p>The primary components within this package include:</p>
 * <ul>
 *   <li>{@link com.aspectran.core.activity.response.dispatch.DispatchResponse}:
 *       A {@link com.aspectran.core.activity.response.Response} implementation that
 *       orchestrates the dispatch action, often delegating to a {@code ViewDispatcher}.</li>
 *   <li>{@link com.aspectran.core.activity.response.dispatch.ViewDispatcher}:
 *       An interface defining the contract for dispatching to a specific view technology,
 *       handling the actual rendering process.</li>
 * </ul>
 */
package com.aspectran.core.activity.response.dispatch;
