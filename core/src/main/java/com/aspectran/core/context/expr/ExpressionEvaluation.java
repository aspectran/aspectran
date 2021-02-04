/*
 * Copyright (c) 2008-2021 The Aspectran Project
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
import com.aspectran.core.activity.ActivityData;
import com.aspectran.core.context.expr.ognl.OgnlSupport;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.type.TokenType;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;

/**
 * ExpressionEvaluator implementation that evaluates expressions written in
 * OGNL-based Aspectran expression language.
 *
 * <p>Created: 2021/01/31</p>
 */
public class ExpressionEvaluation implements ExpressionEvaluator {

    private static final String OGNL_TOKEN_VARIABLE_PREFIX = "#__";

    private static final String TOKEN_VARIABLE_PREFIX = "__";

    private static final int TOKEN_VARIABLE_FIRST_INDEX = 1;

    private final String expression;

    private Object represented;

    private Token[] tokens;

    public ExpressionEvaluation(String expression) throws IllegalRuleException {
        this.expression = expression;
        parseExpression(expression);
    }

    public String getExpression() {
        return expression;
    }

    public Token[] getTokens() {
        return tokens;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <V> V evaluate(Activity activity, Class<V> resultType) {
        if (activity == null) {
            throw new IllegalArgumentException("activity must not be null");
        }
        if (represented == null) {
            return null;
        }
        try {
            ActivityData activityData;
            if (activity.getTranslet() != null) {
                activityData = activity.getTranslet().getActivityData();
            } else {
                activityData = new ActivityData(activity);
            }
            Map context = OgnlSupport.createDefaultContext(activityData);
            if (tokens != null && tokens.length > 0) {
                TokenEvaluator tokenEvaluator = new TokenEvaluation(activity);
                if (tokens.length == 1) {
                    Token token = tokens[0];
                    if (token.getType() != TokenType.TEXT) {
                        String name = TOKEN_VARIABLE_PREFIX + TOKEN_VARIABLE_FIRST_INDEX;
                        Object value = tokenEvaluator.evaluate(token);
                        context.put(name, value);
                    }
                } else {
                    int index = TOKEN_VARIABLE_FIRST_INDEX;
                    for (Token token : tokens) {
                        if (token.getType() != TokenType.TEXT) {
                            String name = TOKEN_VARIABLE_PREFIX + index++;
                            Object value = tokenEvaluator.evaluate(token);
                            context.put(name, value);
                        }
                    }
                }
            }
            return (V)Ognl.getValue(represented, context, activityData, resultType);
        } catch (OgnlException e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    private void parseExpression(String expression) throws IllegalRuleException {
        tokens = TokenParser.makeTokens(expression, true);
        if (tokens != null && tokens.length > 0) {
            if (tokens.length == 1) {
                Token token = tokens[0];
                if (token.getType() == TokenType.TEXT) {
                    represented = OgnlSupport.parseExpression(token.getDefaultValue());
                } else {
                    String ognlVariableName = OGNL_TOKEN_VARIABLE_PREFIX + TOKEN_VARIABLE_FIRST_INDEX;
                    represented = OgnlSupport.parseExpression(ognlVariableName);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                int index = TOKEN_VARIABLE_FIRST_INDEX;
                for (Token token : tokens) {
                    if (token.getType() == TokenType.TEXT) {
                        sb.append(token.getDefaultValue());
                    } else {
                        sb.append(OGNL_TOKEN_VARIABLE_PREFIX).append(index++);
                    }
                }
                represented = OgnlSupport.parseExpression(sb.toString());
            }
        } else {
            represented = OgnlSupport.parseExpression(expression);
        }
    }

}
