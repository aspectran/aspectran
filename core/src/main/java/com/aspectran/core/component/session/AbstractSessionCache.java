/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.Locker.Lock;

import java.util.HashSet;
import java.util.Set;

/**
 * A base implementation of the {@link SessionCache} interface for managing a set of
 * Session objects pertaining to a context in memory.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public abstract class AbstractSessionCache extends AbstractComponent implements SessionCache {

    private static final Log log = LogFactory.getLog(AbstractSessionCache.class);

    /**
     * The SessionHandler related to this SessionCache
     */
    private final SessionHandler sessionHandler;

    /**
     * The authoritative source of session data
     */
    private final SessionDataStore sessionDataStore;

    /**
     * When, if ever, to evict sessions: never; only when the last request for
     * them finishes; after inactivity time (expressed as secs)
     */
    private int evictionIdleSecs = NEVER_EVICT;

    /**
     * If true, as soon as a new session is created, it will be persisted to
     * the SessionDataStore
     */
    private boolean saveOnCreate;

    /**
     * If true, a session that will be evicted from the cache because it has been
     * inactive too long will be saved before being evicted.
     */
    private boolean saveOnInactiveEviction;

    /**
     * If true, a Session whose data cannot be read will be
     * deleted from the SessionDataStore.
     */
    private boolean removeUnloadableSessions;

    public AbstractSessionCache(SessionHandler sessionHandler, SessionDataStore sessionDataStore) {
        this.sessionHandler = sessionHandler;
        this.sessionDataStore = sessionDataStore;
    }

    protected SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    protected SessionDataStore getSessionDataStore() {
        return sessionDataStore;
    }

    @Override
    public int getEvictionIdleSecs() {
        return evictionIdleSecs;
    }

    /**
     * -1 means we never evict inactive sessions.
     * 0 means we evict a session after the last request for it exits
     * &gt;0 is the number of seconds after which we evict inactive sessions from the cache
     */
    @Override
    public void setEvictionIdleSecs(int evictionTimeout) {
        this.evictionIdleSecs = evictionTimeout;
    }

    @Override
    public boolean isSaveOnCreate() {
        return saveOnCreate;
    }

    @Override
    public void setSaveOnCreate(boolean saveOnCreate) {
        this.saveOnCreate = saveOnCreate;
    }

    /**
     * Whether we should save a session that has been inactive before
     * we boot it from the cache.
     *
     * @return true if an inactive session will be saved before being evicted
     */
    @Override
    public boolean isSaveOnInactiveEviction() {
        return saveOnInactiveEviction;
    }

    @Override
    public void setSaveOnInactiveEviction(boolean saveOnEvict) {
        this.saveOnInactiveEviction = saveOnEvict;
    }

    /**
     * @return true if sessions that can't be loaded are deleted from the store
     */
    @Override
    public boolean isRemoveUnloadableSessions() {
        return removeUnloadableSessions;
    }

    /**
     * If a session's data cannot be loaded from the store without error, remove
     * it from the persistent store.
     *
     * @param removeUnloadableSessions whether to delete sessions that can not be loaded
     */
    @Override
    public void setRemoveUnloadableSessions(boolean removeUnloadableSessions) {
        this.removeUnloadableSessions = removeUnloadableSessions;
    }

    @Override
    public BasicSession get(String id) throws Exception {
        BasicSession session;
        Exception ex = null;
        while (true) {
            session = doGet(id);
            if (sessionDataStore == null) {
                break; // can't load any session data so just return null or the session object
            }
            if (session == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Session " + id + " not found locally, attempting to load");
                }
                // didn't get a session, try and create one and put in a placeholder for it
                PlaceHolderSession phs = new PlaceHolderSession(id, sessionHandler);
                Lock phsLock = phs.lock();
                BasicSession bs = doPutIfAbsent(id, phs);
                if (bs == null) {
                    // My placeholder won, go ahead and load the full session data
                    try {
                        session = loadSession(id);
                        if (session == null) {
                            // session does not exist, remove the placeholder
                            doDelete(id);
                            phsLock.close();
                            break;
                        }
                        try (Lock ignored = session.lock()) {
                            // swap it in instead of the placeholder
                            boolean success = doReplace(id, phs, session);
                            if (success) {
                                // successfully swapped in the session
                                session.setResident(true);
                            } else {
                                // something has gone wrong, it should have been our placeholder
                                doDelete(id);
                                session = null;
                                log.warn("Replacement of placeholder for session " + id + " failed");
                            }
                            phsLock.close();
                            break;
                        }
                    } catch (Exception e) {
                        ex = e; // remember a problem happened loading the session
                        doDelete(id); // remove the placeholder
                        phsLock.close();
                        session = null;
                        break;
                    }
                } else {
                    // my placeholder didn't win, check the session returned
                    phsLock.close();
                    try (Lock ignored = bs.lock()) {
                        // is it a placeholder? or is a non-resident session? In both cases, chuck it away and start again
                        if (!bs.isResident() || bs instanceof PlaceHolderSession) {
                            continue;
                        }
                        session = bs;
                        break;
                    }
                }
            } else {
                // check the session returned
                try (Lock ignored = session.lock()) {
                    // is it a placeholder? or is it passivated? In both cases, chuck it away and start again
                    if (!session.isResident() || session instanceof PlaceHolderSession) {
                        continue;
                    }
                    // got the session
                    break;
                }
            }
        }
        if (ex != null) {
            throw ex;
        }
        return session;
    }

    /**
     * Load the info for the session from the session data store.
     *
     * @param id the session id
     * @return a Session object filled with data or null if the session doesn't exist
     * @throws Exception if the session can not be loaded
     */
    private BasicSession loadSession(String id) throws Exception {
        if (sessionDataStore == null) {
            return null; // can't load it
        }
        try {
            SessionData data = sessionDataStore.load(id);
            if (data == null) { // session doesn't exist
                return null;
            }
            return new BasicSession(data, sessionHandler, false);
        } catch (UnreadableSessionDataException e) {
            // can't load the session, delete it
            if (isRemoveUnloadableSessions()) {
                sessionDataStore.delete(id);
            }
            throw e;
        }
    }

    @Override
    public BasicSession add(String id, long time, long maxInactiveInterval) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating new session id=" + id);
        }
        SessionData sessionData = new SessionData(id, time, time, time, maxInactiveInterval);
        BasicSession session = new BasicSession(sessionData, sessionHandler, true);
        if (doPutIfAbsent(id, session) == null) {
            session.setResident(true); // its in the cache
            if (isSaveOnCreate() && sessionDataStore != null) {
                sessionDataStore.store(id, sessionData);
            }
            return session;
        } else {
            throw new IllegalStateException("Session " + id + " already in cache");
        }
    }

    @Override
    public void release(String id, BasicSession session) throws Exception {
        if (id == null || session == null) {
            throw new IllegalArgumentException("Put key=" + id + " session=" + (session == null ? "null" : session.getId()));
        }
        try (Lock ignored = session.lock()) {
            if (!session.isValid()) {
                return;
            }
            if (sessionDataStore == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Putting into SessionCache only id=" + id);
                }
                session.setResident(true);
                doPutIfAbsent(id, session); // ensure it is in our map
                return;
            }
            // don't do anything with the session until the last request for it has finished
            if (session.getRequests() <= 0) {
                // save the session
                sessionDataStore.store(id, session.getSessionData());
                // if we evict on session exit, boot it from the cache
                if (getEvictionIdleSecs() == EVICT_ON_SESSION_EXIT) {
                    if (log.isDebugEnabled()) {
                        log.debug("Eviction on request exit id=" + id);
                    }
                    doDelete(session.getId());
                    session.setResident(false);
                } else {
                    session.setResident(true);
                    doPutIfAbsent(id, session); // ensure it is in our map
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Session " + id + " request=" + session.getRequests());
                }
                session.setResident(true);
                doPutIfAbsent(id, session); // ensure it is the map, but don't save it to the backing store until the last request exists
            }
        }
    }

    @Override
    public boolean exists(String id) throws Exception {
        // try the object store first
        BasicSession bs = doGet(id);
        if (bs != null) {
            try (Lock ignored = bs.lock()) {
                // wait for the lock and check the validity of the session
                return bs.isValid();
            }
        }
        // not there, so find out if session data exists for it
        return (sessionDataStore != null && sessionDataStore.exists(id));
    }

    @Override
    public boolean contains(String id) throws Exception {
        // just ask our object cache, not the store
        return (doGet(id) != null);
    }

    @Override
    public BasicSession delete(String id) throws Exception {
        // get the session, if its not in memory, this will load it
        BasicSession session = get(id);
        // Always delete it from the backing data store
        if (sessionDataStore != null) {
            boolean deleted = sessionDataStore.delete(id);
            if (log.isDebugEnabled()) {
                log.debug("Session " + id + " deleted in session data store: " + deleted);
            }
        }
        // delete it from the session object store
        if (session != null) {
            session.setResident(false);
        }
        return doDelete(id);
    }

    /**
     * Get the session matching the key.
     *
     * @param id the session id
     * @return the Session object matching the id
     */
    public abstract BasicSession doGet(String id);

    /**
     * Put the session into the map if it wasn't already there.
     *
     * @param id the identity of the session
     * @param session the session object
     * @return null if the session wasn't already in the map, or the existing entry otherwise
     */
    public abstract BasicSession doPutIfAbsent(String id, BasicSession session);

    /**
     * Replace the mapping from id to oldValue with newValue.
     *
     * @param id the session id
     * @param oldValue the old value
     * @param newValue the new value
     * @return true if replacement was done
     */
    public abstract boolean doReplace(String id, BasicSession oldValue, BasicSession newValue);

    /**
     * Remove the session with this identity from the store.
     *
     * @param id the session id
     * @return true if removed; false otherwise
     */
    public abstract BasicSession doDelete(String id);

    @Override
    public BasicSession renewSessionId(String oldId, String newId) throws Exception {
        if (!StringUtils.hasText(oldId)) {
            throw new IllegalArgumentException("Old session id is null");
        }
        if (!StringUtils.hasText(oldId)) {
            throw new IllegalArgumentException("New session id is null");
        }
        BasicSession session = get(oldId);
        renewSessionId(session, newId);
        return session;
    }

    /**
     * Swap the id on a session.
     *
     * @param session the session for which to do the swap
     * @param newId the new id
     * @throws Exception if there was a failure saving the change
     */
    protected void renewSessionId(BasicSession session, String newId) throws Exception {
        if (session == null) {
            return;
        }
        try (Lock ignored = session.lock()) {
            String oldId = session.getId();
            session.checkValidForWrite(); // can't change id on invalid session
            session.getSessionData().setId(newId);
            session.getSessionData().setLastSaved(0); // pretend that the session has never been saved before to get a full save
            session.getSessionData().setDirty(true);  // ensure we will try to write the session out

            doPutIfAbsent(newId, session); // put the new id into our map
            doDelete(oldId); // take old out of map

            if (sessionDataStore != null) {
                sessionDataStore.delete(oldId);  //delete the session data with the old id
                sessionDataStore.store(newId, session.getSessionData()); //save the session data with the new id
            }
            if (log.isDebugEnabled()) {
                log.debug("Session id " + oldId + " swapped for new id " + newId);
            }
        }
    }

    @Override
    public Set<String> checkExpiration(Set<String> candidates) {
        if (log.isTraceEnabled()) {
            log.trace("SessionDataStore checking expiration on " + candidates);
        }
        if (sessionDataStore == null) {
            return null;
        }
        Set<String> allCandidates = sessionDataStore.getExpired(candidates);
        Set<String> sessionsInUse = new HashSet<>();
        if (allCandidates != null) {
            for (String c : allCandidates) {
                BasicSession bs = doGet(c);
                if (bs != null && bs.getRequests() > 0) {
                    // if the session is in my cache, check its not in use first
                    sessionsInUse.add(c);
                }
            }
            try {
                allCandidates.removeAll(sessionsInUse);
            } catch (UnsupportedOperationException e) {
                Set<String> tmp = new HashSet<>(allCandidates);
                tmp.removeAll(sessionsInUse);
                allCandidates = tmp;
            }
        }
        return allCandidates;
    }

    /**
     * Check a session for being inactive and
     * thus being able to be evicted, if eviction
     * is enabled.
     *
     * @param session the session to check
     */
    @Override
    public void checkInactiveSession(BasicSession session) {
        if (session == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Checking for idle " +  session.getId());
        }
        try (Lock ignored = session.lock()) {
            if (getEvictionIdleSecs() > 0 && session.isIdleLongerThan(getEvictionIdleSecs()) &&
                    session.isValid() && session.isResident() && session.getRequests() <= 0) {
                // Be careful with saveOnInactiveEviction - you may be able to re-animate a session that was
                // being managed on another node and has expired.
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Evicting idle session " + session.getId());
                    }
                    // save before evicting
                    if (isSaveOnInactiveEviction() && sessionDataStore != null) {
                        sessionDataStore.store(session.getId(), session.getSessionData());
                    }
                    doDelete(session.getId()); // detach from this cache
                    session.setResident(false);
                } catch (Exception e) {
                    log.warn("Passivation of idle session" + session.getId() + " failed", e);
                }
            }
        }
    }

    /**
     * PlaceHolder
     */
    static class PlaceHolderSession extends BasicSession {

        PlaceHolderSession(String id, SessionHandler sessionHandler) {
            super(new SessionData(id, 0, 0, 0, 0), sessionHandler, false);
        }

    }

}
