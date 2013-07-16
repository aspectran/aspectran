/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.token.expression;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.bean.registry.BeanRegistry;
import com.aspectran.core.token.Token;
import com.aspectran.core.type.TokenType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.StringUtils;

/**
 * <p>Created: 2008. 03. 29 오전 12:59:16</p>
 */
public class TokenExpression implements TokenExpressor {
	
	protected AspectranActivity activity;
	
	protected RequestAdapter requestAdapter;
	
	protected BeanRegistry beanRegistry;
	
	protected TokenValueHandler tokenValueHandler;
	
	public TokenExpression(BeanRegistry beanRegistry) {
		this.beanRegistry = beanRegistry;
	}
	
	public TokenExpression(AspectranActivity activity) {
		this.activity = activity;
		this.requestAdapter = activity.getRequestAdapter();
		this.beanRegistry = activity.getBeanRegistry();
	}

	/**
	 * Sets the token value handler.
	 * 
	 * @param tokenValueHandler the new token value handler
	 */
	public void setTokenValueHandler(TokenValueHandler tokenValueHandler) {
		this.tokenValueHandler = tokenValueHandler;
	}

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
			value = token.getDefaultText();
		} else	if(tokenType == TokenType.PARAMETER) {
			value = getParameterValue(token.getName(), token.getDefaultText());
		} else	if(tokenType == TokenType.ATTRIBUTE) {
			value = getAttribute(token.getName());
		} else	if(tokenType == TokenType.REFERENCE_BEAN) {
			//TODO
			value = referenceBean(token);
		}

		if(value != null && tokenValueHandler != null)
			return tokenValueHandler.handle(tokenType, value);
		
		return value;
	}

	/**
	 * Express as String.
	 * 
	 * @param tokens the tokens
	 * 
	 * @return the string
	 */
	public String express(Token[] tokens) {
		if(tokens == null)
			return null;
		
		if(tokens.length == 0)
			return StringUtils.EMPTY;
		
		StringBuilder sb = new StringBuilder();
		
		for(Token t : tokens) {
			Object value = express(t);
			
			if(value != null)
				sb.append(value.toString());
		}
		
		return sb.toString();
	}
	
	/**
	 * Express as String.
	 * 
	 * @param parameterName the parameter name
	 * @param tokens the tokens
	 * 
	 * @return the string
	 */
	public String expressAsString(String parameterName, Token[] tokens) {
		if(tokens == null) {
			Token t = new Token(TokenType.PARAMETER, parameterName);
			return (String)express(t);
		}
		
		return express(tokens);
	}

	/**
	 * Express as Object.
	 * 
	 * @param parameterName the parameter name
	 * @param token the token
	 * 
	 * @return the object
	 */
	public Object expressAsObject(String parameterName, Token token) {
		if(token == null) {
			Token t = new Token(TokenType.ATTRIBUTE, parameterName);
			return express(t);
		}
		
		return express(token);
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
		if(tokensList == null || tokensList.size() == 0)
			return getParameterValueList(parameterName);
		
		List<Object> valueList = new ArrayList<Object>(tokensList.size());

		for(Token[] tokens : tokensList) {
			Object value = null;

			if(tokens == null || tokens.length == 0) {
				value = expressAsObject(parameterName, null);
			} else if(tokens.length == 1) {
				value = expressAsObject(parameterName, tokens[0]);
			} else {
				value = expressAsString(parameterName, tokens);
			}

			valueList.add(value);
		}
		
		return valueList;
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
		Map<String, Object> valueMap = new LinkedHashMap<String, Object>();

		if(tokensMap == null) {
			Token t = new Token(TokenType.PARAMETER, parameterName);
			valueMap.put(parameterName, express(t));
			return valueMap;
		}

		for(Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
			Object value = express(entry.getValue());
			valueMap.put(entry.getKey(), value);
		}
		
		return valueMap;
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
		if(tokensSet == null || tokensSet.size() == 0)
			return null;
		
		Set<Object> valueSet = new HashSet<Object>(tokensSet.size());

		for(Token[] tokens : tokensSet) {
			Object value = null;

			if(tokens == null || tokens.length == 0) {
				value = expressAsObject(parameterName, null);
			} else if(tokens.length == 1) {
				value = expressAsObject(parameterName, tokens[0]);
			} else {
				value = expressAsString(parameterName, tokens);
			}

			valueSet.add(value);
		}
		
		return valueSet;
	}

	protected Object referenceBean(Token token) {
		Object value = beanRegistry.getBean(token.getName(), activity);
		
		if(value != null) {
			if(token.getGetterName() != null)
				value = invokeBeanProperty(value, token.getGetterName());
		}
		
		if(value == null)
			return token.getDefaultText();
		
		return value;
	}
	
//	/**
//	 * Gets the ticket property.
//	 * 
//	 * @param token the token
//	 * 
//	 * @return the ticket property
//	 */
//	protected Object getTicketProperty(Token token) {
//		Object value = null;
//		
//		if(ticketBeanMap != null)
//			value = ticketBeanMap.get(token.getName());
//		
//		if(value != null) {
//			if(token.getGetterName() != null)
//				value = invokeBeanProperty(value, token.getGetterName());
//		}
//		
//		if(value == null)
//			return token.getDefaultText();
//		
//		return value;
//	}

	/**
	 * Gets the attribute.
	 * 
	 * @param name the name
	 * 
	 * @return the attribute
	 */
	protected Object getAttribute(String name) {
		if(requestAdapter == null)
			return null;

		return requestAdapter.getAttribute(name);
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
		}
		
		return value;
	}
	
	/**
	 * Gets the parameter.
	 * 
	 * @param name the name
	 * @param defaultValue the default value
	 * 
	 * @return the value of parameter
	 */
	private Object getParameterValue(String name, String defaultValue) {
		Object value = null;
		
		if(requestAdapter != null && name != null)
			value = requestAdapter.getParameter(name);
		
		if(value == null)
			return defaultValue;
		
		return value;
	}
	
	/**
	 * Gets the parameter values.
	 * 
	 * @param name the name
	 * 
	 * @return the parameter values
	 */
	private List<Object> getParameterValueList(String name) {
		if(requestAdapter == null)
			return null;

		String[] values = requestAdapter.getParameterValues(name);
		
		List<Object> valueList = new ArrayList<Object>(values.length);
		
		for(int i = 0; i < values.length; i++) {
			valueList.add(values[i]);
		}
		
		return valueList;
	}
	
}
