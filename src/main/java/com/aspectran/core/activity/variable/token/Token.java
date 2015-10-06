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
package com.aspectran.core.activity.variable.token;

import com.aspectran.core.context.rule.type.TokenType;

/**
 * <p>Created: 2008. 03. 27 오후 10:20:06</p>
 */
public class Token {
	
	public static final char PARAMETER_SYMBOL = '$';

	public static final char ATTRIBUTE_SYMBOL = '@';

	public static final char REFERENCE_BEAN_SYMBOL = '#';
	
	public static final char START_BRACKET = '{';

	public static final char END_BRACKET = '}';

	public static final char DEFAULT_VALUE_SEPARATOR = ':';
	
	public static final char BEAN_PROPERTY_SEPARATOR = '^';
	
	private TokenType type;

	private String name;
	
	private String defaultValue;
	
	private String getterName;
	
	/**
	 * Instantiates a new token.
	 * 
	 * @param type the type
	 * @param nameOrText the name
	 */
	public Token(TokenType type, String nameOrDefaultValue) {
		this.type = type;

		if(type == TokenType.TEXT)
			this.defaultValue = nameOrDefaultValue;
		else
			this.name = nameOrDefaultValue;
	}
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
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
	 * @param defaultText the default value
	 * 
	 * @return the string
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Gets the getter name of bean.
	 * 
	 * @return the getter name of bean
	 */
	public String getGetterName() {
		return getterName;
	}

	/**
	 * Sets the getter name of bean.
	 * 
	 * @param getterNameOfBean the new getter name of bean
	 */
	public void setGetterName(String getterNameOfBean) {
		this.getterName = getterNameOfBean;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(type == TokenType.TEXT)
			return defaultValue;
		
		StringBuilder sb = new StringBuilder();
		if(type == TokenType.PARAMETER)
			sb.append(PARAMETER_SYMBOL);
		else if(type == TokenType.ATTRIBUTE)
			sb.append(ATTRIBUTE_SYMBOL);
		else if(type == TokenType.REFERENCE_BEAN)
			sb.append(REFERENCE_BEAN_SYMBOL);
		sb.append(START_BRACKET);
		if(name != null)
			sb.append(name);
		if(getterName != null) {
			sb.append(BEAN_PROPERTY_SEPARATOR);
			sb.append(getterName);
		}
		if(defaultValue != null) {
			sb.append(DEFAULT_VALUE_SEPARATOR);
			sb.append(defaultValue);
		}
		sb.append(END_BRACKET);

		return sb.toString();
	}
	
	/**
	 * Checks if is token symbol.
	 * 
	 * @param c the character
	 * 
	 * @return true, if is token symbol
	 */
	public static boolean isTokenSymbol(char c) {
		return (c == PARAMETER_SYMBOL ||
					c == ATTRIBUTE_SYMBOL ||
					c == REFERENCE_BEAN_SYMBOL);
	}
	
	/**
	 * Token type of symbol.
	 * 
	 * @param symbol the symbol
	 * 
	 * @return the token type
	 */
	public static TokenType tokenTypeOfSymbol(char symbol) {
		TokenType type;

		if(symbol == Token.ATTRIBUTE_SYMBOL)
			type = TokenType.ATTRIBUTE;
		else if(symbol == Token.REFERENCE_BEAN_SYMBOL)
			type = TokenType.REFERENCE_BEAN;
		else
			type = TokenType.PARAMETER;
		
		return type;
	}
}
