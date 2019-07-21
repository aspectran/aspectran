package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.context.expr.BooleanExpression;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.ToStringBuilder;

import java.util.List;

/**
 * <p>Created: 2019-07-13</p>
 */
public class ChooseAction implements Executable {

    private final ChooseRule chooseRule;

    private final List<ChooseWhenRule> chooseWhenRules;

    public ChooseAction(ChooseRule chooseRule) {
        this.chooseRule = chooseRule;
        this.chooseWhenRules = chooseRule.getChooseWhenRules();
    }

    @Override
    public String getActionId() {
        return null;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        if (chooseWhenRules != null) {
            for (ChooseWhenRule chooseWhenRule : chooseWhenRules) {
                BooleanExpression expression = new BooleanExpression(activity);
                if (expression.evaluate(chooseWhenRule)) {
                    return chooseWhenRule;
                }
            }
        }
        return ActionResult.NO_RESULT;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.CHOOSE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)chooseRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", getActionType());
        tsb.append("chooseRule", chooseRule);
        return tsb.toString();
    }

}
