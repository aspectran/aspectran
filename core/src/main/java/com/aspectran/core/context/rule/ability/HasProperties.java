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
