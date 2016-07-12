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
 * The enum ItemValueType.
 * 
 * <p>Created: 2008. 03. 29 PM 3:47:00</p>
 */
public enum ItemValueType {

	STRING("string", java.lang.String.class),
	INT("int", java.lang.Integer.class),
	LONG("long", java.lang.Long.class),
	FLOAT("float", java.lang.Float.class),
	DOUBLE("double", java.lang.Double.class),
	BOOLEAN("boolean", java.lang.Boolean.class),
	PARAMETERS("parameters", com.aspectran.core.util.apon.Parameters.class),
	FILE("file", java.io.File.class),
	MULTIPART_FILE("multipart-file", com.aspectran.core.activity.request.parameter.FileParameter.class);

	private final String alias;

	private final Class<?> classType;

	ItemValueType(String alias, Class<?> classType) {
		this.alias = alias;
		this.classType = classType;
	}

	public Class<?> getClassType() {
		return classType;
	}

	@Override
	public String toString() {
		return this.alias;
	}

	/**
	 * Returns a <code>ItemValueType</code> with a value represented by the specified String.
	 *
	 * @param alias the item value type as a String
	 * @return the import type
	 */
	public static ItemValueType resolve(String alias) {
		for(ItemValueType type : values()) {
			if(type.alias.equals(alias))
				return type;
		}
		return null;
	}

}
