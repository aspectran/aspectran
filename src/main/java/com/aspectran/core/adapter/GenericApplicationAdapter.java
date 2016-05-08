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
package com.aspectran.core.adapter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class GenericApplicationAdapter.
 * 
 * @since 2016. 3. 26.
 */
public class GenericApplicationAdapter extends AbstractApplicationAdapter {

	private Map<String, Object> attributes = new HashMap<>();

	/**
	 * Instantiates a new GenericApplicationAdapter.
	 */
	public GenericApplicationAdapter() {
		super(null);
	}
	
	/**
	 * Instantiates a new GenericApplicationAdapter.
	 *
	 * @param adaptee the adaptee
	 */
	public GenericApplicationAdapter(Object adaptee) {
		super(adaptee);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

}
