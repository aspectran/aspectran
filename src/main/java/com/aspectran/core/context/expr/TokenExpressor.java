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
package com.aspectran.core.context.expr;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.aspectran.core.context.expr.token.Token;

/**
 * The Interface TokenExpressor.
 * 
 * <p>Created: 2010. 5. 6. 오전 1:35:16</p>
 */
public interface TokenExpressor {

	/**
	 * Express.
	 * 
	 * @param token the token
	 * @return the object
	 */
	public Object express(Token token);
	
	/**
	 * Express as String.
	 * 
	 * @param tokens the tokens
	 * @return the string
	 */
	public Object express(Token[] tokens);
	
	/**
	 * Express as string.
	 *
	 * @param tokens the tokens
	 * @return the string
	 */
	public String expressAsString(Token[] tokens);

	/**
	 * Express as String.
	 * 
	 * @param parameterName the parameter name
	 * @param tokens the tokens
	 * @return the string
	 */
	public Object express(String parameterName, Token[] tokens);

	public void express(Token[] tokens, Writer writer) throws IOException;

	/**
	 * Express as string.
	 *
	 * @param parameterName the parameter name
	 * @param tokens the tokens
	 * @return the string
	 */
	public String expressAsString(String parameterName, Token[] tokens);

	/**
	 * Express as List.
	 * 
	 * @param parameterName the parameter name
	 * @param tokensList the tokens
	 * @return the object[]
	 */
	public List<Object> expressAsList(String parameterName, List<Token[]> tokensList);

	/**
	 * Express as Set.
	 *
	 * @param parameterName the parameter name
	 * @param tokensSet the tokens set
	 * @return the object[]
	 */
	public Set<Object> expressAsSet(String parameterName, Set<Token[]> tokensSet);
	
	/**
	 * Express as Map.
	 * 
	 * @param parameterName the parameter name
	 * @param tokensMap the tokens map
	 * @return the map
	 */
	public Map<String, Object> expressAsMap(String parameterName, Map<String, Token[]> tokensMap);
	
	/**
	 * Express as Properties.
	 *
	 * @param parameterName the parameter name
	 * @param tokensProp the tokens prop
	 * @return the Properties
	 */
	public Properties expressAsProperties(String parameterName, Properties tokensProp);
	
}
