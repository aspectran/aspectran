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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;
import com.aspectran.core.var.rule.ItemRule;
import com.aspectran.core.var.rule.ItemRuleMap;
import com.aspectran.core.var.token.Token;
import com.aspectran.core.var.type.ItemType;
import com.aspectran.core.var.type.TokenType;

/**
 * The Class ItemRuleNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 오전 6:56:29</p>
 */
public class ItemRuleNodeletAdder implements NodeletAdder {
	
	protected ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new response rule nodelet adder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	public ItemRuleNodeletAdder(ContextBuilderAssistant assistant) {
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
	 * Process.
	 */
	public void process(String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/item", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String type = attributes.get("type");
				String name = attributes.get("name");
				String value = attributes.get("value");
				String valueType = attributes.get("valueType");
				String defaultValue = attributes.get("defaultValue");
				String tokenize = attributes.get("tokenize");

				if(text != null)
					value = text;

				// auto-naming if did not specify the name of the item
				//if(StringUtils.isEmpty(name))
				//	name = getItemNameBaseOnCount(type);
				
				ItemRule itemRule = ItemRule.newInstance(type, name, value, valueType, defaultValue, tokenize);

				assistant.pushObject(itemRule);
				
				if(itemRule.getType() == ItemType.ITEM) {
					//pass
				} else if(itemRule.getType() == ItemType.LIST) {
					List<Token[]> tokensList = new ArrayList<Token[]>();
					itemRule.setValue(tokensList);
				} else if(itemRule.getType() == ItemType.MAP) {
					Map<String, Token[]> tokensMap = new LinkedHashMap<String, Token[]>();
					itemRule.setValue(tokensMap);
				} else if(itemRule.getType() == ItemType.SET) {
					Set<Token[]> tokensSet = new LinkedHashSet<Token[]>();
					itemRule.setValue(tokensSet);
				} else if(itemRule.getType() == ItemType.PROPERTIES) {
					Properties tokensProp = new Properties();
					itemRule.setValue(tokensProp);
				}
			}
		});
		parser.addNodelet(xpath, "/item/reference", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String beanId = attributes.get("bean");
				String parameter = attributes.get("parameter");
				String attribute = attributes.get("attribute");
				String property = attributes.get("property"); // bean's property

				ItemRule itemRule = (ItemRule)assistant.peekObject();
				ItemRule.updateReference(itemRule, beanId, parameter, attribute, property);
			}
		});
		parser.addNodelet(xpath, "/item/value", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				
				ItemRule itemRule = (ItemRule)assistant.peekObject();
				
				if(itemRule.getType() == ItemType.ITEM) {
					if(text != null) {
						itemRule.setValue(text);
					}
				} else {
					Token[] tokens = null;
					
					if(itemRule.getType() == ItemType.LIST || itemRule.getType() == ItemType.SET) {
						tokens = itemRule.makeTokens(text);
					} else if(itemRule.getType() == ItemType.MAP || itemRule.getType() == ItemType.PROPERTIES) {
						if(!StringUtils.isEmpty(name)) {
							tokens = itemRule.makeTokens(text);
						}
					}
					
					assistant.pushObject(name);
					assistant.pushObject(tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/reference", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String beanId = attributes.get("bean");
				String parameter = attributes.get("parameter");
				String attribute = attributes.get("attribute");
				String property = attributes.get("property"); // bean's property

				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					ItemRule itemRule = (ItemRule)object;
					ItemRule.updateReference(itemRule, beanId, parameter, attribute, property);
				} else {
					assistant.popObject(); //discard
					Token[] tokens = ItemRule.makeReferenceTokens(beanId, parameter, attribute, property);
					assistant.pushObject(tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/null", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					//pass
				} else {
					assistant.popObject(); //discard
					assistant.pushObject(null);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					//pass
				} else {
					Token[] tokens = (Token[])assistant.popObject();
					String name = (String)assistant.popObject();
					ItemRule itemRule = (ItemRule)assistant.peekObject();
					
					if(itemRule.getType() == ItemType.LIST) {
						List<Token[]> list = itemRule.getTokensList();
						list.add(tokens);
					} else if(itemRule.getType() == ItemType.SET) {
						Set<Token[]> set = itemRule.getTokensSet();
						set.add(tokens);
					} else if(itemRule.getType() == ItemType.MAP) {
						if(!StringUtils.isEmpty(name)) {
							Map<String, Token[]> map = itemRule.getTokensMap();
							map.put(name, tokens);
						}
					} else if(itemRule.getType() == ItemType.PROPERTIES) {
						if(!StringUtils.isEmpty(name)) {
							Properties prop = itemRule.getTokensProperties();
							prop.put(name, tokens);
						}
					}
				}
			}
		});
		parser.addNodelet(xpath, "/item/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRule itemRule = (ItemRule)assistant.popObject();
				ItemRuleMap itemRuleMap = (ItemRuleMap)assistant.peekObject();

				itemRuleMap.putItemRule(itemRule);

				if(itemRule.isUnknownName())
					ItemRule.namingItemRule(itemRule, itemRuleMap);

				Iterator<Token[]> iter = ItemRule.tokenIterator(itemRule);
				
				if(iter != null) {
					while(iter.hasNext()) {
						for(Token token : iter.next()) {
							if(token.getType() == TokenType.REFERENCE_BEAN) {
								assistant.putBeanReference(token.getName(), itemRule);
							}
						}
					}
				}
			}
		});
	}

}