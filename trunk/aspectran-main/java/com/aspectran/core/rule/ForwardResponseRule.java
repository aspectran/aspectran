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
package com.aspectran.core.rule;

import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.rule.ability.ActionPossessable;
import com.aspectran.core.type.ResponseType;

// TODO: Auto-generated Javadoc
/**
 * <p>Created: 2008. 03. 22 오후 5:51:58</p>
 */
public class ForwardResponseRule extends ActionPossessSupport implements ActionPossessable {
	
	/** The Constant RESPONSE_TYPE. */
	public static final ResponseType RESPONSE_TYPE = ResponseType.FORWARD;

	/** The content type. */
	private String contentType;
	
	/** The translet name. */
	private String transletName;
	
	/** The parameter item rule map. */
	private ItemRuleMap parameterItemRuleMap;

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
	 * Gets the parameter rule map.
	 * 
	 * @return the parameter rule map
	 */
	public ItemRuleMap getParameterItemRuleMap() {
		return parameterItemRuleMap;
	}

	/**
	 * Sets the parameter rule map.
	 * 
	 * @param parameterItemRuleMap the new parameter rule map
	 */
	public void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap) {
		this.parameterItemRuleMap = parameterItemRuleMap;
	}
	
	/**
	 * Adds the parameter rule.
	 * 
	 * @param parameterItemRule the parameter rule
	 */
	public void addParameterValueRule(ItemRule parameterItemRule) {
		if(parameterItemRuleMap == null) 
			parameterItemRuleMap = new ItemRuleMap();
		
		parameterItemRuleMap.putItemRule(parameterItemRule);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{contentType=").append(contentType);
		sb.append(", path=").append(transletName);

		if(actionList != null) {
			sb.append(", actionList=");
			sb.append('[');

			for(int i = 0; i < actionList.size(); i++) {
				Executable action = actionList.get(i);

				if(i > 0)
					sb.append(", ");

				sb.append(action.getActionId());
			}

			sb.append(']');
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
