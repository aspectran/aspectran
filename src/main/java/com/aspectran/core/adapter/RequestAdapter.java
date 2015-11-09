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
import java.util.Map;

import com.aspectran.core.activity.request.parameter.FileParameter;
import com.aspectran.core.context.rule.type.RequestMethodType;

/**
 * The Interface RequestAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface RequestAdapter {
	
	/**
	 * Gets the Adaptee object.
	 *
	 * @param <T> the generic type
	 * @return the Adaptee object
	 */
	public <T> T getAdaptee();
	
	/**
	 * Sets the adaptee.
	 *
	 * @param adaptee the new adaptee
	 */
	public void setAdaptee(Object adaptee);

	/**
	 * Gets the request method.
	 *
	 * @return the request method
	 */
	public RequestMethodType getRequestMethod();

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
	
	/**
	 * Gets the parameter names.
	 *
	 * @return the parameter names
	 */
	public Enumeration<String> getParameterNames();

	/**
	 * Gets the file parameter.
	 *
	 * @param name the parameter name
	 * @return the file parameter
	 */
	public FileParameter getFileParameter(String name);
	
	/**
	 * Gets the file parameter values.
	 *
	 * @param name the parameter name
	 * @return the file parameter values
	 */
	public FileParameter[] getFileParameterValues(String name);

	/**
	 * Sets the file parameter.
	 *
	 * @param name the parameter name
	 * @param fileParameter the file parameter
	 */
	public void setFileParameter(String name, FileParameter fileParameter);

	/**
	 * Sets the file parameter.
	 *
	 * @param name the parameter name
	 * @param fileParameters the file parameters
	 */
	public void setFileParameter(String name, FileParameter[] fileParameters);
	
	/**
	 * Gets the file parameter names.
	 *
	 * @return the parameter names
	 */
	public Enumeration<String> getFileParameterNames();
	
	/**
	 * Removes the file parameter.
	 *
	 * @param name the file parameter name
	 * @return the file parameter[]
	 */
	public FileParameter[] removeFileParameter(String name);
	
	/**
	 * Return a mutable Map of the request parameters,
	 * with parameter names as map keys and parameter values as map values.
	 * If the parameter value type is the String then map value will be of type String.
	 * If the parameter value type is the String array then map value will be of type String array.
	 *
	 * @return the parameter map
	 * 
	 * @since 1.4.0
	 */
	public Map<String, Object> getParameterMap();
	
	/**
	 * Returns the value of the named attribute as an <code>Object</code>, or <code>null</code> if no attribute of the given name exists.
	 *
	 * @param name a String specifying the name of the attribute
	 * @return an Object containing the value of the attribute, or null if the attribute does not exist
	 */
	public <T> T getAttribute(String name);
	
	/**
	 * Stores an attribute in this request.
	 *
	 * @param name specifying the name of the attribute
	 * @param value the Object to be stored
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
	
	/**
	 * Removes an attribute from this request.
	 *
	 * @param name a String specifying the name of the attribute to remove
	 */
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
