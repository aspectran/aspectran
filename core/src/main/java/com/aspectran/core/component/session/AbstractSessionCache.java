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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

import static com.aspectran.core.util.thread.Locker.Lock;

/**
 * <p>Created: 2017. 6. 24.</p>
 */
public abstract class AbstractSessionCache implements SessionCache {

    private static final Log log = LogFactory.getLog(AbstractSessionCache.class);

    protected final SessionHandler sessionHandler;

    protected SessionDataStore sessionDataStore;

    /**
     * When, if ever, to evict sessions: never; only when the last request for
     * them finishes; after inactivity time (expressed as secs)
     */
    protected int evictionPolicy = SessionCache.NEVER_EVICT;

    /**
     * If true, as soon as a new session is created, it will be persisted to
     * the SessionDataStore
     */
    protected boolean saveOnCreate;

    /**
     * If true, a session that will be evicted from the cache because it has been
     * inactive too long will be saved before being evicted.
     */
    protected boolean saveOnInactiveEviction;

    /**
     * If true, a Session whose data cannot be read will be
     * deleted from the SessionDataStore.
     */
    protected boolean removeUnloadableSessions;

    public AbstractSessionCache(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    protected SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    @Override
    public SessionDataStore getSessionDataStore() {
        return sessionDataStore;
    }

    @Override
    public void setSessionDataStore(SessionDataStore sessionDataStore) {
        this.sessionDataStore = sessionDataStore;
    }

    @Override
    public int getEvictionPolicy() {
        return evictionPolicy;
    }

    /**
     * -1 means we never evict inactive sessions.
     * 0 means we evict a session after the last request for it exits
     * &gt;0 is the number of seconds after which we evict inactive sessions from the cache
     */
    @Override
    public void setEvictionPolicy(int evictionTimeout) {
        evictionPolicy = evictionTimeout;
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
     * Get a session object.
     * If the session object is not in this session store, try getting
     * the data for it from a SessionDataStore associated with the
     * session manager.
     *
     * @param id the session id
     */
    @Override
    public Session get(String id) throws Exception {
        Session session;
        Exception ex = null;

        while (true) {
            session = doGet(id);

            if (sessionDataStore == null) {
                break; // can't load any session data so just return null or the session object
            }

            if (session == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Session " + id + " not found locally, attempting to load");
                }

                // didn't get a session, try and create one and put in a placeholder for it
                PlaceHolderSession phs = new PlaceHolderSession (new SessionData(id, 0, 0, 0, 0));
                Lock phsLock = phs.lock();
                Session s = doPutIfAbsent(id, phs);
                if (s == null) {
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
                            if (!success) {
                                // something has gone wrong, it should have been our placeholder
                                doDelete(id);
                                session = null;
                                log.warn("Replacement of placeholder for session " + id + " failed");
                                phsLock.close();
                                break;
                            } else {
                                // successfully swapped in the session
                                session.setResident(true);
                                session.updateInactivityTimer();
                                phsLock.close();
                                break;
                            }
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
                    try (Lock ignored = s.lock()) {
                        // is it a placeholder? or is a non-resident session? In both cases, chuck it away and start again
                        if (!s.isResident() || s instanceof PlaceHolderSession) {
                            continue;
                        }
                        session = s;
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
     * @throws Exception
     */
    private Session loadSession(String id) throws Exception {
        if (sessionDataStore == null) {
            return null; // can't load it
        }
        try {
            SessionData data = sessionDataStore.load(id);
            if (data == null) { // session doesn't exist
                return null;
            }
            return newSession(data);
        } catch (UnreadableSessionDataException e) {
            // can't load the session, delete it
            if (isRemoveUnloadableSessions()) {
                sessionDataStore.delete(id);
            }
            throw e;
        }
    }

    /**
     * Put the Session object back into the session store.
     *
     * <p>This should be called when a request exists the session. Only when the last
     * simultaneous request exists the session will any action be taken.</p>
     *
     * <p>If there is a SessionDataStore write the session data through to it.</p>
     *
     * <p>If the SessionDataStore supports passivation, call the passivate/active listeners.</p>
     *
     * <p>If the evictionPolicy == SessionCache.EVICT_ON_SESSION_EXIT then after we have saved
     * the session, we evict it from the cache.</p>
     */
    @Override
    public void put(String id, Session session) throws Exception {
        if (id == null || session == null) {
            throw new IllegalArgumentException("Put key=" + id + " session=" + (session == null ? "null" : session.getId()));
        }

        try (Lock ignored = session.lock()) {
            if (!session.isValid()) {
                return;
            }

            if (sessionDataStore == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No SessionDataStore, putting into SessionCache only id=" + id);
                }
                session.setResident(true);
                if (doPutIfAbsent(id, session) == null) { // ensure it is in our map
                    session.updateInactivityTimer();
                }
                return;
            }

            // don't do anything with the session until the last request for it has finished
            if ((session.getRequests() <= 0)) {
                // save the session
                if (!sessionDataStore.isPassivating()) {
                    // if our backing datastore isn't the passivating kind, just save the session
                    sessionDataStore.store(id, session.getSessionData());
                    // if we evict on session exit, boot it from the cache
                    if (getEvictionPolicy() == EVICT_ON_SESSION_EXIT) {
                        if (log.isDebugEnabled()) {
                            log.debug("Eviction on request exit id=" + id);
                        }
                        doDelete(session.getId());
                        session.setResident(false);
                    } else {
                        session.setResident(true);
                        if (doPutIfAbsent(id,session) == null) { // ensure it is in our map
                            session.updateInactivityTimer();
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Non passivating SessionDataStore, session in SessionCache only id=" + id);
                        }
                    }
                } else {
                    // backing store supports passivation, call the listeners
                    sessionHandler.willPassivate(session);
                    if (log.isDebugEnabled()) {
                        log.debug("Session passivating id=" + id);
                    }
                    sessionDataStore.store(id, session.getSessionData());

                    if (getEvictionPolicy() == EVICT_ON_SESSION_EXIT) {
                        // throw out the passivated session object from the map
                        doDelete(id);
                        session.setResident(false);
                        if (log.isDebugEnabled()) {
                            log.debug("Evicted on request exit id=" + id);
                        }
                    } else {
                        // reactivate the session
                        sessionHandler.didActivate(session);
                        session.setResident(true);
                        if (doPutIfAbsent(id,session) == null) // ensure it is in our map
                            session.updateInactivityTimer();
                        if (log.isDebugEnabled()) {
                            log.debug("Session reactivated id=" + id);
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Req count=" + session.getRequests() + " for id=" + id);
                }
                session.setResident(true);
                if (doPutIfAbsent(id, session) == null) {
                    // ensure it is the map, but don't save it to the backing store until the last request exists
                    session.updateInactivityTimer();
                }
            }
        }
    }

    /**
     * Check to see if a session corresponding to the id exists.
     *
     * This method will first check with the object store. If it
     * doesn't exist in the object store (might be passivated etc),
     * it will check with the data store.
     *
     * @throws Exception the Exception
     */
    @Override
    public boolean exists(String id) throws Exception {
        // try the object store first
        Session s = doGet(id);
        if (s != null) {
            try (Lock ignored = s.lock()) {
                // wait for the lock and check the validity of the session
                return s.isValid();
            }
        }
        // not there, so find out if session data exists for it
        return (sessionDataStore != null && sessionDataStore.exists(id));
    }

    /**
     * Check to see if this cache contains an entry for the session
     * corresponding to the session id.
     */
    @Override
    public boolean contains(String id) throws Exception {
        // just ask our object cache, not the store
        return (doGet(id) != null);
    }

    /**
     * Remove a session object from this store and from any backing store.
     */
    @Override
    public Session delete(String id) throws Exception {
        // get the session, if its not in memory, this will load it
        Session session = get(id);

        // Always delete it from the backing data store
        if (sessionDataStore != null) {
            boolean deleted = sessionDataStore.delete(id);
            if (log.isDebugEnabled()) {
                log.debug("Session " + id + " deleted in db: " + deleted);
            }
        }

        // delete it from the session object store
        if (session != null) {
            session.stopInactivityTimer();
            session.setResident(false);
        }

        return doDelete(id);
    }

    @Override
    public Set<String> checkExpiration(Set<String> candidates) {
        if (log.isDebugEnabled()) {
            log.debug("SessionDataStore checking expiration on " + candidates);
        }
        if (sessionDataStore == null) {
            return null;
        }
        Set<String> allCandidates = sessionDataStore.getExpired(candidates);
        Set<String> sessionsInUse = new HashSet<>();
        if (allCandidates != null) {
            for (String c : allCandidates) {
                Session s = doGet(c);
                if (s != null && s.getRequests() > 0) {
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
     * @param session session to check
     */
    public void checkInactiveSession(Session session) {
        if (session == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Checking for idle " +  session.getId());
        }
        try (Lock ignored = session.lock()) {
            if (getEvictionPolicy() > 0 && session.isIdleLongerThan(getEvictionPolicy()) &&
                    session.isValid() && session.isResident() && session.getRequests() <= 0) {
                // Be careful with saveOnInactiveEviction - you may be able to re-animate a session that was
                // being managed on another node and has expired.
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Evicting idle session " + session.getId());
                    }

                    // save before evicting
                    if (isSaveOnInactiveEviction() && sessionDataStore != null) {
                        if (sessionDataStore.isPassivating()) {
                            sessionHandler.willPassivate(session);
                        }
                        sessionDataStore.store(session.getId(), session.getSessionData());
                    }

                    doDelete(session.getId()); // detach from this cache
                    session.setResident(false);
                } catch (Exception e) {
                    log.warn("Passivation of idle session" + session.getId() + " failed", e);
                    session.updateInactivityTimer();
                }
            }
        }
    }

    @Override
    public Session newSession(String id, long time, long maxInactiveIntervalMS) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new session id=" + id);
        }
        SessionData sessionData = new SessionData(id, time, time, time, maxInactiveIntervalMS);
        Session session = newSession(sessionData);
        try {
            if (isSaveOnCreate() && sessionDataStore != null) {
                sessionDataStore.store(id, sessionData);
            }
        } catch (Exception e) {
            log.warn("Save of new session " + id + " failed", e);
        }
        return session;
    }

    /**
     * Create a new Session object from pre-existing session data.
     *
     * @param data the session data
     * @return a new Session object
     */
    public abstract Session newSession(SessionData data);

    /**
     * Get the session matching the key.
     *
     * @param id the session id
     * @return the Session object matching the id
     */
    public abstract Session doGet(String id);

    /**
     * Put the session into the map if it wasn't already there.
     *
     * @param id the identity of the session
     * @param session the session object
     * @return null if the session wasn't already in the map, or the existing entry otherwise
     */
    public abstract Session doPutIfAbsent(String id, Session session);

    /**
     * Replace the mapping from id to oldValue with newValue.
     *
     * @param id the session id
     * @param oldValue the old value
     * @param newValue the new value
     * @return true if replacement was done
     */
    public abstract boolean doReplace(String id, Session oldValue, Session newValue);

    /**
     * Remove the session with this identity from the store.
     *
     * @param id the session id
     * @return true if removed; false otherwise
     */
    public abstract Session doDelete(String id);

    /**
     * PlaceHolder
     */
    protected class PlaceHolderSession extends Session {

        /**
         * @param data the session data
         */
        public PlaceHolderSession(SessionData data) {
            super(null, data);
        }

    }

}
