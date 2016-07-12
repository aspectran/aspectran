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
	 * Sets the aspect advice rule registry.
	 *
	 * @param aspectAdviceRuleRegistry the new aspect advice rule registry
	 */
	public void setAspectAdviceRuleRegistry(AspectAdviceRuleRegistry aspectAdviceRuleRegistry) {
		this.aspectAdviceRuleRegistry = aspectAdviceRuleRegistry;
	}

	public AspectAdviceRuleRegistry replicateAspectAdviceRuleRegistry() {
		if(aspectAdviceRuleRegistry == null)
			return null;

		return aspectAdviceRuleRegistry.replicate();
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("method", allowedMethod);
		tsb.append("characterEncoding", characterEncoding);
		tsb.append("attributes", attributeItemRuleMap);
		tsb.append("aspectAdviceRuleRegistry", aspectAdviceRuleRegistry);
		return tsb.toString();
	}
	
	public static RequestRule newInstance(String allowedMethod, String characterEncoding) {
		MethodType allowedethodType = null;
		
		if(allowedMethod != null) {
			allowedethodType = MethodType.resolve(allowedMethod);
			if(allowedethodType == null)
				throw new IllegalArgumentException("No request method type registered for '" + allowedMethod + "'.");
		}
		
		if(characterEncoding != null && !Charset.isSupported(characterEncoding))
			throw new IllegalCharsetNameException("Given charset name is illegal. charsetName: " + characterEncoding);
		
		RequestRule requestRule = new RequestRule();
		requestRule.setAllowedMethod(allowedethodType);
		requestRule.setCharacterEncoding(characterEncoding);
		
		return requestRule;
	}
	
}
