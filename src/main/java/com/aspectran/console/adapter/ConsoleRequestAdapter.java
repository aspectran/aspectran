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
package com.aspectran.console.adapter;

import com.aspectran.console.activity.ConsoleActivity;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;

import java.util.*;

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
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return System.getProperty(FILE_ENCODING_PROP_NAME);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String characterEncoding) {
		System.setProperty(FILE_ENCODING_PROP_NAME, characterEncoding);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		Object value = parameterMap.get(name);

		if(value == null)
			return null;

		return value.toString();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setParameter(java.lang.String, java.lang.String)
	 */
	public void setParameter(String name, String value) {
		parameterMap.put(name, value);
	}

	/* (non-Javadoc)
         * @see com.aspectran.core.adapter.RequestAdapter#getParameterValues(java.lang.String)
         */
	public String[] getParameterValues(String name) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameterMap.keySet());
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttribute(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributeMap.get(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		attributeMap.put(name, o);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributeMap.keySet());
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		attributeMap.remove(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameterMap()
	 */
	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttributeMap()
	 */
	public Map<String, Object> getAttributeMap() {
		return attributeMap;
	}

}
