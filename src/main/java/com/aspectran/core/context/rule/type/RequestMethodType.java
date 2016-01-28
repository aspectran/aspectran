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
 * The Class RequestMethodType.
 * 
 * <p>Created: 2008. 03. 26 AM 12:58:38</p>
 */
public final class RequestMethodType extends Type {
	
	/** The "get" request method type. */
	public static final RequestMethodType GET;

	/** The "post" request method type. */
	public static final RequestMethodType POST;

	/**
	 * The Constant PUT.
	 * @since 1.2.0
	 */
	public static final RequestMethodType PUT;

	/**
	 * The Constant PATCH.
	 * @since 1.2.0
	 */
	public static final RequestMethodType PATCH;
	
	/**
	 * The Constant DELETE.
	 * @since 1.2.0
	 */
	public static final RequestMethodType DELETE;
	
	/**
	 * The Constant HEAD.
	 * @since 1.2.0
	 */
	public static final RequestMethodType HEAD;

	/**
	 * The Constant OPTIONS.
	 * @since 1.2.0
	 */
	public static final RequestMethodType OPTIONS;
	
	/**
	 * The Constant TRACE.
	 * @since 1.2.0
	 */
	public static final RequestMethodType TRACE;
	
	/**
	 * The Constant CONNECT.
	 * @since 1.2.0
	 */
	public static final RequestMethodType CONNECT;
	
	private static final Map<String, RequestMethodType> types;
	
	static {
		GET = new RequestMethodType("GET");
		POST = new RequestMethodType("POST");
		PUT = new RequestMethodType("PUT");
		PATCH = new RequestMethodType("PATCH");
		DELETE = new RequestMethodType("DELETE");
		HEAD = new RequestMethodType("HEAD");
		OPTIONS = new RequestMethodType("OPTIONS");
		TRACE = new RequestMethodType("TRACE");
		CONNECT = new RequestMethodType("CONNECT");

		types = new HashMap<String, RequestMethodType>();
		types.put(GET.toString(), GET);
		types.put(POST.toString(), POST);
		types.put(PUT.toString(), PUT);
		types.put(PATCH.toString(), PATCH);
		types.put(DELETE.toString(), DELETE);
		types.put(HEAD.toString(), HEAD);
		types.put(OPTIONS.toString(), OPTIONS);
		types.put(TRACE.toString(), TRACE);
		types.put(CONNECT.toString(), CONNECT);
	}

	/**
	 * Instantiates a new RequestMethodType.
	 * 
	 * @param type the type
	 */
	private RequestMethodType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>RequestMethodType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the request method type
	 */
	public static RequestMethodType valueOf(String type) {
		return types.get(type.toUpperCase());
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