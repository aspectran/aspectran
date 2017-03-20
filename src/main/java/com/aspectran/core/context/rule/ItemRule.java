/**
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
package com.aspectran.core.context.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.context.builder.apon.params.CallParameters;
import com.aspectran.core.context.builder.apon.params.ItemHolderParameters;
import com.aspectran.core.context.builder.apon.params.ItemParameters;
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
 */
public class ItemRule {

	/**  suffix for array-type item: "[]". */
	private static final String ARRAY_SUFFIX = "[]";
	
	/**  suffix for map-type item: "{}". */
	private static final String MAP_SUFFIX = "{}";

	private ItemType type;
	
	private String name;
	
	private ItemValueType valueType;
	
	private String defaultValue;

	private Boolean tokenize;

	private Token[] tokens;
	
	private List<Token[]> tokensList;
	
	private Map<String, Token[]> tokensMap;

	private Boolean mandatory;

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
	 * Sets the name of a item.
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
		return TokenParser.toString(tokens);
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
				list.add(TokenParser.toString(tokens));
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
			Map<String, String> map = new LinkedHashMap<>(tokensMap.size());
			for (Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
				map.put(entry.getKey(), TokenParser.toString(entry.getValue()));
			}
			return map;
		}
	}

	/**
	 * Sets the value.
	 *
	 * @param tokens the new value
	 */
	public void setValue(Token[] tokens) {
		if (type == null) {
			throw new IllegalArgumentException("The item type must be specified first.");
		}
		if (type != ItemType.SINGLE) {
			throw new IllegalArgumentException("The item type must be 'single': " + this);
		}
		this.tokens = tokens;
	}

	/**
	 * Sets the value.
	 *
	 * @param tokensList the new value
	 */
	public void setValue(List<Token[]> tokensList) {
		if (type == null) {
			throw new IllegalArgumentException("The item type must be specified first.");
		}
		if (!isListableType()) {
			throw new IllegalArgumentException("The item type must be 'array', 'list' or 'set' for this item: " + this);
		}
		this.tokensList = tokensList;
	}

	/**
	 * Sets the value.
	 *
	 * @param tokensMap the tokens map
	 */
	public void setValue(Map<String, Token[]> tokensMap) {
		if (type == null) {
			throw new IllegalArgumentException("The item type must be specified first.");
		}
		if (!isMappableType()) {
			throw new IllegalArgumentException("The item type must be 'map' or 'properties' for this item: " + this);
		}
		this.tokensMap = tokensMap;
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
	 * Gets the default value of this item.
	 *
	 * @return the default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets the default value of this item.
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

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder();
		tsb.append("type", type);
		tsb.append("name", name);
		tsb.append("valueType", valueType);
		if (type == ItemType.SINGLE) {
			tsb.append("value", tokens);
		} else if (isListableType()) {
			tsb.append("value", tokensList);
		} else if (isMappableType()) {
			tsb.append("value", tokensMap);
		}
		tsb.append("tokenize", tokenize);
		tsb.append("mandatory", mandatory);
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
			}
		}
		
		return (value != null ? value.getClass() : Object.class);
	}
	
	/**
	 * Returns a new derived instance of ItemRule.
	 *
	 * @param type the type
	 * @param name the name
	 * @param valueType the value type
	 * @param defaultValue the default value
	 * @param tokenize whether to tokenize
	 * @param mandatory whether or not the item is mandatory
	 * @return the item rule
	 */
	public static ItemRule newInstance(String type, String name, String valueType, String defaultValue, Boolean tokenize, Boolean mandatory) {
		ItemRule itemRule = new ItemRule();
		
		ItemType itemType = ItemType.resolve(type);
		if (type != null && itemType == null) {
			throw new IllegalArgumentException("No item type for '" + type + "'.");
		}
		if (itemType != null) {
			itemRule.setType(itemType);
		} else {
			itemRule.setType(ItemType.SINGLE); //default
		}

		if (!StringUtils.isEmpty(name)) {
			itemRule.setName(name);
		} else {
			itemRule.setAutoGeneratedName(true);
		}

		if (tokenize != null) {
			itemRule.setTokenize(tokenize);
		}

		ItemValueType itemValueType = ItemValueType.resolve(valueType);
		if (valueType != null && itemValueType == null) {
			throw new IllegalArgumentException("No item value type for '" + valueType + "'.");
		}
		itemRule.setValueType(itemValueType);

		if (defaultValue != null) {
			itemRule.setDefaultValue(defaultValue);
		}

		if (mandatory != null) {
			itemRule.setMandatory(mandatory);
		}

		return itemRule;
	}
	
	/**
	 * Returns a made reference token.
	 *
	 * @param bean the bean id
	 * @param template the template id
	 * @param parameter the parameter name
	 * @param attribute the attribute name
	 * @param property the property name
	 * @return the token
	 */
	public static Token makeReferenceToken(String bean, String template, String parameter, String attribute, String property) {
		Token token;

		if (bean != null) {
			token = new Token(TokenType.BEAN, bean);
		} else if (template != null) {
			token = new Token(TokenType.TEMPLATE, template);
		} else if (parameter != null) {
			token = new Token(TokenType.PARAMETER, parameter);
		} else if (attribute != null) {
			token = new Token(TokenType.ATTRIBUTE, attribute);
		} else if (property != null) {
			token = new Token(TokenType.PROPERTY, property);
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
		
		if (itemRule.isListableType()) {
			List<Token[]> list = itemRule.getTokensList();
			if (list != null) {
				iter = list.iterator();
			}
		} else if (itemRule.isMappableType()) {
			Map<String, Token[]> map = itemRule.getTokensMap();
			if (map != null) {
				iter = map.values().iterator();
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
					throw new UnsupportedOperationException("Cannot remove an element of an array.");
				}
			};
		}
		
		return iter;
	}

	/**
	 * Analyze the raw values and assign them to the values of the items.
	 *
	 * @param itemRule the item rule
	 * @param text the raw value of the item
	 */
	public static void setValue(ItemRule itemRule, String text) {
		Token[] tokens = TokenParser.makeTokens(text, itemRule.isTokenize());
		itemRule.setValue(tokens);
	}

	/**
	 * Analyze the raw values and assign them to the values of the items.
	 *
	 * @param itemRule the item rule
	 * @param name the value name; may be null
	 * @param text the raw value of the item
	 */
	public static void addValue(ItemRule itemRule, String name, String text) {
		Token[] tokens = TokenParser.makeTokens(text, itemRule.isTokenize());
		addValue(itemRule, name, tokens);
	}

	/**
	 * Analyze the raw values and assign them to the values of the items.
	 *
	 * @param itemRule the item rule
	 * @param name the value name; may be null
	 * @param tokens an array of tokens
	 */
	public static void addValue(ItemRule itemRule, String name, Token[] tokens) {
		if (itemRule.isListableType()) {
			List<Token[]> tokensList = itemRule.getTokensList();
			if (tokensList == null) {
				tokensList = new ArrayList<>();
				itemRule.setValue(tokensList);
			}
			tokensList.add(tokens);
		} else if (!StringUtils.isEmpty(name) && itemRule.isMappableType()) {
			Map<String, Token[]> tokensMap = itemRule.getTokensMap();
			if (tokensMap == null) {
				tokensMap = new LinkedHashMap<>();
				itemRule.setValue(tokensMap);
			}
			tokensMap.put(name, tokens);
		} else {
			itemRule.setValue(tokens);
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
		if (itemRule.isAutoGeneratedName()) {
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
		for (ItemRule ir : itemRuleMap.values()) {
			if (ir.isAutoGeneratedName() && ir.getType() == itemRule.getType()) {
				count++;
				if (itemRule == ir) {
					break;
				}
			}
		}
		if (itemRule.getType() != ItemType.SINGLE || itemRule.getValueType() == null) {
			String name = itemRule.getType().toString() + count;
			itemRule.setName(name);
		} else {
			if (itemRule.getValueType() != null) {
				String name = itemRule.getValueType().toString() + count;
				itemRule.setName(name);
			}
		}
	}

	/**
	 * Convert the given item parameters list into an {@code ItemRuleMap}.
	 *
	 * @param itemParametersList the item parameters list to convert
	 * @return the item rule map
	 */
	public static ItemRuleMap toItemRuleMap(List<Parameters> itemParametersList) {
		if (itemParametersList == null || itemParametersList.isEmpty()) {
			return null;
		}
		
		ItemRuleMap itemRuleMap = new ItemRuleMap();
		
		for (Parameters parameters : itemParametersList) {
			ItemRule itemRule = toItemRule(parameters);

			// auto-naming if did not specify the name of the item.
			if (StringUtils.isEmpty(itemRule.getName())) {
				itemRule.setAutoGeneratedName(true);
				generateItemName(itemRule, itemRuleMap);
			}

			itemRuleMap.putItemRule(itemRule);
		}
		
		return itemRuleMap;
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
	 *     defaultValue: "default value"
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
	 * @return an {@code ItemRule}
	 */
	public static ItemRule toItemRule(Parameters itemParameters) {
		String type = itemParameters.getString(ItemParameters.type);
		String name = itemParameters.getString(ItemParameters.name);
		String valueType = itemParameters.getString(ItemParameters.valueType);
		String defaultValue = itemParameters.getString(ItemParameters.defaultValue);
		Boolean tokenize = itemParameters.getBoolean(ItemParameters.tokenize);
		Boolean mandatory = itemParameters.getBoolean(ItemParameters.mandatory);
		Parameters callParameters = itemParameters.getParameters(ItemParameters.call);
		
		ItemRule itemRule = ItemRule.newInstance(type, name, valueType, defaultValue, tokenize, mandatory);
		
		if (callParameters != null) {
			String bean = StringUtils.emptyToNull(callParameters.getString(CallParameters.bean));
			String template = StringUtils.emptyToNull(callParameters.getString(CallParameters.template));
			String parameter = StringUtils.emptyToNull(callParameters.getString(CallParameters.parameter));
			String attribute = StringUtils.emptyToNull(callParameters.getString(CallParameters.attribute));
			String property = StringUtils.emptyToNull(callParameters.getString(CallParameters.property));

			Token t = ItemRule.makeReferenceToken(bean, template, parameter, attribute, property);
			if (t != null) {
				Token[] tokens = new Token[] {t};
				ItemRule.addValue(itemRule, null, tokens);
			}
		} else {
			if (itemRule.getType() == ItemType.SINGLE) {
				String text = itemParameters.getString(ItemParameters.value);
				setValue(itemRule, text);
			} else if (itemRule.isListableType()) {
				List<String> stringList = itemParameters.getStringList(ItemParameters.value);
				if (stringList != null) {
					for (String text : stringList) {
						addValue(itemRule, null, text);
					}
				}
			} else if (itemRule.isMappableType()) {
				Parameters parameters = itemParameters.getParameters(ItemParameters.value);
				if (parameters != null) {
					Set<String> parametersNames = parameters.getParameterNameSet();
					if (parametersNames != null) {
						for (String valueName : parametersNames) {
							String text = parameters.getString(valueName);
							addValue(itemRule, valueName, text);
						}
					}
				} 
			}
		}
		
		return itemRule;
	}
	
	/**
	 * Convert the given {@code String} into an Item {@code Parameters}.
	 *
	 * @param text the {@code String} to convert
	 * @return the item parameters list
	 */
	public static List<Parameters> toItemParametersList(String text) {
		Parameters holder = new ItemHolderParameters(text);
		return holder.getParametersList(ItemHolderParameters.item);
	}

	/**
	 * Convert the given {@code String} into an {@code ItemRuleMap}.
	 *
	 * @param text the {@code String} to convert
	 * @return an {@code ItemRuleMap}
	 */
	public static ItemRuleMap toItemRuleMap(String text) {
		Parameters holder = new ItemHolderParameters(text);
		List<Parameters> parametersList = holder.getParametersList(ItemHolderParameters.item);
		return toItemRuleMap(parametersList);
	}
	
}
