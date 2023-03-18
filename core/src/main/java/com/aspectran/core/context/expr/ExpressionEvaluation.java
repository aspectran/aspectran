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
import com.aspectran.core.activity.ActivityData;
import com.aspectran.core.context.expr.ognl.OgnlSupport;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringUtils;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ExpressionEvaluator implementation that evaluates expressions written in
 * OGNL-based Aspectran expression language.
 *
 * <p>Created: 2021/01/31</p>
 *
 * @since 6.11.0
 */
public class ExpressionEvaluation implements ExpressionEvaluator {

    private static final String TOKEN_VAR_NAME_PREFIX = "__";

    private static final String TOKEN_VAR_NAME_SUFFIX = TOKEN_VAR_NAME_PREFIX;

    private static final String TOKEN_VAR_REF_SYMBOL = "#";

    private static final String TOKEN_VAR_REF_NAME_PREFIX = TOKEN_VAR_REF_SYMBOL + TOKEN_VAR_NAME_PREFIX;

    private final String expression;

    private Object represented;

    private Token[] tokens;

    public ExpressionEvaluation(String expression) throws ExpressionParserException {
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
    @SuppressWarnings("unchecked")
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
            OgnlContext ognlContext = OgnlSupport.createDefaultContext();
            String[] tokenVarNames = null;
            if (tokens != null && tokens.length > 0) {
                TokenEvaluator tokenEvaluator = new TokenEvaluation(activity);
                tokenVarNames = putTokenVariables(ognlContext, tokenEvaluator, tokens);
            }
            Object result = Ognl.getValue(represented, ognlContext, activityData, resultType);
            if (tokenVarNames != null && result instanceof String) {
                for (String tokenVarName : tokenVarNames) {
                    String tokenVarRefName = TOKEN_VAR_REF_SYMBOL + tokenVarName;
                    String str = (String)result;
                    if (str.contains(tokenVarRefName)) {
                        Object value = ognlContext.get(tokenVarName);
                        if (value != null) {
                            result = str.replace(tokenVarRefName, value.toString());
                        } else {
                            result = str.replace(tokenVarRefName, StringUtils.EMPTY);
                        }
                    }
                }
            }
            return (V)result;
        } catch (OgnlException e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V evaluate(TokenEvaluator tokenEvaluator, Class<V> resultType) {
        if (tokenEvaluator == null) {
            throw new IllegalArgumentException("tokenEvaluator must not be null");
        }
        if (represented == null) {
            return null;
        }
        try {
            Activity activity = tokenEvaluator.getActivity();
            ActivityData activityData;
            if (activity.getTranslet() != null) {
                activityData = activity.getTranslet().getActivityData();
            } else {
                activityData = new ActivityData(activity);
            }
            OgnlContext ognlContext = OgnlSupport.createDefaultContext();
            String[] tokenVarNames = null;
            if (tokens != null && tokens.length > 0) {
                tokenVarNames = putTokenVariables(ognlContext, tokenEvaluator, tokens);
            }
            Object result = Ognl.getValue(represented, ognlContext, activityData, resultType);
            if (tokenVarNames != null && result instanceof String) {
                for (String tokenVarName : tokenVarNames) {
                    String tokenVarRefName = TOKEN_VAR_REF_NAME_PREFIX + tokenVarName;
                    String str = (String)result;
                    if (str.contains(tokenVarRefName)) {
                        Object value = ognlContext.get(tokenVarName);
                        if (value != null) {
                            result = str.replace(tokenVarRefName, value.toString());
                        } else {
                            result = str.replace(tokenVarRefName, StringUtils.EMPTY);
                        }
                    }
                }
            }
            return (V)result;
        } catch (OgnlException e) {
            throw new ExpressionEvaluationException(expression, e);
        }
    }

    private String[] putTokenVariables(OgnlContext ognlContext, TokenEvaluator tokenEvaluator, Token[] tokens) {
        Set<String> tokenVarNames = new LinkedHashSet<>();
        for (Token token : tokens) {
            if (token.getType() != TokenType.TEXT) {
                String name = makeTokenVarName(token);
                Object value = tokenEvaluator.evaluate(token);
                ognlContext.put(name, value);
                tokenVarNames.add(name);
            }
        }
        return (tokenVarNames.isEmpty() ? null : tokenVarNames.toArray(new String[0]));
    }

    private void parseExpression(String expression) throws ExpressionParserException {
        tokens = TokenParser.makeTokens(expression, true);
        if (tokens != null && tokens.length > 0) {
            if (tokens.length == 1) {
                Token token = tokens[0];
                if (token.getType() == TokenType.TEXT) {
                    represented = OgnlSupport.parseExpression(token.getDefaultValue());
                } else {
                    String ognlVariableName = makeTokenVarRefName(token);
                    represented = OgnlSupport.parseExpression(ognlVariableName);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (Token token : tokens) {
                    if (token.getType() == TokenType.TEXT) {
                        sb.append(token.getDefaultValue());
                    } else {
                        sb.append(makeTokenVarRefName(token));
                    }
                }
                represented = OgnlSupport.parseExpression(sb.toString());
            }
        } else {
            represented = OgnlSupport.parseExpression(expression);
        }
    }

    private String makeTokenVarName(Token token) {
        return TOKEN_VAR_NAME_PREFIX + makeTokenName(token) + TOKEN_VAR_NAME_SUFFIX;
    }

    private String makeTokenVarRefName(Token token) {
        return TOKEN_VAR_REF_NAME_PREFIX + makeTokenName(token) + TOKEN_VAR_NAME_SUFFIX;
    }

    private String makeTokenName(Token token) {
        int hashCode = token.hashCode();
        if (hashCode >= 0) {
            return Long.toString(hashCode, 32);
        } else {
            return Long.toString(hashCode & 0x7fffffff, 32);
        }
    }

}
