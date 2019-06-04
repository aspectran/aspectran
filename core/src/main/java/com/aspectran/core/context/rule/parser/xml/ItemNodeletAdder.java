/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeletAdder;
import com.aspectran.core.util.nodelet.NodeletParser;

/**
 * The Class ItemNodeletAdder.
 * 
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ItemNodeletAdder implements NodeletAdder {

    @Override
    public void add(String xpath, NodeletParser parser) {
        AspectranNodeParser nodeParser = parser.getNodeParser();
        ContextRuleAssistant assistant = nodeParser.getAssistant();

        parser.setXpath(xpath + "/item");
        parser.addNodelet(attrs -> {
            String type = attrs.get("type");
            String name = attrs.get("name");
            String value = attrs.get("value");
            String valueType = attrs.get("valueType");
            Boolean tokenize = BooleanUtils.toNullableBooleanObject(attrs.get("tokenize"));
            Boolean mandatory = BooleanUtils.toNullableBooleanObject(attrs.get("mandatory"));
            Boolean secret = BooleanUtils.toNullableBooleanObject(attrs.get("secret"));

            ItemRule itemRule = ItemRule.newInstance(type, name, valueType, tokenize, mandatory, secret);
            if (value != null && itemRule.getType() == ItemType.SINGLE) {
                itemRule.setValue(value);
            }

            parser.pushObject(itemRule);
        });
        parser.addNodeEndlet(text -> {
            ItemRule itemRule = parser.popObject();
            ItemRuleMap itemRuleMap = parser.peekObject();

            if (itemRule.getType() == ItemType.SINGLE && StringUtils.hasText(text)) {
                itemRule.setValue(text);
            }

            assistant.resolveBeanClass(itemRule);
            itemRuleMap.putItemRule(itemRule);
        });
        parser.setXpath(xpath + "/item/value");
        parser.addNodelet(attrs -> {
            ItemRule itemRule = parser.peekObject();

            String name = attrs.get("name");
            boolean tokenize = BooleanUtils.toBoolean(BooleanUtils.toNullableBooleanObject(attrs.get("tokenize")),
                    itemRule.isTokenize());

            parser.pushObject(name);
            parser.pushObject(tokenize);
            parser.pushObject(null); // tokens
        });
        parser.setXpath(xpath + "/item/value/call");
        parser.addNodelet(attrs -> {
            String bean= attrs.get("bean");
            String template= attrs.get("template");
            String parameter = attrs.get("parameter");
            String attribute = attrs.get("attribute");
            String property = attrs.get("property");

            Token t = ItemRule.makeReferenceToken(bean, template, parameter, attribute, property);
            if (t != null) {
                Token[] tokens = new Token[] { t };

                parser.popObject();
                parser.pushObject(tokens);
            }
        });
        parser.setXpath(xpath + "/item/value/null");
        parser.addNodelet(attrs -> {
            parser.popObject();
            parser.pushObject(null);
        });
        parser.setXpath(xpath + "/item/value");
        parser.addNodeEndlet(text -> {
            Token[] tokens = parser.popObject();
            boolean tokenize = parser.popObject();
            String name = parser.popObject();
            ItemRule itemRule = parser.peekObject();

            if (tokens == null && StringUtils.hasText(text)) {
                tokens = TokenParser.makeTokens(text, tokenize);
            }

            if (itemRule.isListableType()) {
                itemRule.addValue(tokens);
            } else if (itemRule.isMappableType()) {
                itemRule.putValue(name, tokens);
            } else {
                itemRule.setValue(tokens);
            }
        });
        parser.setXpath(xpath + "/item/call");
        parser.addNodelet(attrs -> {
            String bean = StringUtils.emptyToNull(attrs.get("bean"));
            String template = StringUtils.emptyToNull(attrs.get("template"));
            String parameter = StringUtils.emptyToNull(attrs.get("parameter"));
            String attribute = StringUtils.emptyToNull(attrs.get("attribute"));
            String property = StringUtils.emptyToNull(attrs.get("property"));

            ItemRule itemRule = parser.peekObject();

            Token t = ItemRule.makeReferenceToken(bean, template, parameter, attribute, property);
            if (t != null) {
                Token[] tokens = new Token[] { t };
                if (itemRule.isListableType()) {
                    itemRule.addValue(tokens);
                } else {
                    itemRule.setValue(tokens);
                }
            }
        });
    }

}