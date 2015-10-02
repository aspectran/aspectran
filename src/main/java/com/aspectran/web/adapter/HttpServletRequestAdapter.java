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
package com.aspectran.web.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * The Class HttpServletRequestAdapter.
 * 
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public class HttpServletRequestAdapter extends AbstractRequestAdapter implements RequestAdapter {
	
	/**
	 * Instantiates a new http servlet request adapter.
	 *
	 * @param request the request
	 */
	public HttpServletRequestAdapter(HttpServletRequest request) {
		super(request);
		
		setRequestMethod(RequestMethodType.valueOf(request.getMethod()));
	}
	
	public String getCharacterEncoding() {
		return ((HttpServletRequest)adaptee).getCharacterEncoding();
	}
	
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		((HttpServletRequest)adaptee).setCharacterEncoding(characterEncoding);
	}
	
	public String getParameter(String name) {
		return ((HttpServletRequest)adaptee).getParameter(name);
	}
	
	public String[] getParameterValues(String name) {
		return ((HttpServletRequest)adaptee).getParameterValues(name);
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<String> getParameterNames() {
		return ((HttpServletRequest)adaptee).getParameterNames();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T)((HttpServletRequest)adaptee).getAttribute(name);
	}
	
	public void setAttribute(String name, Object o) {
		((HttpServletRequest)adaptee).setAttribute(name, o);
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<String> getAttributeNames() {
		return ((HttpServletRequest)adaptee).getAttributeNames();

	}

	public void removeAttribute(String name) {
		((HttpServletRequest)adaptee).removeAttribute(name);
	}

}
