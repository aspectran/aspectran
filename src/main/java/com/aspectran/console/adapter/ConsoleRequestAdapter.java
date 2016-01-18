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

	private Map<String, String> parameters = new HashMap<String, String>();

	private Map<String, Object> attributes = new HashMap<String, Object>();

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
		return System.getProperty("file.encoding");
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String characterEncoding) {
		System.setProperty("file.encoding", characterEncoding);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		return parameters.get(name);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setParameter(java.lang.String, java.lang.String)
	 */
	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}

	/* (non-Javadoc)
         * @see com.aspectran.core.adapter.RequestAdapter#getParameterValues(java.lang.String)
         */
	public String[] getParameterValues(String name) {
		return parameters.values().toArray(new String[parameters.size()]);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttribute(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)attributes.get(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameterMap()
	 */
	public Map<String, Object> getParameterMap() {
		return new HashMap<String, Object>(parameters);
	}

}
