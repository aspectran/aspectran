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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Represents a fundamental, configurable item used for parameters, attributes, or properties.
 * An ItemRule defines a named value that can be a single value, a list, or a map.
 * It is a versatile building block for passing data into and between various components of the framework.
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

    private BeanRule beanRule;

    private List<BeanRule> beanRuleList;

    private Map<String, BeanRule> beanRuleMap;

    private Boolean mandatory;

    private Boolean secret;

    private boolean autoNamed;

    /**
     * Instantiates a new ItemRule.
     */
    public ItemRule() {
    }

    /**
     * Gets the item type (e.g., SINGLE, ARRAY, MAP).
     * @return the item type
     */
    public ItemType getType() {
        return type;
    }

    /**
     * Sets the item type.
     * @param type the new item type
     */
    public void setType(ItemType type) {
        this.type = type;
    }

    /**
     * Returns the name of the item.
     * @return the name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the item.
     * If the name ends with "[]" or "{}", the item type is automatically set to ARRAY or MAP respectively.
     * @param name the name to set
     */
    public void setName(String name) {
        Assert.hasText(name, "name must not be empty");
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
     * Returns the value of the item as a string.
     * @return the value of the item
     */
    public String getValue() {
        return toValue(tokens);
    }

    private String toValue(Token[] tokens) {
        return (tokens != null ? TokenParser.toString(tokens) : null);
    }

    /**
     * Gets the tokens for a single value item.
     * @return the tokens
     */
    public Token[] getTokens() {
        return tokens;
    }

    /**
     * Gets the list of tokens for a list-type item.
     * @return the tokens list
     */
    public List<Token[]> getTokensList() {
        return tokensList;
    }

    /**
     * Returns a list of string values of this item.
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
     * Gets the map of tokens for a map-type item.
     * @return the tokens map
     */
    public Map<String, Token[]> getTokensMap() {
        return tokensMap;
    }

    /**
     * Returns a map of string values of this item.
     * @return a map of string values
     */
    public Map<String, String> getValueMap() {
        if (tokensMap == null) {
            return null;
        }
        if (tokensMap.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
            map.put(entry.getKey(), toValue(entry.getValue()));
        }
        return map;
    }

    /**
     * Sets the specified value to this Single type item.
     * @param value the value to be analyzed for use as the value of this item
     * @see #setValue(Token[])
     */
    public void setValue(String value) {
        Token[] tokens = TokenParser.makeTokens(value, isTokenize());
        setValue(tokens);
    }

    /**
     * Sets the specified value to this Single type item.
     * @param tokens an array of tokens
     */
    public void setValue(Token[] tokens) {
        if (type == null) {
            type = ItemType.SINGLE;
        }
        checkSingleType();
        this.tokens = tokens;
    }

    /**
     * Puts the specified value with the specified key to this Map type item.
     * @param name the value name; may be null
     * @param value the value to be analyzed for use as the value of this item
     * @see #putValue(String, Token[])
     */
    public void putValue(String name, String value) {
        Token[] tokens = TokenParser.makeTokens(value, isTokenize());
        putValue(name, tokens);
    }

    /**
     * Puts the specified value with the specified key to this Map type item.
     * @param name the value name; may be null
     * @param tokens an array of tokens
     */
    public void putValue(String name, Token[] tokens) {
        if (type == null) {
            type = ItemType.MAP;
        }
        checkMappableType();
        if (tokensMap == null) {
            tokensMap = new LinkedHashMap<>();
        }
        tokensMap.put(name, tokens);
    }

    /**
     * Sets a value to this Map type item.
     * @param tokensMap the tokens map
     */
    public void setValue(Map<String, Token[]> tokensMap) {
        if (type == null) {
            type = ItemType.MAP;
        }
        checkMappableType();
        this.tokensMap = tokensMap;
    }

    /**
     * Sets a value to this Properties type item.
     * @param properties the properties
     */
    public void setValue(Properties properties)  {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        if (type == null) {
            type = ItemType.PROPERTIES;
        }
        checkMappableType();
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
     * @param value the value to be analyzed for use as the value of this item
     * @see #addValue(Token[])
     */
    public void addValue(String value) {
        Token[] tokens = TokenParser.makeTokens(value, isTokenize());
        addValue(tokens);
    }

    /**
     * Adds the specified value to this List type item.
     * @param tokens an array of tokens
     */
    public void addValue(Token[] tokens) {
        if (type == null) {
            type = ItemType.LIST;
        }
        checkListType();
        if (tokensList == null) {
            tokensList = new ArrayList<>();
        }
        tokensList.add(tokens);
    }

    /**
     * Sets a value to this List type item.
     * @param tokensList the tokens list
     */
    public void setValue(List<Token[]> tokensList) {
        if (type == null) {
            type = ItemType.LIST;
        }
        checkListType();
        this.tokensList = tokensList;
    }

    /**
     * Sets a value to this Set type item.
     * @param tokensSet the tokens set
     */
    public void setValue(Set<Token[]> tokensSet) {
        if (tokensSet == null) {
            throw new IllegalArgumentException("tokensSet must not be null");
        }
        if (type == null) {
            type = ItemType.SET;
        }
        checkListType();
        tokensList = new ArrayList<>(tokensSet);
    }

    /**
     * Gets the inner bean rule for a single value item.
     * @return the bean rule
     */
    public BeanRule getBeanRule() {
        checkSingleType();
        return beanRule;
    }

    /**
     * Sets the inner bean rule for a single value item.
     * @param beanRule the bean rule
     */
    public void setBeanRule(BeanRule beanRule) {
        if (type == null) {
            type = ItemType.SINGLE;
        }
        checkSingleType();
        if (valueType == null) {
            valueType = ItemValueType.BEAN;
        }
        this.beanRule = beanRule;
    }

    /**
     * Gets the list of inner bean rules for a list-type item.
     * @return the list of bean rules
     */
    public List<BeanRule> getBeanRuleList() {
        checkListType();
        return beanRuleList;
    }

    /**
     * Adds an inner bean rule to a list-type item.
     * @param beanRule the bean rule to add
     */
    public void addBeanRule(BeanRule beanRule) {
        checkListType();
        if (valueType == null) {
            valueType = ItemValueType.BEAN;
        }
        if (beanRuleList == null) {
            beanRuleList = new ArrayList<>();
        }
        beanRuleList.add(beanRule);
    }

    /**
     * Gets the map of inner bean rules for a map-type item.
     * @return the map of bean rules
     */
    public Map<String, BeanRule> getBeanRuleMap() {
        checkMappableType();
        return beanRuleMap;
    }

    /**
     * Puts an inner bean rule into a map-type item.
     * @param name the key for the bean rule
     * @param beanRule the bean rule to add
     */
    public void putBeanRule(String name, BeanRule beanRule) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (type == null) {
            type = ItemType.MAP;
        }
        checkMappableType();
        if (valueType == null) {
            valueType = ItemValueType.BEAN;
        }
        if (beanRuleMap == null) {
            beanRuleMap = new LinkedHashMap<>();
        }
        beanRuleMap.put(name, beanRule);
    }

    /**
     * Gets the value type of this item (e.g., STRING, INT, BEAN).
     * @return the value type of this item
     */
    public ItemValueType getValueType() {
        return valueType;
    }

    /**
     * Sets the value type of this item.
     * @param valueType the new value type
     */
    public void setValueType(ItemValueType valueType) {
        this.valueType = valueType;
    }

    /**
     * Returns whether to tokenize the item's value.
     * @return whether to tokenize
     */
    public Boolean getTokenize() {
        return tokenize;
    }

    /**
     * Returns whether to tokenize the item's value (defaults to true).
     * @return whether to tokenize
     */
    public boolean isTokenize() {
        return !(tokenize == Boolean.FALSE);
    }

    /**
     * Sets whether to tokenize the item's value.
     * @param tokenize whether to tokenize
     */
    public void setTokenize(Boolean tokenize) {
        this.tokenize = tokenize;
    }

    /**
     * Returns whether the item name was auto-generated.
     * @return true, if the item name was auto-generated
     */
    public boolean isAutoNamed() {
        return autoNamed;
    }

    /**
     * Sets whether the item name was auto-generated.
     * @param autoNamed true, if the item name is auto-generated
     */
    public void setAutoNamed(boolean autoNamed) {
        this.autoNamed = autoNamed;
    }

    /**
     * Returns whether this item is a list-like type (ARRAY, LIST, or SET).
     * @return true, if this item is a list-like type
     */
    public boolean isListableType() {
        return (type == ItemType.ARRAY || type == ItemType.LIST || type == ItemType.SET);
    }

    /**
     * Returns whether this item is a map-like type (MAP or PROPERTIES).
     * @return true, if this item is a map-like type
     */
    public boolean isMappableType() {
        return (type == ItemType.MAP || type == ItemType.PROPERTIES);
    }

    /**
     * Returns whether this item is mandatory.
     * @return whether this item is mandatory
     */
    public Boolean getMandatory() {
        return mandatory;
    }

    /**
     * Returns whether this item is mandatory.
     * @return true if this item is mandatory
     */
    public boolean isMandatory() {
        return (mandatory == Boolean.TRUE);
    }

    /**
     * Sets whether this item is mandatory.
     * @param mandatory whether this item is mandatory
     */
    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Returns whether this item requires secure input (e.g., for logging).
     * @return whether this item requires secure input
     */
    public Boolean getSecret() {
        return secret;
    }

    /**
     * Returns whether this item requires secure input.
     * @return true if this item requires secure input
     */
    public boolean isSecret() {
        return (secret == Boolean.TRUE);
    }

    /**
     * Sets whether this item requires secure input.
     * @param secret whether this item requires secure input
     */
    public void setSecret(Boolean secret) {
        this.secret = secret;
    }

    /**
     * Gets all tokens from the item, regardless of its type (single, list, or map).
     * @return an array of all tokens
     */
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

    /**
     * Checks if the item's value contains a specific token.
     * @param token the token to check for
     * @return true if the token is found, false otherwise
     */
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

    /**
     * Returns whether the item has a value that can be evaluated at runtime.
     * @return true if the item has an evaluatable value
     */
    public boolean isEvaluable() {
        return (tokens != null || tokensList != null || tokensMap != null ||
                beanRule != null || beanRuleList != null || beanRuleMap != null);
    }

    /**
     * Returns whether the item's value consists only of fixed text (no dynamic tokens).
     * @return true if the value is fixed, false otherwise
     */
    public boolean hasOnlyFixedValue() {
        Token[] allTokens = getAllTokens();
        if (allTokens != null) {
            for (Token t : getAllTokens()) {
                if (t.getType() != TokenType.TEXT) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void checkSingleType() {
        if (type != ItemType.SINGLE) {
            throw new IllegalStateException("The type of this item must be 'single'");
        }
    }

    private void checkListType() {
        if (!isListableType()) {
            throw new IllegalArgumentException("The type of this item must be 'array', 'list' or 'set'");
        }
    }

    private void checkMappableType() {
        if (!isMappableType()) {
            throw new IllegalStateException("The type of this item must be 'map' or 'properties'");
        }
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
     * Creates a new instance of ItemRule.
     * @param type the item type (e.g., "single", "list", "map")
     * @param name the name of the item
     * @param valueType the type of the item's value (e.g., "string", "bean")
     * @param tokenize whether to tokenize the value
     * @param mandatory whether the item is mandatory
     * @param secret whether the item value is secret
     * @return a new ItemRule instance
     * @throws IllegalRuleException if the configuration is invalid
     */
    @NonNull
    public static ItemRule newInstance(
            String type, String name, String valueType, Boolean tokenize,
            Boolean mandatory, Boolean secret) throws IllegalRuleException {
        ItemRule itemRule = new ItemRule();

        ItemType itemType = ItemType.resolve(type);
        if (type != null && itemType == null) {
            throw new IllegalRuleException("No item type for '" + type + "'");
        }
        itemRule.setType(itemType != null ? itemType : ItemType.SINGLE);

        if (StringUtils.hasLength(name)) {
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

}
