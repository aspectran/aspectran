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

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.concurrent.AutoLock;
import com.aspectran.utils.scheduling.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.round;

/**
 * Provides a base implementation for the {@link SessionManager} interface.
 *
 * <p>This abstract class handles the common logic for session lifecycle management,
 * including session creation, retrieval, invalidation, and event notification.
 * It coordinates with {@link SessionCache}, {@link SessionStore}, and {@link HouseKeeper}
 * to provide a robust and extensible session management system.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public abstract class AbstractSessionManager extends AbstractComponent implements SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionManager.class);

    private final SessionStatistics statistics = new SessionStatistics();

    private final List<SessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    private final Set<String> candidateSessionIdsForExpiry = ConcurrentHashMap.newKeySet();

    private String workerName;

    private Scheduler scheduler;

    private SessionIdGenerator sessionIdGenerator;

    private SessionCache sessionCache;

    private HouseKeeper houseKeeper;

    /** The default maximum inactive interval in seconds (30 minutes). */
    private volatile int defaultMaxIdleSecs = 30 * 60;

    private int maxIdleSecsForNew;

    /** The last time in milliseconds that orphaned sessions were deleted. */
    private long lastOrphanSweepTime = 0L;

    AbstractSessionManager() {
    }

    public abstract ClassLoader getClassLoader();

    @Override
    public String getWorkerName() {
        return workerName;
    }

    protected void setWorkerName(String workerName) {
        if (this.workerName != null) {
            throw new IllegalStateException("workerName already set");
        }
        if (workerName != null && workerName.contains(".")) {
            throw new IllegalArgumentException("Worker name cannot contain '.'");
        }
        this.workerName = workerName;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    protected void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public SessionIdGenerator getSessionIdGenerator() {
        return sessionIdGenerator;
    }

    protected void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
        this.sessionIdGenerator = sessionIdGenerator;
    }

    @Override
    public SessionCache getSessionCache() {
        return sessionCache;
    }

    protected void setSessionCache(SessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    public HouseKeeper getHouseKeeper() {
        return houseKeeper;
    }

    protected void setHouseKeeper(HouseKeeper houseKeeper) {
        this.houseKeeper = houseKeeper;
    }

    @Override
    public int getDefaultMaxIdleSecs() {
        return defaultMaxIdleSecs;
    }

    @Override
    public void setDefaultMaxIdleSecs(int defaultMaxIdleSecs) {
        this.defaultMaxIdleSecs = defaultMaxIdleSecs;
        if (logger.isDebugEnabled()) {
            logger.debug("{} default maxIdleSecs={}", getComponentName(), defaultMaxIdleSecs);
        }
    }

    @Override
    public int getMaxIdleSecsForNew() {
        return maxIdleSecsForNew;
    }

    public void setMaxIdleSecsForNew(int maxIdleSecsForNew) {
        this.maxIdleSecsForNew = maxIdleSecsForNew;
    }

    @Override
    public ManagedSession getSession(String id) {
        ManagedSession session = null;
        try {
            session = sessionCache.get(id);
            if (session != null) {
                // if the session we got back has expired
                if (session.isExpiredAt(System.currentTimeMillis())) {
                    // expire the session
                    try {
                        session.setDestroyedReason(Session.DestroyedReason.TIMEOUT);
                        session.invalidate();
                    } catch (Exception e) {
                        logger.warn("Invalidating session {} found to be expired when requested", id, e);
                    }
                    return null;
                }
            }
            return session;
        } catch (Exception e) {
            if (session != null && session.isValid()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public ManagedSession createSession(String id) {
        long inactiveInterval;
        if (defaultMaxIdleSecs > 0) {
            inactiveInterval = TimeUnit.SECONDS.toMillis(defaultMaxIdleSecs);
        } else {
            inactiveInterval = -1L;
        }
        long now = System.currentTimeMillis();
        try {
            ManagedSession session = sessionCache.add(id, now, inactiveInterval);
            getStatistics().sessionCreated();
            onSessionCreated(session);
            return session;
        } catch (MaxSessionsExceededException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create new session id=" + id, e);
        }
    }

    protected void refreshSession(ManagedSession session) {
        try {
            sessionCache.refresh(session);
        } catch (Exception e) {
            logger.warn("Session refresh failed", e);
        }
    }

    protected void releaseSession(ManagedSession session) {
        try {
            sessionCache.release(session);
        } catch (Exception e) {
            logger.warn("Session release failed", e);
        }
    }

    @Override
    public String createSessionId(long seedTerm) {
        return sessionIdGenerator.createSessionId(seedTerm);
    }

    @Override
    public String renewSessionId(String oldId, String newId) {
        try {
            ManagedSession session = sessionCache.renewSessionId(oldId, newId);
            if (session == null) {
                // session doesn't exist
                return null;
            }
            for (SessionListener listener : sessionListeners) {
                listener.sessionIdChanged(session, oldId);
            }
            return session.getId();
        } catch (Exception e) {
            logger.warn("Unable to renew session id {} to {}", oldId, newId, e);
            return null;
        }
    }

    @Override
    public ManagedSession removeSession(String id, boolean invalidate) {
        return removeSession(id, invalidate, null);
    }

    @Override
    public ManagedSession removeSession(String id, boolean invalidate, Session.DestroyedReason reason) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        try {
            // Remove the Session object from the session store and any backing data store
            ManagedSession session = sessionCache.delete(id);
            if (invalidate && session != null) {
                // start invalidating if it is not already begun, and call the listeners
                try {
                    if (session.beginInvalidate()) {
                        try {
                            try (AutoLock ignored = session.lock()) {
                                if (reason != null) {
                                    session.setDestroyedReason(reason);
                                }
                                onSessionDestroyed(session);
                            }
                        } catch (Exception e) {
                            logger.warn("Error during Session destroy listener", e);
                        } finally {
                            // call the attribute removed listeners and finally mark it as invalid
                            session.finishInvalidate();
                        }
                    }
                } catch (IllegalStateException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Session {} already invalid", session);
                    }
                }
            }
            return session;
        } catch (Exception e) {
            if (invalidate) {
                logger.warn("Unable to invalidate session id={}", id, e);
            } else {
                logger.warn("Unable to remove session id={}", id, e);
            }
            return null;
        }
    }

    @Override
    public void invalidate(String id) {
        removeSession(id, true);
    }

    @Override
    public void invalidate(String id, Session.DestroyedReason reason) {
        removeSession(id, true, reason);
    }

    /**
     * Handles the expiration of a session's inactivity timer.
     * <p>
     * This method is called when a session has been idle for its configured
     * inactivity period or has reached its absolute expiration time. It will
     * either mark the session for scavenging by the {@link HouseKeeper} or
     * evict it from the cache if it has been idle for too long.
     * </p>
     * @param session the session whose timer has expired
     * @param now the current time in milliseconds
     * @return true if the session has expired and was marked for scavenging, false otherwise
     */
    public boolean sessionInactivityTimerExpired(ManagedSession session, long now) {
        if (session == null) {
            return true;
        }
        try (AutoLock ignored = session.lock()) {
            if (session.isExpiredAt(now)) {
                // instead of expiring the session directly here, accumulate a list of
                // session ids that need to be expired. This is an efficiency measure: as
                // the expiration involves the SessionStore doing a delete, it is
                // most efficient if it can be done as a bulk operation to eg reduce
                // roundtrips to the persistent store.
                if (!addCandidateSessionIdForExpiry(session.getId())) {
                    invalidate(session.getId(), Session.DestroyedReason.TIMEOUT);
                }
                if (session.isTempResident()) {
                    onSessionEvicted(session);
                }
                return true;
            } else {
                // possibly evict the session
                boolean evicted = sessionCache.checkInactiveSession(session);
                if (evicted) {
                    // for evicted sessions, their expiration is checked from the session store
                    addCandidateSessionIdForExpiry(session.getId());
                    onSessionEvicted(session);
                }
                return false;
            }
        }
    }

    /**
     * Adds a session ID to the set of candidates for expiration.
     * This is only effective if a {@link HouseKeeper} is running.
     * @param id the session ID to add
     * @return true if the ID was successfully added, false otherwise
     */
    private boolean addCandidateSessionIdForExpiry(String id) {
        if (getHouseKeeper() != null && getHouseKeeper().isRunning()) {
            candidateSessionIdsForExpiry.add(id);
            if (logger.isTraceEnabled()) {
                logger.trace("Session {} is candidate for expiry", id);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called periodically by the {@link HouseKeeper} to process sessions that
     * have been marked as candidates for expiration.
     * @param scavengingInterval the interval in milliseconds between scavenge cycles
     */
    public void scavenge(long scavengingInterval) {
        // don't attempt to scavenge if we are shutting down
        if (isDestroying() || isDestroyed()) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("{} scavenging sessions", getComponentName());
        }

        // Get a snapshot of the candidates as they are now. Others that
        // arrive during this processing will be dealt with on
        // subsequent call to scavenge
        String[] candidateSessionIds = candidateSessionIdsForExpiry.toArray(new String[0]);
        Set<String> candidates = new HashSet<>(Arrays.asList(candidateSessionIds));
        if (logger.isTraceEnabled()) {
            logger.trace("{} scavenging session ids {}", getComponentName(), candidates);
        }
        try {
            Set<String> checkedCandidates = sessionCache.checkExpiration(candidates);
            if (checkedCandidates != null) {
                for (String id : checkedCandidates) {
                    candidateSessionIdsForExpiry.remove(id);
                    invalidate(id, Session.DestroyedReason.TIMEOUT);
                }
            }
            if (logger.isDebugEnabled()) {
                int before = candidates.size();
                int after = candidateSessionIdsForExpiry.size();
                int scavenged = (checkedCandidates != null ? checkedCandidates.size() : 0);
                int unmanaged = scavenged - before + after;
                if (scavenged != 0 || unmanaged != 0) {
                    ToStringBuilder tsb = new ToStringBuilder("Scavenging status for expired sessions");
                    tsb.append("candidates", before);
                    tsb.append("remains", after);
                    tsb.append("unmanaged", unmanaged);
                    tsb.append("scavenged", scavenged);
                    logger.debug(tsb.toString());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to check expiration on [{}]", StringUtils.joinWithCommas(candidates), e);
        }

        // Periodically but infrequently comb the backing store to delete sessions
        // that expired a very long time ago (ie not being actively
        // managed by any node). As these sessions are not for our context, we
        // can't load them, so they must just be forcibly deleted.
        long now = System.currentTimeMillis();
        try {
            if (now > (lastOrphanSweepTime + scavengingInterval * 10L)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Cleaning orphans at {}, last sweep at {}", now, lastOrphanSweepTime);
                }
                sessionCache.cleanOrphans(now - scavengingInterval * 10L);
            }
        } finally {
            lastOrphanSweepTime = now;
        }
    }

    @Override
    public void addSessionListener(SessionListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("Register session listener {} in {}", listener, getComponentName());
        }
        sessionListeners.add(listener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("Remove session listener {} from {}", listener, getComponentName());
        }
        sessionListeners.remove(listener);
    }

    @Override
    public void clearSessionListeners() {
        sessionListeners.clear();
    }

    /**
     * Notifies registered listeners of a session attribute change.
     * @param session the session on which the attribute changed
     * @param name the name of the attribute
     * @param oldValue the previous value of the attribute, or null if added
     * @param newValue the new value of the attribute, or null if removed
     */
    protected void onSessionAttributeUpdate(Session session, String name, Object oldValue, Object newValue) {
        if (session != null) {
            for (SessionListener listener : sessionListeners) {
                if (oldValue == null) {
                    listener.attributeAdded(session, name, newValue);
                } else if (newValue == null) {
                    listener.attributeRemoved(session, name, oldValue);
                } else {
                    listener.attributeUpdated(session, name, newValue, oldValue);
                }
            }
        }
    }

    /**
     * Notifies registered listeners that a session has been destroyed.
     * Listeners are called in the reverse order they were added.
     * @param session the session that was destroyed
     */
    protected void onSessionDestroyed(Session session) {
        if (session != null && !sessionListeners.isEmpty()) {
            // We need to create our own snapshot to safely iterate over a concurrent list in reverse
            List<SessionListener> listeners = new ArrayList<>(sessionListeners);
            for (ListIterator<SessionListener> iter = listeners.listIterator(listeners.size()); iter.hasPrevious();) {
                iter.previous().sessionDestroyed(session);
            }
        }
    }

    /**
     * Notifies registered listeners that a new session has been created.
     * @param session the session that was created
     */
    protected void onSessionCreated(Session session) {
        for (SessionListener listener : sessionListeners) {
            listener.sessionCreated(session);
        }
    }

    /**
     * Notifies registered listeners that a session has been evicted from the cache.
     * @param session the session that was evicted
     */
    protected void onSessionEvicted(Session session) {
        for (SessionListener listener : sessionListeners) {
            listener.sessionEvicted(session);
        }
    }

    /**
     * Notifies registered listeners that a session has been loaded from the store into the cache.
     * @param session the session that was resided
     */
    protected void onSessionResided(Session session) {
        for (SessionListener listener : sessionListeners) {
            listener.sessionResided(session);
        }
    }

    @Override
    public Set<String> getActiveSessions() {
        return sessionCache.getActiveSessions();
    }

    @Override
    public Set<String> getAllSessions() {
        return sessionCache.getAllSessions();
    }

    @Override
    public void recordSessionTime(@NonNull ManagedSession session) {
        long now = System.currentTimeMillis();
        getStatistics().recordTime(round((now - session.getSessionData().getCreated()) / 1000.0));
    }

    @Override
    public SessionStatistics getStatistics() {
        return statistics;
    }

    @Override
    protected void doInitialize() throws Exception {
        if (sessionCache == null) {
            throw new IllegalStateException("SessionCache is not set");
        }
        if (sessionCache instanceof AbstractComponent component && component.isInitializable()) {
            component.initialize();
        }
        scheduler.start();
        if (houseKeeper != null && !houseKeeper.isRunning()) {
            houseKeeper.start();
        }
    }

    @Override
    protected void doDestroy() throws Exception {
        if (houseKeeper != null) {
            houseKeeper.stop();
        }
        scheduler.stop();
        if (sessionCache instanceof AbstractComponent component) {
            component.destroy();
        }
    }

    @Override
    public String getComponentName() {
        if (getWorkerName() != null) {
            return super.getComponentName() + "(" + getWorkerName() + ")";
        } else {
            return super.getComponentName();
        }
    }

}
