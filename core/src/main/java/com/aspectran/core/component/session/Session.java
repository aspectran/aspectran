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
package com.aspectran.core.component.session;

import java.util.Set;

/**
 * Represents a user session, providing a mechanism to store and retrieve
 * stateful data across a series of interactions.
 *
 * <p>This interface defines the contract for a session, which is independent
 * of the underlying execution environment (e.g., web, shell). It allows
 * applications to manage user-specific or task-specific state in a consistent manner.
 *
 * <p>Created: 2017. 6. 13.</p>
 */
public interface Session {

    /**
     * Returns the unique identifier assigned to this session.
     * @return a string specifying the identifier for this session
     */
    String getId();

    /**
     * Returns the object bound with the specified name in this session, or
     * {@code null} if no object is bound under the name.
     * @param <T> the type of the attribute
     * @param name a string specifying the name of the attribute
     * @return the object with the specified name
     */
    <T> T getAttribute(String name);

    /**
     * Returns a {@code Set} of {@code String} objects containing all the
     * names of the objects bound to this session.
     * @return a set of strings specifying the names of all the attributes
     */
    Set<String> getAttributeNames();

    /**
     * Binds an object to this session, using the name specified.
     * If an object of the same name is already bound to the session,
     * the object is replaced.
     * @param name the name to which the object is bound; cannot be null
     * @param value the object to be bound
     * @return the previous value associated with the specified name, or
     *      {@code null} if there was no previous value
     */
    Object setAttribute(String name, Object value);

    /**
     * Removes the object bound with the specified name from this session.
     * If the session does not have an object bound with the specified name,
     * this method does nothing.
     * @param name the name of the object to remove from this session
     * @return the value of the removed attribute, or {@code null} if no
     *      attribute was removed
     */
    Object removeAttribute(String name);

    /**
     * Returns the time when this session was created, measured in milliseconds
     * since midnight January 1, 1970 GMT.
     * @return a {@code long} specifying when this session was created
     */
    long getCreationTime();

    /**
     * Returns the last time the client sent a request associated with this
     * session, as the number of milliseconds since midnight January 1, 1970 GMT.
     * @return a {@code long} representing the last time the client sent a
     *      request associated with this session
     */
    long getLastAccessedTime();

    /**
     * Returns the maximum time interval, in seconds, that the session will be
     * kept open between client accesses.
     * @return an integer specifying the number of seconds
     */
    int getMaxInactiveInterval();

    /**
     * Specifies the time, in seconds, between client requests before the
     * session will be invalidated.
     * @param secs an integer specifying the number of seconds
     */
    void setMaxInactiveInterval(int secs);

    /**
     * Returns {@code true} if the client does not yet know about the session
     * or if the client chooses not to join the session.
     * A session is considered "new" if it has been created by the server but
     * the client has not yet acknowledged it.
     * @return {@code true} if the session has been created by the server but
     *      the client has not yet joined
     */
    boolean isNew();

    /**
     * Returns whether this session is temporarily resident in the session store.
     * @return true if this session is temporarily resident in the session store
     */
    boolean isTempResident();

    /**
     * Returns whether this session is still valid.
     * A session is valid if it has not been invalidated or expired.
     * @return {@code true} if this session is still valid; {@code false} otherwise
     */
    boolean isValid();

    /**
     * Called by the container when a request enters the session.
     * This updates the last accessed time.
     * @return {@code true} if the session was valid and access is permitted;
     *      {@code false} if the session is invalid
     */
    boolean access();

    /**
     * Called by the container when a request leaves the session.
     * This marks the session as no longer being actively accessed.
     */
    void complete();

    /**
     * Invalidates this session and unbinds any objects bound to it.
     * This can be called by the application, or by the session manager
     * when the session expires or the application is undeployed.
     */
    void invalidate();

    /**
     * Returns the reason why the session was destroyed.
     * @return the reason for destruction
     */
    DestroyedReason getDestroyedReason();

    /**
     * Enumerates the possible reasons for session destruction.
     */
    enum DestroyedReason {
        /**
         * The session was explicitly invalidated by a call to {@link #invalidate()}.
         */
        INVALIDATED,
        /**
         * The session timed out due to inactivity.
         */
        TIMEOUT,
        /**
         * The session was destroyed because the application context was undeployed.
         */
        UNDEPLOY
    }

}
