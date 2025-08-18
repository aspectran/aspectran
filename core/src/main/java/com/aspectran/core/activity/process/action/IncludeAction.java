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
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Map;

/**
 * The {@code IncludeAction} class is responsible for executing another activity by
 * including its execution flow within the current activity context.
 * <p>It evaluates attributes and parameters from the current activity, passes them
 * to the included activity, and executes the target activity using the specified
 * translet name and method type.</p>
 * <p>After execution, the result of the included activity is returned as a {@link ProcessResult}
 * or {@code Void}, depending on whether a result was produced.</p>
 *
 * <p>Created: 2008. 06. 05 PM 9:22:05</p>
 */
public class IncludeAction implements Executable {

    private final IncludeActionRule includeActionRule;

    public IncludeAction(IncludeActionRule includeActionRule) {
        this.includeActionRule = includeActionRule;
    }

    /**
     * Executes the included activity by creating a new {@link InstantActivity} instance
     * with the current activity's context.
     * <p>This method evaluates attribute and parameter rules from the included action rule
     * to extract values from the current activity context.</p>
     * <p>It sets the attribute and parameter maps on the included activity instance,
     * prepares it using the specified translet name and method type, and then performs the execution.</p>
     * <p>If the included activity produces a result, it is returned as a {@link ProcessResult};
     * otherwise, {@code Void.TYPE} is returned.</p>
     * <p>Any exception during execution is wrapped in an {@link ActionExecutionException} and
     * rethrown to maintain error propagation.</p>
     * @param activity the current activity that provides the context for attribute and parameter evaluation
     * @return the result of the included activity as a {@link ProcessResult}, or {@code Void.TYPE}
     *      if no result was produced
     * @throws Exception if an error occurs during execution of the included activity
     */
    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        try {
            InstantActivity instantActivity = new InstantActivity(activity);
            ItemRuleMap attributeItemRuleMap = includeActionRule.getAttributeItemRuleMap();
            ItemRuleMap parameterItemRuleMap = includeActionRule.getParameterItemRuleMap();
            if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
                Map<String, Object> attributeMap = activity.getItemEvaluator().evaluate(attributeItemRuleMap);
                instantActivity.setAttributeMap(attributeMap);
            }
            if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
                ParameterMap parameterMap = activity.getItemEvaluator().evaluateAsParameterMap(parameterItemRuleMap);
                instantActivity.setParameterMap(parameterMap);
            }
            instantActivity.prepare(includeActionRule.getTransletName(), includeActionRule.getMethodType());
            instantActivity.perform();
            ProcessResult processResult = instantActivity.getProcessResult();
            return (processResult != null ? processResult : Void.TYPE);
        } catch (Exception e) {
            throw new ActionExecutionException(this, e);
        }
    }

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

    /**
     * Returns the type of this action, which is always {@link ActionType#INCLUDE}.
     */
    @Override
    public ActionType getActionType() {
        return ActionType.INCLUDE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append(getActionType().toString(), includeActionRule);
        return tsb.toString();
    }

}
