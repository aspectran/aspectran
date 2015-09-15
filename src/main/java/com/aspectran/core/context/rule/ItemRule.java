/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.activity.variable.token.TokenParser;
import com.aspectran.core.context.builder.apon.params.ItemHolderParameters;
import com.aspectran.core.context.builder.apon.params.ItemParameters;
import com.aspectran.core.context.builder.apon.params.ReferenceParameters;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;

/**
 * <p>Created: 2008. 03. 27 오후 3:57:48</p>
 */
/**
 * @author Juho Jeong
 *
 */
public class ItemRule {

	/**  suffix for array-type item: "[]". */
	public static final String ARRAY_SUFFIX = "[]";
	
	/**  suffix for map-type item: "{}". */
	public static final String MAP_SUFFIX = "{}";

	/** The type. */
	private ItemType type;
	
	/** The name. */
	private String name;
	
	/** The value type. */
	private ItemValueType valueType;
	
	/** The default value. */
	private String defaultValue;

	/** The tokenize. */
	private final Boolean tokenize;

	/** The tokens. */
	private Token[] tokens;
	
	/** The tokens list. */
	private List<Token[]> tokensList;
	
	/** The tokens map. */
	private Map<String, Token[]> tokensMap;
	
	/** The unknown name. */
	private boolean unknownName;

	/**
	 * Instantiates a new item rule.
	 */
	public ItemRule() {
		this(Boolean.TRUE);
	}
	
	/**
	 * Instantiates a new item rule.
	 *
	 * @param tokenize the tokenize
	 */
	public ItemRule(Boolean tokenize) {
		this.tokenize = tokenize;
	}

	/**
	 * Returns the item-type of the item, for example, SINGLE, LIST, MAP, SET, PROPERTIES.
	 * 
	 * @return the item type
	 */
	public ItemType getType() {
		return type;
	}
	
	/**
	 * Sets the item-type of a item.
	 *
	 * @param type the new type
	 */
	public void setType(ItemType type) {
		this.type = type;
	}

	/**
	 * Returns the name of the parameter.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the value of the parameter.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return toString(tokens);
	}
	
	/**
	 * Returns the item-value-type of the item, for example, STRING, INT, HASH_MAP, ARRAY_LIST.
	 *
	 * @return the item value type
	 */
	public ItemValueType getValueType() {
		return valueType;
	}

	/**
	 * Sets the item-value-type of a item.
	 *
	 * @param valueType the new value type
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
	 * Gets the tokenize.
	 *
	 * @return the tokenize
	 */
	public Boolean getTokenize() {
		return tokenize;
	}

