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
package com.aspectran.core.activity.variable.token;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.activity.variable.ValueObjectMap;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;

/**
 * <p>Created: 2008. 06. 19 오후 9:43:28</p>
 */
public class ItemTokenExpression extends TokenExpression implements ItemTokenExpressor {

	/**
	 * Instantiates a new value expression.
	 *
	 * @param beanRegistry the bean registry
	 */
	public ItemTokenExpression(BeanRegistry beanRegistry) {
		super(beanRegistry);
	}
	
	/**
	 * Instantiates a new value expression.
	 *
	 * @param activity the activity
	 */
	public ItemTokenExpression(Activity activity) {
		super(activity);
	}
	
	/**
	 * Express.
	 * 
	 * @param itemRuleMap the value rule map
	 * 
	 * @return the value map
	 */
	public ValueObjectMap express(ItemRuleMap itemRuleMap) {
		ValueObjectMap voMap = new ValueObjectMap();
		
		express(itemRuleMap, voMap);
		
		return voMap;
	}
	
	/**
	 * Express.
	 *
	 * @param itemRuleMap the item rule map
	 * @param valueObjectMap the value map
	 */
	public void express(ItemRuleMap itemRuleMap, ValueObjectMap valueObjectMap) {
		for(ItemRule ir : itemRuleMap) {
			ItemType itemType = ir.getType();
			ItemValueType valueType = ir.getValueType();
			Object value = null;
			
			if(itemType == ItemType.SINGLE) {
				Token[] tokens = ir.getTokens();
				value = express(ir.getName(), tokens, valueType);
			} else if(itemType == ItemType.LIST) {
				value = expressAsList(ir.getName(), ir.getTokensList(), valueType);
			} else if(itemType == ItemType.SET) {
				value = expressAsSet(ir.getName(), ir.getTokensSet(), valueType);
			} else if(itemType == ItemType.MAP) {
				value = expressAsMap(ir.getName(), ir.getTokensMap(), valueType);
			} else if(itemType == ItemType.PROPERTIES) {
				value = expressAsProperties(ir.getName(), ir.getTokensMap(), valueType);
			}
			
			valueObjectMap.put(ir.getName(), value);
		}
	}
	
	private Object express(String parameterName, Token[] tokens, ItemValueType valueType) {
		Object value = express(parameterName, tokens);
		return valuelize(value, valueType);
	}
	
	/**
	 * Express as List.
	 * 
	 * @param parameterName the parameter name
	 * @param tokensList the tokens
	 * 
	 * @return the object[]
	 */
	private List<Object> expressAsList(String parameterName, List<Token[]> tokensList, ItemValueType valueType) {
		if(tokensList == null || tokensList.isEmpty())
			return getParameterAsList(parameterName, valueType);
		
		List<Object> valueList = new ArrayList<Object>(tokensList.size());

		for(Token[] tokens : tokensList) {
			Object value = express(parameterName, tokens);
			
			if(valueType != null)
				value = valuelize(value, valueType);
			
			valueList.add(value);
		}
		
		return valueList;
	}

	/**
	 * Express as Set.
	 * 
	 * @param parameterName the parameter name
	 * @param tokensList the tokens
	 * 
	 * @return the object[]
	 */
	private Set<Object> expressAsSet(String parameterName, Set<Token[]> tokensSet, ItemValueType valueType) {
		if(tokensSet == null || tokensSet.isEmpty())
			return getParameterAsSet(parameterName, valueType);
		
		Set<Object> valueSet = new HashSet<Object>(tokensSet.size());

		for(Token[] tokens : tokensSet) {
			Object value = express(parameterName, tokens);
			
			if(valueType != null)
				value = valuelize(value, valueType);
			
			valueSet.add(value);
		}
		
		return valueSet;
	}
	
	/**
	 * Express as Map.
	 * 
	 * @param parameterName the parameter name
	 * @param tokensMap the tokens map
	 * 
	 * @return the map< string, object>
	 */
	private Map<String, Object> expressAsMap(String parameterName, Map<String, Token[]> tokensMap, ItemValueType valueType) {
		if(tokensMap == null || tokensMap.isEmpty()) {
			Object value = getParameter(parameterName, null);
			
			if(value == null)
				return null;
			
			if(valueType != null)
				value = valuelize(value, valueType);
			
			Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
			valueMap.put(parameterName, value);
			return valueMap;
		}
		
		Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
		
		for(Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
			Object value = express(entry.getKey(), entry.getValue());

			if(valueType != null)
				value = valuelize(value, valueType);
			
			valueMap.put(entry.getKey(), value);
		}
		
		return valueMap;
	}
	
