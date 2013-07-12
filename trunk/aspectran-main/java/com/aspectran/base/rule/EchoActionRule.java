/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.base.rule;


/**
 * <p>Created: 2008. 03. 22 오후 5:50:44</p>
 */
public class EchoActionRule {
	
	private String actionId;
	
	private ItemRuleMap itemRuleMap;

	private Boolean hidden;
	
	/**
	 * Gets the action id.
	 * 
	 * @return the action id
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * Sets the action id.
	 * 
	 * @param actionId the new action id
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	/**
	 * Gets the value rule map.
	 * 
	 * @return the value rule map
	 */
	public ItemRuleMap getItemRuleMap() {
		return itemRuleMap;
	}

	/**
	 * Sets the parameter rule map.
	 * 
	 * @param itemRuleMap the parameterRules to set
	 */
	public void setItemRuleMap(ItemRuleMap itemRuleMap) {
		this.itemRuleMap = itemRuleMap;
	}

	/**
	 * Adds the parameter rule.
	 * 
	 * @param itemRule the parameter rule
	 */
	public void addItemRule(ItemRule itemRule) {
		if(itemRuleMap == null) 
			itemRuleMap = new ItemRuleMap();
		
		itemRuleMap.putItemRule(itemRule);
	}
	
	/**
	 * Gets the hidden.
	 * 
	 * @return the hidden
	 */
	public Boolean getHidden() {
		return hidden;
	}

	/**
	 * Sets the hidden.
	 * 
	 * @param hidden the new hidden
	 */
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{id=").append(actionId);
		sb.append(", hidden=").append(hidden);

		if(itemRuleMap != null) {
			sb.append(", items=[");
			int sbLength = sb.length();

			for(String name : itemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}

			sb.append("]");
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
