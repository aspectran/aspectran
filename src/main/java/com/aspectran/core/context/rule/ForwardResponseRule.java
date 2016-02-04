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

import com.aspectran.core.context.rule.ability.ActionPossessable;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.util.BooleanUtils;

/**
 * The Class ForwardResponseRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:51:58</p>
 */
public class ForwardResponseRule extends ActionPossessSupport implements ActionPossessable, Replicable<ForwardResponseRule> {
	
	public static final ResponseType RESPONSE_TYPE = ResponseType.FORWARD;

	private String contentType;
	
	private String transletName;
	
	private ItemRuleMap attributeItemRuleMap;

	private Boolean defaultResponse;

	/**
	 * Gets the content type.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type.
	 * 
	 * @param contentType the new content type
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
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
	 * Adds the attribute value rule.
	 *
	 * @param attributeItemRule the attribute item rule
	 */
	public void addAttributeValueRule(ItemRule attributeItemRule) {
		if(attributeItemRuleMap == null) 
			attributeItemRuleMap = new ItemRuleMap();
		
		attributeItemRuleMap.putItemRule(attributeItemRule);
	}

	/**
	 * Returns whether the default response.
	 *
	 * @return whether the default response
	 */
	public Boolean getDefaultResponse() {
		return defaultResponse;
	}

	/**
	 * Returns whether the default response.
	 *
	 * @return true, if is default response
	 */
	public boolean isDefaultResponse() {
		return BooleanUtils.toBoolean(defaultResponse);
	}

	/**
	 * Sets whether the default response.
	 *
	 * @param defaultResponse whether the default response
	 */
	public void setDefaultResponse(Boolean defaultResponse) {
		this.defaultResponse = defaultResponse;
	}

	@Override
	public ForwardResponseRule replicate() {
		return replicate(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{contentType=").append(contentType);
		sb.append(", translet=").append(transletName);
		if(defaultResponse != null)
			sb.append(", defaultResponse=").append(defaultResponse);
		sb.append("}");
		
		return sb.toString();
	}
	
	/**
	 * Returns a new instance of ForwardResponseRule.
	 *
	 * @param contentType the content type
	 * @param transletName the translet name
	 * @param defaultResponse whether the default response
	 * @return an instance of ForwardResponseRule
	 */
	public static ForwardResponseRule newInstance(String contentType, String transletName, Boolean defaultResponse) {
		if(transletName == null)
			throw new IllegalArgumentException("transletName must not be null");
		
		ForwardResponseRule frr = new ForwardResponseRule();
		frr.setContentType(contentType);
		frr.setTransletName(transletName);
		frr.setDefaultResponse(defaultResponse);

		return frr;
	}
	
	public static ForwardResponseRule newInstance(String transletName) {
		if(transletName == null)
			throw new IllegalArgumentException("transletName must not be null");
		
		ForwardResponseRule frr = new ForwardResponseRule();
		frr.setTransletName(transletName);

		return frr;
	}
	
	public static ForwardResponseRule replicate(ForwardResponseRule forwardResponseRule) {
		ForwardResponseRule frr = new ForwardResponseRule();
		frr.setContentType(forwardResponseRule.getContentType());
		frr.setTransletName(forwardResponseRule.getTransletName());
		frr.setAttributeItemRuleMap(forwardResponseRule.getAttributeItemRuleMap());
		frr.setDefaultResponse(forwardResponseRule.getDefaultResponse());
		frr.setActionList(forwardResponseRule.getActionList());
		
		return frr;
	}
	
}
