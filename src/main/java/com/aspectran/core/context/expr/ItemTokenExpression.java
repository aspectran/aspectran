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
public class ItemTokenExpression extends TokenExpression implements ItemTokenEvaluator {

	/**
	 * Instantiates a new ItemTokenExpression.
	 *
	 * @param activity the current Activity
	 */
	public ItemTokenExpression(Activity activity) {
		super(activity);
	}
	
	@Override
	public Map<String, Object> evaluate(ItemRuleMap itemRuleMap) {
		Map<String, Object> valueMap = new LinkedHashMap<>();
		evaluate(itemRuleMap, valueMap);
		return valueMap;
	}

	@Override
	public void evaluate(ItemRuleMap itemRuleMap, Map<String, Object> valueMap) {
		for(ItemRule ir : itemRuleMap.values()) {
			ItemType itemType = ir.getType();
			ItemValueType valueType = ir.getValueType();
			String name = ir.getName();
			Object value = null;
			
			if(itemType == ItemType.SINGULAR) {
				Token[] tokens = ir.getTokens();
				value = evaluate(name, tokens, valueType);
			} else if(itemType == ItemType.ARRAY) {
				value = evaluateAsArray(name, ir.getTokensList(), valueType);
			} else if(itemType == ItemType.LIST) {
				value = evaluateAsList(name, ir.getTokensList(), valueType);
			} else if(itemType == ItemType.SET) {
				value = evaluateAsSet(name, ir.getTokensList(), valueType);
			} else if(itemType == ItemType.MAP) {
				value = evaluateAsMap(name, ir.getTokensMap(), valueType);
			} else if(itemType == ItemType.PROPERTIES) {
				value = evaluateAsProperties(name, ir.getTokensMap(), valueType);
			}
			
			valueMap.put(name, value);
		}
	}
	
	private Object evaluate(String parameterName, Token[] tokens, ItemValueType valueType) {
		Object value = evaluate(parameterName, tokens);
		
		if(value == null || valueType == null)
			return value;
		
		return valuelize(value, valueType);
	}
	
	@SuppressWarnings("all")
	private Object[] evaluateAsArray(String parameterName, List<Token[]> tokensList, ItemValueType valueType) {
		List<Object> list = evaluateAsList(parameterName, tokensList, valueType);
		
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
	
	private List<Object> evaluateAsList(String parameterName, List<Token[]> tokensList, ItemValueType valueType) {
		if(tokensList == null || tokensList.isEmpty())
			return getParameterAsList(parameterName, valueType);
		
		List<Object> valueList = new ArrayList<>(tokensList.size());
		
		for(Token[] tokens : tokensList) {
			Object value = evaluate(parameterName, tokens);
			
			if(value != null && valueType != null)
				value = valuelize(value, valueType);
			
			valueList.add(value);
		}
		
		return valueList;
	}

	private Set<Object> evaluateAsSet(String parameterName, List<Token[]> tokensList, ItemValueType valueType) {
		if(tokensList == null || tokensList.isEmpty())
			return getParameterAsSet(parameterName, valueType);
		
		Set<Object> valueSet = new HashSet<>(tokensList.size());

		for(Token[] tokens : tokensList) {
			Object value = evaluate(parameterName, tokens);
			
			if(value != null && valueType != null)
				value = valuelize(value, valueType);
			
			valueSet.add(value);
		}
		
		return valueSet;
	}
	
	private Map<String, Object> evaluateAsMap(String parameterName, Map<String, Token[]> tokensMap, ItemValueType valueType) {
		if(tokensMap == null || tokensMap.isEmpty()) {
			Object value = getParameter(parameterName, valueType);
			
			if(value == null)
				return null;
			
			if(valueType != null)
				value = valuelize(value, valueType);
			
			Map<String, Object> valueMap = new LinkedHashMap<>();
			valueMap.put(parameterName, value);
			return valueMap;
		}
		
		Map<String, Object> valueMap = new LinkedHashMap<>();

		for(Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
			Object value = evaluate(entry.getKey(), entry.getValue());

			if(value != null && valueType != null)
				value = valuelize(value, valueType);
			
			valueMap.put(entry.getKey(), value);
		}
		
		return valueMap;
	}
	
	private Properties evaluateAsProperties(String parameterName, Map<String, Token[]> tokensMap, ItemValueType valueType) {
		if(tokensMap == null || tokensMap.isEmpty()) {
			Object value = getParameter(parameterName, valueType);

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
			Object value = evaluate(entry.getKey(), entry.getValue());

			if(value != null && valueType != null)
				value = valuelize(value, valueType);

			if(value != null)
				prop.put(entry.getKey(), value);
		}
		
		return prop;
	}

	private Object getParameter(String name, ItemValueType valueType) {
		if(valueType == ItemValueType.MULTIPART_FILE || valueType == ItemValueType.FILE) {
			return super.getFileParameter(name);
		} else {
			return super.getParameter(name);
		}
	}

	private Object[] getParameterValues(String name, ItemValueType valueType) {
		if(valueType == ItemValueType.MULTIPART_FILE || valueType == ItemValueType.FILE) {
			return super.getFileParameterValues(name);
		} else {
			return super.getParameterValues(name);
		}
	}

	private List<Object> getParameterAsList(String name, ItemValueType valueType) {
		Object[] values = getParameterValues(name, valueType);

		if(values == null)
			return null;
		
		List<Object> valueList = new ArrayList<>(values.length);
		
		for(Object value : values) {
			if(value != null && valueType != null)
				valueList.add(valuelize(value, valueType));
			else
				valueList.add(value);
		}
		
		return valueList;
	}
	
	private Set<Object> getParameterAsSet(String name, ItemValueType valueType) {
		Object[] values = getParameterValues(name, valueType);

		if(values == null)
			return null;

		Set<Object> valueSet = new LinkedHashSet<>(values.length);
		
		for(Object value : values) {
			if(value != null && valueType != null)
				valueSet.add(valuelize(value, valueType));
			else
				valueSet.add(value);
		}

		return valueSet;
	}
	
	private Object valuelize(Object value, ItemValueType valueType) {
		if(valueType == ItemValueType.STRING) {
			return (value instanceof String) ? value : value.toString();
		} else if(valueType == ItemValueType.INT) {
			return (value instanceof Integer) ? value : Integer.valueOf(value.toString());
		} else if(valueType == ItemValueType.LONG) {
			return (value instanceof Long) ? value : Long.valueOf(value.toString());
		} else if(valueType == ItemValueType.FLOAT) {
			return (value instanceof Float) ? value : Float.valueOf(value.toString());
		} else if(valueType == ItemValueType.DOUBLE) {
			return (value instanceof Double) ? value : Double.valueOf(value.toString());
		} else if(valueType == ItemValueType.BOOLEAN) {
			return (value instanceof Boolean) ? value : Boolean.valueOf(value.toString());
		} else if(valueType == ItemValueType.PARAMETERS) {
			return new GenericParameters(value.toString());
		} else if(valueType == ItemValueType.FILE) {
			return (value instanceof FileParameter) ? value : new File(value.toString());
		} else if(valueType == ItemValueType.MULTIPART_FILE) {
			return (value instanceof FileParameter) ? value : new FileParameter(new File(value.toString()));
		} else {
			return value;
		}
	}

}
