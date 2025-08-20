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
 * Contains classes for evaluating item rules defined in Aspectran.
 * <p>This package provides the mechanism for interpreting and resolving the values
 * of {@link com.aspectran.core.context.rule.ItemRule} instances. The evaluator
 * handles the dynamic resolution of item values, which can be simple literals,
 * references to other beans, or complex expressions involving AsEL tokens.</p>
 */
package com.aspectran.core.context.asel.item;