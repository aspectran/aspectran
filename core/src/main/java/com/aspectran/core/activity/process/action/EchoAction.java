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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;

/**
 * {@code EchoAction} to produce attributes.
 *
 * <p>Created: 2008. 03. 22 PM 5:50:44</p>
 */
public class EchoAction implements Executable {

    private final EchoActionRule echoActionRule;

    /**
     * Instantiates a new EchoAction.
     * @param echoActionRule the echo action rule
     */
    public EchoAction(EchoActionRule echoActionRule) {
        this.echoActionRule = echoActionRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        ItemRuleMap itemRuleMap = echoActionRule.getEchoItemRuleMap();
        if (itemRuleMap == null || itemRuleMap.isEmpty()) {
            return null;
        }
        try {
            ItemEvaluator evaluator = new ItemEvaluation(activity);
            return evaluator.evaluate(itemRuleMap);
        } catch (Exception e) {
            throw new ActionExecutionException("Failed to execute action " + this, e);
        }
    }

    /**
     * Returns the echo action rule.
     * @return the echo action rule
     */
    public EchoActionRule getEchoActionRule() {
        return echoActionRule;
    }

    @Override
    public String getActionId() {
        return echoActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return echoActionRule.isHidden();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ECHO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)echoActionRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("echo", echoActionRule);
        return tsb.toString();
    }

}
