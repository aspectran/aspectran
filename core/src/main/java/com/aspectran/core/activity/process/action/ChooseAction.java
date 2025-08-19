/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.value.BooleanExpression;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.List;

/**
 * An action that provides conditional execution flow, similar to a switch statement.
 *
 * <p>This action evaluates a series of {@code <when>} conditions sequentially. The first
 * condition that evaluates to {@code true} has its associated actions executed, and the
 * choose block is then exited. If no conditions match, the actions within the
 * {@code <otherwise>} block, if present, are executed.</p>
 *
 * <p>Created: 2019-07-13</p>
 */
public class ChooseAction implements Executable {

    private final ChooseRule chooseRule;

    private final List<ChooseWhenRule> chooseWhenRules;

    /**
     * Instantiates a new ChooseAction.
     * @param chooseRule the rule that defines the conditional branches
     */
    public ChooseAction(@NonNull ChooseRule chooseRule) {
        this.chooseRule = chooseRule;
        this.chooseWhenRules = chooseRule.getChooseWhenRules();
    }

    /**
     * Executes the choose action by evaluating each {@code <when>} condition.
     * @param activity the current activity
     * @return the {@link ChooseWhenRule} that evaluated to true, or {@link Void#TYPE} if no
     *      condition matched (indicating the 'otherwise' branch should be taken).
     * @throws Exception if an error occurs during condition evaluation
     */
    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        if (chooseWhenRules != null) {
            for (ChooseWhenRule chooseWhenRule : chooseWhenRules) {
                BooleanExpression booleanEvaluation = chooseWhenRule.getBooleanExpression();
                if (booleanEvaluation == null || booleanEvaluation.evaluate(activity)) {
                    return chooseWhenRule;
                }
            }
        }
        return Void.TYPE;
    }

    /**
     * Returns the rule that defines this choose action.
     * @return the choose rule
     */
    public ChooseRule getChooseRule() {
        return chooseRule;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.CHOOSE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append(getActionType().toString(), chooseRule);
        return tsb.toString();
    }

}
