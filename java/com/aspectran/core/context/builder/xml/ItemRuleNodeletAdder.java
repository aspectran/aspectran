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

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

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
	/*
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
	*/
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
				Boolean tokenize = BooleanUtils.toNullableBooleanObject(attributes.get("tokenize"));

				if(text != null)
					value = text;

				// auto-naming if did not specify the name of the item
				//if(StringUtils.isEmpty(name))
				//	name = getItemNameBaseOnCount(type);
				
				ItemRule itemRule = ItemRule.newInstance(type, name, value, valueType, defaultValue, tokenize);

				assistant.pushObject(itemRule);
				
				if(itemRule.getType() != ItemType.SINGLE)
					ItemRule.beginValueCollection(itemRule);
			}
		});
		parser.addNodelet(xpath, "/item/value", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				
				ItemRule itemRule = assistant.peekObject();
				
				Token[] tokens = ItemRule.parseValue(itemRule, name, text);

				if(tokens != null) {
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
					Token[] tokens = assistant.popObject();
					String name = assistant.popObject();
					ItemRule itemRule = assistant.peekObject();

					if(itemRule.getType() != ItemType.SINGLE)
						ItemRule.flushValueCollection(itemRule, name, tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/reference", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String beanId = attributes.get("bean");
				String parameter = attributes.get("parameter");
				String attribute = attributes.get("attribute");
				String property = attributes.get("property"); // bean's property

				ItemRule itemRule = assistant.peekObject();
				ItemRule.updateReference(itemRule, beanId, parameter, attribute, property);
			}
		});
		parser.addNodelet(xpath, "/item/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRule itemRule = assistant.popObject();
				ItemRuleMap itemRuleMap = assistant.peekObject();

				ItemRule.addItemRule(itemRuleMap, itemRule);
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