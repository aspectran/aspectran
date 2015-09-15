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
 * Request method type.
 * 
 * <p>Created: 2008. 03. 26 오전 12:58:38</p>
 */
public final class RequestMethodType extends Type {
	
	/** The "get" request method type. */
	public static final RequestMethodType GET;

	/** The "post" request method type. */
	public static final RequestMethodType POST;
	
	private static final Map<String, RequestMethodType> types;
	
	static {
		GET = new RequestMethodType("get");
		POST = new RequestMethodType("post");

		types = new HashMap<String, RequestMethodType>();
		types.put(GET.toString(), GET);
		types.put(POST.toString(), POST);
	}

	/**
	 * Instantiates a new request method type.
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
		return types.get(type);
	}
}