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
package com.aspectran.core.util.apon;

import com.aspectran.core.context.rule.type.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the type of the parameter value.
 * 
 * <p>Created: 2008. 03. 29 PM 3:47:00</p>
 */
public final class ParameterValueType extends Type {

	public static final ParameterValueType STRING;

	public static final ParameterValueType TEXT;

	public static final ParameterValueType INT;
	
	public static final ParameterValueType LONG;
	
	public static final ParameterValueType FLOAT;
	
	public static final ParameterValueType DOUBLE;
	
	public static final ParameterValueType BOOLEAN;
	
	public static final ParameterValueType VARIABLE;
	
	protected static final ParameterValueType PARAMETERS;
	
	private static final Map<String, ParameterValueType> types;
	
	static {
		STRING = new ParameterValueType("string");
		TEXT = new ParameterValueType("text");
		INT = new ParameterValueType("int");
		LONG = new ParameterValueType("long");
		FLOAT = new ParameterValueType("float");
		DOUBLE = new ParameterValueType("double");
		BOOLEAN = new ParameterValueType("boolean");
		VARIABLE = new ParameterValueType("variable");
		PARAMETERS = new ParameterValueType("parameters");

		types = new HashMap<String, ParameterValueType>();
		types.put(STRING.toString(), STRING);
		types.put(TEXT.toString(), TEXT);
		types.put(INT.toString(), INT);
		types.put(LONG.toString(), LONG);
		types.put(FLOAT.toString(), FLOAT);
		types.put(DOUBLE.toString(), DOUBLE);
		types.put(BOOLEAN.toString(), BOOLEAN);
		types.put(VARIABLE.toString(), VARIABLE);
		types.put(PARAMETERS.toString(), PARAMETERS);
	}

	/**
	 * Instantiates a new ParameterValueType.
	 *
	 * @param type the type
	 */
	private ParameterValueType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ParameterValueType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the item type
	 */
	public static ParameterValueType valueOf(String type) {
		return types.get(type);
	}

	public static ParameterValueType valueOfHint(String name) {
		int hintStartIndex = name.indexOf(AponFormat.ROUND_BRACKET_OPEN);
		
		if(hintStartIndex > 0) {
			int hintEndIndex = name.indexOf(AponFormat.ROUND_BRACKET_CLOSE);
			
			if(hintEndIndex > hintStartIndex) {
				String typeHint = name.substring(hintStartIndex + 1, hintEndIndex);
				return valueOf(typeHint);
			}
		}

		return null;
	}
	
	public static String stripValueTypeHint(String name) {
		int hintStartIndex = name.indexOf(AponFormat.ROUND_BRACKET_OPEN);
		
		if(hintStartIndex > 0)
			return name.substring(0, hintStartIndex);
		
		return name;
	}

	public static ParameterValueType determineParameterValueType(Object value) {
		ParameterValueType parameterValueType;

		if(value instanceof String) {
			if(value.toString().indexOf(AponFormat.NEXT_LINE_CHAR) == -1)
				parameterValueType = ParameterValueType.STRING;
			else
				parameterValueType = ParameterValueType.TEXT;
		} else if(value instanceof Integer) {
			parameterValueType = ParameterValueType.INT;
		} else if(value instanceof Long) {
			parameterValueType = ParameterValueType.LONG;
		} else if(value instanceof Float) {
			parameterValueType = ParameterValueType.FLOAT;
		} else if(value instanceof Double) {
			parameterValueType = ParameterValueType.DOUBLE;
		} else if(value instanceof Boolean) {
			parameterValueType = ParameterValueType.BOOLEAN;
		} else if(value instanceof Parameters) {
			parameterValueType = ParameterValueType.PARAMETERS;
		} else {
			parameterValueType = ParameterValueType.STRING;
		}

		return parameterValueType;
	}
	
}
