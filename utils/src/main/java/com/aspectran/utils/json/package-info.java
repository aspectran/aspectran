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
 * Provides lightweight, dependency-free utilities for JSON processing.
 * <p>
 * This package includes tools for parsing, generating, and manipulating JSON
 * content without requiring external libraries like Jackson or Gson.
 * </p>
 *
 * <p>Key classes in this package include:</p>
 * <ul>
 *   <li>{@link com.aspectran.utils.json.JsonParser} &ndash; A simple parser for converting
 *       a JSON string into a standard Java object model (Maps and Lists).</li>
 *   <li>{@link com.aspectran.utils.json.JsonReader} &ndash; A low-level, streaming
 *       pull-parser for reading JSON token by token. It is efficient for large
 *       datasets and provides a lenient mode for handling non-standard JSON.</li>
 *   <li>{@link com.aspectran.utils.json.JsonWriter} &ndash; A streaming writer for
 *       producing JSON text, with support for pretty-printing and custom
 *       object serialization via {@link com.aspectran.utils.json.JsonSerializer}.</li>
 *   <li>{@link com.aspectran.utils.json.JsonBuilder} &ndash; A fluent builder API that
 *       simplifies the programmatic construction of JSON objects and arrays.</li>
 *   <li>{@link com.aspectran.utils.json.JsonString} &ndash; A wrapper class to embed a
 *       pre-formatted JSON string directly into a larger JSON structure without
 *       re-escaping.</li>
 *   <li>com.aspectran.utils.apon.JsonToParameters &ndash; A utility to convert
 *       JSON text directly into Aspectran's {@code Parameters} objects.</li>
 * </ul>
 *
 * <p>For closeable versions that support try-with-resources, see
 * {@link com.aspectran.utils.json.JsonReaderCloseable} and
 * {@link com.aspectran.utils.json.JsonWriterCloseable}.</p>
 */
package com.aspectran.utils.json;
