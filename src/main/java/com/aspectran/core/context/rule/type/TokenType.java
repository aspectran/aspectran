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
package com.aspectran.core.context.rule.type;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class TokenType.
 * 
 * <p>Created: 2008. 03. 29 AM 1:21:48</p>
 */
public final class TokenType extends Type {
	
	public static final TokenType TEXT;
	
	public static final TokenType PARAMETER;

	public static final TokenType ATTRIBUTE;

	public static final TokenType REFERENCE_BEAN;
	
	private static final Map<String, TokenType> types;
	
	static {
		TEXT = new TokenType("text");
		PARAMETER = new TokenType("parameter");
		ATTRIBUTE = new TokenType("attribute");
		REFERENCE_BEAN = new TokenType("reference-bean");

		types = new HashMap<String, TokenType>();
		types.put(TEXT.toString(), TEXT);
		types.put(PARAMETER.toString(), PARAMETER);
		types.put(ATTRIBUTE.toString(), ATTRIBUTE);
		types.put(REFERENCE_BEAN.toString(), REFERENCE_BEAN);
	}

	/**
	 * Instantiates a new TokenType.
	 * 
	 * @param type the type
	 */
	private TokenType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>TokenType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the token type
	 */
	public static TokenType valueOf(String type) {
		return types.get(type);
	}
	
	/**
	 * Returns an array containing the constants of this type, in the order they are declared.
	 *
	 * @return the string[]
	 */
	public static String[] values() {
		return types.keySet().toArray(new String[types.size()]);
	}

}
