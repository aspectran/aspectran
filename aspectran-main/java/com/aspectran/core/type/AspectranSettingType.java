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
public final class AspectranSettingType extends Type {
	
	/** The Constant USE_NAMESPACES. */
	public static final AspectranSettingType USE_NAMESPACES;

	/** The Constant NULLABLE_CONTENT_ID. */
	public static final AspectranSettingType NULLABLE_CONTENT_ID;
	
	/** The Constant NULLABLE_ACTION_ID. */
	public static final AspectranSettingType NULLABLE_ACTION_ID;
	
	/** The Constant NULLABLE_BEAN_ID. */
	public static final AspectranSettingType NULLABLE_BEAN_ID;
	
	/** The Constant MULTI_ACTIVITY_ENABLE. */
	public static final AspectranSettingType MULTIPLE_TRANSLET_ENABLE;
	
	/** The Constant TRANSLET_NAME_PATTERN_PREFIX. */
	public static final AspectranSettingType TRANSLET_NAME_PATTERN_PREFIX;
	
	/** The Constant TRANSLET_NAME_PATTERN_SUFFIX. */
	public static final AspectranSettingType TRANSLET_NAME_PATTERN_SUFFIX;
	
	/** The Constant TRANSLET_NAME_PATTERN. */
	public static final AspectranSettingType TRANSLET_NAME_PATTERN;
	
	/** The Constant TRANSLET_INTERFACE. */
	public static final AspectranSettingType TRANSLET_INTERFACE_CLASS;
	
	/** The Constant TRANSLET_CLASS. */
	public static final AspectranSettingType TRANSLET_IMPLEMENT_CLASS;
	
	/** The Constant types. */
	private static final Map<String, AspectranSettingType> types;
	
	static {
		USE_NAMESPACES = new AspectranSettingType("useNamespaces");
		NULLABLE_CONTENT_ID = new AspectranSettingType("nullableContentId");
		NULLABLE_ACTION_ID = new AspectranSettingType("nullableActionId");
		NULLABLE_BEAN_ID = new AspectranSettingType("nullableBeanId");
		MULTIPLE_TRANSLET_ENABLE = new AspectranSettingType("multipleTransletEnable");
		TRANSLET_NAME_PATTERN_PREFIX = new AspectranSettingType("transletNamePatternPrefix");
		TRANSLET_NAME_PATTERN_SUFFIX = new AspectranSettingType("transletNamePatternSuffix");
		TRANSLET_NAME_PATTERN = new AspectranSettingType("transletNamePattern");
		TRANSLET_INTERFACE_CLASS = new AspectranSettingType("transletInterfaceClass");
		TRANSLET_IMPLEMENT_CLASS = new AspectranSettingType("transletImplementClass");

		types = new HashMap<String, AspectranSettingType>();
		types.put(USE_NAMESPACES.toString(), USE_NAMESPACES);
		types.put(NULLABLE_CONTENT_ID.toString(), NULLABLE_CONTENT_ID);
		types.put(NULLABLE_ACTION_ID.toString(), NULLABLE_ACTION_ID);
		types.put(NULLABLE_BEAN_ID.toString(), NULLABLE_BEAN_ID);
		types.put(MULTIPLE_TRANSLET_ENABLE.toString(), MULTIPLE_TRANSLET_ENABLE);
		types.put(TRANSLET_NAME_PATTERN_PREFIX.toString(), TRANSLET_NAME_PATTERN_PREFIX);
		types.put(TRANSLET_NAME_PATTERN_SUFFIX.toString(), TRANSLET_NAME_PATTERN_SUFFIX);
		types.put(TRANSLET_NAME_PATTERN.toString(), TRANSLET_NAME_PATTERN);
		types.put(TRANSLET_INTERFACE_CLASS.toString(), TRANSLET_INTERFACE_CLASS);
		types.put(TRANSLET_IMPLEMENT_CLASS.toString(), TRANSLET_IMPLEMENT_CLASS);
	}

	/**
	 * Instantiates a new activity setting type.
	 *
	 * @param type the type
	 */
	private AspectranSettingType(String type) {
		super(type);
	}

	/**
	 * Value of.
	 *
	 * @param type the type
	 * @return the activity setting type
	 */
	public static AspectranSettingType valueOf(String type) {
		return types.get(type);
	}
}
