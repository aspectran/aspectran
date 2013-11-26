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
package com.aspectran.core.var.option;

import java.util.HashMap;
import java.util.Map;

import com.aspectran.core.var.type.Type;


/**
 * Unity of the item.
 * <p>Created: 2008. 03. 29 오후 3:47:00</p>
 */
public final class OptionValueType extends Type {
	
	/** The "string" item type. */
	public static final OptionValueType STRING;

	/** The "integer" item type. */
	public static final OptionValueType INTEGER;
	
	/** The "boolean" item type. */
	public static final OptionValueType BOOLEAN;
	
	private static final Map<String, OptionValueType> types;
	
	static {
		STRING = new OptionValueType("string");
		INTEGER = new OptionValueType("integer");
		BOOLEAN = new OptionValueType("boolean");

		types = new HashMap<String, OptionValueType>();
		types.put(STRING.toString(), STRING);
		types.put(INTEGER.toString(), INTEGER);
		types.put(BOOLEAN.toString(), BOOLEAN);
	}

	/**
	 * Instantiates a new item value type.
	 *
	 * @param type the type
	 * @param fullQualifiedName the full qualified name
	 */
	private OptionValueType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ValueUnityType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the item type
	 */
	public static OptionValueType valueOf(String type) {
		return types.get(type);
	}
}
