/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.activity.variable.token;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.bean.BeanRegistry;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.StringUtils;

/**
 * <p>Created: 2008. 03. 29 오전 12:59:16</p>
 */
public class TokenExpression implements TokenExpressor {
	
	protected Activity activity;
	
	protected RequestAdapter requestAdapter;
	
	protected BeanRegistry beanRegistry;
	
	//protected TokenValueHandler tokenValueHandler;
	
	public TokenExpression(Activity activity) {
		this.activity = activity;
		
		if(activity != null) {
			this.requestAdapter = activity.getRequestAdapter();
			this.beanRegistry = activity.getBeanRegistry();
		}
	}

	/**
	 * Sets the token value handler.
	 * 
	 * @param tokenValueHandler the new token value handler
	 */
	/*
	public void setTokenValueHandler(TokenValueHandler tokenValueHandler) {
		this.tokenValueHandler = tokenValueHandler;
	}
	*/

	/**
	 * Express.
	 * 
	 * @param token the token
	 * 
	 * @return the object
	 */
	public Object express(Token token) {
		TokenType tokenType = token.getType();
		Object value = null;

		if(tokenType == TokenType.TEXT) {
			value = token.getDefaultValue();
		} else	if(tokenType == TokenType.PARAMETER) {
			value = getParameter(token.getName(), token.getDefaultValue());
		} else	if(tokenType == TokenType.ATTRIBUTE) {
			value = getAttribute(token);
		} else	if(tokenType == TokenType.REFERENCE_BEAN) {
			value = referenceBean(token);
		}

//		if(value != null && tokenValueHandler != null)
//			return tokenValueHandler.handle(tokenType, value);
		
		return value;
	}

	/**
	 * Express as String.
	 * 
	 * @param tokens the tokens
	 * 
	 * @return the string
	 */
	public Object express(Token[] tokens) {
		if(tokens == null || tokens.length == 0)
			return null;

		if(tokens.length > 1) {
			StringBuilder sb = new StringBuilder();
			
			for(Token t : tokens) {
				Object value = express(t);
				
				if(value != null)
					sb.append(value.toString());
			}
			
			return sb.toString();
		} else {
			return express(tokens[0]);
		}
	}

	public String expressAsString(Token[] tokens) {
		Object value = express(tokens);
		
		if(value == null)
			return null;
		
		return value.toString();
	}

	/**
	 * Express as String.
	 * 
	 * @param parameterName the parameter name
	 * @param tokens the tokens
	 * 
	 * @return the string
	 */
	public Object express(String parameterName, Token[] tokens) {
		if(tokens == null || tokens.length == 0)
			return getParameter(parameterName, null);
		
		Object value = express(tokens);
		
		if(value == null)
			return null;
		
		return value;
	}

	public String expressAsString(String parameterName, Token[] tokens) {
		Object value = express(parameterName, tokens);
		
		if(value == null)
			return null;
		
		return value.toString();
	}
	
