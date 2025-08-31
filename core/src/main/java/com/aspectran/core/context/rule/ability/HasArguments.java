package com.aspectran.core.context.rule.ability;

import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;

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
