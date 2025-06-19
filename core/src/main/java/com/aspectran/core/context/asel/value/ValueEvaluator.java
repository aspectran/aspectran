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
 * The Expression Evaluator.
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 */
public interface ValueEvaluator {

    /**
     * Evaluates an expression.
     * @param activity the aspectran activity
     * @param resultType the expected type of the result of the evaluation
     * @param <V> the type of the result
     * @return the result of the expression evaluation
     */
    <V> V evaluate(Activity activity, Class<V> resultType);

}
