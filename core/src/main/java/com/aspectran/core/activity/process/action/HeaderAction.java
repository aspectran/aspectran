/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpression;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.ToStringBuilder;

import java.util.List;
import java.util.Map;

/**
 * {@code HeaderAction} to set response headers.
 * 
 * <p>Created: 2016. 08. 23.</p>
 * 
 * @since 3.0.0
 */
public class HeaderAction implements Executable {

    private final HeaderActionRule headerActionRule;

    /**
     * Instantiates a new HeaderAction.
     *
     * @param headerActionRule the header action rule
     */
    public HeaderAction(HeaderActionRule headerActionRule) {
        this.headerActionRule = headerActionRule;
    }

    @Override
    public Object execute(Activity activity) throws Exception {
        ItemRuleMap itemRuleMap = headerActionRule.getHeaderItemRuleMap();
        if (itemRuleMap == null || itemRuleMap.isEmpty()) {
            return null;
        }
        try {
            ItemEvaluator evaluator = new ItemExpression(activity);
            MultiValueMap<String, String> valueMap = evaluator.evaluateAsMultiValueMap(itemRuleMap);
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
            throw new ActionExecutionException("Failed to execute header action " + this, e);
        }
    }

    /**
     * Returns the header action rule.
     *
     * @return the headerActionRule
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
    @SuppressWarnings("unchecked")
    public <T> T getActionRule() {
        return (T)getHeaderActionRule();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", getActionType());
        tsb.append("headerActionRule", headerActionRule);
        return tsb.toString();
    }

}
