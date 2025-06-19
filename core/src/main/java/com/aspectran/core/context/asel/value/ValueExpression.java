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
import com.aspectran.core.context.asel.ExpressionEvaluationException;
import com.aspectran.core.context.asel.ExpressionEvaluator;
import com.aspectran.core.context.asel.ExpressionParserException;
import com.aspectran.core.context.asel.TokenizedExpression;
import com.aspectran.core.context.asel.ognl.OgnlSupport;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ConcurrentReferenceHashMap;
import com.aspectran.utils.annotation.jsr305.Nullable;
import ognl.OgnlContext;

import java.util.Map;

/**
 * ValueEvaluator implementation that evaluates expressions written in
 * OGNL-based Aspectran expression language.
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 */
public class ValueExpression implements ValueEvaluator {

    private static final Map<String, ExpressionEvaluator> cache = new ConcurrentReferenceHashMap<>();

    private final ExpressionEvaluator expressionEvaluator;

    public ValueExpression(String expression) throws ExpressionParserException {
        this.expressionEvaluator = parseExpression(expression);
    }

    @Nullable
    public String getExpressionString() {
        return expressionEvaluator.getExpressionString();
    }

    @Nullable
    public Object getParsedExpression() {
        return expressionEvaluator.getParsedExpression();
    }

    @Nullable
    public Token[] getTokens() {
        return expressionEvaluator.getTokens();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V evaluate(Activity activity, Class<V> resultType) {
        Assert.notNull(activity, "activity must not be null");
        if (getParsedExpression() == null) {
            return null;
        }
        OgnlContext ognlContext = OgnlSupport.createDefaultContext();
        return (V)expressionEvaluator.evaluate(activity, ognlContext, activity.getActivityData(), resultType);
    }

    /**
     * Evaluates an expression.
     * @param expression the expression to be evaluated
     * @param activity the aspectran activity
     * @return the result of the expression evaluation
     * @throws ExpressionEvaluationException thrown when an error occurs during expression evaluation
     */
    public static Object evaluate(String expression, Activity activity) {
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
    public static <V> V evaluate(String expression, Activity activity, Class<V> resultType) {
        try {
            ValueEvaluator valueEvaluator = new ValueExpression(expression);
            return valueEvaluator.evaluate(activity, resultType);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    private static ExpressionEvaluator parseExpression(String expression) throws ExpressionParserException {
        ExpressionEvaluator evaluator = cache.get(expression);
        if (evaluator == null) {
            evaluator = new TokenizedExpression(expression);
            ExpressionEvaluator existing = cache.putIfAbsent(expression, evaluator);
            if (existing != null) {
                evaluator = existing;
            }
        }
        return evaluator;
    }

}
