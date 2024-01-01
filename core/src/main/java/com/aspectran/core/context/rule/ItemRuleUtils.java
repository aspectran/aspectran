/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.component.bean.annotation.AttrItem;
import com.aspectran.core.component.bean.annotation.ParamItem;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.params.EntryParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.Parameters;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Miscellaneous {@link ItemRule} utility methods.
 *
 * @since 6.3.0
 */
public class ItemRuleUtils {

    private ItemRuleUtils() {
    }

    /**
     * Returns the {@code Class} according to the given item value.
     * If the type of the item value is unknown, the class of the value
     * actually assigned is returned.
     * @param itemRule the item rule
     * @param value the value actually assigned to the item
     * @return a {@code Class} determined by the type of item or its actual value
     */
    public static Class<?> getPrototypeClass(ItemRule itemRule, Object value) {
        ItemValueType valueType = itemRule.getValueType();
        if (itemRule.getType() == ItemType.ARRAY) {
            if (valueType == ItemValueType.STRING) {
                return String[].class;
            } else if (valueType == ItemValueType.INT) {
                return Integer[].class;
            } else if (valueType == ItemValueType.LONG) {
                return Long[].class;
            } else if (valueType == ItemValueType.FLOAT) {
                return Float[].class;
            } else if (valueType == ItemValueType.DOUBLE) {
                return Double[].class;
            } else if (valueType == ItemValueType.BOOLEAN) {
                return Boolean[].class;
            } else if (valueType == ItemValueType.PARAMETERS) {
                return Parameters[].class;
            } else if (valueType == ItemValueType.FILE) {
                return File[].class;
            } else if (valueType == ItemValueType.MULTIPART_FILE) {
                return FileParameter[].class;
            } else {
                return (value != null ? value.getClass() : Object[].class);
            }
        } else if (itemRule.getType() == ItemType.LIST) {
            return (value != null ? value.getClass() : List.class);
        } else if (itemRule.getType() == ItemType.MAP) {
            return (value != null ? value.getClass() : Map.class);
        } else if (itemRule.getType() == ItemType.SET) {
            return (value != null ? value.getClass() : Set.class);
        } else if (itemRule.getType() == ItemType.PROPERTIES) {
            return (value != null ? value.getClass() : Properties.class);
        } else {
            if (valueType == ItemValueType.STRING) {
                return String.class;
            } else if (valueType == ItemValueType.INT) {
                return Integer.class;
            } else if (valueType == ItemValueType.LONG) {
                return Long.class;
            } else if (valueType == ItemValueType.FLOAT) {
                return Float.class;
            } else if (valueType == ItemValueType.DOUBLE) {
                return Double.class;
            } else if (valueType == ItemValueType.BOOLEAN) {
                return Boolean.class;
            } else if (valueType == ItemValueType.PARAMETERS) {
                return Parameters.class;
            } else if (valueType == ItemValueType.FILE) {
                return File.class;
            } else if (valueType == ItemValueType.MULTIPART_FILE) {
                return FileParameter.class;
            } else {
                return (value != null ? value.getClass() : Object.class);
            }
        }
    }

    /**
     * Returns an {@code Iterator} of all the tokens the item has.
     * @param itemRule the item rule
     * @return an {@code Iterator} for all tokens
     */
    public static Iterator<Token[]> tokenIterator(ItemRule itemRule) {
        Assert.notNull(itemRule, "itemRule must not be null");
        Iterator<Token[]> it = null;
        if (itemRule.isListableType()) {
            List<Token[]> list = itemRule.getTokensList();
            if (list != null) {
                it = list.iterator();
            }
        } else if (itemRule.isMappableType()) {
            Map<String, Token[]> map = itemRule.getTokensMap();
            if (map != null) {
                it = map.values().iterator();
            }
        } else {
            return new Iterator<Token[]>() {
                private int count = 0;
                @Override
                public boolean hasNext() {
                    return (count++ < 1);
                }
                @Override
                public Token[] next() {
                    return itemRule.getTokens();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Cannot remove an element of an array");
                }
            };
        }
        return it;
    }

