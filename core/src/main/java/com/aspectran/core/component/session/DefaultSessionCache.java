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

import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.statistic.CounterStatistic;
import com.aspectran.core.util.thread.AutoLock;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Implementation of {@code SessionCache}.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public class DefaultSessionCache extends AbstractSessionCache {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionCache.class);

    /** the cache of sessions in a HashMap */
    private final Map<String, DefaultSession> sessions = new ConcurrentHashMap<>();

    private final CounterStatistic statistics = new CounterStatistic();

    private final AtomicLong expiredSessionCount = new AtomicLong();

    private final AtomicLong rejectedSessionCount = new AtomicLong();

    /** Determines the maximum number of active sessions allowed. */
    private volatile int maxSessions;

    public DefaultSessionCache(SessionHandler sessionHandler, SessionStore sessionStore, boolean clusterEnabled) {
        super(sessionHandler, sessionStore, clusterEnabled);
    }

    @Override
    public int getMaxSessions() {
        return maxSessions;
    }

    @Override
    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    @Override
    protected DefaultSession doGet(String id) {
        if (id == null) {
            return null;
        }
        return sessions.get(id);
    }

    @Override
    protected DefaultSession doPutIfAbsent(String id, DefaultSession session) {
        DefaultSession ds = sessions.putIfAbsent(id, session);
        if (ds == null) {
            checkMaxSessions(id);
        }
        return ds;
    }

    @Override
    protected DefaultSession doComputeIfAbsent(String id, Function<String, DefaultSession> mappingFunction) {
        return sessions.computeIfAbsent(id, k -> {
            DefaultSession ds = mappingFunction.apply(k);
            if (ds != null) {
                checkMaxSessions(null);
            }
            return ds;
        });
    }

    @Override
    protected DefaultSession doDelete(String id) {
        DefaultSession ds = sessions.remove(id);
        if (ds != null) {
            statistics.decrement();
            expiredSessionCount.incrementAndGet();
        }
        return ds;
    }

    @Override
    protected boolean doReplace(String id, DefaultSession oldValue, DefaultSession newValue) {
        return sessions.replace(id, oldValue, newValue);
    }

    private void checkMaxSessions(String id) {
        if (maxSessions > 0 && statistics.getCurrent() >= maxSessions) {
            if (id != null) {
                sessions.remove(id);
            }
            rejectedSessionCount.incrementAndGet();
            throw new IllegalStateException("Session was rejected as the maximum number of sessions " +
                    maxSessions + " has been hit");
        } else {
            statistics.increment();
        }
    }

    @Override
    public Set<String> getAllSessions() {
        return sessions.keySet();
    }

    @Override
    public long getActiveSessionCount() {
        return statistics.getCurrent();
    }

    @Override
    public long getHighestSessionCount() {
        return statistics.getMax();
    }

    @Override
    public long getCreatedSessionCount() {
        return statistics.getTotal();
    }

    @Override
    public long getExpiredSessionCount() {
        return expiredSessionCount.get();
    }

    @Override
    public long getRejectedSessionCount() {
        return rejectedSessionCount.get();
    }

    @Override
    public void resetStatistics() {
        statistics.reset();
        expiredSessionCount.set(0L);
        rejectedSessionCount.set(0L);
    }

    @Override
    protected void doInitialize() throws Exception {
        if (getSessionStore() != null) {
            getSessionStore().initialize();
        }
    }

    @Override
    protected void doDestroy() throws Exception {
        // loop over all the sessions in memory (a few times if necessary to catch sessions that have been
        // added while we're running
        int loop = 100;
        while (!sessions.isEmpty() && loop-- >= 0) {
            for (DefaultSession session : sessions.values()) {
                // if we have a backing store so give the session to it to write out if necessary
                if (getSessionStore() != null) {
                    // remove attributes excluded from serialization
                    if (getSessionStore().getNonPersistentAttributes() != null) {
                        for (String name : getSessionStore().getNonPersistentAttributes()) {
                            try {
                                Object old;
                                try (AutoLock ignored = session.lock()) {
                                    old = session.getSessionData().setAttribute(name, null);
                                }
                                if (old != null) {
                                    session.fireSessionAttributeListeners(name, old, null);
                                }
                            } catch (Exception e) {
                                logger.warn("Failed to remove non-persistent attribute: " + name, e);
                            }
                        }
                    }
                    try {
                        getSessionStore().save(session.getId(), session.getSessionData());
                    } catch (Exception e) {
                        logger.warn("Failed to save session data of session id=" + session.getId(), e);
                    }
                    doDelete(session.getId()); // remove from memory
                } else {
                    // not preserving sessions on exit
                    try {
                        session.invalidate();
                        session.setDestroyedReason(Session.DestroyedReason.UNDEPLOY);
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Session invalidation failed, but ignored", e);
                        }
                    }
                }
            }
        }
        if (getSessionStore() != null) {
            getSessionStore().destroy();
        }
    }

}
