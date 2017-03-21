/*
 * Copyright 2008-2017 Juho Jeong
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

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class RequestRule.
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class RequestRule {

	public static final String CHARACTER_ENCODING_SETTING_NAME = "characterEncoding";
	
	public static final String LOCALE_RESOLVER_SETTING_NAME = "localeResolver";

	public static final String LOCALE_CHANGE_INTERCEPTOR_SETTING_NAME = "localeChangeInterceptor";

	private String characterEncoding;
	
	private MethodType allowedMethod;

	private ItemRuleMap parameterItemRuleMap;

	private ItemRuleMap attributeItemRuleMap;

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
	 * Gets the allowed method.
	 *
	 * @return the allowed method
	 */
	public MethodType getAllowedMethod() {
		return allowedMethod;
	}

	/**
	 * Sets the allowed method.
	 *
	 * @param allowedMethod the new allowed method
	 */
	public void setAllowedMethod(MethodType allowedMethod) {
		this.allowedMethod = allowedMethod;
	}

	/**
	 * Gets the parameter item rule map.
	 * 
	 * @return the parameter item rule map
	 */
	public ItemRuleMap getParameterItemRuleMap() {
		return parameterItemRuleMap;
	}

	/**
	 * Sets the attribute item rule map.
	 *
	 * @param parameterItemRuleMap the new attribute item rule map
	 */
	public void setParameterItemRuleMap(ItemRuleMap parameterItemRuleMap) {
		this.parameterItemRuleMap = parameterItemRuleMap;
	}

	/**
	 * Gets the attribute item rule map.
	 *
	 * @return the attribute rule map for attributes
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

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("method", allowedMethod);
		tsb.append("characterEncoding", characterEncoding);
		tsb.append("parameters", parameterItemRuleMap);
		tsb.append("attributes", attributeItemRuleMap);
		return tsb.toString();
	}
	
	public static RequestRule newInstance(String allowedMethod, String characterEncoding) {
		MethodType allowedethodType = null;
		
		if (allowedMethod != null) {
			allowedethodType = MethodType.resolve(allowedMethod);
			if (allowedethodType == null) {
				throw new IllegalArgumentException("No request method type for '" + allowedMethod + "'.");
			}
		}
		
		if (characterEncoding != null && !Charset.isSupported(characterEncoding)) {
			throw new IllegalCharsetNameException("Given charset name is illegal. charsetName: " + characterEncoding);
		}
		
		RequestRule requestRule = new RequestRule();
		requestRule.setAllowedMethod(allowedethodType);
		requestRule.setCharacterEncoding(characterEncoding);
		return requestRule;
	}
	
}
