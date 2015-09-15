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
package com.aspectran.core.adapter;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import com.aspectran.core.activity.request.parameter.FileParameter;

/**
 * The Interface RequestAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface RequestAdapter {
	
	/**
	 * Gets the adaptee.
	 *
	 * @return the adaptee
	 */
	public <T> T getAdaptee();

	/**
	 * Gets the character encoding.
	 * 
	 * @return the character encoding
	 */
	public String getCharacterEncoding();
	
	/**
	 * Sets the character encoding.
	 *
	 * @param characterEncoding the new character encoding
	 */
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException;
	
	/**
	 * Gets the parameter.
	 *
	 * @param name the name
	 * @return the parameter
	 */
	public String getParameter(String name);
	
	/**
	 * Gets the parameter values.
	 *
	 * @param name the name
	 * @return the parameter values
	 */
	public String[] getParameterValues(String name);
	
	public Enumeration<String> getParameterNames();

	public FileParameter getFileParameter(String name);
	
	public FileParameter[] getFileParameterValues(String name);

	public void setFileParameter(String name, FileParameter fileParameter);

	public void setFileParameter(String name, FileParameter[] fileParameters);
	
	public Enumeration<String> getFileParameterNames();
	
	public FileParameter[] removeFileParameter(String name);
	
	/**
	 * Returns the value of the named attribute as an <code>Object</code>, or <code>null</code> if no attribute of the given name exists.
	 *
	 * @param name the name
	 * @return the attribute
	 */
	public <T> T getAttribute(String name);
	
	/**
	 * Stores an attribute in this request.
	 *
	 * @param name the name
	 * @param o the value
	 */
	public void setAttribute(String name, Object value);
	
	/**
	 * Returns an <code>Enumeration</code> containing the
	 * names of the attributes available to this request.
	 * This method returns an empty <code>Enumeration</code>
	 * if the request has no attributes available to it.
	 *
	 * @return the attribute names
	 */
	public Enumeration<String> getAttributeNames();
	
	public void removeAttribute(String name);

	/**
	 * Checks if is max length exceeded.
	 *
	 * @return true, if is max length exceeded
	 */
	public boolean isMaxLengthExceeded();

	/**
	 * Sets the max length exceeded.
	 *
	 * @param maxLengthExceeded the new max length exceeded
	 */
	public void setMaxLengthExceeded(boolean maxLengthExceeded);
	
}
