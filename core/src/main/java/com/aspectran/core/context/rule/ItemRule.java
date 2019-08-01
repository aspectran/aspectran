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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.component.bean.annotation.Attribute;
import com.aspectran.core.component.bean.annotation.Parameter;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.params.EntryParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The Class ItemRule.
 * 
 * <p>Created: 2008. 03. 27 PM 3:57:48</p>
 */
public class ItemRule {

    /**  suffix for array-type item: "[]". */
    private static final String ARRAY_SUFFIX = "[]";

    /**  suffix for map-type item: "{}". */
    private static final String MAP_SUFFIX = "{}";

    private ItemType type;

    private String name;

    private ItemValueType valueType;

    private Boolean tokenize;

    private Token[] tokens;

    private List<Token[]> tokensList;

    private Map<String, Token[]> tokensMap;

    private Boolean mandatory;

    private Boolean secret;

    private boolean autoNamed;

    private BeanRule beanRule;

    private List<BeanRule> beanRuleList;

    private Map<String, BeanRule> beanRuleMap;

    /**
     * Instantiates a new ItemRule.
     */
    public ItemRule() {
    }

    /**
     * Gets the item type.
     *
     * @return the item type
     */
    public ItemType getType() {
        return type;
    }

    /**
     * Sets the item type.
     *
     * @param type the new item type
     */
    public void setType(ItemType type) {
        this.type = type;
    }

    /**
     * Returns the name of the item.
     *
     * @return the name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the item.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        if (name.endsWith(ARRAY_SUFFIX)) {
            this.name = name.substring(0, name.length() - 2);
            type = ItemType.ARRAY;
        } else if (name.endsWith(MAP_SUFFIX)) {
            this.name = name.substring(0, name.length() - 2);
            type = ItemType.MAP;
        } else {
            this.name = name;
            if (type == null) {
                type = ItemType.SINGLE;
            }
        }
    }

    /**
     * Returns the value of the item.
     *
     * @return the value of the item
     */
    public String getValue() {
        return toValue(tokens);
    }

    private String toValue(Token[] tokens) {
        return (tokens != null ? TokenParser.toString(tokens) : null);
    }

    /**
     * Gets the tokens.
     *
     * @return the tokens
     */
    public Token[] getTokens() {
        return tokens;
    }

    /**
     * Gets the list of tokens.
     *
     * @return the tokens list
     */
    public List<Token[]> getTokensList() {
        return tokensList;
    }

    /**
     * Returns a list of string values of this item.
     *
     * @return a list of string values
     */
    public List<String> getValueList() {
        if (tokensList == null) {
            return null;
        }
        if (tokensList.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<String> list = new ArrayList<>(tokensList.size());
            for (Token[] tokens : tokensList) {
                list.add(toValue(tokens));
            }
            return list;
        }
    }

    /**
     * Gets the tokens map.
     *
     * @return the tokens map
     */
    public Map<String, Token[]> getTokensMap() {
        return tokensMap;
    }

    /**
     * Returns a map of string values of this item.
     *
     * @return a map of string values
     */
    public Map<String, String> getValueMap() {
        if (tokensMap == null) {
            return null;
        }
        if (tokensMap.isEmpty()) {
            return new LinkedHashMap<>();
        } else {
            Map<String, String> map = new LinkedHashMap<>();
            for (Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
                map.put(entry.getKey(), toValue(entry.getValue()));
            }
            return map;
        }
    }

    /**
     * Sets the specified value to this Single type item.
     *
     * @param text the value to be analyzed for use as the value of this item
     * @see #setValue(Token[])
     */
    public void setValue(String text) {
        Token[] tokens = TokenParser.makeTokens(text, isTokenize());
        setValue(tokens);
    }

    /**
     * Sets the specified value to this Single type item.
     *
     * @param tokens an array of tokens
     */
    public void setValue(Token[] tokens) {
        if (type == null) {
            type = ItemType.SINGLE;
        }
        if (type != ItemType.SINGLE) {
            throw new IllegalArgumentException("The type of this item must be 'single'");
        }
        this.tokens = tokens;
    }

