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
 * <p>Created: 2019-07-13</p>
 */
public class ChooseAction implements Executable {

    private final ChooseRule chooseRule;

    private final List<ChooseWhenRule> chooseWhenRules;

    public ChooseAction(@NonNull ChooseRule chooseRule) {
        this.chooseRule = chooseRule;
        this.chooseWhenRules = chooseRule.getChooseWhenRules();
    }

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
