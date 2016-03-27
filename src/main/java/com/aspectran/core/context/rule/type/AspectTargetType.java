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
 * Type of AspectTarget.
 */
public enum AspectTargetType {

	TRANSLET("translet"),
	SCHEDULER("scheduler");

	private final String alias;

	AspectTargetType(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return this.alias;
	}

	/**
	 * Returns a <code>AspectTargetType</code> with a value represented by the specified String.
	 *
	 * @param alias the specified String
	 * @return the aspect target type
	 */
	public static AspectTargetType lookup(String alias) {
		for(AspectTargetType type : values()) {
			if(type.alias.equals(alias))
				return type;
		}
		return null;
	}

}
