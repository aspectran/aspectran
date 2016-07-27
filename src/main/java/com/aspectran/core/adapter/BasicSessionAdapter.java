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

import com.aspectran.core.context.bean.scope.SessionScope;

/**
 * The Class BasicSessionAdapter.
 * 
 * @since 2.3.0
 */
public abstract class BasicSessionAdapter extends AbstractSessionAdapter {
	
	private final SessionScope scope = new SessionScope();

	private final Map<String, Object> attributes = new HashMap<>();

	/**
	 * Instantiates a new BasicApplicationAdapter.
	 */
	public BasicSessionAdapter() {
		super(null);
	}
	
	/**
	 * Instantiates a new BasicApplicationAdapter.
	 *
	 * @param adaptee the adaptee
	 */
	public BasicSessionAdapter(Object adaptee) {
		super(adaptee);
	}

	@Override
	public SessionScope getSessionScope() {
		return scope;
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
