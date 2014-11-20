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
package com.aspectran.core.var.apon;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.var.type.Type;


/**
 * Defines the type of the option value.
 * 
 * <p>Created: 2008. 03. 29 오후 3:47:00</p>
 */
public final class ParameterValueType extends Type {
	
	/** The "string" item type. */
	public static final ParameterValueType STRING;

	/** The "integer" item type. */
	public static final ParameterValueType INTEGER;
	
	/** The "boolean" item type. */
	public static final ParameterValueType BOOLEAN;
	
	/** The "string array" item type. */
	public static final ParameterValueType STRING_ARRAY;
	
	/** The "options" item type. */
	protected static final ParameterValueType PARAMETERS;
	
	private static final Map<String, ParameterValueType> types;
	
	static {
		STRING = new ParameterValueType("string");
		INTEGER = new ParameterValueType("integer");
		BOOLEAN = new ParameterValueType("boolean");
		STRING_ARRAY = new ParameterValueType("stringArray");
		PARAMETERS = new ParameterValueType("options");

		types = new HashMap<String, ParameterValueType>();
		types.put(STRING.toString(), STRING);
		types.put(INTEGER.toString(), INTEGER);
		types.put(BOOLEAN.toString(), BOOLEAN);
		types.put(STRING_ARRAY.toString(), STRING_ARRAY);
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
}
