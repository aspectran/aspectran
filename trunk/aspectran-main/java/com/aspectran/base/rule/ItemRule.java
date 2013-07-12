/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.base.rule;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.aspectran.base.token.Token;
import com.aspectran.base.token.TokenParser;
import com.aspectran.base.type.ItemType;
import com.aspectran.base.type.ItemValueType;

/**
 * <p>Created: 2008. 03. 27 오후 3:57:48</p>
 */
public class ItemRule {

	/** suffix for list-type item: "[]" */
	public static final String LIST_SUFFIX = "[]";
	
	/** suffix for map-type item: "{}" */
	public static final String MAP_SUFFIX = "{}";

	private String name;
	
	private ItemType type;

	private ItemValueType valueType;
	
	private String defaultValue;
	
	private Token[] tokens;
	
	private List<Token[]> tokensList;
	
	private Map<String, Token[]> tokensMap;
	
	private Set<Token[]> tokensSet;
	
	private Properties tokensProperties;
	
	private boolean unknownName;

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
		if(tokens == null)
			return null;
		
		StringBuilder sb = new StringBuilder();
		
		for(Token t : tokens) {
			sb.append(t.toString());
		}
		
		return sb.toString();
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
	 * Sets the tokens list.
	 *
	 * @param tokensList the new tokens list
	 */
	public void setTokensList(List<Token[]> tokensList) {
		this.tokensList = tokensList;
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
	 * Gets the tokens map.
	 * 
	 * @return the tokens map
	 */
	public Map<String, Token[]> getTokensMap() {
		return tokensMap;
	}
	
	/**
	 * Gets the tokens set.
	 *
	 * @return the tokens set
	 */
	public Set<Token[]> getTokensSet() {
		return tokensSet;
	}
	
	/**
	 * Gets the tokens properties.
	 *
	 * @return the tokens properties
	 */
	public Properties getTokensProperties() {
		return tokensProperties;
	}
	
	public boolean isUnknownName() {
		return unknownName;
	}

	public void setUnknownName(boolean unknownName) {
		this.unknownName = unknownName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{type=").append(type.toString());
		sb.append(", name=").append(name);
		sb.append(", value=");
		
		if(type == ItemType.ITEM) {
			if(tokens != null) {
				for(Token t : tokens) {
					sb.append(t.toString());
				}
			}
		} else if(type == ItemType.LIST) {
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
		} else if(type == ItemType.MAP) {
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
		} else if(type == ItemType.SET) {
			if(tokensSet != null) {
				sb.append('{');
				
				Iterator<Token[]> iter = tokensSet.iterator();
				
				while(iter.hasNext()) {
					if(sb.length() > 1)
						sb.append(", ");
					
					Token[] ts = iter.next();
					
					for(Token t : ts) {
						sb.append(t.toString());
					}
				}
				
				sb.append('}');
			}
		} else if(type == ItemType.PROPERTIES) {
			if(tokensProperties != null) {
				sb.append('{');
				
				Iterator<Object> iter = tokensProperties.keySet().iterator();
				String key = null;
				
				while(iter.hasNext()) {
					if(key != null)
						sb.append(", ");

					key = (String)iter.next();
					Token[] ts = (Token[])tokensProperties.get(key);
					
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
		if(name.endsWith(LIST_SUFFIX)) {
			this.name = name.substring(0, name.length() - 2);
			type = ItemType.LIST;
		} else if(name.endsWith(MAP_SUFFIX)) {
			this.name = name.substring(0, name.length() - 2);
			type = ItemType.MAP;
		} else {
			this.name = name;
			
			if(type == null)
				type = ItemType.ITEM;
		}
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(String value) {
		checkValueType(null);
		
		if(type == ItemType.ITEM)
			tokens = TokenParser.parse(value);
		else if(type == ItemType.LIST)
			tokensList = TokenParser.parseAsList(value);
		else if(type == ItemType.MAP)
			tokensMap = TokenParser.parseAsMap(value);
	}
	
//	/**
//	 * Sets the value of a item.
//	 * Before setting a value of item, must input a item-name first.
//	 * 
//	 * @param value the value to set
//	 */
//	public void setValue(String value) {
//		checkUnityType(null);
//		
//		if(value == null) {
//			tokens = null;
//			return;
//		}
//
//		// When the parameter type is only and each token trim
//		boolean trimStringToken = (unityType != ItemUnityType.SINGLE);
//		
//		List<Token> tokenList = Tokenizer.tokenize(value, trimStringToken);
//		
//		if(unityType == ItemUnityType.SINGLE) {
//			if(tokenList.size() > 0) {
//				tokens = tokenList.toArray(new Token[tokenList.size()]);
//				
//				if(!trimStringToken) {
//					// 문자열 타입 토큰을 trim하지 않았으면,
//					// 처음과 끝에 위치한 토큰을 각각 앞trim, 뒷trim을 한다.
//					// 영어도 어렵고, 한글도 어렵기는 마찬가지... ㅡ.ㅡ;
//					tokens = Tokenizer.optimizeTokens(tokens);
//				}
//			} else {
//				tokens = null;
//			}
//			
//			tokensArray = null;
//			tokensMap = null;
//		} else
//		// if Parameter type is MAP then remove string type token and blank name token 
//		if(unityType == ItemUnityType.MAP) {
//			for(int i = tokenList.size() - 1; i >= 0; i--) {
//				Token t = tokenList.get(i);
//
//				if(t.getType() == TokenType.TEXT ||
//						t.getName() == null || t.getName().length() == 0 ||
//						t.getText() == null || t.getText().length() == 0) {
//					tokenList.remove(i);
//				}
//			}
//
//			if(tokenList.size() > 0) {
//				tokensMap = new LinkedHashMap<String, Token[]>();
//				
//				for(Token t : tokenList) {
//					Token[] ts = new Token[1];
//					ts[0] = t;
//					tokensMap.put(t.getName(), ts);
//				}
//			} else {
//				tokensMap = null;
//			}
//			
//			tokens = null;
//			tokensArray = null;
//		} else
//		// if Parameter type is ARRAY then trim all string type tokens
//		if(unityType == ItemUnityType.LIST) {
//			for(int i = tokenList.size() - 1; i >= 0; i--) {
//				Token t = tokenList.get(i);
//
//				if(t.getType() == TokenType.TEXT && t.getText() != null) {
//					// remove empty token
//					if(t.getText().trim().length() == 0)
//						tokenList.remove(i);
//				}
//			}
//			
//			if(tokenList.size() > 0) {
//				tokensArray = new Token[tokenList.size()][];
//				
//				for(int i = 0; i < tokensArray.length; i++) {
//					tokensArray[i] = new Token[1];
//					tokensArray[i][0] = tokenList.get(i);
//				}
//			} else {
//				tokensArray = null;
//			}
//
//			tokens = null;
//			tokensMap = null;
//		}
//	}
	
//	/**
//	 * Sets the value of a parameter.
//	 * 
//	 * @param values the new value
//	 */
//	public void setValue(String[] values) {
//		checkUnityType(ItemUnityType.LIST);
//		
//		tokens = null;
//		tokensMap = null;
//
//		if(name.endsWith(ARRAY_SUFFIX) || name.endsWith(MAP_SUFFIX)) {
//			name = name.substring(0, name.length() - 2);
//		}
//
//		if(values == null) {
//			tokensArray = null;
//			return;
//		}
//		
//		tokensArray = new Token[values.length][];
//		
//		for(int i = 0; i < values.length; i++) {
//			if(values[i] == null) {
//				tokensArray[i] = null;
//			} else {
//				List<Token> tokenList = Tokenizer.tokenize(values[i], false);
//				tokensArray[i] = tokenList.toArray(new Token[tokenList.size()]);
//			}
//		}
//	}
//
//	/**
//	 * Sets the value of a parameter.
//	 * 
//	 * @param valueMap the value map
//	 */
//	public void setValue(Map<String, String> valueMap) {
//		checkUnityType(ItemUnityType.MAP);
//		
//		tokens = null;
//		tokensArray = null;
//		
//		if(name.endsWith(ARRAY_SUFFIX) || name.endsWith(MAP_SUFFIX)) {
//			name = name.substring(0, name.length() - 2);
//		}
//		
//		if(valueMap == null) {
//			tokensMap = null;
//			return;
//		}
//
//		tokensMap = new LinkedHashMap<String, Token[]>();
//		
//		for(Map.Entry<String, String> entry : valueMap.entrySet()) {
//			String key = entry.getKey();
//			String value = entry.getValue();
//			
//			if(value == null) {
//				tokensMap.put(key, (Token[])null);
//			} else {
//				List<Token> tokenList = Tokenizer.tokenize(value, false);
//				tokensMap.put(key, tokenList.toArray(new Token[tokenList.size()]));
//			}
//		}
//	}

//	public void setValue(Token[] tokens) {
//		checkUnityType(null);
//
//		if(unityType == ItemUnityType.SINGLE) {
//			this.tokens = tokens;
//		} else if(unityType == ItemUnityType.LIST) {
//			this.tokensArray = new Token[tokens.length][];
//			
//			for(int i = 0; i < this.tokensArray.length; i++) {
//				this.tokensArray[i] = new Token[1];
//				this.tokensArray[i][0] = tokens[i];
//			}
//		}
//	}

	/**
	 * Sets the value.
	 *
	 * @param tokens the new value
	 */
	public void setValue(Token[] tokens) {
			checkValueType(ItemType.ITEM);
			this.tokens = tokens;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensList the new value
	 */
	public void setValue(List<Token[]> tokensList) {
		checkValueType(ItemType.LIST);
		this.tokensList = tokensList;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensMap the tokens map
	 */
	public void setValue(Map<String, Token[]> tokensMap) {
		checkValueType(ItemType.MAP);
		this.tokensMap = tokensMap;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensSet the new value
	 */
	public void setValue(Set<Token[]> tokensSet) {
		checkValueType(ItemType.SET);
		this.tokensSet = tokensSet;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param tokensProperties the new value
	 */
	public void setValue(Properties tokensProperties) {
		checkValueType(ItemType.PROPERTIES);
		this.tokensProperties = tokensProperties;
	}
	
	/**
	 * Check value type.
	 *
	 * @param compareItemType the compare item type
	 */
	private void checkValueType(ItemType compareItemType) {
		if(type == null)
			throw new UnsupportedOperationException("No item-type specified. Set the item-type first.");
		
		if(compareItemType != null) {
			if(type != compareItemType)
				throw new UnsupportedOperationException("The item-type of violation has occurred. current item-type: " + type.toString());
		}	
	}
}
