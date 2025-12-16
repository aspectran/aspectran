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
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

/**
 * An action that sets one or more headers on the HTTP response.
 *
 * <p>This action is used in web applications to dynamically add or modify response
 * headers, such as {@code Content-Type} or {@code Cache-Control}, based on the
 * current context. It evaluates configured item rules to determine the header names
 * and values.</p>
 *
 * <p>Created: 2016. 08. 23.</p>
 *
 * @since 3.0.0
 */
public class HeaderAction implements Executable {

    private final HeaderActionRule headerActionRule;

    /**
     * Instantiates a new HeaderAction.
     * @param headerActionRule the rule that defines the headers to be set
     */
    public HeaderAction(HeaderActionRule headerActionRule) {
        this.headerActionRule = headerActionRule;
    }

    /**
     * Executes the action, evaluating the header values and setting them on the response.
     * @param activity the current activity, which provides the response adapter
     * @return the evaluated header map
     * @throws Exception if an error occurs during value evaluation or header setting
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

    /**
     * Returns the rule that defines this header action.
     * @return the header action rule
     */
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
