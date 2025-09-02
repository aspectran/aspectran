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

public interface HasAttributes {

    /**
     * Gets the attribute item rule map.
     * @return the attribute item rule map
     */
    ItemRuleMap getAttributeItemRuleMap();

    /**
     * Sets the attribute item rule map.
     * @param attributeItemRuleMap the new attribute item rule map
     */
    void setAttributeItemRuleMap(ItemRuleMap attributeItemRuleMap);

    /**
     * Adds the attribute item rule.
     * @param attributeItemRule the attribute item rule
     */
    void addAttributeItemRule(ItemRule attributeItemRule);

    /**
     * Adds a new attribute rule with the specified name and returns it.
     * @param attributeName the attribute name
     * @return the attribute item rule
     */
    default ItemRule newAttributeItemRule(String attributeName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(attributeName);
        addAttributeItemRule(itemRule);
        return itemRule;
    }

}
