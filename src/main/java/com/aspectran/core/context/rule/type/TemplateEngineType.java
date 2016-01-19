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
 * The Class TemplateEngineType.
 * 
 * <p>Created: 2016. 01. 09</p>
 *
 * @since 2.0.0
 */
public final class TemplateEngineType extends Type {

	public static final TemplateEngineType TOKEN;

	public static final TemplateEngineType FREEMAKER;

	public static final TemplateEngineType PEBBLE;

	public static final TemplateEngineType CUSTOM;

	private static final Map<String, TemplateEngineType> types;

	static {
		TOKEN = new TemplateEngineType("token");
		PEBBLE = new TemplateEngineType("pebble");
		FREEMAKER = new TemplateEngineType("freemaker");
		CUSTOM = new TemplateEngineType("custom");

		types = new HashMap<String, TemplateEngineType>();
		types.put(TOKEN.toString(), TOKEN);
		types.put(PEBBLE.toString(), PEBBLE);
		types.put(FREEMAKER.toString(), FREEMAKER);
		types.put(CUSTOM.toString(), CUSTOM);
	}

	/**
	 * Instantiates a new TemplateEngineType.
	 *
	 * @param type the type
	 */
	private TemplateEngineType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>TemplateEngineType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the item type
	 */
	public static TemplateEngineType valueOf(String type) {
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
