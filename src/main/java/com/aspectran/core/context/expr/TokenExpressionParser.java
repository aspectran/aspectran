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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
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
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.PropertiesLoaderUtils;
import com.aspectran.core.util.ResourceUtils;

/**
 * The Class TokenExpressionParser.
 * 
 * <p>Created: 2008. 03. 29 AM 12:59:16</p>
 */
public class TokenExpressionParser implements TokenEvaluator {
	
	protected final Activity activity;
	
	/**
	 * Instantiates a new token expression parser.
	 *
	 * @param activity the activity
	 */
	public TokenExpressionParser(Activity activity) {
		this.activity = activity;
	}

	@Override
	public Object evaluate(Token token) {
		TokenType tokenType = token.getType();
		Object value = null;

		if(tokenType == TokenType.TEXT) {
			value = token.getValue();
		} else if(tokenType == TokenType.PARAMETER) {
			value = getParameter(token.getName(), token.getValue());
		} else if(tokenType == TokenType.ATTRIBUTE) {
			value = getAttribute(token);
		} else if(tokenType == TokenType.BEAN) {
			value = getBean(token);
		} else if(tokenType == TokenType.PROPERTY) {
			value = getProperty(token);
		}
		
		return value;
	}

	@Override
	public Object evaluate(Token[] tokens) {
		if(tokens == null || tokens.length == 0)
			return null;

		if(tokens.length > 1) {
			StringBuilder sb = new StringBuilder();
			
			for(Token t : tokens) {
				Object value = evaluate(t);
				
				if(value != null)
					sb.append(value.toString());
			}
			
			return sb.toString();
		} else {
			return evaluate(tokens[0]);
		}
	}

	@Override
	public void evaluate(Token[] tokens, Writer writer) throws IOException {
		if(tokens == null || tokens.length == 0)
			return;

		for(Token t : tokens) {
			Object value = evaluate(t);

			if(value != null)
				writer.write(value.toString());
		}

		writer.flush();
	}

	@Override
	public String evaluateAsString(Token[] tokens) {
		Object value = evaluate(tokens);
		
		if(value == null)
			return null;
		
		return value.toString();
	}

	@Override
	public Object evaluate(String parameterName, Token[] tokens) {
		if(tokens == null || tokens.length == 0)
			return getParameter(parameterName);
		
		Object value = evaluate(tokens);
		
		if(value == null)
			return null;
		
		return value;
	}

	@Override
	public String evaluateAsString(String parameterName, Token[] tokens) {
		Object value = evaluate(parameterName, tokens);
		
		if(value == null)
			return null;
		
		return value.toString();
	}

	@Override
	public List<Object> evaluateAsList(String parameterName, List<Token[]> tokensList) {
		if(tokensList == null || tokensList.isEmpty())
			return cast(getParameterAsList(parameterName));
		
		List<Object> valueList = new ArrayList<>(tokensList.size());

		for(Token[] tokens : tokensList) {
			Object value = evaluate(parameterName, tokens);
			valueList.add(value);
		}
		
		return valueList;
	}

	@Override
	public Set<Object> evaluateAsSet(String parameterName, Set<Token[]> tokensSet) {
		if(tokensSet == null || tokensSet.isEmpty())
			return cast(getParameterAsSet(parameterName));
		
		Set<Object> valueSet = new HashSet<>(tokensSet.size());

		for(Token[] tokens : tokensSet) {
			Object value = evaluate(parameterName, tokens);
			valueSet.add(value);
		}
		
		return valueSet;
	}

	@Override
	public Map<String, Object> evaluateAsMap(String parameterName, Map<String, Token[]> tokensMap) {
		if(tokensMap == null || tokensMap.isEmpty()) {
			String value = getParameter(parameterName);
			
			if(value == null)
				return null;
			
			Map<String, Object> valueMap = new LinkedHashMap<>();
			valueMap.put(parameterName, value);
			return valueMap;
		}
		
		Map<String, Object> valueMap = new LinkedHashMap<>();
		
		for(Map.Entry<String, Token[]> entry : tokensMap.entrySet()) {
			Object value = evaluate(entry.getKey(), entry.getValue());
			valueMap.put(entry.getKey(), value);
		}
		
		return valueMap;
	}

	@Override
	public Properties evaluateAsProperties(String parameterName, Properties tokensProp) {
		if(tokensProp == null || tokensProp.isEmpty()) {
			String value = getParameter(parameterName);

			if(value == null)
				return null;

			Properties prop = new Properties();
			prop.put(parameterName, value);
			return prop;
		}
		
		Properties prop = new Properties();

		for(Map.Entry<Object, Object> entry : tokensProp.entrySet()) {
			Object value = evaluate(entry.getKey().toString(), (Token[])entry.getValue());
			prop.put(entry.getKey(), value);
		}
		
		return prop;
	}
	
	/**
	 * Gets the parameter value as List.
	 * 
	 * @param name the parameter name
	 * @return the parameter value list
	 */
	private List<String> getParameterAsList(String name) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;
		