    /**
     * Convert the given {@code ItemParameters} list into an {@code ItemRuleMap}.
     * @param itemParametersList the ItemParameters list to convert
     * @return the item rule map
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ItemRuleMap toItemRuleMap(List<ItemParameters> itemParametersList) throws IllegalRuleException {
        if (itemParametersList == null || itemParametersList.isEmpty()) {
            return null;
        }
        ItemRuleMap itemRuleMap = new ItemRuleMap();
        for (ItemParameters parameters : itemParametersList) {
            itemRuleMap.putItemRule(toItemRule(parameters));
        }
        return itemRuleMap;
    }

    /**
     * Convert the given {@code ItemParameters} list into an {@code ItemRuleList}.
     * @param itemParametersList the ItemParameters list to convert
     * @return the item rule list
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ItemRuleList toItemRuleList(List<ItemParameters> itemParametersList) throws IllegalRuleException {
        ItemRuleList itemRuleList = new ItemRuleList();
        if (itemParametersList != null) {
            for (ItemParameters parameters : itemParametersList) {
                itemRuleList.add(toItemRule(parameters));
            }
        }
        return itemRuleList;
    }

    /**
     * Convert the given {@code ItemParameters} into an {@code ItemRule}.
     * @param itemParameters the item parameters
     * @return an instance of {@code ItemRule}
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ItemRule toItemRule(ItemParameters itemParameters) throws IllegalRuleException {
        Assert.notNull(itemParameters, "itemParameters must not be null");
        String type = itemParameters.getString(ItemParameters.type);
        String name = itemParameters.getString(ItemParameters.name);
        String valueType = itemParameters.getString(ItemParameters.valueType);
        Boolean tokenize = itemParameters.getBoolean(ItemParameters.tokenize);
        Boolean mandatory = itemParameters.getBoolean(ItemParameters.mandatory);
        Boolean secret = itemParameters.getBoolean(ItemParameters.secret);

        ItemRule itemRule = ItemRule.newInstance(type, name, valueType, tokenize, mandatory, secret);

        if (itemRule.isListableType()) {
            if (itemRule.getValueType() != ItemValueType.BEAN) {
                List<String> stringList = itemParameters.getStringList(ItemParameters.value);
                if (stringList != null) {
                    for (String value : stringList) {
                        itemRule.addValue(value);
                    }
                }
            }
        } else if (itemRule.isMappableType()) {
            List<EntryParameters> parametersList = itemParameters.getParametersList(ItemParameters.entry);
            if (parametersList != null && itemRule.getValueType() != ItemValueType.BEAN) {
                for (Parameters parameters : parametersList) {
                    if (parameters != null) {
                        String entryName = parameters.getString(EntryParameters.name);
                        String entryValue = parameters.getString(EntryParameters.value);
                        itemRule.putValue(entryName, entryValue);
                    }
                }
            }
        } else {
            if (itemRule.getValueType() != ItemValueType.BEAN) {
                List<String> stringList = itemParameters.getStringList(ItemParameters.value);
                if (stringList != null && !stringList.isEmpty()) {
                    itemRule.setValue(stringList.get(0));
                }
            }
        }

        return itemRule;
    }

    public static ItemRule toItemRule(ParamItem paramItem) throws IllegalRuleException {
        Assert.notNull(paramItem, "paramItem must not be null");
        String name = StringUtils.emptyToNull(paramItem.name());
        String value = StringUtils.emptyToNull(paramItem.value());
        boolean tokenize = paramItem.tokenize();
        boolean mandatory = paramItem.mandatory();
        boolean secret = paramItem.secret();

        ItemRule itemRule = ItemRule.newInstance(
                null, name, null, tokenize, mandatory, secret);
        itemRule.setValue(value);
        return itemRule;
    }

    public static ItemRule toItemRule(AttrItem attrItem) throws IllegalRuleException {
        Assert.notNull(attrItem, "attrItem must not be null");
        String name = StringUtils.emptyToNull(attrItem.name());
        String value = StringUtils.emptyToNull(attrItem.value());
        boolean tokenize = attrItem.tokenize();
        boolean mandatory = attrItem.mandatory();
        boolean secret = attrItem.secret();

        ItemRule itemRule = ItemRule.newInstance(
                null, name, null, tokenize, mandatory, secret);
        itemRule.setValue(value);
        return itemRule;
    }

}
