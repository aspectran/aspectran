/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.web.activity.multipart;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This class functions as a wrapper around HttpServletRequest to provide
 * working getParameter methods for multipart requests.
 * 
 * <p>Created: 2008. 04. 11 오후 1:47:48</p>
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {

	private MultipartRequestHandler handler;
	
	/**
	 * Instantiates a new multipart request wrapper.
	 * 
	 * @param handler the handler
	 * 
	 * @throws MultipartRequestException the multipart request exception
	 */
	public MultipartRequestWrapper(MultipartRequestHandler handler) throws MultipartRequestException {
		super(handler.getRequest());
		this.handler = handler;
		handler.parse();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterNames()
	 */
	@Override
	public Enumeration<String> getParameterNames() {
		return handler.getMultipartParameterNames();
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(String name) {
		return handler.getMultipartParameter(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
	 */
	@Override
	public String[] getParameterValues(String name) {
		return handler.getMultipartParameterValues(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameterMap()
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> map = new HashMap<String, String[]>();
		Enumeration<String> enumeration = getParameterNames();
		
		while(enumeration.hasMoreElements()) {
			String name = enumeration.nextElement();
			map.put(name, getParameterValues(name));
		}
		
		return map;
	}
	
	/**
	 * Gets the multipart item names.
	 * 
	 * @return the multipart item names
	 */
	public Enumeration<String> getMultipartItemNames() {
        return handler.getMultipartFileItemNames();
    }
    
	/**
	 * Gets the multipart file item.
	 * 
	 * @param name the name of the multipart file item
	 * 
	 * @return the multipart file item
	 */
	public MultipartFileItem getMultipartFileItem(String name) {
		return handler.getMultipartFileItem(name);
	}
	
	/**
	 * Gets the multipart file items.
	 * 
	 * @param name the name of the multipart file item
	 * 
	 * @return the multipart items
	 */
	public MultipartFileItem[] getMultipartFileItems(String name) {
		return handler.getMultipartFileItems(name);
	}

	/**
	 * Checks if is max length exceeded.
	 * 
	 * @return true, if is max length exceeded
	 */
	public boolean isMaxLengthExceeded() {
		return handler.isMaxLengthExceeded();
	}
}
