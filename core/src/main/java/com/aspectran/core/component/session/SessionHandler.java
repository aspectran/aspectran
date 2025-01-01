/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.component.Component;
import com.aspectran.utils.thread.Scheduler;

import java.util.Set;

/**
 * The session handler is responsible for session start, session exists, session write,
 * time to live and session destroy.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public interface SessionHandler extends Component {

    String getWorkerName();

    Scheduler getScheduler();

    SessionIdGenerator getSessionIdGenerator();

    SessionCache getSessionCache();

    int getDefaultMaxIdleSecs();

    void setDefaultMaxIdleSecs(int defaultMaxIdleSecs);

    /**
     * Get a known existing session.
     * @param id the session id
     * @return a Session or null if none exists
     */
    ManagedSession getSession(String id);

    /**
     * Create an entirely new Session.
     * @param id identity of session to create
     * @return the new session object
     */
    ManagedSession createSession(String id);

    /**
     * Create a new Session ID.
     * @param seedTerm the seed for RNG
     * @return the new session id
     */
    String createSessionId(long seedTerm);

    /**
     * Change the id of a Session.
     * @param oldId the current session id
     * @param newId the new session id
     * @return the Session after changing its id
     */
    String renewSessionId(String oldId, String newId);

    /**
     * Remove session from manager.
     * @param id the session to remove
     * @param invalidate if false, only remove from cache
     * @return if the session was removed
     */
    ManagedSession removeSession(String id, boolean invalidate);

    ManagedSession removeSession(String id, boolean invalidate, Session.DestroyedReason reason);

    /**
     * Called when a session has expired.
     * @param id the id to invalidate
     */
    void invalidate(String id);

    void invalidate(String id, Session.DestroyedReason reason);

    /**
     * Adds an event listener for session-related events.
     * @param listener the session listener
     * @see #removeSessionListener(SessionListener)
     */
    void addSessionListener(SessionListener listener);

    /**
     * Removes an event listener for session-related events.
     * @param listener the session listener to remove
     * @see #addSessionListener(SessionListener)
     */
    void removeSessionListener(SessionListener listener);

    /**
     * Removes all event listeners for session-related events.
     * @see #removeSessionListener(SessionListener)
     */
    void clearSessionListeners();

    /**
     * @return the identifiers of those sessions that are active on this node, excluding passivated sessions
     */
    Set<String> getActiveSessions();

    /**
     * @return the identifiers of all sessions, including both active and passive
     */
    Set<String> getAllSessions();

    /**
     * Record length of time session has been active. Called when the
     * session is about to be invalidated.
     * @param session the session whose time to record
     */
    void recordSessionTime(ManagedSession session);

    SessionStatistics getStatistics();

}
