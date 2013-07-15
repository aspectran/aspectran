/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
 * Type of the item.
 * <p>Created: 2008. 03. 29 오후 3:47:00</p>
 */
public final class ItemType extends Type {
	
	/** The "item" item type. */
	public static final ItemType ITEM;

	/** The "array" item type. */
	public static final ItemType LIST;
	
	/** The "map" item type. */
	public static final ItemType MAP;

	/** The "set" item type. */
	public static final ItemType SET;
	
	/** The "java.util.Properties" item type. */
	public static final ItemType PROPERTIES;
	
	private static final Map<String, ItemType> types;
	
	static {
		ITEM = new ItemType("item");
		LIST = new ItemType("list");
		MAP = new ItemType("map");
		SET = new ItemType("set");
		PROPERTIES = new ItemType("properties");

		types = new HashMap<String, ItemType>();
		types.put(ITEM.toString(), ITEM);
		types.put(LIST.toString(), LIST);
		types.put(MAP.toString(), MAP);
		types.put(SET.toString(), SET);
		types.put(PROPERTIES.toString(), PROPERTIES);
	}

	/**
	 * Instantiates a new item type.
	 * 
	 * @param type the type
	 */
	private ItemType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ItemType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the item type
	 */
	public static ItemType valueOf(String type) {
		return types.get(type);
	}
}
