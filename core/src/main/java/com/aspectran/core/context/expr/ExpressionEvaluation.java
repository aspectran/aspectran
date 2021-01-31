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

    private static final String OGNL_VARIABLE_PREFIX = "#";

    private static final String TOKEN_VARIABLE_PREFIX = "__";

    private final String expression;

    private final Token[] tokens;

    private final Object represented;

    public ExpressionEvaluation(String expression) throws IllegalRuleException {
        this.expression = expression;
        this.tokens = makeTokens(expression);
        this.represented = parseExpression(expression, this.tokens);
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
            ActivityData root = (activity.getTranslet() != null ? activity.getTranslet().getActivityData() : null);
            Map context = OgnlSupport.createDefaultContext(root);
            if (tokens != null) {
                TokenEvaluator evaluator = new TokenEvaluation(activity);
                int index = 1;
                for (Token token : tokens) {
                    if (token.getType() != TokenType.TEXT) {
                        String name = TOKEN_VARIABLE_PREFIX + index++;
                        Object value = evaluator.evaluate(token);
                        context.put(name, value);
                    }
                }
            }
            return (V)Ognl.getValue(represented, context, root, resultType);
        } catch (OgnlException e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    private Token[] makeTokens(String expression) {
        return TokenParser.makeTokens(expression, true);
    }

    private Object parseExpression(String expression, Token[] tokens) throws IllegalRuleException {
        if (tokens != null) {
            StringBuilder sb = new StringBuilder();
            int index = 1;
            for (Token token : tokens) {
                if (token.getType() == TokenType.TEXT) {
                    sb.append(token.getDefaultValue());
                } else {
                    sb.append(OGNL_VARIABLE_PREFIX).append(TOKEN_VARIABLE_PREFIX).append(index++);
                }
            }
            return OgnlSupport.parseExpression(sb.toString());
        } else {
            return OgnlSupport.parseExpression(expression);
        }
    }

}
