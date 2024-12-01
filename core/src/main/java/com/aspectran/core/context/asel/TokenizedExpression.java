package com.aspectran.core.context.asel;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenEvaluator;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created: 2024. 11. 26.</p>
 */
public class TokenizedExpression implements ExpressionEvaluator {

    private static final String TOKEN_VAR_REF_SYMBOL = "#";

    private static final String TOKEN_VAR_NAME_PREFIX = "__";

    private static final String TOKEN_VAR_NAME_SUFFIX = TOKEN_VAR_NAME_PREFIX;

    private final String expression;

    private String substitutedExpression;

    private Object parsedExpression;

    private Token[] tokens;

    private Map<String, Token> tokenVars;

    private Set<String> tokenVarNames;

    public TokenizedExpression(String expression) throws ExpressionParserException {
        this.expression = expression;
        parseExpression();
    }

    @Override
    @Nullable
    public String getExpressionString() {
        return expression;
    }

    @Override
    @Nullable
    public String getSubstitutedExpression() {
        return substitutedExpression;
    }

    @Override
    @Nullable
    public Object getParsedExpression() {
        return parsedExpression;
    }

    @Override
    @Nullable
    public Token[] getTokens() {
        return tokens;
    }

    @Override
    @Nullable
    public Set<String> getTokenVarNames() {
        return tokenVarNames;
    }

    public boolean hasTokenVars() {
        return (tokenVarNames != null && !tokenVarNames.isEmpty());
    }

    private void parseExpression() throws ExpressionParserException {
        Token[] tokens = TokenParser.makeTokens(expression, true);
        Map<String, Token> tokenVars = null;
        String substitutedExpression = null;
        if (tokens != null && tokens.length > 0) {
            tokenVars = new HashMap<>();
            if (tokens.length == 1) {
                Token token = tokens[0];
                if (token.getType() == TokenType.TEXT) {
                    substitutedExpression = token.getDefaultValue();
                } else {
                    String tokenVarName = createTokenVarName(token);
                    tokenVars.putIfAbsent(tokenVarName, token);
                    substitutedExpression = createTokenVarRefName(tokenVarName);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (Token token : tokens) {
                    if (token.getType() == TokenType.TEXT) {
                        sb.append(token.getDefaultValue());
                    } else {
                        String tokenVarName = createTokenVarName(token);
                        tokenVars.putIfAbsent(tokenVarName, token);
                        substitutedExpression = createTokenVarRefName(tokenVarName);
                        sb.append(substitutedExpression);
                    }
                }
                substitutedExpression = sb.toString();
            }
        }
        this.tokens = tokens;
        this.tokenVars = (tokenVars != null && !tokenVars.isEmpty() ? Collections.unmodifiableMap(tokenVars) : null);
        this.tokenVarNames = (tokenVars != null && !tokenVars.isEmpty() ? Collections.unmodifiableSet(tokenVars.keySet()) : null);
        this.substitutedExpression = substitutedExpression;
        if (substitutedExpression != null) {
            try {
                this.parsedExpression = Ognl.parseExpression(substitutedExpression);
            } catch (OgnlException e) {
                throw new ExpressionParserException(expression, e);
            }
        }
    }

    @Override
    public Object evaluate(Activity activity, OgnlContext ognlContext) {
        return evaluate(activity, ognlContext, null, null);
    }

    @Override
    public Object evaluate(Activity activity, OgnlContext ognlContext, Object root) {
        return evaluate(activity, ognlContext, root, null);
    }

    @Override
    public Object evaluate(Activity activity, OgnlContext ognlContext, Object root, Class<?> resultType) {
        Assert.notNull(activity, "activity must not be null");
        Assert.notNull(ognlContext, "ognlContext must not be null");
        if (getParsedExpression() == null) {
            return null;
        }
        try {
            preProcess(activity, ognlContext);
            Object value = Ognl.getValue(getParsedExpression(), ognlContext, root, resultType);
            return postProcess(activity, ognlContext, value);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(getExpressionString(), e);
        }
    }

    private void preProcess(Activity activity, OgnlContext ognlContext) {
        if (hasTokenVars()) {
            TokenEvaluator tokenEvaluator = activity.getTokenEvaluator();
            resolveTokenVariables(ognlContext, tokenEvaluator);
        }
    }

    private Object postProcess(Activity activity, OgnlContext ognlContext, Object value) {
        if (getTokenVarNames() != null && value instanceof String str) {
            for (String tokenVarName : getTokenVarNames()) {
                String tokenVarRefName = createTokenVarRefName(tokenVarName);
                if (str.contains(tokenVarRefName)) {
                    Object tokenValue = ognlContext.get(tokenVarName);
                    String replacement;
                    if (tokenValue != null) {
                        replacement = ToStringBuilder.toString(tokenValue, activity.getStringifyContext());
                    } else {
                        replacement = StringUtils.EMPTY;
                    }
                    str = str.replace(tokenVarRefName, replacement);
                }
            }
            return str;
        } else {
            return value;
        }
    }

    private void resolveTokenVariables(OgnlContext ognlContext, TokenEvaluator tokenEvaluator) {
        for (Map.Entry<String, Token> entry : tokenVars.entrySet()) {
            String tokenVarName = entry.getKey();
            Token token = entry.getValue();
            Object value = tokenEvaluator.evaluate(token);
            ognlContext.put(tokenVarName, value);
        }
    }

    @NonNull
    private static String createTokenVarName(Token token) {
        return TOKEN_VAR_NAME_PREFIX + createTokenName(token) + TOKEN_VAR_NAME_SUFFIX;
    }

    @NonNull
    private static String createTokenVarRefName(String tokenVarName) {
        return TOKEN_VAR_REF_SYMBOL + tokenVarName;
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

}
