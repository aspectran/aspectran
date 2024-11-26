/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.core.context.asel.ognl.OgnlSupport;
import com.aspectran.core.context.asel.ognl.expression.TokenizedExpression;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.utils.annotation.jsr305.Nullable;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 * ExpressionEvaluable implementation that evaluates expressions written in
 * OGNL-based Aspectran expression language.
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 */
public class ValueExpression implements ExpressionEvaluable {

    private final TokenizedExpression tokenizedExpression;

    public ValueExpression(String expression) throws ExpressionParserException {
        this.tokenizedExpression = new TokenizedExpression(expression);
    }

    @Nullable
    public String getExpressionString() {
        return tokenizedExpression.getExpressionString();
    }

    @Nullable
    public Object getParsedExpression() {
        return tokenizedExpression.getParsedExpression();
    }

    @Nullable
    public Token[] getTokens() {
        return tokenizedExpression.getTokens();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V evaluate(Activity activity, Class<V> resultType) {
        if (activity == null) {
            throw new IllegalArgumentException("activity must not be null");
        }
        if (getParsedExpression() == null) {
            return null;
        }
        try {
            OgnlContext ognlContext = OgnlSupport.createDefaultContext();
            tokenizedExpression.preProcess(activity, ognlContext);
            Object value = Ognl.getValue(getParsedExpression(), ognlContext, activity.getActivityData(), resultType);
            return (V)tokenizedExpression.postProcess(ognlContext, value);
        } catch (OgnlException e) {
            throw new ExpressionEvaluationException(getExpressionString(), e);
        }
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
            ExpressionEvaluable valueExpression = new ValueExpression(expression);
            return valueExpression.evaluate(activity, resultType);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

}
