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
package com.aspectran.base.type;

import java.util.HashMap;
import java.util.Map;


/**
 * Content Type.
 * 
 * <p>나열한 컨텍스트를 병합하는 방법</p>
 * <h4>지정 가능한 Content-Type의 종류:</h4>
 * <dl>
 * <dt>merge</dt><dd>뒤에 위치한 컨텍스트 요소가 앞에 위치한 컨텍스트에 존재할 경우 앞에 위치한 컨텍스트 요소를 뒤에 위치한 컨텍스트 요소로 교체한다.</dd>
 * <dt>mix</dt><dd>뒤에 위치한 컨텍스트 요소가 앞에 위치한 컨텍스트에 존재할 경우 뒤에 위치한 컨텍스트의 요소를 무시한다.</dd>
 * </dl>
 * 
 * <p>Created: 2009. 03. 29 오후 7:00:00</p>
 * @deprecated
 */
public final class ContextMergeMode extends Type {

	/** The "merge" context merge mode type. */
	public static final ContextMergeMode REPLACE;

	/** The "skip" context merge mode type. */
	public static final ContextMergeMode SKIP;
	
	private static final Map<String, ContextMergeMode> types;
	
	static {
		REPLACE = new ContextMergeMode("replace");
		SKIP = new ContextMergeMode("skip");

		types = new HashMap<String, ContextMergeMode>();
		types.put(REPLACE.toString(), REPLACE);
		types.put(SKIP.toString(), SKIP);
	}

	/**
	 * Instantiates a new content type.
	 * 
	 * @param type the type
	 */
	private ContextMergeMode(String type) {
		super(type);
	}

	/**
	 * Returns a <code>ContentType</code> with a value represented by the specified String.
	 * 
	 * @param type the type
	 * 
	 * @return the content type
	 */
	public static ContextMergeMode valueOf(String type) {
		if(type == null)
			return null;
		
		return types.get(type);
	}
}
