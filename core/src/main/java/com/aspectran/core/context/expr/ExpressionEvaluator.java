/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.context.expr;

import com.aspectran.core.activity.Activity;

/**
 * The Expression Evaluator.
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 */
public interface ExpressionEvaluator {

    /**
     * Evaluates an expression.
     * @param activity the aspectran activity
     * @param resultType the expected type of the result of the evaluation
     * @param <V> the type of the result
     * @return the result of the expression evaluation
     */
    <V> V evaluate(Activity activity, Class<V> resultType);

    /**
     * Evaluates an expression.
     * @param tokenEvaluator the token evaluator
     * @param resultType the expected type of the result of the evaluation
     * @param <V> the type of the result
     * @return the result of the expression evaluation.
     */
    <V> V evaluate(TokenEvaluator tokenEvaluator, Class<V> resultType);

    /**
     * Evaluates an expression.
     * @param expression the expression to be evaluated
     * @param activity the aspectran activity
     * @return the result of the expression evaluation
     * @throws ExpressionEvaluationException thrown when an error occurs during expression evaluation
     */
    static Object evaluate(String expression, Activity activity) {
        return evaluate(expression, activity, null);
    }

    /**
     * Evaluates an expression.
     * @param expression the expression to be evaluated
     * @param activity the aspectran activity
     * @param resultType the expected type of the result of the evaluation
     * @param <V> the type of the result
     * @return the result of the expression evaluation
     * @throws ExpressionEvaluationException thrown when an error occurs during expression evaluation
     */
    static <V> V evaluate(String expression, Activity activity, Class<V> resultType) {
        try {
            return new ExpressionEvaluation(expression).evaluate(activity, resultType);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    /**
     * Evaluates an expression.
     * @param expression the expression to be evaluated
     * @param tokenEvaluator the token evaluator
     * @return the result of the expression evaluation
     * @throws ExpressionEvaluationException thrown when an error occurs during expression evaluation
     */
    static Object evaluate(String expression, TokenEvaluator tokenEvaluator) {
        return evaluate(expression, tokenEvaluator, null);
    }

    /**
     * Evaluates an expression.
     * @param expression the expression to be evaluated
     * @param tokenEvaluator the token evaluator
     * @param resultType the expected type of the result of the evaluation
     * @param <V> the type of the result
     * @return the result of the expression evaluation
     * @throws ExpressionEvaluationException thrown when an error occurs during expression evaluation
     */
    static <V> V evaluate(String expression, TokenEvaluator tokenEvaluator, Class<V> resultType) {
        try {
            return new ExpressionEvaluation(expression).evaluate(tokenEvaluator, resultType);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

}
