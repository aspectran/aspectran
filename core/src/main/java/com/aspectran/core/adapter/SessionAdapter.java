/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.component.bean.scope.SessionScope;

import java.util.Enumeration;

/**
 * The Interface SessionAdapter.
 *
 * @since 2011. 3. 13.
 */
public interface SessionAdapter {

    /**
     * Returns the adaptee object to provide session information.
     *
     * @param <T> the type of the adaptee object
     * @return the adaptee object
     */
    <T> T getAdaptee();

    /**
     * Gets the session scope.
     *
     * @return the session scope
     */
    SessionScope getSessionScope();

    /**
     * Returns a string containing the unique identifier assigned to this session.
     * The identifier is assigned by the servlet container and is implementation dependent.
     *
     * @return a string specifying the identifier assigned to this session
     * @since 1.5.0
     */
    String getId();

    boolean isNew();

    /**
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @return a long specifying when this session was created,
     *         expressed in milliseconds since 1/1/1970 GMT
     * @since 1.5.0
     */
    long getCreationTime();

    /**
     * Returns the last time the client sent a request associated with this session,
     * as the number of milliseconds since midnight January 1, 1970 GMT,
     * and marked by the time the container received the request.
     *
     * Actions that your application takes, such as getting or setting a value associated with the session,
     * do not affect the access time.
     *
     * @return a long representing the last time the client sent a request associated with this session,
     *         expressed in milliseconds since 1/1/1970 GMT
     * @since 1.5.0
     */
    long getLastAccessedTime();

    void setMaxInactiveInterval(int interval);

    /**
     * Returns the maximum time interval, in seconds, that the servlet container will keep
     * this session open between client accesses.
     * After this interval, the servlet container will invalidate the session.
     * The maximum time interval can be set with the {@code setMaxInactiveInterval} method.
     * A negative time indicates the session should never timeout.
     *
     * @return an integer specifying the number of seconds this session
     *         remains open between client requests
     * @since 1.5.0
     */
    int getMaxInactiveInterval();

    /**
     * Returns an Enumeration of String objects containing the names
     * of all the objects bound to this session.
     *
     * @return an Enumeration of String objects specifying the names
     *         of all the objects bound to this session
     * @since 1.5.0
     */
    Enumeration<String> getAttributeNames();

    /**
     * Returns the value of the named attribute as a given type,
     * or {@code null} if no attribute of the given name exists.
     *
     * @param <T> the generic type
     * @param name a {@code String} specifying the name of the attribute
     * @return an {@code Object} containing the value of the attribute,
     *         or {@code null} if the attribute does not exist
     */
    <T> T getAttribute(String name);

    /**
     * Stores an attribute in this session.
     *
     * @param name specifying the name of the attribute
     * @param value the {@code Object} to be stored
     */
    void setAttribute(String name, Object value);

    /**
     * Removes the object bound with the specified name from this session.
     * If the session does not have an object bound with the specified name,
     * this method does nothing.
     *
     * @param name the name of the object to remove from this session
     */
    void removeAttribute(String name);

    /**
     * Invalidates this session then unbinds any objects bound to it.
     */
    void invalidate();

}
