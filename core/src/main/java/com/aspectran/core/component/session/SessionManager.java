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

import com.aspectran.utils.scheduling.Scheduler;

import java.util.Set;

/**
 * Manages the lifecycle of sessions, including creation, retrieval, invalidation,
 * and persistence.
 *
 * <p>This interface defines the central component for session management in
 * Aspectran. It is responsible for coordinating the {@link SessionCache},
 * {@link SessionStore}, and {@link SessionIdGenerator} to provide a robust
 * session mechanism. It also manages session expiration and event listeners.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public interface SessionManager {

    /**
     * Returns the worker name for the session manager.
     * @return the worker name
     */
    String getWorkerName();

    /**
     * Returns the scheduler used for session-related tasks, such as scavenging.
     * @return the scheduler
     */
    Scheduler getScheduler();

    /**
     * Returns the session ID generator.
     * @return the session ID generator
     */
    SessionIdGenerator getSessionIdGenerator();

    /**
     * Returns the session cache.
     * @return the session cache
     */
    SessionCache getSessionCache();

    /**
     * Returns the default maximum inactive interval in seconds for sessions.
     * @return the default maximum idle time in seconds
     */
    int getDefaultMaxIdleSecs();

    /**
     * Sets the default maximum inactive interval in seconds for sessions.
     * @param defaultMaxIdleSecs the default maximum idle time in seconds
     */
    void setDefaultMaxIdleSecs(int defaultMaxIdleSecs);

    /**
     * Returns the maximum inactive interval in seconds for new sessions that have
     * no attributes.
     * @return the maximum idle time in seconds for new sessions
     */
    int getMaxIdleSecsForNew();

    /**
     * Retrieves an existing session by its ID.
     * @param id the session ID
     * @return the managed session, or {@code null} if no session exists with that ID
     */
    ManagedSession getSession(String id);

    /**
     * Creates a new session with the specified ID.
     * @param id the unique identifier for the new session
     * @return the newly created session
     */
    ManagedSession createSession(String id);

    /**
     * Creates a new, unique session ID.
     * @param seedTerm a seed value to initialize the random number generator
     * @return a unique session ID
     */
    String createSessionId(long seedTerm);

    /**
     * Renews the ID of an existing session.
     * @param oldId the current session ID
     * @param newId the new session ID to assign
     * @return the new session ID
     */
    String renewSessionId(String oldId, String newId);

    /**
     * Removes a session from the manager.
     * @param id the ID of the session to remove
     * @param invalidate if {@code true}, the session is invalidated before being
     *      removed; if {@code false}, it is only removed from the cache
     * @return the removed session, or {@code null} if the session was not found
     */
    ManagedSession removeSession(String id, boolean invalidate);

    /**
     * Removes a session from the manager with a specified reason for destruction.
     * @param id the ID of the session to remove
     * @param invalidate if {@code true}, the session is invalidated before being removed
     * @param reason the reason for the session's destruction
     * @return the removed session, or {@code null} if the session was not found
     */
    ManagedSession removeSession(String id, boolean invalidate, Session.DestroyedReason reason);

    /**
     * Invalidates the session with the specified ID due to a timeout.
     * @param id the ID of the session to invalidate
     */
    void invalidate(String id);

    /**
     * Invalidates the session with the specified ID for a given reason.
     * @param id the ID of the session to invalidate
     * @param reason the reason for invalidation
     */
    void invalidate(String id, Session.DestroyedReason reason);

    /**
     * Adds a listener for session-related events.
     * @param listener the session listener to add
     */
    void addSessionListener(SessionListener listener);

    /**
     * Removes a listener for session-related events.
     * @param listener the session listener to remove
     */
    void removeSessionListener(SessionListener listener);

    /**
     * Removes all registered session event listeners.
     */
    void clearSessionListeners();

    /**
     * Returns the set of IDs for sessions that are currently active in the cache.
     * This excludes sessions that may be persisted but are not currently in memory.
     * @return a set of active session IDs
     */
    Set<String> getActiveSessions();

    /**
     * Returns the set of IDs for all sessions known to the session store.
     * This includes both sessions active in the cache and those that are passivated.
     * @return a set of all session IDs
     */
    Set<String> getAllSessions();

    /**
     * Records the total time a session has been active.
     * This is typically called just before a session is invalidated.
     * @param session the session whose active time is to be recorded
     */
    void recordSessionTime(ManagedSession session);

    /**
     * Returns statistics about session usage.
     * @return the session statistics
     */
    SessionStatistics getStatistics();

}
