/*
 * Copyright (c) 2008-present The Aspectran Project
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
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an abstraction for a user session within a specific runtime environment.
 *
 * <p>Implementations of this interface encapsulate a container-specific session object
 * (e.g., {@code HttpSession} in a web environment), exposing a consistent API for
 * managing session identity, lifecycle, attributes, and a lazily created
 * {@link SessionScope}. This allows session handling logic to remain uniform
 * across different execution contexts.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface SessionAdapter {

    /**
     * Returns the underlying native session object that this adapter wraps.
     * @param <T> the type of the adaptee object
     * @return the adaptee object
     */
    <T> T getAdaptee();

    /**
     * Returns the session scope associated with this session.
     * The session scope is used to manage session-scoped beans.
     * @param create if {@code true}, a new session scope will be created if one does not exist
     * @return the session scope, or {@code null} if {@code create} is false and no scope exists
     */
    SessionScope getSessionScope(boolean create);

    /**
     * Returns the unique identifier assigned to this session.
     * @return the session ID
     * @since 1.5.0
     */
    String getId();

    /**
     * Returns the time this session was created, in milliseconds since the epoch.
     * @return the session creation time
     * @since 1.5.0
     */
    long getCreationTime();

    /**
     * Returns the last time the client sent a request associated with this session,
     * in milliseconds since the epoch.
     * @return the last accessed time
     * @since 1.5.0
     */
    long getLastAccessedTime();

    /**
     * Sets the maximum time interval, in seconds, that the session will be kept open
     * between client accesses. A negative or zero value indicates that the session
     * should never time out.
     * @param interval the maximum inactive interval, in seconds
     */
    void setMaxInactiveInterval(int interval);

    /**
     * Returns the maximum time interval, in seconds, that the session will be kept open
     * between client accesses.
     * @return the maximum inactive interval, in seconds
     * @since 1.5.0
     */
    int getMaxInactiveInterval();

    /**
     * Returns an enumeration of all attribute names bound to this session.
     * @return an enumeration of attribute names, or {@code null} if the session is invalid
     * @since 1.5.0
     */
    @Nullable
    Enumeration<String> getAttributeNames();

    /**
     * Returns an unmodifiable {@code Set} of all attribute names bound to this session.
     * @return an unmodifiable set of attribute names, or an empty set if the session is invalid
     */
    default Set<String> getAttributeNameSet() {
        Enumeration<String> names = getAttributeNames();
        if (names == null) {
            return Collections.emptySet();
        }
        return Collections.list(names).stream().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns whether an attribute with the specified name exists in this session.
     * @param name the name of the attribute to check
     * @return {@code true} if the attribute exists; {@code false} otherwise
     */
    default boolean containsAttribute(String name) {
        return (getAttribute(name) != null);
    }

    /**
     * Returns the session attribute with the specified name.
     * @param <T> the type of the attribute
     * @param name the name of the attribute
     * @return the attribute value, or {@code null} if the attribute is not found
     */
    <T> T getAttribute(String name);

    /**
     * Returns the session attribute with the specified name, or a default value if not found.
     * @param <T> the type of the attribute
     * @param name the name of the attribute
     * @param defaultValue the default value to return if the attribute is not found
     * @return the attribute value, or {@code defaultValue} if not found
     */
    default <T> T getAttribute(String name, T defaultValue) {
        T value = getAttribute(name);
        return (value != null ? value : defaultValue);
    }

    /**
     * Binds an object as an attribute to this session.
     * @param name the name of the attribute
     * @param value the attribute value
     */
    void setAttribute(String name, Object value);

    /**
     * Removes the attribute with the specified name from this session.
     * @param name the name of the attribute to remove
     */
    void removeAttribute(String name);

    /**
     * Removes all attributes currently bound to this session.
     */
    default void clear() {
        Enumeration<String> names = getAttributeNames();
        if (names != null) {
            for (String name : Collections.list(names)) {
                removeAttribute(name);
            }
        }
    }

    /**
     * Invalidates this session and unbinds any objects bound to it.
     */
    void invalidate();

    /**
     * Checks if this session is still valid.
     * A session is valid if it has not been invalidated.
     * @return {@code true} if the session is valid, {@code false} otherwise
     */
    boolean isValid();

    /**
     * Checks if this session was newly created for the current request.
     * A session is considered new if it was created by the server but the client
     * has not yet acknowledged it.
     * @return {@code true} if the session is new, {@code false} otherwise
     */
    boolean isNew();

    /**
     * Returns the remaining time in milliseconds until this session becomes inactive.
     * A negative value indicates that the session never times out.
     * @return the remaining time in milliseconds
     */
    default long getRemainingInactiveInterval() {
        int maxInactiveInterval = getMaxInactiveInterval();
        if (maxInactiveInterval <= 0) {
            return -1L;
        }
        long lastAccessedTime = getLastAccessedTime();
        if (lastAccessedTime <= 0L) {
            return maxInactiveInterval * 1000L;
        }
        long remaining = (lastAccessedTime + (maxInactiveInterval * 1000L)) - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }

    /**
     * Changes the current session ID to a new one and returns the new session ID.
     * @return the new session ID, or {@code null} if the session is invalid or cannot be changed
     */
    String changeSessionId();

}
