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
 * The enum ScopeType.
 * 
 * <p>Created: 2008. 12. 22 PM 2:48:00</p>
 */
public enum ScopeType {

	SINGLETON("singleton"),
	PROTOTYPE("prototype"),
	REQUEST("request"),
	SESSION("session"),
	APPLICATION("application");

	private final String alias;

	private ScopeType(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return this.alias;
	}

	/**
	 * Returns a <code>ScopeType</code> with a value represented by the specified String.
	 *
	 * @param alias the specified String
	 * @return the scope type
	 */
	public static ScopeType lookup(String alias) {
		for(ScopeType type : values()) {
			if(type.alias.equals(alias))
				return type;
		}
		return null;
	}

}
