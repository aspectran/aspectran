/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.rule;

import com.aspectran.core.util.BooleanUtils;

/**
 * The Class IncludeActionRule.
 * 
 * <p>Created: 2008. 06. 05 PM 9:25:40</p>
 */
public class IncludeActionRule {
	
	private String actionId;
	
	private String transletName;
	
	private ItemRuleMap attributeItemRuleMap;
	
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
	 * Sets the id.
	 *
	 * @param actionId the new id
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	
	/**
	 * Gets the translet name.
	 *
	 * @return the translet name
	 */
	public String getTransletName() {
		return transletName;
	}

	/**
	 * Sets the translet name.
	 *
	 * @param transletName the new translet name
	 */
	public void setTransletName(String transletName) {
		this.transletName = transletName;
	}

	/**
	 * Gets the attribute item rule map.
	 *
	 * @return the attribute item rule map
	 */
	public ItemRuleMap getAttributeItemRuleMap() {
		return attributeItemRuleMap;
	}

	/**
	 * Sets the attribute item rule map.
	 *
	 * @param attributeItemRuleMap the new attribute item rule map
	 */
	public void setAttributeItemRuleMap(ItemRuleMap attributeItemRuleMap) {
		this.attributeItemRuleMap = attributeItemRuleMap;
	}

	/**
	 * Adds the attribute item rule.
	 *
	 * @param attributeItemRule the attribute item rule
	 */
	public void addAttributeItemRule(ItemRule attributeItemRule) {
		if(attributeItemRuleMap == null) 
			attributeItemRuleMap = new ItemRuleMap();
		
		attributeItemRuleMap.putItemRule(attributeItemRule);
	}
	
	/**
	 * Returns whether to hide result of the action.
	 * 
	 * @return true, if is hidden
	 */
	public Boolean getHidden() {
		return hidden;
	}

	/**
	 * Returns whether to hide result of the action.
	 *
	 * @return true, if is hidden
	 */
	public Boolean isHidden() {
		return BooleanUtils.toBoolean(hidden);
	}

	/**
	 * Sets whether to hide result of the action.
	 * 
	 * @param hidden whether to hide result of the action
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
		sb.append(", transletName=").append(transletName);
		if(attributeItemRuleMap != null) {
			sb.append(", attributes=[");
			int sbLength = sb.length();
			for(String name : attributeItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}
			sb.append("]");
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	/**
	 * Returns a new instance of IncludeActionRule.
	 *
	 * @param id the action id
	 * @param transletName the translet name
	 * @param hidden whether to hide result of the action
	 * @return the include action rule
	 */
	public static IncludeActionRule newInstance(String id, String transletName, Boolean hidden) {
		IncludeActionRule includeActionRule = new IncludeActionRule();
		includeActionRule.setActionId(id);
		includeActionRule.setTransletName(transletName);
		includeActionRule.setHidden(hidden);

		return includeActionRule;
	}

}