		List<String> valueList = new ArrayList<>(values.length);
		Collections.addAll(valueList, values);
		
		return valueList;
	}
	
	/**
	 * Gets the parameter values.
	 * 
	 * @param name the parameter name
	 * @return the parameter values
	 */
	private Set<String> getParameterAsSet(String name) {
		String[] values = getParameterValues(name);
		
		if(values == null)
			return null;

		Set<String> valueSet = new LinkedHashSet<>(values.length);
		Collections.addAll(valueSet, values);
		
		return valueSet;
	}

	/**
	 * Gets the parameter.
	 * 
	 * @param name the parameter name
	 * @param defaultValue the default value
	 * @return a <code>String</code> representing the
	 *			single value of the parameter
	 */
	private String getParameter(String name, String defaultValue) {
		String value = getParameter(name);
		
		if(value != null)
			return value;
		
		return defaultValue;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param name the parameter name
	 * @return a <code>String</code> representing the
	 *			single value of the parameter
	 */
	protected String getParameter(String name) {
		if(activity.getRequestAdapter() != null)
			return activity.getRequestAdapter().getParameter(name);

		return null;
	}

	/**
	 * Get parameter values.
	 *
	 * @param name the name
	 * @return an array of <code>String</code> objects
	 *			containing the parameter's values
	 */
	protected String[] getParameterValues(String name) {
		if(activity.getRequestAdapter() != null)
			return activity.getRequestAdapter().getParameterValues(name);

		return null;
	}
	
	/**
	 * Gets the parameter.
	 *
	 * @param name the parameter name
	 * @return a <code>FileParameter</code> representing the
	 *			single value of the parameter
	 */
	protected FileParameter getFileParameter(String name) {
		if(activity.getRequestAdapter() != null)
			return activity.getRequestAdapter().getFileParameter(name);

		return null;
	}

	/**
	 * Gets the file parameters.
	 *
	 * @param name the file parameter name
	 * @return an array of <code>FileParameter</code> objects
	 *			containing the parameter's values
	 */
	protected FileParameter[] getFileParameterValues(String name) {
		if(activity.getRequestAdapter() != null)
			return activity.getRequestAdapter().getFileParameterValues(name);

		return null;
	}

	/**
	 * Returns the value of the named attribute as an Object from the request attributes or action results.
	 *
	 * @param token the token
	 * @return the object
	 */
	protected Object getAttribute(Token token) {
		Object object = null;

		if(activity.getProcessResult() != null)
			object = activity.getProcessResult().getResultValue(token.getName());

		if(object == null && activity.getRequestAdapter() != null)
			object = activity.getRequestAdapter().getAttribute(token.getName());

		if(object != null && token.getGetterName() != null)
			object = getBeanProperty(object, token.getGetterName());

		return object;
	}

	/**
	 * Returns the bean instance that matches the given token.
	 *
	 * @param token the token
	 * @return an instance of the bean
	 */
	protected Object getBean(Token token) {
		Object value;

		if(token.getAlternativeValue() != null)
			value = activity.getBean((Class<?>)token.getAlternativeValue());
		else
			value = activity.getBean(token.getName());

		if(value != null && token.getGetterName() != null)
			value = getBeanProperty(value, token.getGetterName());

		return value;
	}

	/**
	 * Invoke bean's property.
	 *
	 * @param object the object
	 * @param propertyName the property name
	 * @return the object
	 */
	protected Object getBeanProperty(final Object object, String propertyName) {
		Object value;

		try {
			value = BeanUtils.getObject(object, propertyName);
		} catch(InvocationTargetException e) {
			// ignore
			value = null;
		}

		return value;
	}

	/**
	 * Returns the bean instance that matches the given token.
	 *
	 * @param token the token
	 * @return an environment variable
	 */
	protected Object getProperty(Token token) {
		String name = token.getName();
		Object value;

		if(name.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
			String resourceName = name.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
			try {
				value = PropertiesLoaderUtils.loadProperties(resourceName, activity.getActivityContext().getClassLoader());
			} catch(IOException e) {
				throw new TokenEvaluationException("Failed to load properties file for token", token,  e);
			}
		} else {
			value = activity.getActivityContext().getContextEnvironment().getProperty(token.getName());
		}

		if(value != null && token.getGetterName() != null)
			value = getBeanProperty(value, token.getGetterName());

		return value;
	}
	
	/**
	 * This method will cast List&lt;"?"&gt; to List&lt;T&gt; assuming ? is castable to T.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	protected static <T> List<T> cast(List<?> list) {
        return (List<T>)list;
	}
	
	/**
	 * This method will cast Set&lt;"?"&gt; to Set&lt;T&gt; assuming ? is castable to T.
	 *
	 * @param <T> the generic type
	 * @param set the set
	 * @return the sets the
	 */
	@SuppressWarnings("unchecked")
	protected static <T> Set<T> cast(Set<?> set) {
		return (Set<T>)set;
	}
	
}
