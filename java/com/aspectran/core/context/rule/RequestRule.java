/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;

import com.aspectran.core.context.aspect.AspectAdviceRuleRegistry;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * <p>Created: 2008. 03. 22 오후 5:48:09</p>
 */
public class RequestRule implements AspectAdviceSupport, Cloneable {

	public static final String CHARACTER_ENCODING_SETTING_NAME = "characterEncoding";
	
	private String characterEncoding;
	
	private RequestMethodType method;

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
	 * Gets the request method type.
	 * 
	 * @return the request method type
	 */
	public RequestMethodType getMethod() {
		return method;
	}

	/**
	 * Sets the request method type.
	 * 
	 * @param method the request method type
	 */
	public void setMethod(RequestMethodType method) {
		this.method = method;
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
	 * @param parameterRuleMap the new parameter rule map for attributes
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
	
	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry() {
		return aspectAdviceRuleRegistry;
	}

	public AspectAdviceRuleRegistry getAspectAdviceRuleRegistry(boolean clone) throws CloneNotSupportedException {
		if(clone && aspectAdviceRuleRegistry != null)
			return (AspectAdviceRuleRegistry)aspectAdviceRuleRegistry.clone();
		
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
	
	public List<AspectAdviceRule> getExceptionRaisedAdviceRuleList() {
		if(aspectAdviceRuleRegistry == null)
			return null;
		
		return aspectAdviceRuleRegistry.getExceptionRaisedAdviceRuleList();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{method=").append(method);
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
	
	public RequestRule clone() {
		RequestRule newRequestRule = new RequestRule();
		newRequestRule.setCharacterEncoding(characterEncoding);
		newRequestRule.setMethod(method);
		newRequestRule.setAttributeItemRuleMap(attributeItemRuleMap);
		
		return newRequestRule;
	}
	
	public static RequestRule newInstance(String method, String characterEncoding) {
		RequestMethodType methodType = null;
		
		if(method != null) {
			methodType = RequestMethodType.valueOf(method);
			
			if(methodType == null)
				throw new IllegalArgumentException("Unknown request method type '" + method + "'");
		}
		
		if(characterEncoding != null && !Charset.isSupported(characterEncoding))
			throw new IllegalCharsetNameException("Given charset name is illegal. '" + characterEncoding + "'");
		
		RequestRule requestRule = new RequestRule();
		requestRule.setMethod(methodType);
		requestRule.setCharacterEncoding(characterEncoding);
		
		return requestRule;
	}
	
}
