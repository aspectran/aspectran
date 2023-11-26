/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.thread.AutoLock;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A base implementation of the {@link SessionCache} interface for managing a set of
 * Session objects pertaining to a context in memory.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public abstract class AbstractSessionCache extends AbstractComponent implements SessionCache {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionCache.class);

    /**
     * The SessionHandler related to this SessionCache
     */
    private final SessionHandler sessionHandler;

    /**
     * The authoritative source of session data
     */
    private final SessionStore sessionStore;

    /**
     * Whether to support session clustering
     */
    private final boolean clusterEnabled;

    /**
     * When, if ever, to evict sessions: never; only when the last request for
     * them finishes; after inactivity time (expressed as secs)
     */
    private int evictionIdleSecs = NEVER_EVICT;

    /**
     * If true, as soon as a new session is created, it will be persisted to
     * the SessionStore
     */
    private boolean saveOnCreate;

    /**
     * If true, a session that will be evicted from the cache because it has been
     * inactive too long will be saved before being evicted.
     */
    private boolean saveOnInactiveEviction;

    /**
     * If true, a Session whose data cannot be read will be
     * deleted from the SessionStore.
     */
    private boolean removeUnloadableSessions;

    public AbstractSessionCache(SessionHandler sessionHandler, SessionStore sessionStore, boolean clusterEnabled) {
        this.sessionHandler = sessionHandler;
        this.sessionStore = sessionStore;
        this.clusterEnabled = (clusterEnabled && sessionStore != null);
    }

    protected SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    protected SessionStore getSessionStore() {
        return sessionStore;
    }

    protected SessionStatistics getStatistics() {
        return sessionHandler.getStatistics();
    }

    @Override
    public boolean isClusterEnabled() {
        return clusterEnabled;
    }

    @Override
    public int getEvictionIdleSecs() {
        return evictionIdleSecs;
    }

    /**
     * -1 means we never evict inactive sessions.
     * 0 means we evict a session after the last request for it exits.
     * &gt;0 is the number of seconds after which we evict inactive sessions from the cache.
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
     * @param removeUnloadableSessions whether to delete sessions that can not be loaded
     */
    @Override
    public void setRemoveUnloadableSessions(boolean removeUnloadableSessions) {
        this.removeUnloadableSessions = removeUnloadableSessions;
    }

    @Override
    public DefaultSession get(String id) throws Exception {
        AtomicBoolean resident = new AtomicBoolean(true);
        AtomicReference<Exception> thrown = new AtomicReference<>();
        DefaultSession session;
        session = doComputeIfAbsent(id, k -> {
            if (logger.isTraceEnabled()) {
                logger.trace("Session " + id + " not found locally in " + this + ", attempting to load");
            }
            try {
                DefaultSession stored = loadSession(id);
                if (stored != null) {
                    try (AutoLock ignored = stored.lock()) {
                        stored.setResident(true); // ensure freshly loaded session is resident
                    }
                    resident.set(false);
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Session " + id + " not loaded by store");
                    }
                }
                return stored;
            } catch (Exception e) {
                thrown.set(e);
                return null;
            }
        });
        if (thrown.get() != null) {
            throw thrown.get();
        }
        if (session != null) {
            try (AutoLock ignored = session.lock()) {
                if (!session.isResident()) {
                    // session isn't marked as resident in cache
                    if (logger.isTraceEnabled()) {
                        logger.debug("Non-resident session " + id + " in cache");
                    }
                    return null;
                }
                if (isClusterEnabled() && resident.get() && session.getRequests() <= 0) {
                    DefaultSession stored = loadSession(id);
                    if (stored != null) {
                        // swap it in instead of the local session
                        boolean success = doReplace(id, session, stored);
                        if (success) {
                            // successfully swapped with the stored session
                            session = stored;
                            session.setResident(true);
                        } else {
                            // retry because it was updated by another thread
                            return get(id);
                        }
                    } else {
                        // is the session already destroyed? it must be removed from the cache
                        doDelete(id);
                        session.setResident(false);
                        session = null;
                    }
                }
            }
        }
        return session;
    }

    /**
     * Load the info for the session from the session store.
     * @param id the session id
     * @return a Session object filled with data or null if the session doesn't exist
     * @throws Exception if the session can not be loaded
     */
    private DefaultSession loadSession(String id) throws Exception {
        if (sessionStore == null) {
            return null; // can't load it
        }
        try {
            SessionData data = sessionStore.load(id);
            if (data != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Session " + id + " loaded from session store " + sessionStore);
                }
                return new DefaultSession(data, sessionHandler, false);
            } else {
                // session doesn't exist
                return null;
            }
        } catch (UnreadableSessionDataException e) {
            // can't load the session, delete it
            if (isRemoveUnloadableSessions()) {
                sessionStore.delete(id);
            }
            throw e;
        }
    }

    @Override
    public DefaultSession add(String id, long time, long maxInactiveInterval) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Create new session id=" + id);
        }
        SessionData data = new SessionData(id, time, time, time, maxInactiveInterval);
        DefaultSession session = new DefaultSession(data, sessionHandler, true);
        if (doPutIfAbsent(id, session) == null) {
            session.setResident(true); // it's in the cache
            if (sessionStore != null && (isSaveOnCreate() || isClusterEnabled())) {
                sessionStore.save(id, data);
            }
            return session;
        } else {
            throw new IllegalStateException("Session " + id + " already in cache");
        }
    }

    @Override
    public void release(String id, DefaultSession session) throws Exception {
        if (id == null || session == null) {
            throw new IllegalArgumentException("Put key=" + id + " session=" +
                (session == null ? "null" : session.getId()));
        }
        try (AutoLock ignored = session.lock()) {
            if (!session.isValid()) {
                return;
            }
            if (sessionStore == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Putting into SessionCache only id=" + id);
                }
                session.setResident(true);
                doPutIfAbsent(id, session); // ensure it is in our map
                return;
            }
            // don't do anything with the session until the last request for it has finished
            if (session.getRequests() <= 0) {
                // save the session
                sessionStore.save(id, session.getSessionData());
                // if we evict on session exit, boot it from the cache
                if (getEvictionIdleSecs() == EVICT_ON_SESSION_EXIT) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Eviction on request exit id=" + id);
                    }
                    doDelete(session.getId());
                    session.setResident(false);
                } else {
                    session.setResident(true);
                    doPutIfAbsent(id, session); // ensure it is in our map
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Session " + id + " request=" + session.getRequests());
                }
                session.setResident(true);
                doPutIfAbsent(id, session); // ensure it is the map,
                // but don't save it to the backing store until the last request exists
            }
        }
    }

    @Override
    public boolean exists(String id) throws Exception {
        if (isClusterEnabled()) {
            DefaultSession session = get(id);
            if (session != null) {
                return session.isValid();
            } else {
                return false;
            }
        } else {
            // try to find it in the cache first
            DefaultSession session = doGet(id);
            if (session != null) {
                return session.isValid();
            }
            // not there, so find out if session data exists for it
            return (sessionStore != null && sessionStore.exists(id));
        }
    }

    @Override
    public boolean contains(String id) throws Exception {
        // just ask our object cache, not the store
        return (doGet(id) != null);
    }

    @Override
    public DefaultSession delete(String id) throws Exception {
        // get the session, if it's not in memory, this will load it
        DefaultSession session = get(id);
        // Always delete it from the backing data store
        if (sessionStore != null) {
            boolean deleted = sessionStore.delete(id);
            if (logger.isTraceEnabled()) {
                logger.trace("Session " + id + " deleted in session data store: " + deleted);
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
     * @param id the session id
     * @return the Session object matching the id
     */
    protected abstract DefaultSession doGet(String id);

    /**
     * Put the session into the map if it wasn't already there.
     * @param id the identity of the session
     * @param session the session object
     * @return null if the session wasn't already in the map, or the existing entry otherwise
     */
    protected abstract DefaultSession doPutIfAbsent(String id, DefaultSession session);

    /**
     * Compute the mappingFunction to create a Session object if the session
     * with the given id isn't already in the map, otherwise return the existing Session.
     * This method is expected to have precisely the same behaviour as
     * {@link java.util.concurrent.ConcurrentHashMap#computeIfAbsent}
     * @param id the session id
     * @param mappingFunction the function to load the data for the session
     * @return an existing Session from the cache
     */
    protected abstract DefaultSession doComputeIfAbsent(String id, Function<String, DefaultSession> mappingFunction);

    /**
     * Replace the mapping from id to oldValue with newValue.
     * @param id the session id
     * @param oldValue the old value
     * @param newValue the new value
     * @return true if replacement was done
     */
    protected abstract boolean doReplace(String id, DefaultSession oldValue, DefaultSession newValue);

    /**
     * Remove the session with this identity from the store.
     * @param id the session id
     * @return the Session object if removed; null otherwise
     */
    protected abstract DefaultSession doDelete(String id);

    @Override
    public DefaultSession renewSessionId(String oldId, String newId) throws Exception {
        if (!StringUtils.hasText(oldId)) {
            throw new IllegalArgumentException("Old session id is null");
        }
        if (!StringUtils.hasText(oldId)) {
            throw new IllegalArgumentException("New session id is null");
        }
        DefaultSession session = get(oldId);
        renewSessionId(session, newId);
        return session;
    }

    /**
     * Swap the id on a session.
     * @param session the session for which to do the swap
     * @param newId the new id
     * @throws Exception if there was a failure saving the change
     */
    protected void renewSessionId(DefaultSession session, String newId) throws Exception {
        if (session == null) {
            return;
        }
        try (AutoLock ignored = session.lock()) {
            String oldId = session.getId();
            session.checkValidForWrite(); // can't change id on invalid session
            session.getSessionData().setId(newId);
            session.getSessionData().setLastSaved(0); // pretend that the session has never been saved before to get a full save
            session.getSessionData().setDirty(true);  // ensure we will try to write the session out

            doPutIfAbsent(newId, session); // put the new id into our map
            doDelete(oldId); // take old out of map

            if (sessionStore != null) {
                sessionStore.delete(oldId);  //delete the session data with the old id
                sessionStore.save(newId, session.getSessionData()); //save the session data with the new id
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Session id " + oldId + " swapped for new id " + newId);
            }
        }
    }

    @Override
    public Set<String> checkExpiration(Set<String> candidates) {
        if (!isInitialized()) {
            return null;
        }
        if (sessionStore == null) {
            return candidates;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("SessionStore checking expiration on " + candidates);
        }
        Set<String> allCandidates = sessionStore.getExpired(candidates);
        Set<String> sessionsInUse = new HashSet<>();
        if (allCandidates != null) {
            for (String id : allCandidates) {
                DefaultSession session = doGet(id);
                if (session != null && session.getRequests() > 0) {
                    // if the session is in my cache, check it's not in use first
                    sessionsInUse.add(id);
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
     * Check a session for being inactive and thus being able to be evicted,
     * if eviction is enabled.
     * @param session the session to check
     * @return true if evicted session, false otherwise
     */
    @Override
    public boolean checkInactiveSession(DefaultSession session) {
        if (session == null) {
            return false;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Checking for idle session id=" +  session.getId());
        }
        try (AutoLock ignored = session.lock()) {
            if (getEvictionIdleSecs() > 0 && session.isIdleLongerThan(getEvictionIdleSecs()) &&
                    session.isValid() && session.isResident() && session.getRequests() <= 0) {
                // Be careful with saveOnInactiveEviction - you may be able to re-animate a session that was
                // being managed on another node and has expired.
                if (logger.isDebugEnabled()) {
                    logger.debug("Evict idle session id=" + session.getId());
                }
                // save before evicting
                if (sessionStore != null && (isClusterEnabled() || isSaveOnInactiveEviction())) {
                    sessionStore.save(session.getId(), session.getSessionData());
                }
                doDelete(session.getId()); // detach from this cache
                session.setResident(false);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Passivation of idle session " + session.getId() + " failed", e);
        }
        return false;
    }

    @Override
    public void cleanOrphans(long time) {
        if (sessionStore != null) {
            sessionStore.cleanOrphans(time);
        }
    }

}
