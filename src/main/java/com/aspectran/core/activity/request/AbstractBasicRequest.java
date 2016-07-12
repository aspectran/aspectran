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
package com.aspectran.core.activity.request;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class AbstractBasicRequest.
 * 
 * @since 2016. 2. 13.
 */
public abstract class AbstractBasicRequest extends AbstractRequest {

	private String characterEncoding;

	private Map<String, Object> attributeMap = new HashMap<>();

	public AbstractBasicRequest() {
		super();
	}

	public AbstractBasicRequest(Map<String, String[]> parameterMap) {
		super(parameterMap);
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributeMap.get(name);
	}

	public void setAttribute(String name, Object o) {
		attributeMap.put(name, o);
	}

	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributeMap.keySet());
	}

	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}
	
}
