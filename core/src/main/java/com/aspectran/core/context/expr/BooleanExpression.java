package com.aspectran.core.context.expr;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.CaseWhenRule;
import com.aspectran.core.util.StringUtils;

/**
 * <p>Created: 2019-01-06</p>
 */
public class BooleanExpression {

    protected final Activity activity;

    public BooleanExpression(Activity activity) {
        this.activity = activity;
    }

    public boolean evaluate(CaseWhenRule caseWhenRule) {
        if (caseWhenRule.getExpression() == null) {
            return false;
        }
        return true;
    }

    public static Token[] parseTokens(String expression) {
        if (StringUtils.hasLength(expression)) {
            Token[] tokens = TokenParser.parse(expression);
            if (tokens != null && tokens.length > 0) {
                return tokens;
            }
        }
        return null;
    }

}
