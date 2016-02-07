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
package com.aspectran.console.adapter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.aspectran.console.activity.ConsoleActivity;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;

/**
 * The Class ConsoleRequestAdapter.
 * 
 * @author Juho Jeong
 * @since 2016. 1. 18.
 */
public class ConsoleRequestAdapter extends AbstractRequestAdapter implements RequestAdapter {

	protected static final String FILE_ENCODING_PROP_NAME = "file.encoding";
	
	private Map<String, Object> parameterMap = new HashMap<String, Object>();

	private Map<String, Object> attributeMap = new HashMap<String, Object>();

	/**
	 * Instantiates a new ConsoleRequestAdapter.
	 *
	 * @param activity the console activity
	 */
	public ConsoleRequestAdapter(ConsoleActivity activity) {
		super(activity);
	}

	@Override
	public String getCharacterEncoding() {
		return System.getProperty(FILE_ENCODING_PROP_NAME);
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) {
		System.setProperty(FILE_ENCODING_PROP_NAME, characterEncoding);
	}

	@Override
	public String getParameter(String name) {
		Object value = parameterMap.get(name);
		if(value == null)
			return null;

		return value.toString();
	}

	@Override
	public void setParameter(String name, String value) {
		parameterMap.put(name, value);
	}

	@Override
	public String[] getParameterValues(String name) {
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameterMap.keySet());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributeMap.get(name);
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributeMap.put(name, o);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributeMap.keySet());
	}

	@Override
	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}

}
