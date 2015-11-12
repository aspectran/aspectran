/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.core.context.rule.type;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ImportType.
 * 
 * <p>Created: 2008. 04. 25 오전 16:47:38</p>
 */
public final class ImportType extends Type {

	public static final ImportType RESOURCE;

	public static final ImportType FILE;

	public static final ImportType URL;
	
	private static final Map<String, ImportType> types;
	
	static {
		RESOURCE = new ImportType("resource");
		FILE = new ImportType("file");
		URL = new ImportType("url");

		types = new HashMap<String, ImportType>();
		types.put(RESOURCE.toString(), RESOURCE);
		types.put(FILE.toString(), FILE);
		types.put(URL.toString(), URL);
	}

	/**
	 * Instantiates a resource import type.
	 * 
	 * @param type the type
	 */
	private ImportType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ResourceImportType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the transform type
	 */
	public static ImportType valueOf(String type) {
		if(type == null)
			return null;
		
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
