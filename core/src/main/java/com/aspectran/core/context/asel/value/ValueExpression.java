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
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlOps;

import java.util.HashSet;
import java.util.Set;

/**
 * ExpressionEvaluable implementation that evaluates expressions written in
 * OGNL-based Aspectran expression language.
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 */
public class ValueExpression implements ExpressionEvaluable {

    private static final String TOKEN_VAR_NAME_PREFIX = "__";

    private static final String TOKEN_VAR_NAME_SUFFIX = TOKEN_VAR_NAME_PREFIX;

    private static final String TOKEN_VAR_REF_SYMBOL = "#";

    private static final String TOKEN_VAR_REF_NAME_PREFIX = TOKEN_VAR_REF_SYMBOL + TOKEN_VAR_NAME_PREFIX;

    private final String expression;

    private Object parsedExpression;

    private Token[] tokens;

    public ValueExpression(String expression) throws ExpressionParserException {
        this.expression = expression;
        parseExpression(expression);
    }

    public String getExpressionString() {
        return expression;
    }

    public Token[] getTokens() {
        return tokens;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V evaluate(Activity activity, Class<V> resultType) {
        if (activity == null) {
            throw new IllegalArgumentException("activity must not be null");
        }
        if (parsedExpression == null) {
            return null;
        }
        try {
            OgnlContext ognlContext = OgnlSupport.createDefaultContext();
            String[] tokenVarNames = null;
            if (tokens != null && tokens.length > 0) {
                TokenEvaluator tokenEvaluator = activity.getTokenEvaluator();
                tokenVarNames = resolveTokenVariables(ognlContext, tokenEvaluator, tokens);
            }
            Object root = activity.getActivityData();
            Object value = Ognl.getValue(parsedExpression, ognlContext, root, resultType);
            if (tokenVarNames != null && value instanceof String str) {
                for (String tokenVarName : tokenVarNames) {
                    String tokenVarRefName = TOKEN_VAR_REF_SYMBOL + tokenVarName;
                    if (str.contains(tokenVarRefName)) {
                        Object tokenValue = ognlContext.get(tokenVarName);
                        String replacement;
                        if (tokenValue != null) {
                            replacement = (String)OgnlOps.convertValue(tokenValue, String.class, true);
                        } else {
                            replacement = StringUtils.EMPTY;
                        }
                        str = str.replace(tokenVarRefName, replacement);
                    }
                }
                return (V)str;
            } else {
                return (V)value;
            }
        } catch (OgnlException e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    private void parseExpression(String expression) throws ExpressionParserException {
        Token[] tokens = TokenParser.makeTokens(expression, true);
        Object parsedExpression;
        if (tokens != null && tokens.length > 0) {
            if (tokens.length == 1) {
                Token token = tokens[0];
                if (token.getType() == TokenType.TEXT) {
                    parsedExpression = OgnlSupport.parseExpression(token.getDefaultValue());
                } else {
                    String ognlVariableName = createTokenVarRefName(token);
                    parsedExpression = OgnlSupport.parseExpression(ognlVariableName);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (Token token : tokens) {
                    if (token.getType() == TokenType.TEXT) {
                        sb.append(token.getDefaultValue());
                    } else {
                        sb.append(createTokenVarRefName(token));
                    }
                }
                parsedExpression = OgnlSupport.parseExpression(sb.toString());
            }
        } else {
            parsedExpression = OgnlSupport.parseExpression(expression);
        }
        this.parsedExpression = parsedExpression;
        this.tokens = tokens;
    }

    @Nullable
    private static String[] resolveTokenVariables(
            OgnlContext ognlContext, TokenEvaluator tokenEvaluator, @NonNull Token[] tokens) {
        Set<String> tokenVarNames = new HashSet<>();
        for (Token token : tokens) {
            if (token.getType() != TokenType.TEXT) {
                String name = createTokenVarName(token);
                Object value = tokenEvaluator.evaluate(token);
                ognlContext.put(name, value);
                tokenVarNames.add(name);
            }
        }
        return (tokenVarNames.isEmpty() ? null : tokenVarNames.toArray(new String[0]));
    }

    @NonNull
    private static String createTokenVarName(Token token) {
        return TOKEN_VAR_NAME_PREFIX + createTokenName(token) + TOKEN_VAR_NAME_SUFFIX;
    }

    @NonNull
    private static String createTokenVarRefName(Token token) {
        return TOKEN_VAR_REF_NAME_PREFIX + createTokenName(token) + TOKEN_VAR_NAME_SUFFIX;
    }

    @NonNull
    private static String createTokenName(@NonNull Token token) {
        int hashCode = token.hashCode();
        if (hashCode >= 0) {
            return Long.toString(hashCode, 32);
        } else {
            return Long.toString(hashCode & 0x7fffffff, 32);
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
