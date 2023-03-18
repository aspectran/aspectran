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
package com.aspectran.core.context.rule;

import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class EchoActionRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:50:44</p>
 */
public class EchoActionRule {

    private String actionId;

    private ItemRuleMap echoItemRuleMap;

    private Boolean hidden;

    /**
     * Gets the action id.
     *
     * @return the action id
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Sets the action id.
     *
     * @param actionId the new action id
     */
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    /**
     * Gets the echo item rule map.
     *
     * @return the echo item rule map
     */
    public ItemRuleMap getEchoItemRuleMap() {
        return echoItemRuleMap;
    }

    /**
     * Sets the echo item rule map.
     *
     * @param echoItemRuleMap the new echo item rule map
     */
    public void setEchoItemRuleMap(ItemRuleMap echoItemRuleMap) {
        this.echoItemRuleMap = echoItemRuleMap;
    }

    /**
     * Adds a new echo item rule with the specified name and returns it.
     *
     * @param echoName the echo name
     * @return the echo item rule
     */
    public ItemRule newEchoItemRule(String echoName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(echoName);
        addEchoItemRule(itemRule);
        return itemRule;
    }

    /**
     * Adds the echo item rule.
     *
     * @param itemRule the echo item rule
     */
    public void addEchoItemRule(ItemRule itemRule) {
        if (echoItemRuleMap == null) {
            echoItemRuleMap = new ItemRuleMap();
        }
        echoItemRuleMap.putItemRule(itemRule);
    }

    /**
     * Returns whether to hide result of the action.
     *
     * @return true, if is hidden
     */
    public Boolean getHidden() {
        return hidden;
    }

    /**
     * Returns whether to hide result of the action.
     *
     * @return true, if is hidden
     */
    public boolean isHidden() {
        return BooleanUtils.toBoolean(hidden);
    }

    /**
     * Sets whether to hide result of the action.
     *
     * @param hidden whether to hide result of the action
     */
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", actionId);
        if (echoItemRuleMap != null) {
            tsb.append("attributes", echoItemRuleMap.keySet());
        }
        tsb.append("hidden", hidden);
        return tsb.toString();
    }

    /**
     * Returns a new derived instance of EchoActionRule.
     *
     * @param id the action id
     * @param hidden whether to hide result of the action
     * @return the echo action rule
     */
    public static EchoActionRule newInstance(String id, Boolean hidden) {
        EchoActionRule echoActionRule = new EchoActionRule();
        echoActionRule.setActionId(id);
        echoActionRule.setHidden(hidden);
        return echoActionRule;
    }

}
