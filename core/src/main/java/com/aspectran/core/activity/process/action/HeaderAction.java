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
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.List;
import java.util.Map;

/**
 * {@code HeaderAction} is an executable action that sets HTTP response headers based on configured
 * header rules. It evaluates a rule map that maps header names to values, resolves those values
 * using the activity's item evaluator, and then applies them to the response using the response
 * adapter. This action is typically used in web applications to set headers such as Content-Type,
 * Cache-Control, or X-Frame-Options.
 *
 * <p>For example, a rule like {@code "Content-Type: application/json"} would result in the response
 * header being set to {@code "application/json"}. Multiple values per header are supported via
 * multi-value maps.</p>
 *
 * <p>Created: 2016. 08. 23.</p>
 *
 * @since 3.0.0
 */
public class HeaderAction implements Executable {

    private final HeaderActionRule headerActionRule;

    /**
     * Instantiates a new HeaderAction.
     * @param headerActionRule the header action rule
     */
    public HeaderAction(HeaderActionRule headerActionRule) {
        this.headerActionRule = headerActionRule;
    }

    /**
     * Executes the header action by evaluating the header rules and setting response headers.
     * <p>
     * This method retrieves the header item rule map from the action rule, evaluates it using the
     * activity's item evaluator to produce a map of header names to values, and then sets each
     * header in the response using the response adapter. If no headers are defined or the evaluation
     * results in an empty map, this method returns {@code Void.TYPE} and does not modify the response.
     * </p>
     * @param activity the current activity context containing the request and response objects
     * @return the evaluated header map, or {@code Void.TYPE} if no headers are defined
     * @throws Exception if an error occurs during evaluation or header setting, wrapped in an
     *                   {@link ActionExecutionException}
     */
    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        ItemRuleMap itemRuleMap = headerActionRule.getHeaderItemRuleMap();
        if (itemRuleMap == null || itemRuleMap.isEmpty()) {
            return Void.TYPE;
        }
        try {
            ItemEvaluator itemEvaluator = activity.getItemEvaluator();
            MultiValueMap<String, String> valueMap = itemEvaluator.evaluateAsMultiValueMap(itemRuleMap);
            if (!valueMap.isEmpty()) {
                ResponseAdapter responseAdapter = activity.getResponseAdapter();
                for (Map.Entry<String, List<String>> entry : valueMap.entrySet()) {
                    String name = entry.getKey();
                    List<String> values = entry.getValue();
                    for (String value : values) {
                        responseAdapter.addHeader(name, value);
                    }
                }
            }
            return valueMap;
        } catch (Exception e) {
            throw new ActionExecutionException(this, e);
        }
    }

    public HeaderActionRule getHeaderActionRule() {
        return headerActionRule;
    }

    @Override
    public String getActionId() {
        return headerActionRule.getActionId();
    }

    @Override
    public boolean isHidden() {
        return headerActionRule.isHidden();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.HEADER;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append(getActionType().toString(), headerActionRule);
        return tsb.toString();
    }

}