	/**
	 * Express as List.
	 * 
	 * @param parameterName the parameter name
	 * @param tokensList the tokens
	 * 
	 * @return the object[]
	 */
	public List<Object> expressAsList(String parameterName, List<Token[]> tokensList) {
		if(tokensList == null || tokensList.isEmpty())
			return cast(getParameterAsList(parameterName));
		
		List<Object> valueList = new ArrayList<Object>(tokensList.size());

		for(Token[] tokens : tokensList) {
			Object value = express(parameterName, tokens);
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
	public Set<Object> expressAsSet(String parameterName, Set<Token[]> tokensSet) {
		if(tokensSet == null || tokensSet.isEmpty())
			return cast(getParameterAsSet(parameterName));
		
		Set<Object> valueSet = new HashSet<Object>(tokensSet.size());

		for(Token[] tokens : tokensSet) {
			Object value = express(parameterName, tokens);
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
	public Map<String, Object> expressAsMap(String parameterName, Map<String, Token[]> tokensMap) {
		if(tokensMap == null || tokensMap.isEmpty()) {
			String value = getParameter(parameterName, null);
			
			if(value == null)
				return null;
			
			Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
			valueMap.put(parameterName, value);
			return valueMap;
		}
		
		Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
		
		for(Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
			Object value = express(entry.getKey(), entry.getValue());
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
	public Properties expressAsProperties(String parameterName, Properties tokensProp) {
		if(tokensProp == null || tokensProp.isEmpty()) {
			String value = getParameter(parameterName, null);

			if(value == null)
				return null;

			Properties prop = new Properties();
			prop.put(parameterName, value);
			return prop;
		}
		
		Properties prop = new Properties();

		for(Map.Entry<Object, Object> entry : tokensProp.entrySet()) {
			Object value = express(entry.getKey().toString(), (Token[])entry.getValue());
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
	private List<String> getParameterAsList(String name) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;
		
		List<String> valueList = new ArrayList<String>(values.length);
		
		for(int i = 0; i < values.length; i++) {
			valueList.add(values[i]);
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
	private Set<String> getParameterAsSet(String name) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;

		Set<String> valueSet = new LinkedHashSet<String>(values.length);
		
		for(int i = 0; i < values.length; i++) {
			valueSet.add(values[i]);
		}
		
		return valueSet;
	}

	/**
	 * Gets the parameter.
	 * 
	 * @param name the name
	 * @param defaultValue the default value
	 * 
	 * @return the value of parameter
	 */
	protected String getParameter(String name, String defaultValue) {
		String value = null;
		
		if(requestAdapter != null && name != null)
			value = requestAdapter.getParameter(name);
		
		if(value == null)
			return defaultValue;
		
		return value;
	}
	
	protected String[] getParameterValues(String name) {
		if(requestAdapter == null)
			return null;

		return requestAdapter.getParameterValues(name);
	}
	
	/**
	 * Gets the attribute object from request attributes or action results.
	 * 
	 * @param alias the attribute name or action id.
	 * 
	 * @return the object
	 */
	protected Object getAttribute(Token token) {
		Object value = null;
		
		if(requestAdapter != null)
			value = requestAdapter.getAttribute(token.getName());
		
		if(value == null && activity.getProcessResult() != null)
			value = activity.getProcessResult().getResultValue(token.getName());

		if(value != null) {
			if(token.getGetterName() != null)
				value = invokeObjectProperty(value, token.getGetterName());
		}
		
		return value;
	}
	
	protected Object referenceBean(Token token) {
		Object value = beanRegistry.getBean(token.getName());
		
		if(value != null) {
			if(token.getGetterName() != null)
				value = invokeBeanProperty(value, token.getGetterName());
		}
		
		if(value == null)
			return token.getDefaultValue();
		
		return value;
	}

	/**
	 * Invoke bean property.
	 * 
	 * @param object the object
	 * @param propertyName the property name
	 * 
	 * @return the object
	 */
	protected Object invokeBeanProperty(Object object, String propertyName) {
		Object value = null;
		
		try {
			value = BeanUtils.getObject(object, propertyName);
		} catch(InvocationTargetException e) {
			// ignore
			//e.printStackTrace();
		}
		
		return value;
	}
	
	protected Object invokeObjectProperty(Object object, String propertyName) {
		Object value = null;
		
		try {
			if(propertyName.indexOf('.') > -1) {
				StringTokenizer parser = new StringTokenizer(propertyName, ".");
				value = object;
				while(parser.hasMoreTokens()) {
					value = getProperty(value, parser.nextToken());

					if(value == null) {
						break;
					}
				}
				return value;
			} else {
				value = getProperty(object, propertyName);
			}
		} catch(InvocationTargetException e) {
			// ignore
			//e.printStackTrace();
		}
		
		return value;
	}
	
	private Object getProperty(Object object, String name) throws InvocationTargetException {
		try {
			Object value = null;
			if(name.indexOf("[") > -1) {
				value = getIndexedProperty(object, name);
			} else {
				if(object instanceof Map<?, ?>) {
					value = ((Map<?, ?>)object).get(name);
				} else {
					value = MethodUtils.invokeMethod(object, name, null);
				}
			}
			return value;
		} catch(InvocationTargetException e) {
			throw e;
		} catch(Throwable t) {
			if(object == null) {
				throw new InvocationTargetException(t, "Could not get property '" + name
						+ "' from null reference. Cause: " + t.toString());
			} else {
				throw new InvocationTargetException(t, "Could not get property '" + name + "' from "
						+ object.getClass().getName() + ". Cause: " + t.toString());
			}
		}
	}
	
	private Object getIndexedProperty(Object object, String indexedName) throws InvocationTargetException {

		try {
			String name = indexedName.substring(0, indexedName.indexOf("["));
			int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
			Object list = null;

			if(StringUtils.isEmpty(name)) {
				list = object;
			} else {
				list = getProperty(object, name);
			}

			Object value = null;

			if(list instanceof List<?>) {
				value = ((List<?>)list).get(i);
			} else if(list instanceof Object[]) {
				value = ((Object[])list)[i];
			} else if(list instanceof char[]) {
				value = new Character(((char[])list)[i]);
			} else if(list instanceof boolean[]) {
				value = Boolean.valueOf(((boolean[])list)[i]);
			} else if(list instanceof byte[]) {
				value = new Byte(((byte[])list)[i]);
			} else if(list instanceof double[]) {
				value = new Double(((double[])list)[i]);
			} else if(list instanceof float[]) {
				value = new Float(((float[])list)[i]);
			} else if(list instanceof int[]) {
				value = new Integer(((int[])list)[i]);
			} else if(list instanceof long[]) {
				value = new Long(((long[])list)[i]);
			} else if(list instanceof short[]) {
				value = new Short(((short[])list)[i]);
			} else {
				throw new IllegalArgumentException("The '" + name + "' property of the " + object.getClass().getName()
						+ " class is not a List or Array.");
			}

			return value;
		} catch(InvocationTargetException e) {
			throw e;
		} catch(Exception e) {
			throw new InvocationTargetException(e, "Error getting ordinal list from JavaBean. Cause: " + e);
		}
	}
	
	/**
	 * This method will cast List<"?"> to List<T> assuming ? is castable to T.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> List<T> cast(List<?> list){
        return (List<T>)list;
	}
	
	/**
	 * This method will cast Set<"?"> to Set<T> assuming ? is castable to T.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> Set<T> cast(Set<?> set){
		return (Set<T>)set;
	}
	
}
