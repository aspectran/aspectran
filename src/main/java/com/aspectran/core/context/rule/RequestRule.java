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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * The Class RequestRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class RequestRule {

	/** The Constant CHARACTER_ENCODING_SETTING_NAME. */
	public static final String CHARACTER_ENCODING_SETTING_NAME = "characterEncoding";
	
	private String characterEncoding;
	
	private RequestMethodType requestMethod;

	private ItemRuleMap attributeItemRuleMap;

	private AspectAdviceRuleRegistry aspectAdviceRuleRegistry;
	
	public RequestRule() {
	}
	
	/**
	 * Gets the character encoding.
	 * 
	 * @return the character encoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * Sets the character encoding.
	 * 
	 * @param characterEncoding the new character encoding
	 */
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	/**
	 * Gets the request method.
	 *
	 * @return the request method
	 */
	public RequestMethodType getRequestMethod() {
		return requestMethod;
	}

	/**
	 * Sets the request method.
	 *
	 * @param requestMethod the new request method
	 */
	public void setRequestMethod(RequestMethodType requestMethod) {
		this.requestMethod = requestMethod;
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
	 * @param attributeItemRuleMap the new attribute item rule map
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
	 * Gets the aspect advice rule registry.
	 *
	 * @return the aspect advice rule registry
	 */
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	/**
	 * Gets the aspect advice rule registry.
	 *
	 * @param clone the clone
	 * @return the aspect advice rule registry
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(boolean clone) throws CloneNotSupportedException {
		if(clone && aspectAdviceRuleRegistry != null)
			return (AspectAdviceRuleRegistry)aspectAdviceRuleRegistry.clone();
		
		return aspectAdviceRuleRegistry;
	}
	
	/**
	 * Sets the aspect advice rule registry.
	 *
	 * @param aspectAdviceRuleRegistry the new aspect advice rule registry
	 */
	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{method=").append(requestMethod);
		sb.append(", characterEncoding=").append(characterEncoding);
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
	
	public static RequestRule newInstance(String requestMethod, String characterEncoding) {
		RequestMethodType requestMethodType = null;
		
		if(requestMethod != null) {
			requestMethodType = RequestMethodType.valueOf(requestMethod);
			
			if(requestMethodType == null)
				throw new IllegalArgumentException("No request method type registered for '" + requestMethod + "'.");
		}
		
		if(characterEncoding != null && !Charset.isSupported(characterEncoding))
			throw new IllegalCharsetNameException("Given charset name is illegal. '" + characterEncoding + "'");
		
		RequestRule requestRule = new RequestRule();
		requestRule.setRequestMethod(requestMethodType);
		requestRule.setCharacterEncoding(characterEncoding);
		
		return requestRule;
	}
	
}
