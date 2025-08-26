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
 * An in-memory cache for active sessions.
 *
 * <p>This interface defines the contract for managing a set of {@link Session}
 * objects in memory to improve performance by reducing the need to access the
 * underlying {@link SessionStore}. It handles session loading, caching, eviction,
 * and persistence policies.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public interface SessionCache {

    /** Eviction policy: never evict sessions from the cache. */
    int NEVER_EVICT = -1;

    /** Eviction policy: evict sessions when they are no longer in use by any request. */
    int EVICT_ON_SESSION_EXIT = 0;

    /** Eviction policy: evict sessions after a period of inactivity. */
    int EVICT_ON_INACTIVITY = 1;

    /**
     * Checks if the session manager is running in a clustered environment.
     * @return true if clustering is enabled, false otherwise
     */
    boolean isClusterEnabled();

    /**
     * Returns the maximum number of active sessions allowed in this cache.
     * @return the maximum number of active sessions, or 0 for no limit
     */
    int getMaxActiveSessions();

    /**
     * Sets the maximum number of active sessions allowed in this session cache.
     * If the number of active sessions exceeds this limit, attempts to create
     * new sessions will be rejected.
     * @param maxActiveSessions the maximum number of active sessions; 0 for no limit
     */
    void setMaxActiveSessions(int maxActiveSessions);

    /**
     * Returns the idle time in seconds after which a session may be evicted from the cache.
     * @return the eviction timeout in seconds
     */
    int getEvictionIdleSecs();

    /**
     * Returns the idle time in seconds for new, empty sessions before they may be evicted.
     * @return the eviction timeout in seconds for new sessions
     */
    int getEvictionIdleSecsForNew();

    /**
     * Checks if newly created sessions should be saved to the store immediately.
     * @return true if sessions are saved upon creation, false otherwise
     */
    boolean isSaveOnCreate();

    /**
     * Checks if a session should be saved to the store before being evicted due to inactivity.
     * @return true if sessions are saved before eviction, false otherwise
     */
    boolean isSaveOnInactiveEviction();

    /**
     * Sets the policy for whether a session should be saved before being evicted due to inactivity.
     * @param saveOnEvict true to save sessions before eviction, false otherwise
     */
    void setSaveOnInactiveEviction(boolean saveOnEvict);

    /**
     * Checks if sessions that cannot be loaded from the store should be removed.
     * @return true to remove unloadable sessions, false otherwise
     */
    boolean isRemoveUnloadableSessions();

    /**
     * Sets the policy for whether to delete session data from the store if it is unreadable.
     * @param removeUnloadableSessions true to delete unreadable session data, false otherwise
     */
    void setRemoveUnloadableSessions(boolean removeUnloadableSessions);

    /**
     * Retrieves a session from the cache, loading it from the {@link SessionStore} if necessary.
     * @param id the session ID
     * @return the managed session, or null if not found
     * @throws Exception if an error occurs during loading
     */
    ManagedSession get(String id) throws Exception;

    /**
     * Adds a new session to the cache.
     * @param id the session ID
     * @param time the creation timestamp
     * @param inactiveInterval the maximum inactive interval in milliseconds
     * @return the newly created session
     * @throws Exception if the maximum number of active sessions is exceeded
     */
    ManagedSession add(String id, long time, long inactiveInterval) throws Exception;

    /**
     * Refreshes a session's data from the underlying store.
     * This is typically used in a clustered environment to ensure data consistency.
     * @param session the session to refresh
     * @throws Exception if an error occurs during refresh
     */
    void refresh(ManagedSession session) throws Exception;

    /**
     * Releases a session after a request has finished using it.
     * The cache may use this event to trigger writing session data to the store.
     * @param session the session to release
     * @throws Exception if an error occurs during release
     */
    void release(ManagedSession session) throws Exception;

    /**
     * Checks if a session exists, consulting the {@link SessionStore}.
     * @param id the session ID
     * @return true if the session exists in the store, false otherwise
     * @throws Exception if an error occurs
     */
    boolean exists(String id) throws Exception;

    /**
     * Checks if a session is present in the in-memory cache.
     * This method does NOT consult the {@link SessionStore}.
     * @param id the session ID
     * @return true if the session is in the cache, false otherwise
     * @throws Exception if an error occurs
     */
    boolean contains(String id) throws Exception;

    /**
     * Deletes a session from both the cache and the {@link SessionStore}.
     * @param id the session ID
     * @return the session that was deleted, or null if not found
     * @throws Exception if an error occurs during deletion
     */
    ManagedSession delete(String id) throws Exception;

    /**
     * Renews the ID of a session in both the cache and the {@link SessionStore}.
     * @param oldId the current session ID
     * @param newId the new session ID
     * @return the session with its ID renewed
     * @throws Exception if an error occurs
     */
    ManagedSession renewSessionId(String oldId, String newId) throws Exception;

    /**
     * Checks a set of candidate session IDs for expiration.
     * This involves checking both the in-memory cache and the authoritative {@link SessionStore}.
     * @param candidates the set of session IDs to check
     * @return the set of session IDs that have been confirmed as expired
     */
    Set<String> checkExpiration(Set<String> candidates);

    /**
     * Checks a specific session to determine if it should be evicted or expired.
     * @param session the session to check
     * @return true if the session was evicted, false otherwise
     */
    boolean checkInactiveSession(ManagedSession session);

    /**
     * Removes all unmanaged (orphan) sessions that expired at or before the given time.
     * @param time the time before which the sessions must have expired
     */
    void cleanOrphans(long time);

    /**
     * Returns the set of IDs for sessions currently active in the cache.
     * @return a set of active session IDs
     */
    Set<String> getActiveSessions();

    /**
     * Returns the set of IDs for all sessions known to the session store.
     * @return a set of all session IDs in the store
     */
    Set<String> getAllSessions();

}
