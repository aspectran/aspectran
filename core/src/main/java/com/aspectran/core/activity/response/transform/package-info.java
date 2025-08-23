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
 * Provides classes related to transforming the activity's processing results into various output formats.
 *
 * <p>This package defines the core mechanism for converting the structured data
 * produced by an {@link com.aspectran.core.activity.Activity} (i.e., {@link com.aspectran.core.activity.process.result.ProcessResult})
 * into a client-consumable format such as JSON, XML, APON, or plain text.
 * It includes the base {@link com.aspectran.core.activity.response.transform.TransformResponse} interface
 * and various concrete implementations for different transformation types.</p>
 */
package com.aspectran.core.activity.response.transform;
