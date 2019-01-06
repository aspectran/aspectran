package com.aspectran.core.context.expr;

import com.aspectran.core.activity.Activity;

/**
 * <p>Created: 2019-01-06</p>
 */
public class CaseExpression {

    protected final Activity activity;

    public CaseExpression(Activity activity) {
        this.activity = activity;
    }

    public boolean test(String expression) {
        return true;
    }

}
