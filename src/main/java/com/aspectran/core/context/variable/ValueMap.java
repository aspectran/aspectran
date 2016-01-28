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
package com.aspectran.core.context.variable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class ValueMap.
 * 
 * <p>Created: 2008. 06. 11 PM 8:55:13</p>
 */
public class ValueMap extends LinkedHashMap<String, Object> {

	/** @serial */
	static final long serialVersionUID = -8779174783802247545L;

	public ValueMap() {
		super();
	}

	/**
	 * Instantiates a new ValueMap.
	 * 
	 * @param map the map
	 */
	public ValueMap(Map<String, Object> map) {
		super(map);
	}
	
	/**
	 * Returns the string value to which the specified key is mapped, or null if this map contains no mapping for the key.
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
	
	/**
	 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the value
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(String key) {
		return (T)get(key);
	}
	
}
