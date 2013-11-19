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
package com.aspectran.core.context.builder.xml.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.xml.XmlBuilderAssistant;
import com.aspectran.core.rule.ItemRule;
import com.aspectran.core.rule.ItemRuleMap;
import com.aspectran.core.token.Token;
import com.aspectran.core.token.TokenParser;
import com.aspectran.core.type.ItemType;
import com.aspectran.core.type.ItemValueType;
import com.aspectran.core.type.TokenType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class ItemRuleNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class ItemRuleNodeletAdder implements NodeletAdder {
	
	protected XmlBuilderAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	public ItemRuleNodeletAdder(XmlBuilderAssistant assistant) {
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
		String beanId = attributes.getProperty("bean");					
		String parameter = attributes.getProperty("parameter");					
		String attribute = attributes.getProperty("attribute");					
		String property = attributes.getProperty("property");					

		Token[] tokens = new Token[1];
		
		if(!StringUtils.isEmpty(beanId)) {
			tokens[0] = new Token(TokenType.REFERENCE_BEAN, beanId);
			
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
					ir.setType(ItemType.ITEM); //default
				
				ItemValueType itemValueType = ItemValueType.valueOf(valueType);
				
				if(valueType != null) {
					if(itemValueType == null || itemValueType == ItemValueType.CUSTOM)
						itemValueType = new ItemValueType(valueType); //full qualified
					
					if(itemValueType != null)
						ir.setValueType(itemValueType);
				}
				
				if(defaultValue != null)
					ir.setDefaultValue(defaultValue);
				
				if(text != null || value != null)
					ir.setValue((text == null) ? value : text);
			
				assistant.pushObject(ir);
				
				if(ir.getType() == ItemType.ITEM) {
					//pass
				} else if(ir.getType() == ItemType.LIST) {
					List<Token[]> tokensList = new ArrayList<Token[]>();
					ir.setValue(tokensList);
				} else if(ir.getType() == ItemType.MAP) {
					Map<String, Token[]> tokensMap = new LinkedHashMap<String, Token[]>();
					ir.setValue(tokensMap);
				} else if(ir.getType() == ItemType.SET) {
					Set<Token[]> tokensSet = new LinkedHashSet<Token[]>();
					ir.setValue(tokensSet);
				} else if(ir.getType() == ItemType.PROPERTIES) {
					Properties tokensProp = new Properties();
					ir.setValue(tokensProp);
				}
			}
		});
		parser.addNodelet(xpath, "/item/reference", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRule ir = (ItemRule)assistant.peekObject();
				
				if(ir.getType() == ItemType.ITEM) {
					Token[] tokens = getReferenceTokens(attributes);
					if(tokens[0] != null)
						ir.setValue(tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				String name = attributes.getProperty("name");
				String tokenize = attributes.getProperty("tokenize");
				boolean isTokenize = !(tokenize != null && Boolean.valueOf(tokenize) == Boolean.FALSE);
				
				ItemRule ir = (ItemRule)assistant.peekObject();
				
				if(ir.getType() == ItemType.ITEM) {
					if(text != null) {
						if(isTokenize)
							ir.setValue(text);
						else {
							Token[] tokens = new Token[1];
							tokens[0] = new Token(TokenType.TEXT, ir.getName());
							ir.setValue(tokens);
						}
					}
				} else {
					Token[] tokens = null;
					
					if(ir.getType() == ItemType.LIST || ir.getType() == ItemType.SET) {
						if(isTokenize)
							tokens = TokenParser.parse(text);
						else {
							tokens = new Token[1];
							tokens[0] = new Token(TokenType.TEXT, text);
						}
					} else if(ir.getType() == ItemType.MAP || ir.getType() == ItemType.PROPERTIES) {
						if(!StringUtils.isEmpty(name)) {
							if(isTokenize)
								tokens = TokenParser.parse(text);
							else {
								tokens = new Token[1];
								tokens[0] = new Token(TokenType.TEXT, text);
							}
						}
					}
					
					assistant.pushObject(name);
					assistant.pushObject(tokens);
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
				} else {
					assistant.popObject(); //discard
					Token[] tokens = getReferenceTokens(attributes);
					assistant.pushObject(tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/null", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					ItemRule ir = (ItemRule)object;
					Token[] tokens = getReferenceTokens(attributes);
					if(tokens[0] != null)
						ir.setValue(tokens);
				} else {
					assistant.popObject(); //discard
					assistant.pushObject(null);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					//pass
				} else {
					Token[] tokens = (Token[])assistant.popObject();
					String name = (String)assistant.popObject();
					ItemRule ir = (ItemRule)assistant.peekObject();
					
					if(ir.getType() == ItemType.LIST) {
						List<Token[]> list = ir.getTokensList();
						list.add(tokens);
					} else if(ir.getType() == ItemType.SET) {
						Set<Token[]> set = ir.getTokensSet();
						set.add(tokens);
					} else if(ir.getType() == ItemType.MAP) {
						if(!StringUtils.isEmpty(name)) {
							Map<String, Token[]> map = ir.getTokensMap();
							map.put(name, tokens);
						}
					} else if(ir.getType() == ItemType.PROPERTIES) {
						if(!StringUtils.isEmpty(name)) {
							Properties prop = ir.getTokensProperties();
							prop.put(name, tokens);
						}
					}
				}
			}
		});
		parser.addNodelet(xpath, "/item/end()", new Nodelet() {
			public void process(Node node, Properties attributes, String text) throws Exception {
				ItemRule ir = (ItemRule)assistant.popObject();
				ItemRuleMap irm = (ItemRuleMap)assistant.peekObject();

				if(ir.isUnknownName())
					namingItemRule(ir, irm);

				irm.putItemRule(ir);
				
				inspectBeanReference(ir);
			}
		});
	}
	
	private void inspectBeanReference(ItemRule ir) {
		if(ir.getType() == ItemType.LIST) {
			List<Token[]> list = ir.getTokensList();
			for(Token[] tokens : list) {
				inspectBeanReference(ir, tokens);
			}
		} else if(ir.getType() == ItemType.SET) {
			Set<Token[]> set = ir.getTokensSet();
			for(Token[] tokens : set) {
				inspectBeanReference(ir, tokens);
			}
		} else if(ir.getType() == ItemType.MAP) {
			Map<String, Token[]> map = ir.getTokensMap();
			for(Token[] tokens : map.values()) {
				inspectBeanReference(ir, tokens);
			}
		} else if(ir.getType() == ItemType.PROPERTIES) {
			Properties prop = ir.getTokensProperties();
			Iterator<?> iter =  prop.values().iterator();
			while(iter.hasNext()) {
				inspectBeanReference(ir, (Token[])iter.next());
			}
		}
	}
	
	private void inspectBeanReference(ItemRule ir, Token[] tokens) {
		for(Token token : tokens) {
			if(token.getType() == TokenType.REFERENCE_BEAN) {
				assistant.putBeanReference(token.getName(), ir);
			}
		}
	}
}