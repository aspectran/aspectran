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
package com.aspectran.core.context.builder.xml;

import com.aspectran.core.context.builder.assistant.ContextBuilderAssistant;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.xml.NodeletAdder;
import com.aspectran.core.util.xml.NodeletParser;

/**
 * The Class ItemNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ItemNodeletAdder implements NodeletAdder {
	
	protected final ContextBuilderAssistant assistant;
	
	/**
	 * Instantiates a new ItemNodeletAdder.
	 *
	 * @param assistant the assistant for Context Builder
	 */
	ItemNodeletAdder(ContextBuilderAssistant assistant) {
		this.assistant = assistant;
	}

	@Override
	public void process(final String xpath, NodeletParser parser) {
		parser.addNodelet(xpath, "/item", (node, attributes, text) -> {
			String type = attributes.get("type");
			String name = attributes.get("name");
			String value = attributes.get("value");
			String valueType = attributes.get("valueType");
			String defaultValue = attributes.get("defaultValue");
			Boolean tokenize = BooleanUtils.toNullableBooleanObject(attributes.get("tokenize"));
			Boolean mandatory = BooleanUtils.toNullableBooleanObject(attributes.get("mandatory"));

			if (StringUtils.hasText(text)) {
				value = text;
			}

			ItemRule itemRule = ItemRule.newInstance(type, name, valueType, defaultValue, tokenize, mandatory);

			if (value != null && itemRule.getType() == ItemType.SINGLE) {
				ItemRule.setValue(itemRule, value);
			}

			assistant.pushObject(itemRule);
		});
		parser.addNodelet(xpath, "/item/value", (node, attributes, text) -> {
			ItemRule itemRule = assistant.peekObject();

			String name = attributes.get("name");
			boolean tokenize = BooleanUtils.toBoolean(BooleanUtils.toNullableBooleanObject(attributes.get("tokenize")), itemRule.isTokenize());

			Token[] tokens = TokenParser.makeTokens(text, tokenize);

			assistant.pushObject(name);
			assistant.pushObject(tokens);
		});
		parser.addNodelet(xpath, "/item/value/call", (node, attributes, text) -> {
			String bean= attributes.get("bean");
			String template= attributes.get("template");
			String parameter = attributes.get("parameter");
			String attribute = attributes.get("attribute");
			String property = attributes.get("property");

			Token t = ItemRule.makeReferenceToken(bean, template, parameter, attribute, property);
			if (t != null) {
				Token[] tokens = new Token[] {t};

				assistant.popObject();
				assistant.pushObject(tokens);
			}
		});
		parser.addNodelet(xpath, "/item/value/null", (node, attributes, text) -> {
			assistant.popObject();
			assistant.pushObject(null);
		});
		parser.addNodelet(xpath, "/item/value/end()", (node, attributes, text) -> {
			Token[] tokens = assistant.popObject();
			String name = assistant.popObject();
			ItemRule itemRule = assistant.peekObject();

			ItemRule.addValue(itemRule, name, tokens);
		});
		parser.addNodelet(xpath, "/item/call", (node, attributes, text) -> {
			String bean = StringUtils.emptyToNull(attributes.get("bean"));
			String template = StringUtils.emptyToNull(attributes.get("template"));
			String parameter = StringUtils.emptyToNull(attributes.get("parameter"));
			String attribute = StringUtils.emptyToNull(attributes.get("attribute"));
			String property = StringUtils.emptyToNull(attributes.get("property"));

			ItemRule itemRule = assistant.peekObject();

			Token t = ItemRule.makeReferenceToken(bean, template, parameter, attribute, property);
			if (t != null) {
				Token[] tokens = new Token[] {t};
				ItemRule.addValue(itemRule, null, tokens);
			}
		});
		parser.addNodelet(xpath, "/item/end()", (node, attributes, text) -> {
			ItemRule itemRule = assistant.popObject();
			ItemRuleMap itemRuleMap = assistant.peekObject();

			ItemRule.addItemRule(itemRule, itemRuleMap);

			assistant.resolveBeanClass(itemRule);
		});
	}

}