/*
 * Copyright (c) 2008-present The Aspectran Project
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

import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The Class ItemNodeParser.
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ItemNodeletGroup extends NodeletGroup {

    private static final ItemNodeletGroup INSTANCE = new ItemNodeletGroup();

    static ItemNodeletGroup instance() {
        return INSTANCE;
    }

    ItemNodeletGroup() {
        super("item");
        nodelet(attrs -> {
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
            AspectranNodeParser.current().pushObject(itemRule);
        })
        .mount(InnerBeanNodeletGroup.instance())
        .endNodelet(text -> {
            ItemRule itemRule = AspectranNodeParser.current().popObject();
            ItemRuleMap itemRuleMap = AspectranNodeParser.current().peekObject();

            if (itemRule.getType() == ItemType.SINGLE && StringUtils.hasText(text)) {
                itemRule.setValue(text);
            }

            AspectranNodeParser.current().getAssistant().resolveBeanClass(itemRule);
            itemRuleMap.putItemRule(itemRule);
        })
        .child("value")
            .endNodelet(text -> {
                if (StringUtils.hasText(text)) {
                    ItemRule itemRule = AspectranNodeParser.current().peekObject();

                    if (itemRule.getValueType() == ItemValueType.BEAN) {
                        throw new IllegalRuleException(
                                "<bean> and <value> elements cannot exist together within an <item> element");
                    }

                    if (itemRule.isListableType()) {
                        itemRule.addValue(TokenParser.makeTokens(text, itemRule.isTokenize()));
                    } else if (itemRule.getType() == ItemType.SINGLE) {
                        itemRule.setValue(TokenParser.makeTokens(text, itemRule.isTokenize()));
                    }
                }
            })
        .parent().child("entry")
            .nodelet(attrs -> {
                String name = attrs.get("name");
                String value = attrs.get("value");
                String tokenize = attrs.get("tokenize");

                AspectranNodeParser.current().pushObject(tokenize);
                AspectranNodeParser.current().pushObject(value);
                AspectranNodeParser.current().pushObject(name);
            })
            .mount(InnerBeanNodeletGroup.instance())
            .endNodelet(text -> {
                String name = AspectranNodeParser.current().popObject();
                String value = AspectranNodeParser.current().popObject();
                String tokenize = AspectranNodeParser.current().popObject();
                ItemRule itemRule = AspectranNodeParser.current().peekObject();

                if (itemRule.isMappableType() && itemRule.getValueType() != ItemValueType.BEAN) {
                    boolean isTokenize = BooleanUtils.toBoolean(
                            BooleanUtils.toNullableBooleanObject(tokenize), itemRule.isTokenize());
                    Token[] tokens = null;
                    if (value != null) {
                        tokens = TokenParser.makeTokens(value, isTokenize);
                    } else if (StringUtils.hasText(text)) {
                        tokens = TokenParser.makeTokens(text, isTokenize);
                    }
                    itemRule.putValue(name, tokens);
                }
            });
    }

}
