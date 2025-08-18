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
 * Provides classes and interfaces for representing and handling responses
 * within an {@link com.aspectran.core.activity.Activity}.
 * <p>
 * The response package defines abstractions that allow activities to produce
 * output in a consistent manner. Typical responsibilities include:
 * <ul>
 *   <li>Encapsulating response content such as text, binary data, or structured data</li>
 *   <li>Supporting multiple response formats and output streams</li>
 *   <li>Providing adapters to write responses to various targets (e.g., HTTP, console, files)</li>
 *   <li>Managing response metadata such as status codes, headers, and content type</li>
 * </ul>
 * These components decouple the activity execution from the output mechanism,
 * enabling flexible integration with different environments.
 * </p>
 */
package com.aspectran.core.activity.response;
