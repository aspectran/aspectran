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
package com.aspectran.core.context.builder.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.AspectranSettingAssistant;
import com.aspectran.core.rule.ItemRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.token.Token;
import com.aspectran.core.token.TokenParser;
import com.aspectran.core.type.ItemType;
import com.aspectran.core.type.ItemValueType;
import com.aspectran.core.type.TokenType;
import com.aspectran.util.StringUtils;
import com.aspectran.util.xml.Nodelet;
import com.aspectran.util.xml.NodeletAdder;
import com.aspectran.util.xml.NodeletParser;

/**
 * The Class ItemRuleNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class ItemRuleNodeletAdder implements NodeletAdder {
	
	protected AspectranSettingAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	public ItemRuleNodeletAdder(AspectranSettingAssistant assistant) {
		this.assistant = assistant;
	}
	
	protected void namingItemRule(ItemRule itemRule, ItemRuleMap itemRuleMap) {
		int count = 0;
		ItemRule first = null;
		
		for(ItemRule ir : itemRuleMap) {
			if(ir.isUnknownName() && ir.getType() == itemRule.getType()) {
				count++;
				
				if(count == 1)
					first = ir;
			}
		}
		
		if(count == 0) {
			itemRule.setName(itemRule.getType().toString());
		} else {
			if(count == 1) {
				String name = first.getType().toString() + count;
				first.setName(name);
			}				
			
			String name = itemRule.getType().toString() + (++count);
			itemRule.setName(name);
		}
	}
	
	/**
	 * Gets the reference tokens.
	 * 
	 * @param attributes the attributes
	 * 
	 * @return the reference tokens
	 */
	private Token[] getReferenceTokens(Properties attributes) {
		String bean = attributes.getProperty("bean");					
		String parameter = attributes.getProperty("parameter");					
		String attribute = attributes.getProperty("attribute");					
		String property = attributes.getProperty("property");					

		Token[] tokens = new Token[1];
		
		if(!StringUtils.isEmpty(bean)) {
			tokens[0] = new Token(TokenType.REFERENCE_BEAN, bean);
			
			if(!StringUtils.isEmpty(property))
				tokens[0].setGetterName(property);
		} else if(!StringUtils.isEmpty(parameter))
			tokens[0] = new Token(TokenType.PARAMETER, parameter);
		else if(!StringUtils.isEmpty(attribute))
			tokens[0] = new Token(TokenType.ATTRIBUTE, attribute);
		else
			tokens[0] = null;
		
		return tokens;
	}
	
	/**
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/item", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				String type = attributes.getProperty("type");
				String valueType = attributes.getProperty("valueType");
				String value = attributes.getProperty("value");
				String defaultValue = attributes.getProperty("defaultValue");

				// auto-naming if did not specify the name of the item
				//if(StringUtils.isEmpty(name))
				//	name = getItemNameBaseOnCount(type);
				
				ItemRule ir = new ItemRule();

				if(!StringUtils.isEmpty(name)) {
					ir.setName(name);
				} else {
					ir.setUnknownName(true);
				}
				
				ItemType itemType = ItemType.valueOf(type);
				
				if(type != null && itemType == null)
					throw new IllegalArgumentException("No item-type registered for type '" + type + "'");
				
				if(itemType != null)
					ir.setType(itemType);
				else
					ir.setType(ItemType.ITEM);
				
				ItemValueType itemValueType = ItemValueType.valueOf(valueType);
				
				if(valueType != null) {
					if(itemValueType == null || itemValueType == ItemValueType.CUSTOM)
						itemValueType = new ItemValueType(valueType);
					
					if(itemValueType != null)
						ir.setValueType(itemValueType);
				}
				
				if(defaultValue != null)
					ir.setDefaultValue(defaultValue);
				
				if(text != null || value != null)
					ir.setValue((text == null) ? value : text);
			
				assistant.pushObject(ir);
				
				if(ir.getType() == ItemType.LIST) {
					List<Token[]> tokensList = new ArrayList<Token[]>();
					assistant.pushObject(tokensList);
				} else if(ir.getType() == ItemType.MAP) {
					Map<String, Token[]> tokensMap = new LinkedHashMap<String, Token[]>();
					assistant.pushObject(tokensMap);
				} else if(ir.getType() == ItemType.SET) {
					Set<Token[]> tokensSet = new HashSet<Token[]>();
					assistant.pushObject(tokensSet);
				} else if(ir.getType() == ItemType.PROPERTIES) {
					Properties tokensProperties = new Properties();
					assistant.pushObject(tokensProperties);
				}
			}
		});
		parser.addNodelet(xpath, "/item/reference", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					ItemRule ir = (ItemRule)object;
					
					Token[] tokens = getReferenceTokens(attributes);
					
					if(tokens[0] != null)
						ir.setValue(tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String tokenize = attributes.getProperty("tokenize");
				
				boolean isTokenize = !(tokenize != null && Boolean.valueOf(tokenize) == Boolean.FALSE);
				
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					ItemRule ir = (ItemRule)object;
					
					if(text != null) {
						if(isTokenize)
							ir.setValue(text);
						else {
							Token[] tokens = new Token[1];
							tokens[0] = new Token(TokenType.TEXT, ir.getName());
							ir.setValue(tokens);
						}
					}
				} else if(object instanceof List<?>) {
					@SuppressWarnings("unchecked")
					List<Token[]> tokensList = (List<Token[]>)object;
					Token[] tokens;
					
					if(isTokenize)
						tokens = TokenParser.parse(text);
					else {
						tokens = new Token[1];
						tokens[0] = new Token(TokenType.TEXT, text);
					}
					
					tokensList.add(tokens);
				} else if(object instanceof Map<?, ?>) {
					String name = attributes.getProperty("name");

					if(!StringUtils.isEmpty(name)) {
						@SuppressWarnings("unchecked")
						Map<String, Token[]> tokensMap = (Map<String, Token[]>)object;
						Token[] tokens;
						
						if(isTokenize)
							tokens = TokenParser.parse(text);
						else {
							tokens = new Token[1];
							tokens[0] = new Token(TokenType.TEXT, text);
						}
						
						tokensMap.put(name, tokens);
					}
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/reference", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					ItemRule ir = (ItemRule)object;
					
					Token[] tokens = getReferenceTokens(attributes);
					
					if(tokens[0] != null)
						ir.setValue(tokens);
				} else if(object instanceof List<?>) {
					@SuppressWarnings("unchecked")
					List<Token[]> tokensList = (List<Token[]>)object;
					
					if(tokensList.size() > 0) {
						Token[] tokens = getReferenceTokens(attributes);
						tokensList.add(tokensList.size() - 1, tokens);
					}
				} else if(object instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
					Map<String, Token[]> tokensMap = (Map<String, Token[]>)object;

					String[] keys = tokensMap.keySet().toArray(new String[tokensMap.size()]);
					
					if(keys.length > 0) {
						Token[] tokens = TokenParser.parse(text);
						tokensMap.put(keys[keys.length - 1], tokens);
					}
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/null", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					ItemRule ir = (ItemRule)object;
					ir.setValue((Token[])null);
				} else if(object instanceof List<?>) {
					@SuppressWarnings("unchecked")
					List<Token[]> tokensList = (List<Token[]>)object;
					
					if(tokensList.size() > 0)
						tokensList.add(tokensList.size() - 1, null);
				} else if(object instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
					Map<String, Token[]> tokensMap = (Map<String, Token[]>)object;
					
					String[] keys = tokensMap.keySet().toArray(new String[tokensMap.size()]);
					
					if(keys.length > 0)
						tokensMap.put(keys[keys.length - 1], null);
				}
			}
		});
		parser.addNodelet(xpath, "/item/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				Object object = assistant.popObject();
				
				if(object instanceof ItemRule) {
					ItemRuleMap irm = (ItemRuleMap)assistant.peekObject();
					ItemRule ir = (ItemRule)object;

					if(ir.isUnknownName()) {
						namingItemRule(ir, irm);
					}

					irm.putItemRule(ir);
				} else if(object instanceof List<?>) {
					@SuppressWarnings("unchecked")
					List<Token[]> tokensList = (List<Token[]>)object;
					
					ItemRule ir = (ItemRule)assistant.popObject();
					ir.setValue(tokensList);
					
					ItemRuleMap irm = (ItemRuleMap)assistant.peekObject();

					if(ir.isUnknownName()) {
						namingItemRule(ir, irm);
					}

					irm.putItemRule(ir);
				} else if(object instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
					Map<String, Token[]> tokensMap = (Map<String, Token[]>)object;
					
					ItemRule ir = (ItemRule)assistant.popObject();
					ir.setValue(tokensMap);
					
					ItemRuleMap irm = (ItemRuleMap)assistant.peekObject();

					if(ir.isUnknownName()) {
						namingItemRule(ir, irm);
					}

					irm.putItemRule(ir);
				}
			}
		});
	}
}