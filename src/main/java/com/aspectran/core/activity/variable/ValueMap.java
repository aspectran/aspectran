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
package com.aspectran.core.activity.variable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2008. 06. 11 오후 8:55:13</p>
 */
public class ValueMap extends LinkedHashMap<String, Object> {

	/** @serial */
	static final long serialVersionUID = -8779174783802247545L;

	public ValueMap() {
		super();
	}

	/**
	 * Instantiates a new value map.
	 * 
	 * @param map the map
	 */
	public ValueMap(Map<String, Object> map) {
		super(map);
	}
	
	/**
	 * Gets the string.
	 * 
	 * @param key the key
	 * 
	 * @return the string
	 */
	public String getString(String key) {
		Object o = get(key);
		
		if(o == null)
			return null;
		
		return o.toString();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue(String key) {
		return (T)get(key);
	}
	
}
