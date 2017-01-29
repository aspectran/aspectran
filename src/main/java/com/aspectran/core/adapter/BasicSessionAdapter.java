/**
 * Copyright 2008-2017 Juho Jeong
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
	
	private final Map<String, Object> attributes = new HashMap<>();

	private final SessionScope sessionScope = new SessionScope();

	/**
	 * Instantiates a new BasicSessionAdapter.
	 *
	 * @param adaptee the adaptee object
	 */
	public BasicSessionAdapter(Object adaptee) {
		super(adaptee);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public SessionScope getSessionScope() {
		return sessionScope;
	}

}
