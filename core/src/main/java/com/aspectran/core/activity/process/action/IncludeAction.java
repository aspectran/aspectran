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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.ToStringBuilder;

import java.util.Map;

/**
 * {@code IncludeAction} to get the execution result of another translet.
 *
 * <p>Created: 2008. 06. 05 PM 9:22:05</p>
 */
public class IncludeAction implements Executable {

    private final IncludeActionRule includeActionRule;

    /**
     * Instantiates a new IncludeAction.
     * @param includeActionRule the process call action rule
     */
    public IncludeAction(IncludeActionRule includeActionRule) {
        this.includeActionRule = includeActionRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        try {
            InstantActivity instantActivity = new InstantActivity(activity.getActivityContext());
            ItemRuleMap parameterItemRuleMap = includeActionRule.getParameterItemRuleMap();
            ItemRuleMap attributeItemRuleMap = includeActionRule.getAttributeItemRuleMap();
            if ((parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) ||
                    (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty())) {
                ItemEvaluator evaluator = new ItemEvaluation(activity);
                if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
                    ParameterMap parameterMap = evaluator.evaluateAsParameterMap(parameterItemRuleMap);
                    instantActivity.setParameterMap(parameterMap);
                }
                if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
                    Map<String, Object> attributeMap = evaluator.evaluate(attributeItemRuleMap);
                    instantActivity.setAttributeMap(attributeMap);
                }
            }
            instantActivity.prepare(includeActionRule.getTransletName(), includeActionRule.getMethodType());
            instantActivity.perform();
            return instantActivity.getProcessResult();
        } catch (Exception e) {
            throw new ActionExecutionException("Failed to execute action " + this, e);
        }
    }

    /**
     * Returns the include action rule.
     * @return the include action rule
     */
    public IncludeActionRule getIncludeActionRule() {
        return includeActionRule;
    }

    @Override
    public String getActionId() {
        return includeActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return includeActionRule.isHidden();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.INCLUDE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)getIncludeActionRule();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("include", includeActionRule);
        return tsb.toString();
    }

}
