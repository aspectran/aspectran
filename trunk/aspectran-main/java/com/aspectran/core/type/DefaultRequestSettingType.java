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
 * The Class ActivitySettingType.
 */
public final class DefaultRequestSettingType extends Type {
	
	/** The Constant USE_NAMESPACES. */
	public static final DefaultRequestSettingType CHARACTER_ENCODING;
	
	/** The Constant MULTI_ACTIVITY_ENABLE. */
	public static final DefaultRequestSettingType DYNAMIC_TYPES;
	
	/** The Constant types. */
	private static final Map<String, DefaultRequestSettingType> types;
	
	static {
		CHARACTER_ENCODING = new DefaultRequestSettingType("characterEncoding");
		DYNAMIC_SETTING = new DefaultRequestSettingType("multiActivityEnable");

		types = new HashMap<String, DefaultRequestSettingType>();
		types.put(CHARACTER_ENCODING.toString(), CHARACTER_ENCODING);
		types.put(DYNAMIC_TYPES.toString(), DYNAMIC_TYPES);
	}

	/**
	 * Instantiates a new activity setting type.
	 *
	 * @param type the type
	 */
	private DefaultRequestSettingType(String type) {
		super(type);
	}

	/**
	 * Value of.
	 *
	 * @param type the type
	 * @return the activity setting type
	 */
	public static DefaultRequestSettingType valueOf(String type) {
		return types.get(type);
	}
}
