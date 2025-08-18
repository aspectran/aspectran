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
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * {@code EchoAction} is an executable action that evaluates a set of item rules to produce attribute values
 * during the execution of an activity. It retrieves an {@link ItemRuleMap} from the configured rule,
 * evaluates it using the activity's item evaluator, and returns the resulting value. This action is typically
 * used to expose data from the current context to a view or to trigger side effects based on attribute values.
 *
 * <p>This action supports dynamic evaluation through configurable rules and is part of the Aspectran
 * activity processing pipeline. The action type is always {@link ActionType#ECHO}, indicating its purpose
 * is to echo (produce) values rather than modify the activity state.</p>
 *
 * <p>Example usage: When a request parameter is defined, an echo action can extract and return its value
 * to be displayed in a response or used in a template.</p>
 */
public class EchoAction implements Executable {

    private final EchoActionRule echoActionRule;

    /**
     * Creates a new instance of {@code EchoAction} with the specified rule configuration.
     * The rule defines which attributes to evaluate and how to produce output values.
     * This rule is used during execution to retrieve an {@link ItemRuleMap} and evaluate it
     * using the current activity's item evaluator.
     *
     * @param echoActionRule the rule configuration that defines the attribute evaluation logic
     *        and output behavior; must not be null
     * @throws IllegalArgumentException if the rule is null
     */
    public EchoAction(EchoActionRule echoActionRule) {
        this.echoActionRule = echoActionRule;
    }

    /**
     * Executes the echo action by evaluating the configured item rules to produce a value.
     * The action first retrieves the item rule map from the echo action rule, then evaluates
     * it using the activity's item evaluator. If no rules are defined or the rule map is empty,
     * the action returns {@link Void#TYPE} to indicate no output was produced.
     * <p>The evaluation may involve dynamic expressions, attribute references, or conditions,
     * and any errors during evaluation are wrapped in an {@link ActionExecutionException}
     * and re-thrown to ensure proper error handling in the activity pipeline.</p>
     * @param activity the current activity containing the item evaluator and state
     * @return the evaluated result of the item rules, or {@link Void#TYPE} if no rules exist
     * @throws Exception if an error occurs during evaluation or rule processing
     */
    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        ItemRuleMap itemRuleMap = echoActionRule.getEchoItemRuleMap();
        if (itemRuleMap == null || itemRuleMap.isEmpty()) {
            return Void.TYPE;
        }
        try {
            return activity.getItemEvaluator().evaluate(itemRuleMap);
        } catch (Exception e) {
            throw new ActionExecutionException(this, e);
        }
    }

    /**
     * Returns the rule configuration that defines how this echo action evaluates attributes
     * and produces output values.
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

    /**
     * Returns the type of this action, which is always {@link ActionType#ECHO}.
     */    @Override
    public ActionType getActionType() {
        return ActionType.ECHO;
    }

    /**
     * Returns a string representation of this action, including the rule configuration.
     * This is useful for debugging and logging purposes.
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append(getActionType().toString(), echoActionRule);
        return tsb.toString();
    }

}
