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
import com.aspectran.core.context.rule.ItemEntry;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.HasArguments;
import com.aspectran.core.context.rule.ability.HasAttributes;
import com.aspectran.core.context.rule.ability.HasParameters;
import com.aspectran.core.context.rule.ability.HasProperties;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A generic {@code NodeletAdder} for parsing item-like elements such as
 * {@code <item>}, {@code <property>}, and {@code <argument>}.
 * <p>This class handles the common logic for creating {@link com.aspectran.core.context.rule.ItemRule}
 * instances from XML attributes and content.</p>
 *
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ItemNodeletAdder implements NodeletAdder {

    private static volatile ItemNodeletAdder INSTANCE;

    static ItemNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (ItemNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ItemNodeletAdder("item");
                }
            }
        }
        return INSTANCE;
    }

    private final String name;

    ItemNodeletAdder(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child(name)
            .nodelet(attrs -> {
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
                AspectranNodeParsingContext.pushObject(itemRule);
            })
            .mount(InnerBeanNodeletGroup.instance())
            .endNodelet(text -> {
                ItemRule itemRule = AspectranNodeParsingContext.popObject();
                if (itemRule.getType() == ItemType.SINGLE && StringUtils.hasText(text)) {
                    itemRule.setValue(text);
                }
                AspectranNodeParsingContext.getCurrentRuleParsingContext().resolveBeanClass(itemRule);

                Object object = AspectranNodeParsingContext.peekObject();
                if (object instanceof ItemRuleMap irm) {
                    irm.putItemRule(itemRule);
                }  else if (object instanceof TransletRule transletRule) {
                    RequestRule requestRule = transletRule.touchRequestRule(false);
                    if (getName().equals(ParameterNodeletAdder.instance().getName())) {
                        requestRule.addParameterItemRule(itemRule);
                    } else if (getName().equals(AttributeNodeletAdder.instance().getName())) {
                        requestRule.addAttributeItemRule(itemRule);
                    }
                } else if (getName().equals(ParameterNodeletAdder.instance().getName())) {
                    ((HasParameters)object).addParameterItemRule(itemRule);
                } else if (getName().equals(AttributeNodeletAdder.instance().getName())) {
                    ((HasAttributes)object).addAttributeItemRule(itemRule);
                } else if (getName().equals(ArgumentNodeletAdder.instance().getName())) {
                    ((HasArguments)object).addArgumentItemRule(itemRule);
                } else if (getName().equals(PropertyNodeletAdder.instance().getName())) {
                    ((HasProperties)object).addPropertyItemRule(itemRule);
                }
            })
            .child("value")
                .endNodelet(text -> {
                    if (StringUtils.hasText(text)) {
                        ItemRule itemRule = AspectranNodeParsingContext.peekObject();

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

                    ItemRule itemRule = AspectranNodeParsingContext.peekObject();

                    ItemEntry itemEntry = ItemEntry.of(itemRule, name, value, tokenize);
                    AspectranNodeParsingContext.pushObject(itemEntry);
                })
                .mount(InnerBeanNodeletGroup.instance())
                .endNodelet(text -> {
                    ItemEntry itemEntry = AspectranNodeParsingContext.popObject();
                    ItemRule itemRule = itemEntry.getItemRule();

                    if (itemRule.isMappableType() && itemRule.getValueType() != ItemValueType.BEAN) {
                        Token[] tokens = null;
                        if (itemEntry.getValue() != null) {
                            tokens = TokenParser.makeTokens(itemEntry.getValue(), itemEntry.isTokenizable());
                        } else if (StringUtils.hasText(text)) {
                            tokens = TokenParser.makeTokens(text, itemEntry.isTokenizable());
                        }
                        itemRule.putValue(itemEntry.getName(), tokens);
                    }
                });
    }

}
