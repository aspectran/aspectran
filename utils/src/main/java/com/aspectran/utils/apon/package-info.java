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
 * APON (Aspectran Parameters Object Notation) utilities.
 * <p>
 * APON is a human-friendly data notation inspired by JSON and optimized for
 * configuration and structured data exchange in Aspectran. Unlike JSON, APON
 * uses newlines as separators instead of commas, supports a multi-line <em>text</em>
 * value type, and can explicitly declare value types (e.g. string, int, boolean,
 * parameters). APON is primarily used to represent Aspectran configuration and
 * to convert it into strongly-typed objects.
 * </p>
 *
 * <p>Key types include:</p>
 * <ul>
 *   <li>{@link com.aspectran.utils.apon.Parameters} – the central contract representing a
 *       set of named parameters with typed values; implemented by
 *       {@link com.aspectran.utils.apon.VariableParameters} (dynamic structure) and
 *       {@link com.aspectran.utils.apon.ArrayParameters} (root array of nameless entries).</li>
 *   <li>{@link com.aspectran.utils.apon.AbstractParameters} – base class providing common
 *       storage, type-safe accessors, and support for hierarchical/nested parameters.</li>
 *   <li>{@link com.aspectran.utils.apon.AponReader} and {@link com.aspectran.utils.apon.AponWriter} –
 *       streaming reader/writer for APON text. Closeable variants are available as
 *       {@link com.aspectran.utils.apon.AponReaderCloseable} and
 *       {@link com.aspectran.utils.apon.AponWriterCloseable}.</li>
 *   <li>{@link com.aspectran.utils.apon.AponLines} – a small builder to programmatically
 *       assemble APON text using a fluent API.</li>
 *   <li>{@link com.aspectran.utils.apon.JsonToParameters} and
 *       {@link com.aspectran.utils.apon.XmlToParameters} – helpers to convert JSON or XML
 *       into {@code Parameters}.</li>
 * </ul>
 *
 * <p>See the user guide for an introduction and examples.</p>
 */
package com.aspectran.utils.apon;