	/**
	 * Express as Properties.
	 * 
	 * @param parameterName the parameter name
	 * @param tokensMap the tokens map
	 * 
	 * @return the Properties
	 */
	private Properties expressAsProperties(String parameterName, Map<String, Token[]> tokensMap, ItemValueType valueType) {
		if(tokensMap == null || tokensMap.isEmpty()) {
			Object value = getParameter(parameterName, null);

			if(value == null)
				return null;

			if(valueType != null)
				value = valuelize(value, valueType);

			Properties prop = new Properties();
			prop.put(parameterName, value);
			return prop;
		}
		
		Properties prop = new Properties();

		for(Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
			Object value = express(entry.getKey(), entry.getValue());

			if(valueType != null)
				value = valuelize(value, valueType);

			prop.put(entry.getKey(), value);
		}
		
		return prop;
	}
	
	/**
	 * Gets the parameter values.
	 * 
	 * @param name the name
	 * 
	 * @return the parameter values
	 */
	private List<Object> getParameterAsList(String name, ItemValueType valueType) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;
		
		List<Object> valueList = new ArrayList<Object>(values.length);
		
		for(String value : values) {
			if(valueType != null)
				valueList.add(valuelize((Object)value, valueType));
			else
				valueList.add(value);
		}
		
		return valueList;
	}
	
	/**
	 * Gets the parameter values.
	 * 
	 * @param name the name
	 * 
	 * @return the parameter values
	 */
	private Set<Object> getParameterAsSet(String name, ItemValueType valueType) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;

		Set<Object> valueSet = new LinkedHashSet<Object>(values.length);
		
		for(String value : values) {
			if(valueType != null)
				valueSet.add(valuelize((Object)value, valueType));
			else
				valueSet.add(value);
		}

		return valueSet;
	}
	
	private Object valuelize(Object value, ItemValueType valueType) {
		if(valueType == ItemValueType.STRING) {
			if(value instanceof String)
				return value;
			else
				return value.toString();
		} else if(valueType == ItemValueType.INT) {
			if(value instanceof Integer)
				return value;
			else
				return Integer.valueOf(value.toString());
		} else if(valueType == ItemValueType.LONG) {
			if(value instanceof Long)
				return value;
			else
				return Long.valueOf(value.toString());
		} else if(valueType == ItemValueType.FLOAT)
			if(value instanceof Float)
				return value;
			else
				return Float.valueOf(value.toString());
		else if(valueType == ItemValueType.DOUBLE)
			if(value instanceof Double)
				return value;
			else
				return Double.valueOf(value.toString());
		else if(valueType == ItemValueType.FILE) {
			String filePath = value.toString();
			value = new File(filePath);
		} else if(valueType == ItemValueType.MULTIPART_FILE) {
			String filePath = value.toString();
			File file = new File(filePath);
			value = new FileParameter(file);
		}
		
		return value;
	}

//	
//	/**
//	 * Express as parameter map.
//	 * 
//	 * @param parameterRuleMap the parameter rule map
//	 * 
//	 * @return the parameter map
//	 */
//	public ParameterMap expressAsParameterMap(ParameterRuleMap parameterRuleMap) {
//		if(parameterRuleMap == null || parameterRuleMap.size() == 0)
//			return null;
//		
//		ParameterMap parameterMap = new ParameterMap();
//		
//		for(ParameterRule pr : parameterRuleMap) {
//			String name = pr.getName();
//			ParameterUnityType paramType = pr.getUnityType();
//			
//			if(paramType == ParameterUnityType.UNITY) {
//				Token[] tokens = pr.getTokens();
//				String value = null;
//
//				if(tokens == null || tokens.length == 0) {
//					value = expressAsString(pr.getName(), null);
//				} else if(tokens.length == 1) {
//					Object obj = expressAsObject(pr.getName(), tokens[0]);
//
//					if(obj != null)
//						value = obj.toString();
//				} else {
//					value = expressAsString(pr.getName(), tokens);
//				}
//
//				parameterMap.put(name, value);
//			} else if(paramType == ParameterUnityType.ARRAY) {
//				Object[] value = expressAsArray(pr.getName(), pr.getTokensArray());
//				parameterMap.put(name, value);
//			} else if(paramType == ParameterUnityType.MAP) {
//				Object value = expressAsMap(pr.getName(), pr.getTokensMap());
//				parameterMap.put(name, value.toString());
//			}
//		}
//		
//		return parameterMap;
//	}
}
