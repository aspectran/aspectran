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
import com.aspectran.core.util.statistic.SampleStatistic;
import com.aspectran.core.util.thread.AutoLock;
import com.aspectran.core.util.thread.ScheduledExecutorScheduler;
import com.aspectran.core.util.thread.Scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.round;

/**
 * Abstract Implementation of SessionHandler.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public abstract class AbstractSessionHandler extends AbstractComponent implements SessionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionHandler.class);

    private final SampleStatistic sessionTimeStats = new SampleStatistic();

    private final List<SessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    private final Set<String> candidateSessionIdsForExpiry = ConcurrentHashMap.newKeySet();

    private final Scheduler scheduler;

    private String workerName;

    private SessionIdGenerator sessionIdGenerator;

    private SessionCache sessionCache;

    private HouseKeeper houseKeeper;

    /** 30 minute default */
    private volatile int defaultMaxIdleSecs = 30 * 60;

    public AbstractSessionHandler() {
        scheduler = new ScheduledExecutorScheduler(String.format("SessionScheduler-%x", hashCode()), false);
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String getWorkerName() {
        return workerName;
    }

    protected void setWorkerName(String workerName) {
        if (workerName != null && workerName.contains(".")) {
            throw new IllegalArgumentException("Worker name cannot contain '.'");
        }
        this.workerName = workerName;
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
            if (defaultMaxIdleSecs <= 0) {
                logger.debug("Sessions created by this manager are immortal (default maxInactiveInterval="
                        + defaultMaxIdleSecs + ")");
            } else {
                logger.debug("SessionHandler default maxInactiveInterval=" + defaultMaxIdleSecs);
            }
        }
    }

    @Override
    public DefaultSession getSession(String id) {
        try {
            DefaultSession session = sessionCache.get(id);
            if (session != null) {
                // if the session we got back has expired
                if (session.isExpiredAt(System.currentTimeMillis())) {
                    // expire the session
                    try {
                        session.setDestroyedReason(Session.DestroyedReason.TIMEOUT);
                        session.invalidate();
                    } catch (Exception e) {
                        logger.warn("Invalidating session " + id + " found to be expired when requested", e);
                    }
                    return null;
                }
            }
            return session;
        } catch (Exception e) {
            logger.warn(e);
            return null;
        }
    }

    @Override
    public DefaultSession createSession(String id) {
        long created = System.currentTimeMillis();
        long maxInactiveInterval = (defaultMaxIdleSecs > 0 ? defaultMaxIdleSecs * 1000L : -1L);
        try {
            DefaultSession session = sessionCache.add(id, created, maxInactiveInterval);
            fireSessionCreatedListeners(session);
            return session;
        } catch (Exception e) {
            throw new RuntimeException("Could not create a new session", e);
        }
    }

    @Override
    public void releaseSession(DefaultSession session) {
        try {
            sessionCache.release(session.getId(), session);
        } catch (Exception e) {
            logger.warn("Session failed to save", e);
        }
    }

    @Override
    public String createSessionId(long seedTerm) {
        return sessionIdGenerator.createSessionId(seedTerm);
    }

    @Override
    public String renewSessionId(String oldId, String newId) {
        try {
            DefaultSession session = sessionCache.renewSessionId(oldId, newId);
            for (SessionListener listener : sessionListeners) {
                listener.sessionIdChanged(session, oldId);
            }
            return session.getId();
        } catch (Exception e) {
            logger.warn("Failed to renew session", e);
        }
        return null;
    }

    @Override
    public DefaultSession removeSession(String id, boolean invalidate) {
        return removeSession(id, invalidate, null);
    }

    @Override
    public DefaultSession removeSession(String id, boolean invalidate, Session.DestroyedReason reason) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        try {
            // Remove the Session object from the session store and any backing data store
            DefaultSession session = sessionCache.delete(id);
            if (invalidate && session != null) {
                // start invalidating if it is not already begun, and call the listeners
                try {
                    if (session.beginInvalidate()) {
                        try (AutoLock ignored = session.lock()) {
                            if (reason != null) {
                                session.setDestroyedReason(reason);
                            }
                            fireSessionDestroyedListeners(session);
                        } catch (Exception e) {
                            logger.warn("Session listener threw exception", e);
                        }
                        // call the attribute removed listeners and finally mark it as invalid
                        session.finishInvalidate();
                    }
                } catch (IllegalStateException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Session " + session + " already invalid");
                    }
                }
            }
            return session;
        } catch (Exception e) {
            logger.warn("Unable to invalidate session", e);
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

    @Override
    public void sessionInactivityTimerExpired(DefaultSession session, long now) {
        if (session == null) {
            return;
        }

        // check if the session is:
        // 1. valid
        // 2. expired
        // 3. idle
        try (AutoLock ignored = session.lock()) {
            if (session.getRequests() > 0) {
                return; // session can't expire or be idle if there is a request in it
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Inspecting session " + session.getId() + ", valid=" + session.isValid());
            }
            if (!session.isValid()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Session " + session.getId() + " is no longer valid");
                }
                return; // do nothing, session is no longer valid
            }
            if (session.isExpiredAt(now)) {
                // instead of expiring the session directly here, accumulate a list of
                // session ids that need to be expired. This is an efficiency measure: as
                // the expiration involves the SessionStore doing a delete, it is
                // most efficient if it can be done as a bulk operation to eg reduce
                // roundtrips to the persistent store. Only do this if the HouseKeeper that
                // does the scavenging is configured to actually scavenge
                if (getHouseKeeper() != null && getHouseKeeper().isRunning()) {
                    candidateSessionIdsForExpiry.add(session.getId());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Session " + session.getId() + " is candidate for expiry");
                    }
                } else {
                    invalidate(session.getId(), Session.DestroyedReason.TIMEOUT);
                }
            } else {
                // possibly evict the session
                sessionCache.checkInactiveSession(session);
            }
        }
    }

    @Override
    public void scavenge() {
        // don't attempt to scavenge if we are shutting down
        if (isDestroying() || isDestroyed()) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace(getComponentName() + " scavenging sessions");
        }
        // Get a snapshot of the candidates as they are now. Others that
        // arrive during this processing will be dealt with on
        // subsequent call to scavenge
        String[] candidateSessionIds = candidateSessionIdsForExpiry.toArray(new String[0]);
        Set<String> candidates = new HashSet<>(Arrays.asList(candidateSessionIds));
        candidateSessionIdsForExpiry.removeAll(candidates);
        if (logger.isTraceEnabled()) {
            logger.trace(getComponentName() + " scavenging session ids " + candidates);
        }
        try {
            candidates = sessionCache.checkExpiration(candidates);
            if (candidates != null) {
                for (String id : candidates) {
                    try {
                        invalidate(id, Session.DestroyedReason.TIMEOUT);
                    } catch (Exception e) {
                        logger.warn(e);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    @Override
    public void addSessionListener(SessionListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registered session listener " + listener);
        }
        sessionListeners.add(listener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removed session listener " + listener);
        }
        sessionListeners.remove(listener);
    }

    @Override
    public void clearSessionListeners() {
        sessionListeners.clear();
    }

    @Override
    public void fireSessionAttributeListeners(Session session, String name, Object oldValue, Object newValue) {
        if (session == null) {
            return;
        }
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

    @Override
    public void fireSessionDestroyedListeners(Session session) {
        if (session == null) {
            return;
        }
        if (!sessionListeners.isEmpty()) {
            // We need to create our own snapshot to safely iterate over a concurrent list in reverse
            List<SessionListener> listeners = new ArrayList<>(sessionListeners);
            for (ListIterator<SessionListener> iter = listeners.listIterator(listeners.size()); iter.hasPrevious();) {
                iter.previous().sessionDestroyed(session);
            }
        }
    }

    /**
     * Call the session lifecycle listeners.
     * @param session the session on which to call the lifecycle listeners
     */
    private void fireSessionCreatedListeners(Session session) {
        if (session == null) {
            return;
        }
        for (SessionListener listener : sessionListeners) {
            listener.sessionCreated(session);
        }
    }

    @Override
    public void recordSessionTime(DefaultSession session) {
        long now = System.currentTimeMillis();
        sessionTimeStats.record(round((now - session.getSessionData().getCreated()) / 1000.0));
    }

    @Override
    public long getSessionTimeMax() {
        return sessionTimeStats.getMax();
    }

    @Override
    public long getSessionTimeTotal() {
        return sessionTimeStats.getTotal();
    }

    @Override
    public long getSessionTimeMean() {
        return Math.round(sessionTimeStats.getMean());
    }

    @Override
    public double getSessionTimeStdDev() {
        return sessionTimeStats.getStdDev();
    }

    @Override
    public void statsReset() {
        sessionTimeStats.reset();
    }

    @Override
    protected void doInitialize() throws Exception {
        scheduler.start();
        if (houseKeeper != null) {
            houseKeeper.start();
        }
    }

    @Override
    protected void doDestroy() throws Exception {
        if (houseKeeper != null) {
            houseKeeper.stop();
        }
        scheduler.stop();
        sessionCache.destroy();
    }

    @Override
    public String getComponentName() {
        if (workerName != null) {
            return super.getComponentName() + "(" + workerName + ")";
        } else {
            return super.getComponentName();
        }
    }

}
