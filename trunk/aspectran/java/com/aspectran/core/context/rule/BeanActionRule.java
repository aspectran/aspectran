/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
import com.aspectran.core.context.rule.ability.ArgumentPossessable;
import com.aspectran.core.context.rule.ability.PropertyPossessable;
import com.aspectran.core.util.BooleanUtils;

/**
 * <p>Created: 2008. 03. 22 오후 5:50:35</p>
 */
public class BeanActionRule implements ArgumentPossessable, PropertyPossessable, AspectAdviceSupport {
	
	protected String actionId;
	
	protected String beanId;
	
	protected String methodName;

	protected ItemRuleMap propertyItemRuleMap;
	
	protected ItemRuleMap argumentItemRuleMap;
	
	protected Boolean hidden;
	
	private AspectAdviceRule aspectAdviceRule;
	
	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;

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
	
	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	/**
	 * Gets the action method name.
	 * 
	 * @return the action method name
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Sets the action method name.
	 * 
	 * @param methodName the new action method name
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	/**
	 * Gets the hidden.
	 * 
	 * @return the hidden
	 */
	public Boolean getHidden() {
		return hidden;
	}

	public boolean isHidden() {
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

	/**
	 * Gets the parameter rule map for properties.
	 * 
	 * @return the parameter rule map
	 */
	public ItemRuleMap getPropertyItemRuleMap() {
		return propertyItemRuleMap;
	}

	/**
	 * Sets the parameter rule map for properties.
	 * 
	 * @param parameterRuleMap the new parameter rule map
	 */
	public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
		this.propertyItemRuleMap = propertyItemRuleMap;
	}

	/**
	 * Adds the parameter rule for property.
	 * 
	 * @param parameterRule the item rule for property
	 */
	public void addPropertyItemRule(ItemRule propertyItemRule) {
		if(propertyItemRuleMap == null) 
			propertyItemRuleMap = new ItemRuleMap();
		
		propertyItemRuleMap.putItemRule(propertyItemRule);
	}
	
	/**
	 * Gets the argument item rule map.
	 *
	 * @return the argument item rule map
	 */
	public ItemRuleMap getArgumentItemRuleMap() {
		return argumentItemRuleMap;
	}

	/**
	 * Sets the argument item rule map.
	 *
	 * @param argumentItemRuleMap the new argument item rule map
	 */
	public void setArgumentItemRuleMap(ItemRuleMap argumentItemRuleMap) {
		this.argumentItemRuleMap = argumentItemRuleMap;
	}

	/**
	 * Adds the item rule for argument.
	 * 
	 * @param parameterRule the item rule for argument
	 */
	public void addArgumentItemRule(ItemRule argumentItemRule) {
		if(argumentItemRuleMap == null) 
			argumentItemRuleMap = new ItemRuleMap();
		
		argumentItemRuleMap.putItemRule(argumentItemRule);
	}

	public AspectAdviceRule getAspectAdviceRule() {
		return aspectAdviceRule;
	}

	public void setAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
		this.aspectAdviceRule = aspectAdviceRule;
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
		sb.append(", bean=").append(beanId);
		sb.append(", method=").append(methodName);
		sb.append(", hidden=").append(hidden);

		if(propertyItemRuleMap != null) {
			sb.append(", properties=[");
			int sbLength = sb.length();

			for(String name : propertyItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}

			sb.append("]");
		}
		
		if(argumentItemRuleMap != null) {
			sb.append(", arguments=[");
			int sbLength = sb.length();
			
			for(String name : argumentItemRuleMap.keySet()) {
				if(sb.length() > sbLength)
					sb.append(", ");
				
				sb.append(name);
			}
			
			sb.append("]");
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public static BeanActionRule newInstance(String id, String beanId, String methodName, Boolean hidden) {
		BeanActionRule beanActionRule = new BeanActionRule();
		beanActionRule.setActionId(id);
		beanActionRule.setBeanId(beanId);
		beanActionRule.setMethodName(methodName);
		beanActionRule.setHidden(hidden);

		return beanActionRule;
	}
	
}
