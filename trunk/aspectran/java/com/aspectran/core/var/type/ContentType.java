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
package com.aspectran.core.var.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Content Type.
 * 
 * <p>주로 응답기(Responser)가 생성하는 응답문서의 Content-Type을 지정하기 위해 사용된다.</p>
 * <h4>지정 가능한 Content-Type의 종류:</h4>
 * <dl>
 * <dt>text/plain</dt><dd>TEXT 문서</dd>
 * <dt>text/html</dt><dd>HTML 문서</dd>
 * <dt>text/xml</dt><dd>XML 문서</dd>
 * </dl>
 * 
 * <p>Created: 2008. 03. 26 오전 12:58:38</p>
 */
public final class ContentType extends Type {

	/** The "text/plain" content type. */
	public static final ContentType TEXT_PLAIN;

	/** The "text/xml" content type. */
	public static final ContentType TEXT_XML;
	
	/** The "text/json" content type. */
	public static final ContentType TEXT_JSON;
	
	/** The "text/apon" content type. */
	public static final ContentType TEXT_APON;
	
	/** The "text/html" content type. */
	public static final ContentType TEXT_HTML;
	
	private static final Map<String, ContentType> types;
	
	static {
		TEXT_PLAIN = new ContentType("text/plain");
		TEXT_XML = new ContentType("text/xml");
		TEXT_JSON = new ContentType("text/json");
		TEXT_APON = new ContentType("text/apon");
		TEXT_HTML = new ContentType("text/html");

		types = new HashMap<String, ContentType>();
		types.put(TEXT_PLAIN.toString(), TEXT_PLAIN);
		types.put(TEXT_XML.toString(), TEXT_XML);
		types.put(TEXT_JSON.toString(), TEXT_JSON);
		types.put(TEXT_APON.toString(), TEXT_APON);
		types.put(TEXT_HTML.toString(), TEXT_HTML);
	}

	/**
	 * Instantiates a new content type.
	 * 
	 * @param type the type
	 */
	private ContentType(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ContentType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the content type
	 */
	public static ContentType valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
}
