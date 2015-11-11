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
package com.aspectran.core.adapter;

import java.util.Enumeration;

import com.aspectran.core.context.bean.scope.SessionScope;

/**
 * The Interface SessionAdapter.
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface SessionAdapter {

	/**
	 * Gets the Adaptee object.
	 *
	 * @param <T> the generic type
	 * @return the Adaptee object
	 */
	public <T> T getAdaptee();
	
	/**
	 * Gets the session scope.
	 *
	 * @return the session scope
	 */	
	public SessionScope getSessionScope();
	
	/**
	 * Returns a string containing the unique identifier assigned to this session. The identifier is assigned by the servlet container and is implementation dependent.
	 *
	 * @return a string specifying the identifier assigned to this session
	 * 
	 * @since 1.5.0
	 */
	public String getId();
	
	/**
	 * Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT.
	 *
	 * @return a long specifying when this session was created, expressed in milliseconds since 1/1/1970 GMT
	 * 
	 * @since 1.5.0
	 */
	public long getCreationTime();
	
	/**
	 * Returns the last time the client sent a request associated with this session,
	 * as the number of milliseconds since midnight January 1, 1970 GMT,
	 * and marked by the time the container received the request.
	 * 
	 * Actions that your application takes, such as getting or setting a value associated with the session, do not affect the access time. 
	 *
	 * @return a long representing the last time the client sent a request associated with this session, expressed in milliseconds since 1/1/1970 GMT
	 * 
	 * @since 1.5.0
	 */
	public long getLastAccessedTime();
	
	/**
	 * Returns the maximum time interval, in seconds, that the servlet container will keep this session open between client accesses.
	 * After this interval, the servlet container will invalidate the session.
	 * The maximum time interval can be set with the setMaxInactiveInterval method.
	 * A negative time indicates the session should never timeout.
	 *
	 * @return an integer specifying the number of seconds this session remains open between client requests
	 * 
	 * @since 1.5.0
	 */
	public int getMaxInactiveInterval();
	
	/**
	 * Returns an Enumeration of String objects containing the names of all the objects bound to this session.
	 *
	 * @return an Enumeration of String objects specifying the names of all the objects bound to this session
	 * 
	 * @since 1.5.0
	 */
	public Enumeration<String> getAttributeNames();
	
	/**
	 * Gets the attribute.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the attribute
	 */
	public <T> T getAttribute(String name);

	/**
	 * Sets the attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setAttribute(String name, Object value);
	
	/**
	 * Invalidates this session then unbinds any objects bound to it.
	 */
	public void invalidate();
	
	/**
	 * No longer use the adaptee object.
	 */
	public void release();
	
}
