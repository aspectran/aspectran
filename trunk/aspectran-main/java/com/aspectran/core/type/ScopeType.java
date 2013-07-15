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
 * Token type.
 * 
 * <p>Created: 2008. 12. 22 오후 2:48:00</p>
 */
public final class ScopeType extends Type {
	
	/** The "singleton" scope type. */
	public static final ScopeType SINGLETON;
	
	/** The "prototype" scope type. */
	public static final ScopeType PROTOTYPE;
	
	/** The "request" scope type. */
	public static final ScopeType REQUEST;
	
	/** The "session" scope type. */
	public static final ScopeType SESSION;

	/** The "context" scope type. */
	public static final ScopeType CONTEXT;
	
	/** The "application" scope type. */
	public static final ScopeType APPLICATION;
	
	private static final Map<String, ScopeType> types;
	
	static {
		SINGLETON = new ScopeType("singleton");
		PROTOTYPE = new ScopeType("prototype");
		REQUEST = new ScopeType("request");
		SESSION = new ScopeType("session");
		CONTEXT = new ScopeType("context");
		APPLICATION = new ScopeType("application");

		types = new HashMap<String, ScopeType>();
		types.put(SINGLETON.toString(), SINGLETON);
		types.put(PROTOTYPE.toString(), PROTOTYPE);
		types.put(REQUEST.toString(), REQUEST);
		types.put(SESSION.toString(), SESSION);
		types.put(CONTEXT.toString(), CONTEXT);
		types.put(APPLICATION.toString(), APPLICATION);
	}

	/**
	 * Instantiates a new scope type.
	 * 
	 * @param type the type
	 */
	private ScopeType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ScopeType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the scope type
	 */
	public static ScopeType valueOf(String type) {
		return types.get(type);
	}
}
