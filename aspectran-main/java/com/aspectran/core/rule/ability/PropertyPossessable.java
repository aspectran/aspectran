/**
 * 
 */
package com.aspectran.core.rule.ability;

import com.aspectran.core.rule.ItemRule;
import com.aspectran.core.rule.ItemRuleMap;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface PropertyPossessable {

	/**
	 * Gets the parameter rule map for properties.
	 * 
	 * @return the parameter rule map
	 */
	public ItemRuleMap getPropertyItemRuleMap();
	
	/**
	 * Sets the parameter rule map for properties.
	 * 
	 * @param parameterRuleMap the new parameter rule map
	 */
	public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap);

	/**
	 * Adds the parameter rule for property.
	 * 
	 * @param parameterRule the item rule for property
	 */
	public void addPropertyItemRule(ItemRule propertyItemRule);

}
