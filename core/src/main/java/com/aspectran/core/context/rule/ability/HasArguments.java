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
 * An interface for rules that can have constructor arguments.
 */
public interface HasArguments {

    /**
     * Gets the argument item rule map.
     * @return the argument item rule map
     */
    ItemRuleMap getArgumentItemRuleMap();

    /**
     * Sets the argument item rule map.
     * @param argumentItemRuleMap the new argument item rule map
     */
    void setArgumentItemRuleMap(ItemRuleMap argumentItemRuleMap);

    /**
     * Adds the argument item rule.
     * @param argumentItemRule the new argument item rule
     */
    void addArgumentItemRule(ItemRule argumentItemRule);

    /**
     * Adds a new argument rule with the specified name and returns it.
     * @param argumentName the argument name
     * @return the argument item rule
     */
    default ItemRule newArgumentItemRule(String argumentName) {
        ItemRule itemRule = new ItemRule();
        itemRule.setName(argumentName);
        addArgumentItemRule(itemRule);
        return itemRule;
    }

}