    /**
     * Puts the specified value with the specified key to this Map type item.
     *
     * @param name the value name; may be null
     * @param text the value to be analyzed for use as the value of this item
     * @see #putValue(String, Token[])
     */
    public void putValue(String name, String text) {
        Token[] tokens = TokenParser.makeTokens(text, isTokenize());
        putValue(name, tokens);
    }

    /**
     * Puts the specified value with the specified key to this Map type item.
     *
     * @param name the value name; may be null
     * @param tokens an array of tokens
     */
    public void putValue(String name, Token[] tokens) {
        if (type == null) {
            type = ItemType.MAP;
        }
        if (!isMappableType()) {
            throw new IllegalArgumentException("The type of this item must be 'map' or 'properties'");
        }
        if (tokensMap == null) {
            tokensMap = new LinkedHashMap<>();
        }
        tokensMap.put(name, tokens);
    }

    /**
     * Sets a value to this Map type item.
     *
     * @param tokensMap the tokens map
     */
    public void setValue(Map<String, Token[]> tokensMap) {
        if (type == null) {
            type = ItemType.MAP;
        }
        if (!isMappableType()) {
            throw new IllegalArgumentException("The type of this item must be 'map' or 'properties'");
        }
        this.tokensMap = tokensMap;
    }

    /**
     * Sets a value to this Properties type item.
     *
     * @param properties the properties
     */
    public void setValue(Properties properties)  {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        if (type == null) {
            type = ItemType.PROPERTIES;
        }
        if (!isMappableType()) {
            throw new IllegalArgumentException("The type of this item must be 'properties' or 'map'");
        }
        tokensMap = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            Object o = properties.get(key);
            if (o instanceof Token[]) {
                tokensMap.put(key, (Token[])o);
            } else if (o instanceof Token) {
                Token[] tokens = new Token[] { (Token)o };
                tokensMap.put(key, tokens);
            } else {
                Token[] tokens = TokenParser.makeTokens(o.toString(), isTokenize());
                putValue(name, tokens);
            }
        }
    }

    /**
     * Adds the specified value to this List type item.
     *
     * @param text the value to be analyzed for use as the value of this item
     * @see #addValue(Token[])
     */
    public void addValue(String text) {
        Token[] tokens = TokenParser.makeTokens(text, isTokenize());
        addValue(tokens);
    }

    /**
     * Adds the specified value to this List type item.
     *
     * @param tokens an array of tokens
     */
    public void addValue(Token[] tokens) {
        if (type == null) {
            type = ItemType.LIST;
        }
        if (!isListableType()) {
            throw new IllegalArgumentException("The type of this item must be 'array', 'list' or 'set'");
        }
        if (tokensList == null) {
            tokensList = new ArrayList<>();
        }
        tokensList.add(tokens);
    }

    /**
     * Sets a value to this List type item.
     *
     * @param tokensList the tokens list
     */
    public void setValue(List<Token[]> tokensList) {
        if (type == null) {
            type = ItemType.LIST;
        }
        if (!isListableType()) {
            throw new IllegalArgumentException("The item type must be 'array', 'list' or 'set' for this item " + this);
        }
        this.tokensList = tokensList;
    }

    /**
     * Sets a value to this Set type item.
     *
     * @param tokensSet the tokens set
     */
    public void setValue(Set<Token[]> tokensSet) {
        if (tokensSet == null) {
            throw new IllegalArgumentException("tokensSet must not be null");
        }
        if (type == null) {
            type = ItemType.SET;
        }
        if (!isListableType()) {
            throw new IllegalArgumentException("The type of this item must be 'set', 'array' or 'list'");
        }
        tokensList = new ArrayList<>(tokensSet);
    }

    /**
     * Gets the value type of this item.
     *
     * @return the value type of this item
     */
    public ItemValueType getValueType() {
        return valueType;
    }

    /**
     * Sets the value type of this item.
     *
     * @param valueType the new value type
     */
    public void setValueType(ItemValueType valueType) {
        this.valueType = valueType;
    }

    /**
     * Returns whether to tokenize.
     *
     * @return whether to tokenize
     */
    public Boolean getTokenize() {
        return tokenize;
    }

    /**
     * Returns whether tokenize.
     *
     * @return whether tokenize
     */
    public boolean isTokenize() {
        return !(tokenize == Boolean.FALSE);
    }

    /**
     * Sets whether tokenize.
     *
     * @param tokenize whether tokenize
     */
    public void setTokenize(Boolean tokenize) {
        this.tokenize = tokenize;
    }

    /**
     * Returns whether the item name was auto generated.
     *
     * @return true, if the item name was auto generated
     */
    public boolean isAutoNamed() {
        return autoNamed;
    }

    /**
     * Sets whether the item is an auto generated name.
     *
     * @param autoNamed true, if the item name is auto generated
     */
    public void setAutoNamed(boolean autoNamed) {
        this.autoNamed = autoNamed;
    }

    /**
     * Return whether this item is listable type.
     *
     * @return true, if this item is listable type
     */
    public boolean isListableType() {
        return (type == ItemType.ARRAY || type == ItemType.LIST || type == ItemType.SET);
    }

    /**
     * Return whether this item is mappable type.
     *
     * @return true, if this item is mappable type
     */
    public boolean isMappableType() {
        return (type == ItemType.MAP || type == ItemType.PROPERTIES);
    }

    /**
     * Returns whether this item is mandatory.
     *
     * @return whether or not this item is mandatory
     */
    public Boolean getMandatory() {
        return mandatory;
    }

    /**
     * Returns whether this item is mandatory.
     *
     * @return whether or not this item is mandatory
     */
    public boolean isMandatory() {
        return (mandatory == Boolean.TRUE);
    }

    /**
     * Sets whether this item is mandatory.
     *
     * @param mandatory whether or not this item is mandatory
     */
    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Returns whether this item requires secure input.
     *
     * @return whether or not this item requires secure input
     */
    public Boolean getSecret() {
        return secret;
    }

    /**
     * Returns whether this item requires secure input.
     *
     * @return whether or not this item requires secure input
     */
    public boolean isSecret() {
        return (secret == Boolean.TRUE);
    }

    /**
     * Sets whether this item requires secure input.
     *
     * @param secret whether or not this item requires secure input
     */
    public void setSecret(Boolean secret) {
        this.secret = secret;
    }

    public Token[] getAllTokens() {
        if (type == ItemType.SINGLE) {
            return tokens;
        } else if (isListableType()) {
            if (tokensList == null || tokensList.isEmpty()) {
                return null;
            } else if (tokensList.size() == 1) {
                return tokensList.get(0);
            } else {
                List<Token> list = new ArrayList<>();
                for (Token[] tokens : tokensList) {
                    Collections.addAll(list, tokens);
                }
                return list.toArray(new Token[0]);
            }
        } else if (isMappableType()) {
            if (tokensMap == null || tokensMap.isEmpty()) {
                return null;
            } else if (tokensMap.size() == 1) {
                Iterator<Token[]> it = tokensMap.values().iterator();
                if (it.hasNext()) {
                    return it.next();
                } else {
                    return new Token[0];
                }
            } else {
                List<Token> list = new ArrayList<>();
                for (Token[] tokens : tokensMap.values()) {
                    Collections.addAll(list, tokens);
                }
                return list.toArray(new Token[0]);
            }
        } else {
            return null;
        }
    }

    public boolean containsToken(Token token) {
        Token[] allTokens = getAllTokens();
        if (allTokens != null) {
            for (Token t : allTokens) {
                if (t != null && t.equals(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    public BeanRule getBeanRule() {
        if (type != ItemType.SINGLE) {
            throw new IllegalStateException("The type of this item must be 'single'");
        }
        return beanRule;
    }

    public void setBeanRule(BeanRule beanRule) {
        if (type == null) {
            type = ItemType.SINGLE;
        } else if (type != ItemType.SINGLE) {
            throw new IllegalStateException("The type of this item must be 'single'");
        }
        if (valueType == null) {
            valueType = ItemValueType.BEAN;
        }
        this.beanRule = beanRule;
    }

    public List<BeanRule> getBeanRuleList() {
        if (!isListableType()) {
            throw new IllegalStateException("The type of this item must be 'array' or 'list'");
        }
        return beanRuleList;
    }

    public void addBeanRule(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        if (valueType == null) {
            valueType = ItemValueType.BEAN;
        }
        if (beanRuleList == null) {
            beanRuleList = new ArrayList<>();
        }
        beanRuleList.add(beanRule);
    }

    public Map<String, BeanRule> getBeanRuleMap() {
        if (!isMappableType()) {
            throw new IllegalStateException("The type of this item must be 'map' or 'properties'");
        }
        return beanRuleMap;
    }

    public void putBeanRule(String name, BeanRule beanRule) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        if (valueType == null) {
            valueType = ItemValueType.BEAN;
        }
        if (beanRuleMap == null) {
            beanRuleMap = new LinkedHashMap<>();
        }
        beanRuleMap.put(name, beanRule);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", type);
        tsb.append("name", name);
        tsb.append("valueType", valueType);
        if (type == ItemType.SINGLE) {
            if (valueType == ItemValueType.BEAN) {
                tsb.append("value", beanRule);
            } else {
                tsb.append("value", tokens);
            }
        } else if (isListableType()) {
            if (valueType == ItemValueType.BEAN) {
                tsb.append("value", beanRuleList);
            } else {
                tsb.append("value", tokensList);
            }
        } else if (isMappableType()) {
            if (valueType == ItemValueType.BEAN) {
                tsb.append("value", beanRuleMap);
            } else {
                tsb.append("value", tokensMap);
            }
        }
        tsb.append("tokenize", tokenize);
        tsb.append("mandatory", mandatory);
        tsb.append("secret", secret);
        return tsb.toString();
    }

    /**
     * Gets the class of value.
     *
     * @param ir the item rule
     * @param value the value
     * @return the class of value
     */
    public static Class<?> getPrototypeClass(ItemRule ir, Object value) {
        ItemValueType valueType = ir.getValueType();
        if (ir.getType() == ItemType.ARRAY) {
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
                return (value != null ? value.getClass() : String[].class);
            }
        } else if (ir.getType() == ItemType.LIST) {
            return (value != null ? value.getClass() : List.class);
        } else if (ir.getType() == ItemType.MAP) {
            return (value != null ? value.getClass() : Map.class);
        } else if (ir.getType() == ItemType.SET) {
            return (value != null ? value.getClass() : Set.class);
        } else if (ir.getType() == ItemType.PROPERTIES) {
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
                return (value != null ? value.getClass() : String.class);
            }
        }
    }

    /**
     * Returns a new derived instance of ItemRule.
     *
     * @param type the item type
     * @param name the name of the item
     * @param valueType the type of value an item can have
     * @param tokenize whether to tokenize
     * @param mandatory whether or not this item is mandatory
     * @param secret whether or not this item requires secure input
     * @return the item rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ItemRule newInstance(String type, String name, String valueType, Boolean tokenize,
                                       Boolean mandatory, Boolean secret) throws IllegalRuleException {
        ItemRule itemRule = new ItemRule();

        ItemType itemType = ItemType.resolve(type);
        if (type != null && itemType == null) {
            throw new IllegalRuleException("No item type for '" + type + "'");
        }
        if (itemType != null) {
            itemRule.setType(itemType);
        } else {
            itemRule.setType(ItemType.SINGLE); // default
        }

        if (!StringUtils.isEmpty(name)) {
            itemRule.setName(name);
        } else {
            itemRule.setAutoNamed(true);
        }

        if (tokenize != null) {
            itemRule.setTokenize(tokenize);
        }

        if (valueType != null) {
            ItemValueType itemValueType = ItemValueType.resolve(valueType);
            if (itemValueType == null) {
                throw new IllegalRuleException("No item value type for '" + valueType + "'");
            }
            itemRule.setValueType(itemValueType);
        }

        if (mandatory != null) {
            itemRule.setMandatory(mandatory);
        }

        if (secret != null) {
            itemRule.setSecret(secret);
        }

        return itemRule;
    }

    /**
     * Returns a {@code Token} iterator.
     *
     * @param itemRule the item rule
     * @return the iterator for tokens
     */
    public static Iterator<Token[]> tokenIterator(ItemRule itemRule) {
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
     * Convert the given item parameters list into an {@code ItemRuleMap}.
     *
     * @param itemParametersList the item parameters list to convert
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
     * Convert the given item parameters list into an {@code ItemRuleList}.
     *
     * @param itemParametersList the item parameters list to convert
     * @return the item rule list
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ItemRuleList toItemRuleList(List<ItemParameters> itemParametersList) throws IllegalRuleException {
        ItemRuleList itemRuleList = new ItemRuleList();
        for (ItemParameters parameters : itemParametersList) {
            itemRuleList.add(ItemRule.toItemRule(parameters));
        }
        return itemRuleList;
    }

    /**
     * Convert the given item parameters into an {@code ItemRule}.
     * <pre>
     * [
     *   {
     *     type: "map"
     *     name: "property1"
     *     value: {
     *       code1: "value1"
     *       code2: "value2"
     *     }
     *     valueType: "java.lang.String"
     *     tokenize: true
     *   }
     *   {
     *     name: "property2"
     *     value(int): 123
     *   }
     *   {
     *     name: "property2"
     *     reference: {
     *       bean: "a.bean"
     *     }
     *   }
     * ]
     * </pre>
     *
     * @param itemParameters the item parameters
     * @return an instance of {@code ItemRule}
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static ItemRule toItemRule(ItemParameters itemParameters) throws IllegalRuleException {
        String type = itemParameters.getString(ItemParameters.type);
        String name = itemParameters.getString(ItemParameters.name);
        String valueType = itemParameters.getString(ItemParameters.valueType);
        Boolean tokenize = itemParameters.getBoolean(ItemParameters.tokenize);
        Boolean mandatory = itemParameters.getBoolean(ItemParameters.mandatory);
        Boolean secret = itemParameters.getBoolean(ItemParameters.secret);

        ItemRule itemRule = ItemRule.newInstance(type, name, valueType, tokenize, mandatory, secret);

        if (itemRule.isListableType()) {
            List<String> stringList = itemParameters.getStringList(ItemParameters.value);
            if (stringList != null) {
                for (String value : stringList) {
                    itemRule.addValue(value);
                }
            }
        } else if (itemRule.isMappableType()) {
            List<EntryParameters> parametersList = itemParameters.getParametersList(ItemParameters.entry);
            if (parametersList != null) {
                for (Parameters parameters : parametersList) {
                    if (parameters != null) {
                        String entryName = parameters.getString(EntryParameters.name);
                        String entryValue = parameters.getString(EntryParameters.value);
                        itemRule.putValue(entryName, entryValue);
                    }
                }
            }
        } else {
            List<String> stringList = itemParameters.getStringList(ItemParameters.value);
            if (stringList != null && !stringList.isEmpty()) {
                itemRule.setValue(stringList.get(0));
            }
        }

        return itemRule;
    }

    public static ItemRuleMap toItemRuleMap(Parameter[] parameters) throws IllegalRuleException {
        if (parameters == null || parameters.length == 0) {
            return null;
        }
        ItemRuleMap itemRuleMap = new ItemRuleMap();
        for (Parameter parameter : parameters) {
            itemRuleMap.putItemRule(toItemRule(parameter));
        }
        return itemRuleMap;
    }

    public static ItemRule toItemRule(Parameter parameter) throws IllegalRuleException {
        String name = parameter.name();
        String value = parameter.value();
        boolean tokenize = parameter.tokenize();
        boolean mandatory = parameter.mandatory();
        boolean secret = parameter.secret();

        ItemRule itemRule = ItemRule.newInstance(null, name, null,
                tokenize, mandatory, secret);
        itemRule.setValue(value);
        return itemRule;
    }

    public static ItemRuleMap toItemRuleMap(Attribute[] attributes) throws IllegalRuleException {
        if (attributes == null || attributes.length == 0) {
            return null;
        }
        ItemRuleMap itemRuleMap = new ItemRuleMap();
        for (Attribute attribute : attributes) {
            itemRuleMap.putItemRule(toItemRule(attribute));
        }
        return itemRuleMap;
    }

    public static ItemRule toItemRule(Attribute attribute) throws IllegalRuleException {
        String name = attribute.name();
        String value = attribute.value();
        boolean tokenize = attribute.tokenize();
        boolean mandatory = attribute.mandatory();
        boolean secret = attribute.secret();

        ItemRule itemRule = ItemRule.newInstance(null, name, null,
                tokenize, mandatory, secret);
        itemRule.setValue(value);
        return itemRule;
    }

}
