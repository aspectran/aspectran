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
