/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.util.apon;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.var.type.Type;


/**
 * Defines the type of the option value.
 * 
 * <p>Created: 2008. 03. 29 오후 3:47:00</p>
 */
public final class ParameterValueType extends Type {

	private static final String VALUE_TYPE_HINT_OPEN = "(";
	
	private static final String VALUE_TYPE_HINT_CLOSE = ")";

	public static final ParameterValueType STRING;

	public static final ParameterValueType TEXT;

	public static final ParameterValueType INTEGER;
	
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
		INTEGER = new ParameterValueType("integer");
		LONG = new ParameterValueType("long");
		FLOAT = new ParameterValueType("float");
		DOUBLE = new ParameterValueType("double");
		BOOLEAN = new ParameterValueType("boolean");
		VARIABLE = new ParameterValueType("variable");
		PARAMETERS = new ParameterValueType("parameters");

		types = new HashMap<String, ParameterValueType>();
		types.put(TEXT.toString(), TEXT);
		types.put(STRING.toString(), STRING);
		types.put(INTEGER.toString(), INTEGER);
		types.put(LONG.toString(), LONG);
		types.put(FLOAT.toString(), FLOAT);
		types.put(DOUBLE.toString(), DOUBLE);
		types.put(BOOLEAN.toString(), BOOLEAN);
		types.put(VARIABLE.toString(), VARIABLE);
		types.put(PARAMETERS.toString(), PARAMETERS);
	}

	/**
	 * Instantiates a new item value type.
	 *
	 * @param type the type
	 * @param fullQualifiedName the full qualified name
	 */
	private ParameterValueType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>OptionValueType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the item type
	 */
	public static ParameterValueType valueOf(String type) {
		return types.get(type);
	}

	public static ParameterValueType valueOfHint(String name) {
		int hintStartIndex = name.indexOf(VALUE_TYPE_HINT_OPEN);
		
		if(hintStartIndex > 0) {
			int hintEndIndex = name.indexOf(VALUE_TYPE_HINT_CLOSE);
			
			if(hintEndIndex > hintStartIndex) {
				String typeHint = name.substring(hintStartIndex + 1, hintEndIndex);
				
				return valueOf(typeHint);
				
			}
		}

		return null;
	}
	
	public static String stripValueTypeHint(String name) {
		int hintStartIndex = name.indexOf(VALUE_TYPE_HINT_OPEN);
		
		if(hintStartIndex > 0)
			return name.substring(0, hintStartIndex);
		
		return name;
	}
	
}
