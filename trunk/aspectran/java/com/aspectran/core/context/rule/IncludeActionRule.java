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
package com.aspectran.core.context.rule;

import java.util.List;

import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.util.BooleanUtils;


/**
 * <p>Created: 2008. 06. 05 오후 9:25:40</p>
 */
public class IncludeActionRule {
	
	/** The id. */
	private String actionId;
	
	/** The translet name. */
	private String transletName;
	
	/** The attribute item rule map. */
	private ItemRuleMap attributeItemRuleMap;
	
	/** The hidden. */
	private Boolean hidden;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

	/**
	 * Gets the id.
	 *
	 * @return the id
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
	 * Gets the parameter rule map for attributes.
	 * 
	 * @return the parameter rule map for attributes
	 */
	public ItemRuleMap getAttributeItemRuleMap() {
		return attributeItemRuleMap;
	}

	/**
	 * Sets the parameter rule map for attributes.
	 * 
	 * @param attributeItemRuleMap the new parameter rule map for attributes
	 */
	public void setAttributeItemRuleMap(ItemRuleMap attributeItemRuleMap) {
		this.attributeItemRuleMap = attributeItemRuleMap;
	}

	/**
	 * Adds the parameter rule for attributes.
	 * 
	 * @param attributeItemRule the parameter rule for attributes
	 */
	public void addAttributeItemRule(ItemRule attributeItemRule) {
		if(attributeItemRuleMap == null) 
			attributeItemRuleMap = new ItemRuleMap();
		
		attributeItemRuleMap.putItemRule(attributeItemRule);
	}
	
	/**
	 * Gets the hidden.
	 * 
	 * @return the hidden
	 */
	public Boolean getHidden() {
		return hidden;
	}

	public Boolean isHidden() {
		return BooleanUtils.toBoolean(hidden);
	}

	/**
	 * Sets the hidden.
	 * 
	 * @param hidden the new hidden
	 */
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}
	
	public List<AspectAdviceRule> getBeforeAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getBeforeAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getAfterAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getAfterAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getFinallyAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getFinallyAdviceRuleList();
	}
	
	public List<AspectAdviceRule> getExceptionRaizedAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getExceptionRaizedAdviceRuleList();
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
	
	public static IncludeActionRule newInstance(String id, String transletName, Boolean hidden) {
		IncludeActionRule includeActionRule = new IncludeActionRule();
		includeActionRule.setActionId(id);
		includeActionRule.setTransletName(transletName);
		includeActionRule.setHidden(hidden);

		return includeActionRule;
	}
	
}
