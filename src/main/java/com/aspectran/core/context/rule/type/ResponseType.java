/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.rule.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Response type.
 * 
 * <p>Created: 2008. 05. 02 오전 16:21:38</p>
 */
public final class ResponseType extends Type {

	/** The "transform" response type. */
	public static final ResponseType TRANSFORM;

	/** The "dispatch" response type. */
	public static final ResponseType DISPATCH;
	
	/** The "redirect" response type. */
	public static final ResponseType REDIRECT;

	/** The "forward" response type. */
	public static final ResponseType FORWARD;
	
	private static final Map<String, ResponseType> types;
	
	static {
		TRANSFORM = new ResponseType("transform");
		DISPATCH = new ResponseType("dispatch");
		REDIRECT = new ResponseType("redirect");
		FORWARD = new ResponseType("forward");

		types = new HashMap<String, ResponseType>();
		types.put(TRANSFORM.toString(), TRANSFORM);
		types.put(DISPATCH.toString(), DISPATCH);
		types.put(REDIRECT.toString(), REDIRECT);
		types.put(FORWARD.toString(), FORWARD);
	}

	/**
	 * Instantiates a new response type.
	 * 
	 * @param type the type
	 */
	private ResponseType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ResponseType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the response type
	 */
	public static ResponseType valueOf(String type) {
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
