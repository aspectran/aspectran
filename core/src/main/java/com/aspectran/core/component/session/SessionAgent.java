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

import java.util.Collections;
import java.util.Enumeration;

/**
 * Acts as a delegate for session management on a per-request basis.
 * It provides a simplified interface for accessing and manipulating the session
 * associated with the current activity.
 *
 * <p>Created: 2017. 9. 10.</p>
 */
public class SessionAgent {

    private final SessionManager sessionManager;

    private volatile String sessionId;

    /**
     * Instantiates a new SessionAgent.
     * @param sessionManager the session manager
     */
    public SessionAgent(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Returns the unique identifier assigned to this session.
     * @return the session id
     */
    public String getId() {
        return getSession(true).getId();
    }

    /**
     * Returns the time when this session was created, measured in milliseconds
     * since midnight January 1, 1970 GMT.
     * @return the session creation time
     */
    public long getCreationTime() {
        return getSession(true).getCreationTime();
    }

    /**
     * Returns the last time the client sent a request associated with this session,
     * as the number of milliseconds since midnight January 1, 1970 GMT.
     * @return the last accessed time
     */
    public long getLastAccessedTime() {
        return getSession(true).getLastAccessedTime();
    }

    /**
     * Returns the maximum time interval, in seconds, that the session will be kept open
     * between client accesses.
     * @return the maximum inactive interval
     */
    public int getMaxInactiveInterval() {
        return getSession(true).getMaxInactiveInterval();
    }

    /**
     * Specifies the time, in seconds, between client requests before the session
     * will be invalidated.
     * @param secs the maximum inactive interval in seconds
     */
    public void setMaxInactiveInterval(int secs) {
        getSession(true).setMaxInactiveInterval(secs);
    }

    /**
     * Returns an {@link Enumeration} of {@link String} objects containing the names of all the
     * objects bound to this session.
     * @return an enumeration of attribute names
     */
    public Enumeration<String> getAttributeNames() {
        Session session = getSession(false);
        if (session == null) {
            return null;
        }
        return Collections.enumeration(session.getAttributeNames());
    }

    /**
     * Returns the object bound with the specified name in this session, or
     * {@code null} if no object is bound under the name.
     * @param <T> the type of the attribute
     * @param name a string specifying the name of the object
     * @return the object with the specified name
     */
    public <T> T getAttribute(String name) {
        Session session = getSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute(name);
    }

    /**
     * Binds an object to this session, using the name specified.
     * @param name the name to which the object is bound
     * @param value the object to be bound
     */
    public void setAttribute(String name, Object value) {
        getSession(true).setAttribute(name, value);
    }

    /**
     * Removes the object bound with the specified name from this session.
     * @param name the name of the object to remove from this session
     */
    public void removeAttribute(String name) {
        Session session = getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    /**
     * Returns the current session associated with this request, or if the
     * request does not have a session, creates one.
     * @param create {@code true} to create a new session for this request if necessary;
     *               {@code false} to return {@code null} if there's no current session
     * @return the session associated with this request or {@code null} if {@code create} is
     *         {@code false} and the request has no valid session
     */
    public Session getSession(boolean create) {
        if (sessionId == null && !create) {
            return null;
        }
        if (sessionId != null) {
            Session session = sessionManager.getSession(sessionId);
            if (session == null && create) {
                session = createSession();
            }
            return session;
        } else {
            return createSession();
        }
    }

    /**
     * Creates a new session.
     * @return the new session
     */
    private Session createSession() {
        sessionId = sessionManager.createSessionId(hashCode());
        return sessionManager.createSession(sessionId);
    }

    /**
     * Invalidates this session then unbinds any objects bound to it.
     */
    public void invalidate() {
        Session session = getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Checks whether the session is still valid.
     * @return {@code true} if the session is valid, {@code false} otherwise
     */
    public boolean isValid() {
        Session session = getSession(false);
        return (session != null && session.isValid());
    }

    /**
     * Returns {@code true} if the client does not yet know about the session
     * or if the client chooses not to join the session.
     * @return {@code true} if the session is new, {@code false} otherwise
     */
    public boolean isNew() {
        Session session = getSession(false);
        return (session == null || session.isNew());
    }

    /**
     * Called when a session is first accessed by an {@code Activity}.
     */
    public void access() {
        Session session = getSession(false);
        if (session != null) {
            session.access();
        }
    }

    /**
     * Called when a session is last accessed by an {@code Activity}.
     */
    public void complete() {
        Session session = getSession(false);
        if (session != null) {
            session.complete();
        }
    }

}
