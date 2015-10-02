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
 * <p>Created: 2015. 10. 01</p>
 */
public class ParameterMap extends LinkedHashMap<String, String> {

	/** @serial */
	private static final long serialVersionUID = -7721113073663638892L;

	public ParameterMap() {
		super();
	}

	/**
	 * Instantiates a new parameter map.
	 *
	 * @param map the map
	 */
	public ParameterMap(Map<String, String> map) {
		super(map);
	}
	
}
