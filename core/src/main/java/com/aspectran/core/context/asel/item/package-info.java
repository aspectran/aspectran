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
 * Provides the mechanism for evaluating {@link com.aspectran.core.context.rule.ItemRule} instances.
 * <p>This package acts as a higher-level evaluation layer on top of the AsEL token engine.
 * It takes parsed {@link com.aspectran.core.context.rule.ItemRule}s, orchestrates the
 * evaluation of any tokens within them using a {@link com.aspectran.core.context.asel.token.TokenEvaluator},
 * and then transforms the results into strongly-typed Java collections and objects (e.g., List, Map, Properties).
 * This corresponds to the simple evaluation context of AsEL, where token expressions are used for direct
 * value retrieval, not for complex OGNL expressions.</p>
 */
package com.aspectran.core.context.asel.item;
