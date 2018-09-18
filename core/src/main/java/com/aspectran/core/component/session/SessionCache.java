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
package com.aspectran.core.component.session;

import java.util.Set;

/**
 * The Interface SessionCache.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public interface SessionCache {

    int NEVER_EVICT = -1;

    int EVICT_ON_SESSION_EXIT = 0;

    int EVICT_ON_INACTIVITY = 1; // any number equal or greater is time in seconds

    SessionDataStore getSessionDataStore();

    /**
     * A SessionDataStore that is the authoritative source
     * of session information.
     * @param sds the session data store
     */
    void setSessionDataStore(SessionDataStore sds);

    int getEvictionPolicy();

    /**
     * Sessions in this cache can be:
     * <ul>
     * <li>never evicted</li>
     * <li>evicted once the last request exits</li>
     * <li>evicted after a configurable period of inactivity</li>
     * </ul>
     *
     * @param policy -1 is never evict; 0 is evict-on-exit; and any other positive
     *      value is the time in seconds that a session can be idle before it can
     *      be evicted.
     */
    void setEvictionPolicy(int policy);

    boolean isSaveOnCreate();

    boolean isRemoveUnloadableSessions();

    /**
     * If the data for a session exists but is unreadable,
     * the SessionCache can instruct the SessionDataStore to delete it.
     *
     * @param removeUnloadableSessions whether or not SessionCache will delete
     *      session data that can not be loaded from the SessionDataStore
     */
    void setRemoveUnloadableSessions(boolean removeUnloadableSessions);

    /**
     * Whether or not a session that is newly created should be
     * immediately saved. If false, a session that is created and
     * invalidated within a single request is never persisted.
     *
     * @param saveOnCreate if true, immediately save the newly created session
     */
    void setSaveOnCreate(boolean saveOnCreate);

    boolean isSaveOnInactiveEviction();

    /**
     * Whether or not a a session that is about to be evicted should
     * be saved before being evicted.
     *
     * @param saveOnEvict if true, save the session before eviction
     */
    void setSaveOnInactiveEviction(boolean saveOnEvict);

    /**
     * Get an existing Session. If necessary, the cache will load the data for
     * the session from the configured SessionDataStore.
     *
     * @param id the session id
     * @return the Session if one exists, null otherwise
     * @throws Exception if an error occurs
     */
    Session get(String id) throws Exception;

    /**
     * Finish using a Session. This is called by the SessionHandler
     * once a request is finished with a Session. SessionCache
     * implementations may want to delay writing out Session contents
     * until the last request exits a Session.
     *
     * @param id the session id
     * @param session the session object
     * @throws Exception if an error occurs
     */
    void put(String id, Session session) throws Exception;

    /**
     * Check to see if a session exists: WILL consult the
     * SessionDataStore.
     *
     * @param id the session id
     * @return true if the session exists; false otherwise
     * @throws Exception if an error occurs
     */
    boolean exists(String id) throws Exception;

    /**
     * Check to see if a Session is in the cache. Does NOT consult
     * the SessionDataStore.
     *
     * @param id the session id
     * @return true if a Session object matching the id is present
     *      in the cache; false otherwise
     * @throws Exception if an error occurs
     */
    boolean contains(String id) throws Exception;

    /**
     * Remove a Session completely: from both this
     * cache and the SessionDataStore.
     *
     * @param id the session id
     * @return the Session that was removed, null otherwise
     * @throws Exception if an error occurs when deleting a session
     */
    Session delete(String id) throws Exception;

    /**
     * Check a list of session ids that belong to potentially expired
     * sessions. The Session in the cache should be checked,
     * but also the SessionDataStore, as that is the authoritative
     * source of all session information.
     *
     * @param candidates the session ids to check
     * @return the set of session ids that have actually expired: this can
     *      be a superset of the original candidate list.
     */
    Set<String> checkExpiration(Set<String> candidates);

    /**
     * Check a Session to see if it might be appropriate to
     * evict or expire.
     *
     * @param session the session object
     */
    void checkInactiveSession(Session session);

    Session newSession(String id, long time, long maxInactiveIntervalMS);

    /**
     * Re-materialize a Session that has previously existed.
     *
     * @param data the session data
     * @return a Session object for the data supplied
     */
    Session newSession(SessionData data);

    /**
     * @return the number of sessions in the cache
     */
    long getSessionsCurrent();

    /**
     * @return the max number of sessions in the cache
     */
    long getSessionsMax();

    /**
     * Returns a running total of sessions in the cache.
     *
     * @return a running total of sessions in the cache
     */
    long getSessionsTotal();

    /**
     * Resets the running total session count in the cache.
     */
    void resetStats();

    void clear();

}
