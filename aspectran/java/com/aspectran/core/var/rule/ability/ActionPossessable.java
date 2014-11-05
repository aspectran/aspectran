/**
 * 
 */
package com.aspectran.core.var.rule.ability;

import com.aspectran.core.activity.process.ActionList;

/**
 * The Interface ActionPossessable.
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 */
public interface ActionPossessable {

	/**
	 * Gets the action list.
	 *
	 * @return the action list
	 */
	public ActionList getActionList();
	
	/**
	 * Sets the action list.
	 *
	 * @param actionList the new action list
	 */
	public void setActionList(ActionList actionList);

}
