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

public interface HasProperties {

    /**
     * Gets the property item rule map.
     * @return the property item rule map
     */
    ItemRuleMap getPropertyItemRuleMap();

    /**
     * Sets the property item rule map.
     * @param propertyItemRuleMap the new property item rule map
     */
    void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap);

    /**
     * Adds the property item rule.
     * @param propertyItemRule the new property item rule
     */
    void addPropertyItemRule(ItemRule propertyItemRule);

    /**
     * Adds a new property rule with the specified name and returns it.
     * @param propertyName the property name
     * @return the property item rule
     */
    default ItemRule newPropertyItemRule(String propertyName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(propertyName);
        addPropertyItemRule(itemRule);
        return itemRule;
    }

}
