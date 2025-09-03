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
package com.aspectran.core.context.rule;

import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Rule for an action that sets headers in the response.
 *
 * <p>Created: 2016. 08. 23.</p>
 *
 * @since 3.0.0
 */
public class HeaderActionRule {

    private String actionId;

    private ItemRuleMap headerItemRuleMap;

    private Boolean hidden;

    /**
     * Gets the action id.
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Sets the action id.
     * @param actionId the new action id
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Gets the map of headers to be set.
     * @return the header item rule map
     */
    public ItemRuleMap getHeaderItemRuleMap() {
        return headerItemRuleMap;
    }

    /**
     * Sets the map of headers to be set.
     * @param headerItemRuleMap the new header item rule map
     */
    public void setHeaderItemRuleMap(ItemRuleMap headerItemRuleMap) {
        this.headerItemRuleMap = headerItemRuleMap;
    }

    /**
     * Adds a new header rule with the specified name and returns it.
     * @param headerName the header name
     * @return the header item rule
     */
    public ItemRule newHeaderItemRule(String headerName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(headerName);
        addHeaderItemRule(itemRule);
        return itemRule;
    }

    /**
     * Adds a header item rule.
     * @param headerItemRule the header item rule
     */
    public void addHeaderItemRule(ItemRule headerItemRule) {
        if (headerItemRuleMap == null) {
            headerItemRuleMap = new ItemRuleMap();
        }
        headerItemRuleMap.putItemRule(headerItemRule);
    }

    /**
     * Returns whether to hide the result of the action.
     * @return true, if this action is hidden
     */
    public Boolean getHidden() {
        return hidden;
    }

    /**
     * Returns whether to hide the result of the action.
     * @return true, if this action is hidden
     */
    public boolean isHidden() {
        return BooleanUtils.toBoolean(hidden, true);
    }

    /**
     * Sets whether to hide the result of the action.
     * @param hidden whether to hide the result of the action
     */
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", actionId);
        if (headerItemRuleMap != null) {
            tsb.append("headers", headerItemRuleMap.keySet());
        }
        tsb.append("hidden", hidden);
        return tsb.toString();
    }

    /**
     * Creates a new instance of HeaderActionRule.
     * @param id the action id
     * @param hidden whether to hide the result of the action
     * @return the new header action rule
     */
    @NonNull
    public static HeaderActionRule newInstance(String id, Boolean hidden) {
        HeaderActionRule headerActionRule = new HeaderActionRule();
        headerActionRule.setActionId(id);
        headerActionRule.setHidden(hidden);
        return headerActionRule;
    }

}
