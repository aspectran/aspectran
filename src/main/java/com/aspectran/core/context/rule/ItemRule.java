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
package com.aspectran.core.context.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.context.builder.apon.params.ItemHolderParameters;
import com.aspectran.core.context.builder.apon.params.ItemParameters;
import com.aspectran.core.context.builder.apon.params.ReferenceParameters;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class ItemRule.
 * 
 * <p>Created: 2008. 03. 27 PM 3:57:48</p>
 * 
 * @author Juho Jeong
 */
public class ItemRule {

	/**  suffix for array-type item: "[]". */
	public static final String ARRAY_SUFFIX = "[]";
	
	/**  suffix for map-type item: "{}". */
	public static final String MAP_SUFFIX = "{}";

	private ItemType type;
	
	private String name;
	
	private ItemValueType valueType;
	
	private String defaultValue;

	private Boolean tokenize;

	private Token[] tokens;
	
	private List<Token[]> tokensList;
	
	private Map<String, Token[]> tokensMap;
	
	private boolean autoGeneratedName;

	/**
	 * Instantiates a new ItemRule.
	 */
	public ItemRule() {
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
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
	 * Returns the value of the item.
	 * 
	 * @return the value of the item
	 */
	public String getValue() {
		return TokenParser.toString(tokens);
	}

	/**
	 * Gets the value type  of the item.
	 *
	 * @return the value type of the item
	 */
	public ItemValueType getValueType() {
		return valueType;
	}

	/**
	 * Sets the value type of the item.
	 *
	 * @param valueType the new value type of the item
	 */
	public void setValueType(ItemValueType valueType) {
		this.valueType = valueType;
	}

	/**
	 * Gets the default value.
	 *
	 * @return the default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets the default value.
	 *
	 * @param defaultValue the new default value
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
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
	 * Returns whether to tokenize.
	 *
	 * @return whether to tokenize
	 */
	public boolean isTokenize() {
		return !(tokenize == Boolean.FALSE);
	}
	
	/**
	 * Sets whether to tokenize.
	 *
	 * @param tokenize whether to tokenize
	 */
	public void setTokenize(Boolean tokenize) {
		this.tokenize = tokenize;
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
	 * Gets the value list.
	 *
	 * @return the value list
	 */
	public List<String> getValueList() {
		if(tokensList == null)
			return null;
		
		List<String> list = new ArrayList<String>();
		
		if(tokensList.isEmpty())
			return list;
		
		for(Token[] tokens : tokensList) {
			list.add(TokenParser.toString(tokens));
		}
		
		return list;
	}
	
	/**
	 * Gets the tokens map.
	 * 
	 * @return the tokens map
	 */
	public Map<String, Token[]> getTokensMap() {
		return tokensMap;
	}

	public Map<String, String> getValueMap() {
		if(tokensMap == null)
			return null;
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		if(tokensMap.size() == 0)
			return map;
		
		for(Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
			map.put(entry.getKey(), TokenParser.toString(entry.getValue()));
		}
		
		return map;
	}
	
	/**
	 * Returns whether the item name was auto generated.
	 *
	 * @return true, if the item name was auto generated
	 */
	public boolean isAutoGeneratedName() {
		return autoGeneratedName;
	}

	/**
	 * Sets whether the item is an auto generated name.
	 *
	 * @param autoGeneratedName true, if the item name is auto generated
	 */
	public void setAutoGeneratedName(boolean autoGeneratedName) {
		this.autoGeneratedName = autoGeneratedName;
	}

	/**
	 * Return whether the item is listable type.
	 *
	 * @return true, if the item is listable type
	 */
	public boolean isListableType() {
		return (type == ItemType.ARRAY || type == ItemType.LIST || type == ItemType.SET);
	}

	/**
	 * Return whether the item is mappable type.
	 *
	 * @return true, if the item is mappable type
	 */
	public boolean isMappableType() {
		return (type == ItemType.MAP || type == ItemType.PROPERTIES);
	}

	/**
	 * Sets the name of a item.
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		if(name.endsWith(ARRAY_SUFFIX)) {
			this.name = name.substring(0, name.length() - 2);
			type = ItemType.ARRAY;
		} else if(name.endsWith(MAP_SUFFIX)) {
			this.name = name.substring(0, name.length() - 2);
			type = ItemType.MAP;
		} else {
			this.name = name;
			if(type == null)
				type = ItemType.SINGULAR;
		}
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(String value) {
		Token[] tokens;
		
		if(isTokenize())
			tokens = TokenParser.parse(value);
		else {
			tokens = new Token[1];
			tokens[0] = new Token(TokenType.TEXT, value);
		}

		setValue(tokens);
	}

	/**
	 * Sets the value.
	 *
	 * @param tokens the new value
	 */
	public void setValue(Token[] tokens) {
		if(type == null)
			throw new IllegalArgumentException("Item type is must not be null.");

		if(type != ItemType.SINGULAR)
			throw new IllegalArgumentException("Invalid value type for the item rule " + this);

		this.tokens = tokens;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensList the new value
	 */
	public void setValue(List<Token[]> tokensList) {
		if(type == null)
			throw new IllegalArgumentException("Item type is must not be null.");

		if(!isListableType())
			throw new IllegalArgumentException("Invalid value type for the item rule " + this);

		this.tokensList = tokensList;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensMap the tokens map
	 */
	public void setValue(Map<String, Token[]> tokensMap) {
		if(type == null)
			throw new IllegalArgumentException("Item type is must not be null.");

		if(!isMappableType())
			throw new IllegalArgumentException("Invalid value type for the item rule " + this);

		this.tokensMap = tokensMap;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("type", type);
		tsb.append("name", name);
		tsb.append("valueType", valueType);
		if(type == ItemType.SINGULAR) {
			tsb.append("value", tokens);
		} else if(type == ItemType.ARRAY || type == ItemType.LIST || type == ItemType.SET) {
			tsb.append("value", tokensList);
		} else if(type == ItemType.MAP || type == ItemType.PROPERTIES) {
			tsb.append("value", tokensMap);
		}
		tsb.append("tokenize", tokenize);
		tsb.append("autoGeneratedName", autoGeneratedName);
		return tsb.toString();
	}

	/**
	 * Gets the class of value.
	 *
	 * @param ir the item rule
	 * @param value the value
	 * @return the class of value
	 */
	public static Class<?> getClassOfValue(ItemRule ir, Object value) {
		ItemValueType valueType = ir.getValueType();
		
		if(ir.getType() == ItemType.ARRAY) {
			if(valueType == ItemValueType.STRING) {
				return String[].class;
			} else if(valueType == ItemValueType.INT) {
				return Integer[].class;
			} else if(valueType == ItemValueType.LONG) {
				return Long[].class;
			} else if(valueType == ItemValueType.FLOAT) {
				return Float[].class;
			} else if(valueType == ItemValueType.DOUBLE) {
				return Double[].class;
			} else if(valueType == ItemValueType.BOOLEAN) {
				return Boolean[].class;
			} else if(valueType == ItemValueType.PARAMETERS) {
				return Parameters[].class;
			} else if(valueType == ItemValueType.FILE) {
				return File[].class;
			} else if(valueType == ItemValueType.MULTIPART_FILE) {
				return FileParameter[].class;
			}
		}
		
		if(value != null)
			return value.getClass();
		
		return Object.class;
	}
	
	/**
	 * Returns a new derived instance of ItemRule.
	 *
	 * @param type the type
	 * @param name the name
	 * @param value the value
	 * @param valueType the value type
	 * @param defaultValue the default value
	 * @param tokenize whether to tokenize
	 * @return the item rule
	 */
	public static ItemRule newInstance(String type, String name, String value, String valueType, String defaultValue, Boolean tokenize) {
		ItemRule itemRule = new ItemRule();
		
		ItemType itemType = ItemType.lookup(type);

		if(type != null && itemType == null)
			throw new IllegalArgumentException("No item type registered for '" + type + "'.");
		
		if(itemType != null)
			itemRule.setType(itemType);
		else
			itemRule.setType(ItemType.SINGULAR); //default

		if(!StringUtils.isEmpty(name)) {
			itemRule.setName(name);
		} else {
			itemRule.setAutoGeneratedName(true);
		}

		if(value != null)
			itemRule.setValue(value);
		
		ItemValueType itemValueType = ItemValueType.lookup(valueType);
		
		if(valueType != null && itemValueType == null)
			throw new IllegalArgumentException("No item value type registered for '" + valueType + "'.");
		
		itemRule.setValueType(itemValueType);

		if(defaultValue != null)
			itemRule.setDefaultValue(defaultValue);
		
		if(tokenize != null)
			itemRule.setTokenize(tokenize);
		
		return itemRule;
	}
	
	/**
	 * Update reference.
	 *
	 * @param itemRule the item rule
	 * @param beanId the bean id
	 * @param parameter the parameter name
	 * @param attribute the attribute name
	 * @param property the bean's property
	 */
	public static void updateReference(ItemRule itemRule, String beanId, String parameter, String attribute, String property) {
		Token token = makeReferenceToken(beanId, parameter, attribute, property);
		if(token != null)
			itemRule.setValue(new Token[] { token });
	}
	
	/**
	 * Returns a made reference token.
	 *
	 * @param beanId the bean id
	 * @param parameter the parameter name
	 * @param attribute the attribute name
	 * @param property the property for bean or attribute object
	 * @return the token
	 */
	public static Token makeReferenceToken(String beanId, String parameter, String attribute, String property) {
		Token token;
		
		if(beanId != null) {
			token = new Token(TokenType.BEAN, beanId);
			if(property != null)
				token.setPropertyName(property); // bean property
		} else if(parameter != null) {
			token = new Token(TokenType.PARAMETER, parameter);
		} else if(attribute != null) {
			token = new Token(TokenType.ATTRIBUTE, attribute);
			if(property != null)
				token.setPropertyName(property); // object property
		} else {
			token = null;
		}

		return token;
	}
	
	/**
	 * Returns a {@code Token} iterator.
	 *
	 * @param itemRule the item rule
	 * @return the iterator for tokens
	 */
	public static Iterator<Token[]> tokenIterator(ItemRule itemRule) {
		Iterator<Token[]> iter = null;
		
		if(itemRule.isListableType()) {
			List<Token[]> list = itemRule.getTokensList();
			iter = list.iterator();
		} else if(itemRule.isMappableType()) {
			Map<String, Token[]> map = itemRule.getTokensMap();
			if(map != null)
				iter = map.values().iterator();
		}
		
		return iter;
	}
	
	/**
	 * Parses the value.
	 *
	 * @param itemRule the item rule
	 * @param valueText the value text
	 * @return the token[]
	 */
	public static Token[] parseValue(ItemRule itemRule, String valueText) {
		if(itemRule.getType() == ItemType.SINGULAR) {
			if(valueText != null) {
				itemRule.setValue(valueText);
			}
			
			return null;
		} else {
			Token[] tokens = null;
			
			if(itemRule.isListableType()) {
				tokens = TokenParser.makeTokens(valueText, itemRule.isTokenize());
			} else if(itemRule.isMappableType()) {
				if(!StringUtils.isEmpty(valueText)) {
					tokens = TokenParser.makeTokens(valueText, itemRule.isTokenize());
				}
			}
			
			return tokens;
		}
	}
	
	/**
	 * Begin value collection.
	 *
	 * @param itemRule the item rule
	 */
	public static void beginValueCollection(ItemRule itemRule) {
		if(itemRule.isListableType()) {
			List<Token[]> tokensList = new ArrayList<Token[]>();
			itemRule.setValue(tokensList);
		} else if(itemRule.isMappableType()) {
			Map<String, Token[]> tokensMap = new LinkedHashMap<String, Token[]>();
			itemRule.setValue(tokensMap);
		}
	}
	
	/**
	 * Finish value collection.
	 *
	 * @param itemRule the item rule
	 * @param name the name
	 * @param tokens the tokens
	 */
	public static void flushValueCollection(ItemRule itemRule, String name, Token[] tokens) {
		if(itemRule.isListableType()) {
			List<Token[]> list = itemRule.getTokensList();
			list.add(tokens);
		} else if(itemRule.isMappableType()) {
			if(!StringUtils.isEmpty(name)) {
				Map<String, Token[]> map = itemRule.getTokensMap();
				map.put(name, tokens);
			}
		}
	}
	
	/**
	 * Adds the item rule.
	 *
	 * @param itemRuleMap the item rule map
	 * @param itemRule the item rule
	 */
	public static void addItemRule(ItemRule itemRule, ItemRuleMap itemRuleMap) {
		// auto-naming if did not specify the name of the item.
		if(itemRule.isAutoGeneratedName()) {
			generateItemName(itemRule, itemRuleMap);
		}
		
		itemRuleMap.putItemRule(itemRule);
	}

	
	/**
	 * Auto-naming for unnamed item name.
	 * Auto-naming if did not specify the name of the item.
	 *
	 * @param itemRule the item rule
	 * @param itemRuleMap the item rule map
	 */
	private static void generateItemName(ItemRule itemRule, ItemRuleMap itemRuleMap) {
		int count = 1;
		
		for(ItemRule ir : itemRuleMap.values()) {
			if(ir.isAutoGeneratedName() && ir.getType() == itemRule.getType()) {
				count++;
				
				if(itemRule == ir)
					break;
			}
		}
		
		if(itemRule.getType() != ItemType.SINGULAR || itemRule.getValueType() == null) {
			String name = itemRule.getType().toString() + count;
			itemRule.setName(name);
		} else {
			if(itemRule.getValueType() != null) {
				String name = itemRule.getValueType().toString() + count;
				itemRule.setName(name);
			}
		}
	}

	/**
	 * Convert to item rule map from parameters list.
	 *
	 * @param itemParametersList the item parameters list
	 * @return the item rule map
	 */
	public static ItemRuleMap toItemRuleMap(List<Parameters> itemParametersList) {
		if(itemParametersList == null || itemParametersList.isEmpty())
			return null;
		
		ItemRuleMap itemRuleMap = new ItemRuleMap();
		
		for(Parameters parameters : itemParametersList) {
			ItemRule itemRule = toItemRule(parameters);

			// auto-naming if did not specify the name of the item.
			if(StringUtils.isEmpty(itemRule.getName())) {
				itemRule.setAutoGeneratedName(true);
				generateItemName(itemRule, itemRuleMap);
			}

			itemRuleMap.putItemRule(itemRule);
		}
		
		return itemRuleMap;
	}
	
	/**
	 * Convert then Parameters to the item rule.
	 * <pre>
	 * [
	 * 	{
	 * 		type: map
	 * 		name: property1
	 * 		value: {
	 * 			code1: value1
	 * 			code2: value2
	 * 		}
	 * 		valueType: java.lang.String
	 * 		defaultValue: default value
	 * 		tokenize: true
	 * 	}
	 * 	{
	 * 		name: property2
	 * 		value(int): 123
	 * 	}
	 * 	{
	 * 		name: property2
	 * 		reference: {
	 * 			bean: a.bean
	 * 		}
	 * 	}
	 * ]
	 * </pre>
	 *
	 * @param itemParameters the item parameters
	 * @return the item rule
	 */
	public static ItemRule toItemRule(Parameters itemParameters) {
		String type = itemParameters.getString(ItemParameters.type);
		String name = itemParameters.getString(ItemParameters.name);
		String valueType = itemParameters.getString(ItemParameters.valueType);
		String defaultValue = itemParameters.getString(ItemParameters.defaultValue);
		Boolean tokenize = itemParameters.getBoolean(ItemParameters.tokenize);
		Parameters referenceParameters = itemParameters.getParameters(ItemParameters.reference);
		
		ItemRule itemRule = ItemRule.newInstance(type, name, null, valueType, defaultValue, tokenize);
		
		if(referenceParameters != null) {
			String bean = StringUtils.emptyToNull(referenceParameters.getString(ReferenceParameters.bean));
			String parameter = StringUtils.emptyToNull(referenceParameters.getString(ReferenceParameters.parameter));
			String attribute = StringUtils.emptyToNull(referenceParameters.getString(ReferenceParameters.attribute));
			String property = StringUtils.emptyToNull(referenceParameters.getString(ReferenceParameters.property));
			
			updateReference(itemRule, bean, parameter, attribute, property);
		} else {
			if(itemRule.getType() == ItemType.SINGULAR) {
				String value = itemParameters.getString(ItemParameters.value);
				parseValue(itemRule, value);
			} else if(itemRule.isListableType()) {
				List<String> stringList = itemParameters.getStringList(ItemParameters.value);
				
				if(stringList != null) {
					beginValueCollection(itemRule);
					for(String value : stringList) {
						Token[] tokens = parseValue(itemRule, value);
						flushValueCollection(itemRule, name, tokens);
					}
				}
			} else if(itemRule.isMappableType()) {
				Parameters parameters = itemParameters.getParameters(ItemParameters.value);

				if(parameters != null) {
					Set<String> parametersNames = parameters.getParameterNameSet();
					
					if(parametersNames != null) {
						beginValueCollection(itemRule);
						for(String valueName : parametersNames) {
							Token[] tokens = parseValue(itemRule, parameters.getString(valueName));
							flushValueCollection(itemRule, valueName, tokens);
						}
					}
				} 
			}
		}
		
		return itemRule;
	}
	
	/**
	 * Convert to item parameters from a string.
	 *
	 * @param text the text
	 * @return the list
	 */
	public static List<Parameters> toItemParametersList(String text) {
		Parameters holder = new ItemHolderParameters(text);
		return holder.getParametersList(ItemHolderParameters.item);
	}
	
}
