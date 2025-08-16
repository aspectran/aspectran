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
 * Wildcard matching utilities specialized for Aspectran.
 * <p>
 * This package provides a compact and fast wildcard engine similar to Ant Path
 * Pattern, used throughout Aspectran to match request paths, bean names, and
 * other identifiers. Patterns are compiled to an efficient internal
 * representation and evaluated character-by-character without regular
 * expressions.
 * </p>
 *
 * <h2>Pattern syntax</h2>
 * <ul>
 *   <li><code>*</code> – matches zero or more characters within a single
 *       segment (does not cross the separator).</li>
 *   <li><code>?</code> – matches exactly one character within a segment.</li>
 *   <li><code>+</code> – matches one or more characters within a segment.</li>
 *   <li><code>**</code> – cross-segment wildcard; when a separator is configured
 *       it may match across separators.</li>
 *   <li><code>\</code> – escape character to treat the following character as a
 *       literal.</li>
 * </ul>
 *
 * <h2>Separator-aware matching</h2>
 * <p>
 * A pattern may be compiled with a segment separator (e.g. '/'). When a
 * separator is set, single-segment wildcards (<code>*</code>, <code>?</code>,
 * <code>+</code>) do not cross the separator, while the <code>**</code> token
 * is allowed to span across it. When no separator is set (the default), all
 * wildcards behave as plain character wildcards.
 * </p>
 *
 * <h2>Key classes</h2>
 * <ul>
 *   <li>{@link com.aspectran.utils.wildcard.WildcardPattern} – compiles pattern
 *       text, exposes {@code matches} and helper utilities such as
 *       {@code hasWildcards}.</li>
 *   <li>{@link com.aspectran.utils.wildcard.WildcardMatcher} – a stateful matcher
 *       useful for repeated matching and segment iteration when a separator is
 *       configured.</li>
 *   <li>{@link com.aspectran.utils.wildcard.WildcardMasker} – masks an input
 *       according to the wildcard positions in a pattern.</li>
 *   <li>{@link com.aspectran.utils.wildcard.WildcardPatterns} – a holder for
 *       multiple compiled patterns with a convenience {@code matches-any}
 *       operation.</li>
 *   <li>{@link com.aspectran.utils.wildcard.IncludeExcludeParameters} and
 *       {@link com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns} –
 *       helpers to configure and evaluate include/exclude rules.</li>
 * </ul>
 */
package com.aspectran.utils.wildcard;
