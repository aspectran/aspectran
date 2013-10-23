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
package com.aspectran.core.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Unity of the item.
 * <p>Created: 2008. 03. 29 오후 3:47:00</p>
 */
public final class ItemValueType extends Type {
	
	/** The "string" item type. */
	public static final ItemValueType STRING;

	/** The "int" item type. */
	public static final ItemValueType INT;
	
	/** The "hashMap" item type. */
	//public static final ItemValueType HASH_MAP;

	/** The "linkedHashMap" item type. */
	//public static final ItemValueType LINKED_HASH_MAP;
	
	/** The "arrayList" item type. */
	//public static final ItemValueType ARRAY_LIST;
	
	/** The "custom" item type. */
	public static final ItemValueType CUSTOM;
	
	/** The "custom" item type. */
	public static final ItemValueType FILE;
	
	private static final Map<String, ItemValueType> types;
	
	private String fullQualifiedName ;
	
	static {
		STRING = new ItemValueType("string", "java.lang.String");
		INT = new ItemValueType("int", "int");
		//HASH_MAP = new ItemValueType("hashMap", "java.util.HashMap");
		//LINKED_HASH_MAP = new ItemValueType("linkedHashMap", "java.util.LinkedHashMap");
		//ARRAY_LIST = new ItemValueType("arrayList", "java.util.ArrayList");
		CUSTOM = new ItemValueType("custom", null);
		FILE = new ItemValueType("file", null);

		types = new HashMap<String, ItemValueType>();
		types.put(STRING.toString(), STRING);
		types.put(INT.toString(), INT);
		//types.put(HASH_MAP.toString(), HASH_MAP);
		//types.put(LINKED_HASH_MAP.toString(), LINKED_HASH_MAP);
		//types.put(ARRAY_LIST.toString(), ARRAY_LIST);
		types.put(CUSTOM.toString(), CUSTOM);
		types.put(FILE.toString(), FILE);
	}

	/**
	 * Instantiates a new item value type.
	 *
	 * @param type the type
	 * @param fullQualifiedName the full qualified name
	 */
	private ItemValueType(String type, String fullQualifiedName) {
		super(type);
		this.fullQualifiedName  = fullQualifiedName;
	}

	/**
	 * Instantiates a new item value type.
	 *
	 * @param fullQualifiedName the full qualified name
	 */
	public ItemValueType(String fullQualifiedName) {
		super(CUSTOM.toString());
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
	 * Returns a <code>ValueUnityType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the item type
	 */
	public static ItemValueType valueOf(String type) {
		return types.get(type);
	}
}
