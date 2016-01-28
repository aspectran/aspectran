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
 * The Class ItemValueType.
 * 
 * <p>Created: 2008. 03. 29 PM 3:47:00</p>
 */
public final class ItemValueType extends Type {
	
	public static final ItemValueType STRING;

	public static final ItemValueType INT;
	
	public static final ItemValueType LONG;
	
	public static final ItemValueType FLOAT;
	
	public static final ItemValueType DOUBLE;
	
	public static final ItemValueType BOOLEAN;
	
	public static final ItemValueType PARAMETERS;
	
	public static final ItemValueType FILE;
	
	public static final ItemValueType MULTIPART_FILE;
	
	private static final Map<String, ItemValueType> types;

	private String fullQualifiedName;
	
	static {
		STRING = new ItemValueType("string", "java.lang.String");
		INT = new ItemValueType("int", "java.lang.Integer");
		LONG = new ItemValueType("long", "java.lang.Long");
		FLOAT = new ItemValueType("float", "java.lang.Float");
		DOUBLE = new ItemValueType("double", "java.lang.Double");
		BOOLEAN = new ItemValueType("boolean", "java.lang.Boolean");
		PARAMETERS = new ItemValueType("parameters", "com.aspectran.core.util.apon.Parameters");
		FILE = new ItemValueType("file", "java.io.File");
		MULTIPART_FILE = new ItemValueType("multipart-file", "com.aspectran.core.activity.request.parameter.FileParameter");

		types = new HashMap<String, ItemValueType>();
		types.put(STRING.toString(), STRING);
		types.put(INT.toString(), INT);
		types.put(LONG.toString(), LONG);
		types.put(FLOAT.toString(), FLOAT);
		types.put(DOUBLE.toString(), DOUBLE);
		types.put(BOOLEAN.toString(), BOOLEAN);
		types.put(PARAMETERS.toString(), PARAMETERS);
		types.put(FILE.toString(), FILE);
		types.put(MULTIPART_FILE.toString(), MULTIPART_FILE);
	}

	/**
	 * Instantiates a new ItemValueType.
	 *
	 * @param type the type
	 * @param fullQualifiedName the full qualified name
	 */
	private ItemValueType(String type, String fullQualifiedName) {
		super(type);
		this.fullQualifiedName  = fullQualifiedName;
	}
	
	/**
	 * Gets the full qualified name.
	 *
	 * @return the full qualified name
	 */
	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

	/**
	 * Returns a <code>ItemValueType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the item type
	 */
	public static ItemValueType valueOf(String type) {
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
