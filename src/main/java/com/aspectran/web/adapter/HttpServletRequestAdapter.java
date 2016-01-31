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
package com.aspectran.web.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * The Class HttpServletRequestAdapter.
 * 
 * @since 2011. 3. 13.
 * @author Juho Jeong
 */
public class HttpServletRequestAdapter extends AbstractRequestAdapter implements RequestAdapter {
	
	/**
	 * Instantiates a new HttpServletRequestAdapter.
	 *
	 * @param request the HTTP request
	 */
	public HttpServletRequestAdapter(HttpServletRequest request) {
		super(request);
		
		setRequestMethod(RequestMethodType.valueOf(request.getMethod()));
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return ((HttpServletRequest)adaptee).getCharacterEncoding();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		((HttpServletRequest)adaptee).setCharacterEncoding(characterEncoding);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		return ((HttpServletRequest)adaptee).getParameter(name);
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setParameter(java.lang.String, java.lang.String)
	 */
	public void setParameter(String name, String value) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		return ((HttpServletRequest)adaptee).getParameterValues(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameterNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<String> getParameterNames() {
		return ((HttpServletRequest)adaptee).getParameterNames();
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttribute(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)((HttpServletRequest)adaptee).getAttribute(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		((HttpServletRequest)adaptee).setAttribute(name, o);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttributeNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<String> getAttributeNames() {
		return ((HttpServletRequest)adaptee).getAttributeNames();
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		((HttpServletRequest)adaptee).removeAttribute(name);
	}
	
	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getParameterMap()
	 */
	public Map<String, Object> getParameterMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		Enumeration<String> enm = getParameterNames();
		
	    while(enm.hasMoreElements()) {
	        String name = enm.nextElement();
	        String[] values = getParameterValues(name);
			if(values != null && values.length == 1) {
				params.put(name, values[0]);
			} else {
				params.put(name, values);
			}
	    }
	    
	    return params;
	}

	/* (non-Javadoc)
	 * @see com.aspectran.core.adapter.RequestAdapter#getAttributeMap()
	 */
	public Map<String, Object> getAttributeMap() {
		Map<String, Object> attrs = new HashMap<String, Object>();
		Enumeration<String> enm = getAttributeNames();

		while(enm.hasMoreElements()) {
			String name = enm.nextElement();
			Object value = getAttribute(name);
			attrs.put(name, value);
		}

		return attrs;
	}

	@Override
	public Locale getLocale() {
		if(super.locale != null)
			return super.locale;

		return ((HttpServletRequest)adaptee).getLocale();
	}

}
