package com.aspectran.core.context.expr;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityDataMap;
import com.aspectran.core.context.expr.ognl.OgnlSupport;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.IllegalRuleException;

/**
 * It supports expressions in the CHOOSE-WHEN statement,
 * and evaluates the expression as a boolean result.
 *
 * <p>Created: 2019-01-06</p>
 *
 * @since 6.0.0
 */
public class BooleanExpression {

    protected final Activity activity;

    public BooleanExpression(Activity activity) {
        this.activity = activity;
    }

    public boolean evaluate(ChooseWhenRule chooseWhenRule) throws IllegalRuleException {
        if (chooseWhenRule.getExpression() == null) {
            return true;
        }
        ActivityDataMap root = (activity.getTranslet() != null ? activity.getTranslet().getActivityDataMap() : null);
        return OgnlSupport.evaluateAsBoolean(chooseWhenRule.getExpression(), chooseWhenRule.getRepresented(), root);
    }

    public static Object parseExpression(String expression) throws IllegalRuleException {
        return OgnlSupport.parseExpression(expression);
    }

}
