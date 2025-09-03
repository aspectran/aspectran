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
package com.aspectran.core.context.rule.ability;

import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;

/**
 * An interface for rules that can have parameters.
 */
public interface HasParameters {

    /**
     * Gets the parameter item rule map.
     * @return the parameter item rule map
     */
    ItemRuleMap getParameterItemRuleMap();

    /**
     * Sets the parameter item rule map.
     * @param parameterItemRuleMap the new parameter item rule map
     */
    void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap);

    /**
     * Adds the parameter item rule.
     * @param parameterItemRule the new parameter item rule
     */
    void addParameterItemRule(ItemRule parameterItemRule);

    /**
     * A convenience method to create a new parameter rule with the specified name and add it.
     * @param parameterName the parameter name
     * @return the new parameter item rule
     */
    default ItemRule newParameterItemRule(String parameterName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(parameterName);
        addParameterItemRule(itemRule);
        return itemRule;
    }

}
