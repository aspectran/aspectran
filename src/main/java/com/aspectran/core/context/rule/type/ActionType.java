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
 * Action Type.
 * 
 * <p>Created: 2008. 03. 26 오전 12:58:38</p>
 */
public final class ActionType extends Type {

	public static final ActionType ECHO;

	public static final ActionType BEAN;
	
	public static final ActionType INCLUDE;
	
	private static final Map<String, ActionType> types;
	
	static {
		ECHO = new ActionType("echo");
		BEAN = new ActionType("bean");
		INCLUDE = new ActionType("include");

		types = new HashMap<String, ActionType>();
		types.put(ECHO.toString(), ECHO);
		types.put(BEAN.toString(), BEAN);
		types.put(INCLUDE.toString(), INCLUDE);
	}

	/**
	 * Instantiates a new ActionType.
	 * 
	 * @param type the type
	 */
	private ActionType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ActionType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the action type
	 */
	public static ActionType valueOf(String type) {
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
