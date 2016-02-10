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
package com.aspectran.core.context.expr.token;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.TokenType;

/**
 * The Class Token.
 *
 *<ul>
 *  <li>${parameterName}
 *  <li>${parameterName:defaultValue}
 *  <li>{@literal @}{attributeName}
 *  <li>{@literal @}{attributeName:defaultValue}
 *  <li>{@literal @}{attributeName^getterName:defaultValue}
 *  <li>#{beanId}
 *  <li>#{beanId^getterName}
 *  <li>#{class:className}
 *  <li>#{class:className^getterName}
 *</ul>
 *
 * <p>Created: 2008. 03. 27 PM 10:20:06</p>
 */
public class Token {
	
	public static final char PARAMETER_SYMBOL = '$';

	public static final char ATTRIBUTE_SYMBOL = '@';

	public static final char BEAN_SYMBOL = '#';
	
	public static final char START_BRACKET = '{';

	public static final char END_BRACKET = '}';

	public static final char VALUE_SEPARATOR = ':';
	
	public static final char PROPERTY_SEPARATOR = '^';
	
	private TokenType type;

	private String name;
	
	private String value;
	
	private String getterName;

	private Class<?> beanClass;
	
	/**
	 * Instantiates a new Token.
	 *
	 * @param type the token type
	 * @param nameOrValue token's name or value of this token
	 */
	public Token(TokenType type, String nameOrValue) {
		this.type = type;

		if(type == TokenType.TEXT)
			this.value = nameOrValue;
		else
			this.name = nameOrValue;
	}
	
	/**
	 * Gets the token type.
	 * 
	 * @return the token type
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * Gets the token name.
	 * 
	 * @return the token name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the token's default value or bean's class name.
	 * If the token's type is Bean and token's name is "class" then token's value is class name of the Bean. Others that is default value.
	 * 
	 * @return the default value or bean's class name
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the token's default value or bean's class name.
	 *
	 * @param value the default value or bean's class name
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Gets the getter name of the bean.
	 * 
	 * @return the getter name of the bean
	 */
	public String getGetterName() {
		return getterName;
	}

	/**
	 * Sets the getter name of the bean.
	 * 
	 * @param getterNameOfBean the new getter name of the bean
	 */
	public void setGetterName(String getterNameOfBean) {
		this.getterName = getterNameOfBean;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	@Override
	public String toString() {
		if(type == TokenType.TEXT) {
			return value;
		}

		if(type == TokenType.PARAMETER) {
			StringBuilder sb = new StringBuilder();
			sb.append(PARAMETER_SYMBOL);
			sb.append(START_BRACKET);
			if(name != null) sb.append(name);
			if(value != null) {
				sb.append(VALUE_SEPARATOR);
				sb.append(value);
			}
			sb.append(END_BRACKET);
			return sb.toString();
		} else if(type == TokenType.ATTRIBUTE) {
			StringBuilder sb = new StringBuilder();
			sb.append(ATTRIBUTE_SYMBOL);
			sb.append(START_BRACKET);
			if(name != null)
				sb.append(name);
			if(getterName != null) {
				sb.append(PROPERTY_SEPARATOR);
				sb.append(getterName);
			}
			if(value != null) {
				sb.append(VALUE_SEPARATOR);
				sb.append(value);
			}
			sb.append(END_BRACKET);
			return sb.toString();
		} else if(type == TokenType.BEAN) {
			StringBuilder sb = new StringBuilder();
			sb.append(BEAN_SYMBOL);
			sb.append(START_BRACKET);
			if(beanClass != null) {
				sb.append(BeanRule.CLASS_DIRECTIVE);
				sb.append(VALUE_SEPARATOR);
				sb.append(value);
			} else if(name != null) {
				sb.append(name);
			}
			if(getterName != null) {
				sb.append(PROPERTY_SEPARATOR);
				sb.append(getterName);
			}
			sb.append(END_BRACKET);
			return sb.toString();
		}

		return "Invalid Token: " + this;
	}
	
	/**
	 * Returns whether a specified character is the token symbol.
	 * 
	 * @param c a character
	 * @return true, if a specified character is one of the token symbols
	 */
	public static boolean isTokenSymbol(char c) {
		return (c == PARAMETER_SYMBOL ||
					c == ATTRIBUTE_SYMBOL ||
					c == BEAN_SYMBOL);
	}
	
	/**
	 * Returns the token type for the specified character.
	 * 
	 * @param symbol the token symbol character
	 * @return the token type
	 */
	public static TokenType resolveTypeAsSymbol(char symbol) {
		TokenType type;

		if(symbol == Token.ATTRIBUTE_SYMBOL)
			type = TokenType.ATTRIBUTE;
		else if(symbol == Token.BEAN_SYMBOL)
			type = TokenType.BEAN;
		else
			type = TokenType.PARAMETER;
		
		return type;
	}

}
