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
 * A concrete implementation of {@link ValueEvaluator} that parses and evaluates
 * AsEL expressions.
 * <p>This class acts as a primary entry point for AsEL expression evaluation. It internally
 * parses an expression string into a reusable, tokenized representation (an instance of
 * {@link ExpressionEvaluator}) and caches the result for performance. The actual
 * evaluation logic is delegated to the underlying {@link ExpressionEvaluator} instance.</p>
 *
 * <p>It also provides static helper methods for convenient, one-off expression
 * evaluations.</p>
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 * @see ValueEvaluator
 * @see TokenizedExpression
 */
public class ValueExpression implements ValueEvaluator {

    private static final Map<String, ExpressionEvaluator> cache = new ConcurrentReferenceHashMap<>();

    private final ExpressionEvaluator expressionEvaluator;

    /**
     * Constructs a new {@code ValueExpression}, parsing the given expression string.
     * <p>The parsed representation is retrieved from a cache if available, or created
     * and cached for future use.</p>
     * @param expression the expression string to parse
     * @throws ExpressionParserException if the expression has a syntax error
     */
    public ValueExpression(String expression) throws ExpressionParserException {
        this.expressionEvaluator = parseExpression(expression);
    }

    /**
     * Returns the original expression string.
     * @return the expression string
     */
    @Nullable
    public String getExpressionString() {
        return expressionEvaluator.getExpressionString();
    }

    /**
     * Returns the parsed OGNL expression object.
     * @return the parsed expression, or {@code null} if not applicable
     */
    @Nullable
    public Object getParsedExpression() {
        return expressionEvaluator.getParsedExpression();
    }

    /**
     * Returns the array of tokens parsed from the expression.
     * @return the array of tokens, or {@code null} if not applicable
     */
    @Nullable
    public Token[] getTokens() {
        return expressionEvaluator.getTokens();
    }

    /**
     * {@inheritDoc}
     * <p>This implementation delegates the evaluation to the underlying, cached
     * {@link ExpressionEvaluator}, providing it with a default OGNL context.</p>
     */
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
     * A convenience method to parse and evaluate an expression in a single call.
     * @param expression the expression to be evaluated
     * @param activity the current activity context
     * @return the result of the expression evaluation
     * @throws ExpressionEvaluationException if an error occurs during parsing or evaluation
     */
    public static Object evaluate(String expression, Activity activity) {
        return evaluate(expression, activity, null);
    }

    /**
     * A convenience method to parse and evaluate an expression in a single call,
     * converting the result to the specified type.
     * @param expression the expression to be evaluated
     * @param activity the current activity context
     * @param resultType the expected type of the result of the evaluation
     * @param <V> the type of the result
     * @return the result of the expression evaluation
     * @throws ExpressionEvaluationException if an error occurs during parsing or evaluation
     */
    public static <V> V evaluate(String expression, Activity activity, Class<V> resultType) {
        try {
            ValueEvaluator valueEvaluator = new ValueExpression(expression);
            return valueEvaluator.evaluate(activity, resultType);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    /**
     * Parses an expression string into an {@link ExpressionEvaluator}.
     * <p>Uses a static cache to avoid re-parsing the same expression string.</p>
     * @param expression the expression string to parse
     * @return a cached or new {@code ExpressionEvaluator} instance
     * @throws ExpressionParserException if the expression has a syntax error
     */
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
