/**
 * 
 */
package com.aspectran.core.var.rule.ability;

import com.aspectran.core.var.rule.ItemRule;
import com.aspectran.core.var.rule.ItemRuleMap;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface AspectPossessable {

	/**
	 * Gets the argument item rule map.
	 *
	 * @return the argument item rule map
	 */
	public ItemRuleMap getArgumentItemRuleMap();
	
	/**
	 * Sets the argument item rule map.
	 *
	 * @param argumentItemRuleMap the new argument item rule map
	 */
	public void setArgumentItemRuleMap(ItemRuleMap argumentItemRuleMap);

	/**
	 * Adds the item rule for argument.
	 * 
	 * @param parameterRule the item rule for argument
	 */
	public void addArgumentItemRule(ItemRule argumentItemRule);

}
