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
        parser.addNodeEndlet(text -> {
            if (StringUtils.hasText(text)) {
                ItemRule itemRule = parser.peekObject();
                if (itemRule.isListableType()) {
                    itemRule.addValue(TokenParser.makeTokens(text, itemRule.isTokenize()));
                } else if (itemRule.getType() == ItemType.SINGLE) {
                    itemRule.setValue(TokenParser.makeTokens(text, itemRule.isTokenize()));
                }
            }
        });
        parser.setXpath(xpath + "/item/entry");
        parser.addNodelet(attrs -> {
            String name = attrs.get("name");
            String value = attrs.get("value");
            String tokenize = attrs.get("tokenize");

            parser.pushObject(tokenize);
            parser.pushObject(value);
            parser.pushObject(name);
        });
        parser.addNodeEndlet(text -> {
            String name = parser.popObject();
            String value = parser.popObject();
            String tokenize = parser.popObject();
            ItemRule itemRule = parser.peekObject();

            if (itemRule.isMappableType()) {
                boolean isTokenize = BooleanUtils.toBoolean(BooleanUtils.toNullableBooleanObject(tokenize),
                        itemRule.isTokenize());
                Token[] tokens = null;
                if (StringUtils.hasText(value)) {
                    tokens = TokenParser.makeTokens(value, isTokenize);
                } else if (StringUtils.hasText(text)) {
                    tokens = TokenParser.makeTokens(text, isTokenize);
                }
                itemRule.putValue(name, tokens);
            }
        });
    }

}
