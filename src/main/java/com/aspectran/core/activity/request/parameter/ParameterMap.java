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
package com.aspectran.core.activity.request.parameter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class ParameterMap.
 * 
 * <p>Created: 2008. 06. 11 PM 8:55:13</p>
 */
public class ParameterMap extends LinkedHashMap<String, String[]> {

	/** @serial */
	private static final long serialVersionUID = 1709146569240133920L;

	public ParameterMap() {
		super();
	}

	public ParameterMap(Map<String, String[]> params) {
		super(params);
	}

	/**
	 * Returns the string value to which the specified name is mapped,
	 * or null if this map contains no mapping for the name.
	 * 
	 * @param name the parameter name
	 * @return the string
	 */
	public String getParameter(String name) {
		String[] values = get(name);
		return (values != null && values.length > 0 ? values[0] : null);
	}
	
	/**
	 * Returns the string values to which the specified name is mapped,
	 * or null if this map contains no mapping for the name.
	 *
	 * @param name the parameter name
	 * @return the string
	 */
	public String[] getParameterValues(String name) {
		return get(name);
	}

	/**
	 * Sets the parameter.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setParameter(String name, String value) {
		String[] values = new String[] { value };
		put(name, values);
	}

	/**
	 * Set parameter name and value.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public void setParameter(String name, String[] values) {
		put(name, values);
	}

	/**
	 * Returns the parameters names.
	 *
	 * @return the parameter names
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(keySet());
	}

}
