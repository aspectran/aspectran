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
package com.aspectran.core.context.builder.xml;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

import com.aspectran.core.context.builder.ContextBuilderAssistant;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.Nodelet;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class ItemNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
public class ItemNodeletAdder implements NodeletAdder {
	
	protected final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new ItemNodeletAdder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	public ItemNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	@Override
	public void process(final String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/item", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String type = attributes.get("type");
				String name = attributes.get("name");
				String value = attributes.get("value");
				String valueType = attributes.get("valueType");
				String defaultValue = attributes.get("defaultValue");
				Boolean tokenize = BooleanUtils.toNullableBooleanObject(attributes.get("tokenize"));

				if(StringUtils.hasText(text))
					value = text;

				ItemRule itemRule = ItemRule.newInstance(type, name, value, valueType, defaultValue, tokenize);

				assistant.pushObject(itemRule);
				
				if(itemRule.getType() != ItemType.SINGULAR)
					ItemRule.beginValueCollection(itemRule);
			}
		});
		parser.addNodelet(xpath, "/item/value", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String name = attributes.get("name");
				
				ItemRule itemRule = assistant.peekObject();
				
				Token[] tokens = ItemRule.parseValue(itemRule, text);

				if(tokens != null) {
					assistant.pushObject(name);
					assistant.pushObject(tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/reference", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String bean= attributes.get("bean");
				String parameter = attributes.get("parameter");
				String attribute = attributes.get("attribute");
				String property = attributes.get("property"); // property for bean or attribute object

				Object object = assistant.peekObject();
				
				if(object instanceof ItemRule) {
					ItemRule.updateReference((ItemRule)object, bean, parameter, attribute, property);
				} else {
					assistant.popObject(); // discard tokens
					Token t = ItemRule.makeReferenceToken(bean, parameter, attribute, property);
					Token[] tokens = new Token[] { t };
					assistant.pushObject(tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/null", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof Token[]) {
					// replace tokens to null
					assistant.popObject();
					assistant.pushObject(null);
				}
			}
		});
		parser.addNodelet(xpath, "/item/value/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				Object object = assistant.peekObject();
				
				if(object instanceof Token[]) {
					Token[] tokens = assistant.popObject();
					String name = assistant.popObject();
					ItemRule itemRule = assistant.peekObject();

					if(itemRule.getType() != ItemType.SINGULAR)
						ItemRule.flushValueCollection(itemRule, name, tokens);
				}
			}
		});
		parser.addNodelet(xpath, "/item/reference", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				String beanId = StringUtils.emptyToNull(attributes.get("bean"));
				String parameter = StringUtils.emptyToNull(attributes.get("parameter"));
				String attribute = StringUtils.emptyToNull(attributes.get("attribute"));
				String property = StringUtils.emptyToNull(attributes.get("property")); // bean's property

				ItemRule itemRule = assistant.peekObject();
				ItemRule.updateReference(itemRule, beanId, parameter, attribute, property);
			}
		});
		parser.addNodelet(xpath, "/item/end()", new Nodelet() {
			public void process(Node node, Map<String, String> attributes, String text) throws Exception {
				ItemRule itemRule = assistant.popObject();
				ItemRuleMap itemRuleMap = assistant.peekObject();

				ItemRule.addItemRule(itemRule, itemRuleMap);
				Iterator<Token[]> iter = ItemRule.tokenIterator(itemRule);
				
				if(iter != null) {
					while(iter.hasNext()) {
						for(Token t : iter.next()) {
							String name = t.getName();
							if(t.getType() == TokenType.BEAN && name != null) {
								if(name.equals(BeanRule.CLASS_DIRECTIVE)) {
									Class<?> beanClass = assistant.loadClass(t.getValue());
									t.setBeanClass(beanClass);
									assistant.putBeanReference(beanClass, t);
								} else {
									assistant.putBeanReference(name, t);
								}
							}
						}
					}
				}
			}
		});
	}

}