	/**
	 * Checks if is tokenize.
	 *
	 * @return the boolean
	 */
	public boolean isTokenize() {
		return BooleanUtils.toBoolean(tokenize);
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

	public List<String> getValueList() {
		if(tokensList == null)
			return null;
		
		List<String> list = new ArrayList<String>();
		
		if(tokensList.size() == 0)
			return list;
		
		for(Token[] tokens : tokensList) {
			list.add(toString(tokens));
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
			map.put(entry.getKey(), toString(entry.getValue()));
		}
		
		return map;
	}
	
	/**
	 * Checks if is unknown name.
	 *
	 * @return true, if is unknown name
	 */
	public boolean isUnknownName() {
		return unknownName;
	}

	/**
	 * Sets the unknown name.
	 *
	 * @param unknownName the new unknown name
	 */
	public void setUnknownName(boolean unknownName) {
		this.unknownName = unknownName;
	}

	private String toString(Token[] tokens) {
		if(tokens == null)
			return null;
		
		if(tokens.length == 0)
			return StringUtils.EMPTY;
		
		StringBuilder sb = new StringBuilder();
		
		for(Token t : tokens) {
			sb.append(t.toString());
		}
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{type=").append(type.toString());
		sb.append(", name=").append(name);
		sb.append(", valueType=").append(valueType);
		sb.append(", value=");
		
		if(type == ItemType.SINGLE) {
			if(tokens != null) {
				for(Token t : tokens) {
					sb.append(t.toString());
				}
			}
		} else if(type == ItemType.ARRAY || type == ItemType.LIST || type == ItemType.SET) {
			if(tokensList != null) {
				sb.append('[');

				for(int i = 0; i < tokensList.size(); i++) {
					Token[] ts = tokensList.get(i);
					
					if(i > 0)
						sb.append(", ");

					for(Token t : ts) {
						sb.append(t.toString());
					}
				}

				sb.append(']');
			}
		} else if(type == ItemType.MAP || type == ItemType.PROPERTIES) {
			if(tokensMap != null) {
				sb.append('{');
				
				Iterator<String> iter = tokensMap.keySet().iterator();
				String key = null;
				
				while(iter.hasNext()) {
					if(key != null)
						sb.append(", ");

					key = iter.next();
					Token[] ts = tokensMap.get(key);
					
					sb.append(key).append("=");
					
					for(Token t : ts) {
						sb.append(t.toString());
					}
				}
				
				sb.append('}');
			}
		}
		
		sb.append("}");
		
		return sb.toString();
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
				type = ItemType.SINGLE;
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
		checkValueType(ItemType.SINGLE, null, null);
		this.tokens = tokens;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensList the new value
	 */
	public void setValue(List<Token[]> tokensList) {
		checkValueType(ItemType.ARRAY, ItemType.LIST, ItemType.SET);
		this.tokensList = tokensList;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensMap the tokens map
	 */
	public void setValue(Map<String, Token[]> tokensMap) {
		checkValueType(ItemType.MAP, ItemType.PROPERTIES, null);
		this.tokensMap = tokensMap;
	}

	/**
	 * Make tokens.
	 *
	 * @param text the text
	 * @return the token[]
	 */
	public Token[] makeTokens(String text) {
		Token[] tokens;
		
		if(isTokenize())
			tokens = TokenParser.parse(text);
		else {
			tokens = new Token[1];
			tokens[0] = new Token(TokenType.TEXT, text);
		}
		
		return tokens;
	}
	
	/**
	 * Check value type.
	 *
	 * @param compareItemType the compare item type
	 */
	private void checkValueType(ItemType compareItemType, ItemType compareItemType2, ItemType compareItemType3) {
		if(type == null)
			throw new IllegalArgumentException("item-type is required");
		
		if(type != compareItemType && type != compareItemType2 && type != compareItemType3)
			throw new IllegalArgumentException("The item-type of violation has occurred. current item-type: " + type.toString());
	}
	
	public static Class<?> getValueClass(ItemRule ir, Object value) {
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
		
		return value.getClass();
	}
	
	/**
	 * New instance.
	 *
	 * @param type the type
	 * @param name the name
	 * @param value the value
	 * @param valueType the value type
	 * @param defaultValue the default value
	 * @param tokenize the tokenize
	 * @return the item rule
	 */
	public static ItemRule newInstance(String type, String name, String value, String valueType, String defaultValue, Boolean tokenize) {
		ItemRule itemRule;

		if(tokenize != null)
			itemRule = new ItemRule(tokenize);
		else
			itemRule = new ItemRule();
		
		ItemType itemType = ItemType.valueOf(type);
		
		if(type != null && itemType == null)
			throw new IllegalArgumentException("Unknown Item Type: " + type);
		
		if(itemType != null)
			itemRule.setType(itemType);
		else
			itemRule.setType(ItemType.SINGLE); //default

		if(!StringUtils.isEmpty(name)) {
			itemRule.setName(name);
		} else {
			itemRule.setUnknownName(true);
		}

		if(value != null)
			itemRule.setValue(value);
		
		ItemValueType itemValueType = ItemValueType.valueOf(valueType);
		
		if(valueType != null && itemValueType == null)
			throw new IllegalArgumentException("Unknown Item Value Type: " + valueType);
		
		itemRule.setValueType(itemValueType);

		if(defaultValue != null)
			itemRule.setDefaultValue(defaultValue);
		
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
		Token[] tokens = makeReferenceTokens(beanId, parameter, attribute, property);
		
		if(tokens[0] != null)
			itemRule.setValue(tokens);
	}
	
	/**
	 * Make reference tokens.
	 *
	 * @param beanId the bean id
	 * @param parameter the parameter
	 * @param attribute the attribute
	 * @param property the property
	 * @return the token[]
	 */
	public static Token[] makeReferenceTokens(String beanId, String parameter, String attribute, String property) {
		Token[] tokens = new Token[1];
		
		if(!StringUtils.isEmpty(beanId)) {
			tokens[0] = new Token(TokenType.REFERENCE_BEAN, beanId);
			
			if(!StringUtils.isEmpty(property))
				tokens[0].setGetterName(property);
		} else if(!StringUtils.isEmpty(parameter))
			tokens[0] = new Token(TokenType.PARAMETER, parameter);
		else if(!StringUtils.isEmpty(attribute))
			tokens[0] = new Token(TokenType.ATTRIBUTE, attribute);
		else
			tokens[0] = null;
		
		return tokens;
	}
	
	/**
	 * Naming for Unnamed item name.
	 *
	 * @param itemRule the item rule
	 * @param itemRuleMap the item rule map
	 */
	public static void naming(ItemRule itemRule, ItemRuleMap itemRuleMap) {
		int count = 0;
		
		for(ItemRule ir : itemRuleMap) {
			if(ir.isUnknownName() && ir.getType() == itemRule.getType()) {
				count++;
				
				if(itemRule == ir)
					break;
			}
		}
		
		if(count == 0) {
			itemRule.setName(itemRule.getType().toString());
		} else {
			String name = itemRule.getType().toString() + count;
			itemRule.setName(name);
		}
	}

	
	/**
	 * Token iterator.
	 *
	 * @param itemRule the item rule
	 * @return the iterator
	 */
	public static Iterator<Token[]> tokenIterator(ItemRule itemRule) {
		Iterator<Token[]> iter = null;
		
		if(itemRule.getType() == ItemType.ARRAY || itemRule.getType() == ItemType.LIST || itemRule.getType() == ItemType.SET) {
			List<Token[]> list = itemRule.getTokensList();
			iter = list.iterator();
		} else if(itemRule.getType() == ItemType.MAP || itemRule.getType() == ItemType.PROPERTIES) {
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
	 * @param valueName the value name
	 * @param valueText the value text
	 * @return the token[]
	 */
	public static Token[] parseValue(ItemRule itemRule, String valueName, String valueText) {
		if(itemRule.getType() == ItemType.SINGLE) {
			if(valueText != null) {
				itemRule.setValue(valueText);
			}
			
			return null;
		} else {
			Token[] tokens = null;
			
			if(itemRule.getType() == ItemType.ARRAY || itemRule.getType() == ItemType.LIST || itemRule.getType() == ItemType.SET) {
				tokens = itemRule.makeTokens(valueText);
			} else if(itemRule.getType() == ItemType.MAP || itemRule.getType() == ItemType.PROPERTIES) {
				if(!StringUtils.isEmpty(valueText)) {
					tokens = itemRule.makeTokens(valueText);
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
		if(itemRule.getType() == ItemType.ARRAY || itemRule.getType() == ItemType.LIST || itemRule.getType() == ItemType.SET) {
			List<Token[]> tokensList = new ArrayList<Token[]>();
			itemRule.setValue(tokensList);
		} else if(itemRule.getType() == ItemType.MAP || itemRule.getType() == ItemType.PROPERTIES) {
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
		if(itemRule.getType() == ItemType.ARRAY || itemRule.getType() == ItemType.LIST || itemRule.getType() == ItemType.SET) {
			List<Token[]> list = itemRule.getTokensList();
			list.add(tokens);
		} else if(itemRule.getType() == ItemType.MAP || itemRule.getType() == ItemType.PROPERTIES) {
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
	public static void addItemRule(ItemRuleMap itemRuleMap, ItemRule itemRule) {
		itemRuleMap.putItemRule(itemRule);

		if(itemRule.isUnknownName())
			ItemRule.naming(itemRule, itemRuleMap);
	}
	
	public static ItemRuleMap toItemRuleMap(List<Parameters> itemParametersList) {
		if(itemParametersList == null || itemParametersList.isEmpty())
			return null;
		
		ItemRuleMap itemRuleMap = new ItemRuleMap();
		
		for(Parameters parameters : itemParametersList) {
			ItemRule itemRule = toItemRule(parameters);
			itemRuleMap.putItemRule(itemRule);
		}
		
		return itemRuleMap;
	}
	
	/**
	 * Convert then Parameters to the item rule.
	 * <pre>
	 * [
	 * 	{
	 *		type: map
	 *		name: property1
	 *		value: {
	 *			code1: value1
	 *			code2: value2
	 *		}
	 *		valueType: java.lang.String
	 *		defaultValue: default value
	 *		tokenize: true
	 *	}
	 *	{
	 *		name: property2
	 *		value(int): 123
	 *	}
	 *	{
	 *		name: property2
	 *		reference: {
	 *			bean: a.bean
	 *		}
	 *	}
	 * ]
	 *</pre>
	 *
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
			String bean = referenceParameters.getString(ReferenceParameters.bean);
			String parameter = referenceParameters.getString(ReferenceParameters.parameter);
			String attribute = referenceParameters.getString(ReferenceParameters.attribute);
			String property = referenceParameters.getString(ReferenceParameters.property);
			
			updateReference(itemRule, bean, parameter, attribute, property);
		} else {
			if(itemRule.getType() == ItemType.SINGLE) {
				String value = itemParameters.getString(ItemParameters.value);
				parseValue(itemRule, null, value);
			} else if(itemRule.getType() == ItemType.ARRAY || itemRule.getType() == ItemType.LIST || itemRule.getType() == ItemType.SET) {
				List<String> stringList = itemParameters.getStringList(ItemParameters.value);
				
				if(stringList != null) {
					beginValueCollection(itemRule);
					for(String value : stringList) {
						Token[] tokens = parseValue(itemRule, null, value);
						flushValueCollection(itemRule, name, tokens);
					}
				}
			} else if(itemRule.getType() == ItemType.MAP || itemRule.getType() == ItemType.PROPERTIES) {
				Parameters parameters = itemParameters.getParameters(ItemParameters.value);

				if(parameters != null) {
					Set<String> parametersNames = parameters.getParameterNameSet();
					
					if(parametersNames != null) {
						beginValueCollection(itemRule);
						for(String valueName : parametersNames) {
							Token[] tokens = parseValue(itemRule, valueName, parameters.getString(valueName));
							flushValueCollection(itemRule, valueName, tokens);
						}
					}
				} 
			}
		}
		
		return itemRule;
	}
	
	public static List<Parameters> toItemParametersList(String text) {
		Parameters holder = new ItemHolderParameters(text);
		return holder.getParametersList(ItemHolderParameters.item);
	}
	
}
