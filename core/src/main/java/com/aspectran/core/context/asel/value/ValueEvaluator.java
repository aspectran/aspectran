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
package com.aspectran.core.context.asel.value;

import com.aspectran.core.activity.Activity;

/**
 * Defines the contract for a "ValueEvaluator" that evaluates expressions, which can
 * contain both AsEL token expressions and OGNL expressions.
 *
 * <p>This interface represents the core mechanism for resolving dynamic values.
 * The evaluation process typically involves two stages: first, any embedded AsEL
 * tokens (e.g., <code>${...}</code>, <code>#{...}</code>) are resolved to their
 * corresponding objects. Second, the resulting string is evaluated as an OGNL
 * expression, allowing for complex logic, method calls, and property access.</p>
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 */
public interface ValueEvaluator {

    /**
     * Evaluates the underlying expression and returns the result.
     * <p>The evaluation process involves resolving any AsEL tokens first, followed by
     * executing the resulting OGNL expression. The final value is then converted to
     * the specified {@code resultType}.</p>
     * @param activity the current activity context, used to resolve tokens and provide
     *      the root object for OGNL evaluation
     * @param resultType the expected type of the result
     * @param <V> the generic type of the result
     * @return the result of the expression evaluation, converted to the specified type
     * @throws com.aspectran.core.context.asel.ExpressionEvaluationException if an error occurs during evaluation
     */
    <V> V evaluate(Activity activity, Class<V> resultType);

}
