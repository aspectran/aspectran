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
package com.aspectran.core.context.expr;

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
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.util.apon.GenericParameters;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class ItemTokenExpression.
 *
 * @since 2008. 06. 19
 */
public class ItemExpression extends TokenExpression implements ItemExpressor {

	/**
	 * Instantiates a new ItemTokenExpression.
	 *
	 * @param activity the current Activity
	 */
	public ItemExpression(Activity activity) {
		super(activity);
	}
	
	@Override
	public Map<String, Object> express(ItemRuleMap itemRuleMap) {
		Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
		express(itemRuleMap, valueMap);
		return valueMap;
	}

	@Override
	public void express(ItemRuleMap itemRuleMap, Map<String, Object> valueMap) {
		for(ItemRule ir : itemRuleMap) {
			ItemType itemType = ir.getType();
			ItemValueType valueType = ir.getValueType();
			String name = ir.getName();
			Object value = null;
			
			if(itemType == ItemType.SINGULAR) {
				Token[] tokens = ir.getTokens();
				value = express(name, tokens, valueType);
			} else if(itemType == ItemType.ARRAY) {
				value = expressAsArray(name, ir.getTokensList(), valueType);
			} else if(itemType == ItemType.LIST) {
				value = expressAsList(name, ir.getTokensList(), valueType);
			} else if(itemType == ItemType.SET) {
				value = expressAsSet(name, ir.getTokensList(), valueType);
			} else if(itemType == ItemType.MAP) {
				value = expressAsMap(name, ir.getTokensMap(), valueType);
			} else if(itemType == ItemType.PROPERTIES) {
				value = expressAsProperties(name, ir.getTokensMap(), valueType);
			}
			
			valueMap.put(name, value);
		}
	}
	
	private Object express(String parameterName, Token[] tokens, ItemValueType valueType) {
		Object value = express(parameterName, tokens);
		
		if(value == null || valueType == null)
			return value;
		
		return valuelize(value, valueType);
	}
	
	/**
	 * Express as Array.
	 *
	 * @param parameterName the parameter name
	 * @param tokensList the tokens
	 * @param valueType the value type
	 * @return the object[]
	 */
	private Object expressAsArray(String parameterName, List<Token[]> tokensList, ItemValueType valueType) {
		List<Object> list = expressAsList(parameterName, tokensList, valueType);
		
		if(valueType == ItemValueType.STRING) {
			return list.toArray(new String[list.size()]);
		} else if(valueType == ItemValueType.INT) {
			return list.toArray(new Integer[list.size()]);
		} else if(valueType == ItemValueType.LONG) {
			return list.toArray(new Long[list.size()]);
		} else if(valueType == ItemValueType.FLOAT) {
			return list.toArray(new Float[list.size()]);
		} else if(valueType == ItemValueType.DOUBLE) {
			return list.toArray(new Double[list.size()]);
		} else if(valueType == ItemValueType.BOOLEAN) {
			return list.toArray(new Boolean[list.size()]);
		} else if(valueType == ItemValueType.PARAMETERS) {
			return list.toArray(new Parameters[list.size()]);
		} else if(valueType == ItemValueType.FILE) {
			return list.toArray(new File[list.size()]);
		} else if(valueType == ItemValueType.MULTIPART_FILE) {
			return list.toArray(new FileParameter[list.size()]);
		} else {
			return list.toArray(new Object[list.size()]);
		}
	}
	
	/**
	 * Express as List.
	 *
	 * @param parameterName the parameter name
	 * @param tokensList the tokens
	 * @param valueType the value type
	 * @return the object[]
	 */
	private List<Object> expressAsList(String parameterName, List<Token[]> tokensList, ItemValueType valueType) {
		if(tokensList == null || tokensList.isEmpty())
			return getParameterAsList(parameterName, valueType);
		
		List<Object> valueList = new ArrayList<Object>(tokensList.size());
		
		for(Token[] tokens : tokensList) {
			Object value = express(parameterName, tokens);
			
			if(value != null && valueType != null)
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
	 * @param valueType the value type
	 * @return the object[]
	 */
	private Set<Object> expressAsSet(String parameterName, List<Token[]> tokensList, ItemValueType valueType) {
		if(tokensList == null || tokensList.isEmpty())
			return getParameterAsSet(parameterName, valueType);
		
		Set<Object> valueSet = new HashSet<Object>(tokensList.size());

		for(Token[] tokens : tokensList) {
			Object value = express(parameterName, tokens);
			
			if(value != null && valueType != null)
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
	 * @param valueType the value type
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

			if(value != null && valueType != null)
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
	 * @param valueType the value type
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

			if(value != null && valueType != null)
				value = valuelize(value, valueType);

			prop.put(entry.getKey(), value);
		}
		
		return prop;
	}
	
	/**
	 * Gets the parameter values.
	 *
	 * @param name the name
	 * @param valueType the value type
	 * @return the parameter values
	 */
	private List<Object> getParameterAsList(String name, ItemValueType valueType) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;
		
		List<Object> valueList = new ArrayList<Object>(values.length);
		
		for(String value : values) {
			if(value != null && valueType != null)
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
	 * @param valueType the value type
	 * @return the parameter values
	 */
	private Set<Object> getParameterAsSet(String name, ItemValueType valueType) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;

		Set<Object> valueSet = new LinkedHashSet<Object>(values.length);
		
		for(String value : values) {
			if(value != null && valueType != null)
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
		} else if(valueType == ItemValueType.FLOAT) {
			if(value instanceof Float)
				return value;
			else
				return Float.valueOf(value.toString());
		} else if(valueType == ItemValueType.DOUBLE) {
			if(value instanceof Double)
				return value;
			else
				return Double.valueOf(value.toString());
		} else if(valueType == ItemValueType.BOOLEAN) {
			if(value instanceof Boolean)
				return value;
			else
				return Boolean.valueOf(value.toString());
		} else if(valueType == ItemValueType.PARAMETERS) {
			String text = value.toString();
			value = new GenericParameters(text);
		} else if(valueType == ItemValueType.FILE) {
			String filePath = value.toString();
			value = new File(filePath);
		} else if(valueType == ItemValueType.MULTIPART_FILE) {
			String filePath = value.toString();
			File file = new File(filePath);
			value = new FileParameter(file);
		}
		
		return value;
	}

}
