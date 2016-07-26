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

/**
 * The enum DefaultSettingType.
 */
public enum DefaultSettingType {

	TRANSLET_NAME_PATTERN("transletNamePattern"),
	TRANSLET_NAME_PREFIX("transletNamePrefix"),
	TRANSLET_NAME_SUFFIX("transletNameSuffix"),
	TRANSLET_INTERFACE_CLASS("transletInterfaceClass"),
	TRANSLET_IMPLEMENTATION_CLASS("transletImplementationClass"),
	NULLABLE_CONTENT_ID("nullableContentId"),
	NULLABLE_ACTION_ID("nullableActionId"),
	BEAN_PROXIFIER("beanProxifier"),
	POINTCUT_PATTERN_VERIFIABLE("pointcutPatternVerifiable"),
	DEFAULT_TEMPLATE_ENGINE("defaultTemplateEngine");

	private final String alias;

	DefaultSettingType(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return this.alias;
	}

	/**
	 * Returns a {@code DefaultSettingType} with a value represented by the specified String.
	 *
	 * @param alias the default setting type as a String
	 * @return the default setting type
	 */
	public static DefaultSettingType resolve(String alias) {
		for(DefaultSettingType type : values()) {
			if(type.alias.equals(alias))
				return type;
		}
		return null;
	}

}
