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
package com.aspectran.core.context.rule.type;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ActivitySettingType.
 */
public final class DefaultSettingType extends Type {
	
	/** The Constant USE_NAMESPACES. */
	public static final DefaultSettingType USE_NAMESPACES;

	/** The Constant NULLABLE_CONTENT_ID. */
	public static final DefaultSettingType NULLABLE_CONTENT_ID;
	
	/** The Constant NULLABLE_ACTION_ID. */
	public static final DefaultSettingType NULLABLE_ACTION_ID;
	
	/** The Constant NULLABLE_BEAN_ID. */
	public static final DefaultSettingType NULLABLE_BEAN_ID;
	
	/** The Constant TRANSLET_NAME_PATTERN_PREFIX. */
	public static final DefaultSettingType TRANSLET_NAME_PATTERN_PREFIX;
	
	/** The Constant TRANSLET_NAME_PATTERN_SUFFIX. */
	public static final DefaultSettingType TRANSLET_NAME_PATTERN_SUFFIX;
	
	/** The Constant TRANSLET_NAME_PATTERN. */
	public static final DefaultSettingType TRANSLET_NAME_PATTERN;
	
	/** The Constant TRANSLET_INTERFACE. */
	public static final DefaultSettingType TRANSLET_INTERFACE_CLASS;
	
	/** The Constant TRANSLET_CLASS. */
	public static final DefaultSettingType TRANSLET_IMPLEMENT_CLASS;

	/** The Constant ACTIVITY_DEFAULT_HANDLER. */
	public static final DefaultSettingType ACTIVITY_DEFAULT_HANDLER;
	
	/** The Constant BEAN_PROXY_MODE. */
	public static final DefaultSettingType BEAN_PROXY_MODE;
	
	/** The Constant types. */
	private static final Map<String, DefaultSettingType> types;
	
	static {
		USE_NAMESPACES = new DefaultSettingType("useNamespaces");
		NULLABLE_CONTENT_ID = new DefaultSettingType("nullableContentId");
		NULLABLE_ACTION_ID = new DefaultSettingType("nullableActionId");
		NULLABLE_BEAN_ID = new DefaultSettingType("nullableBeanId");
		TRANSLET_NAME_PATTERN_PREFIX = new DefaultSettingType("transletNamePatternPrefix");
		TRANSLET_NAME_PATTERN_SUFFIX = new DefaultSettingType("transletNamePatternSuffix");
		TRANSLET_NAME_PATTERN = new DefaultSettingType("transletNamePattern");
		TRANSLET_INTERFACE_CLASS = new DefaultSettingType("transletInterfaceClass");
		TRANSLET_IMPLEMENT_CLASS = new DefaultSettingType("transletImplementClass");
		ACTIVITY_DEFAULT_HANDLER = new DefaultSettingType("activityDefaultHandler");
		BEAN_PROXY_MODE = new DefaultSettingType("beanProxyMode");

		types = new HashMap<String, DefaultSettingType>();
		types.put(USE_NAMESPACES.toString(), USE_NAMESPACES);
		types.put(NULLABLE_CONTENT_ID.toString(), NULLABLE_CONTENT_ID);
		types.put(NULLABLE_ACTION_ID.toString(), NULLABLE_ACTION_ID);
		types.put(NULLABLE_BEAN_ID.toString(), NULLABLE_BEAN_ID);
		types.put(TRANSLET_NAME_PATTERN_PREFIX.toString(), TRANSLET_NAME_PATTERN_PREFIX);
		types.put(TRANSLET_NAME_PATTERN_SUFFIX.toString(), TRANSLET_NAME_PATTERN_SUFFIX);
		types.put(TRANSLET_NAME_PATTERN.toString(), TRANSLET_NAME_PATTERN);
		types.put(TRANSLET_INTERFACE_CLASS.toString(), TRANSLET_INTERFACE_CLASS);
		types.put(TRANSLET_IMPLEMENT_CLASS.toString(), TRANSLET_IMPLEMENT_CLASS);
		types.put(ACTIVITY_DEFAULT_HANDLER.toString(), ACTIVITY_DEFAULT_HANDLER);
		types.put(BEAN_PROXY_MODE.toString(), BEAN_PROXY_MODE);
	}

	/**
	 * Instantiates a new activity setting type.
	 *
	 * @param type the type
	 */
	private DefaultSettingType(String type) {
		super(type);
	}

	/**
	 * Value of.
	 *
	 * @param type the type
	 * @return the activity setting type
	 */
	public static DefaultSettingType valueOf(String type) {
		return types.get(type);
	}
}
