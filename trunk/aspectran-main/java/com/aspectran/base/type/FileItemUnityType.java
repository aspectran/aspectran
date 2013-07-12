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
package com.aspectran.base.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Unity of the file item.
 * <p>Created: 2008. 03. 29 오후 3:47:00</p>
 */
public final class FileItemUnityType extends Type {
	
	/** The "single" file parameter unity type. */
	public static final FileItemUnityType SINGLE;

	/** The "array" file parameter unity type. */
	public static final FileItemUnityType ARRAY;
	
	private static final Map<String, FileItemUnityType> types;
	
	static {
		SINGLE = new FileItemUnityType("single");
		ARRAY = new FileItemUnityType("list");

		types = new HashMap<String, FileItemUnityType>();
		types.put(SINGLE.toString(), SINGLE);
		types.put(ARRAY.toString(), ARRAY);
	}

	/**
	 * Instantiates a new file item unity type.
	 * 
	 * @param type the type
	 */
	private FileItemUnityType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>FileItemUnityType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the file item unity type
	 */
	public static FileItemUnityType valueOf(String type) {
		return types.get(type);
	}
}
