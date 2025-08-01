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

import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.thread.AutoLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Implementation of {@code SessionCache}.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public class DefaultSessionCache extends AbstractSessionCache {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionCache.class);

    /** the cache of sessions in a HashMap */
    private final Map<String, ManagedSession> sessions = new ConcurrentHashMap<>();

    /** Determines the maximum number of concurrent active sessions allowed. */
    private volatile int maxActiveSessions;

    public DefaultSessionCache(AbstractSessionManager sessionManager, SessionStore sessionStore, boolean clusterEnabled) {
        super(sessionManager, sessionStore, clusterEnabled);
    }

    @Override
    public int getMaxActiveSessions() {
        return maxActiveSessions;
    }

    @Override
    public void setMaxActiveSessions(int maxActiveSessions) {
        this.maxActiveSessions = maxActiveSessions;
    }

    @Override
    protected ManagedSession doGet(String id) {
        return (id != null ? sessions.get(id) : null);
    }

    @Override
    protected ManagedSession doPutIfAbsent(String id, ManagedSession session) {
        AtomicBoolean absent = new AtomicBoolean(false);
        ManagedSession current = doComputeIfAbsent(id, k -> {
            absent.set(true);
            return session;
        });
        return (absent.get() ? null : current);
    }

    @Override
    protected ManagedSession doComputeIfAbsent(String id, Function<String, ManagedSession> mappingFunction) {
        return sessions.computeIfAbsent(id, k -> {
            checkMaxSessions(id);
            ManagedSession session = mappingFunction.apply(k);
            if (session != null) {
                getStatistics().sessionActivated();
            }
            return session;
        });
    }

    @Override
    protected ManagedSession doDelete(String id) {
        ManagedSession session = sessions.remove(id);
        if (session != null) {
            getStatistics().sessionInactivated();
        }
        return session;
    }

    private void checkMaxSessions(String id) {
        if (maxActiveSessions > 0 && getStatistics().getNumberOfActives() >= maxActiveSessions) {
            getStatistics().sessionRejected();
            if (logger.isDebugEnabled()) {
                logger.debug("Reject session id={}; Exceeded maximum number of sessions allowed", id);
            }
            throw new MaxSessionsExceededException(id, maxActiveSessions);
        }
    }

    @Override
    public Set<String> getActiveSessions() {
        return new HashSet<>(sessions.keySet());
    }

    @Override
    public Set<String> getAllSessions() {
        if (getSessionStore() != null) {
            return getSessionStore().getAllSessions();
        } else {
            return getActiveSessions();
        }
    }

    @Override
    protected void doInitialize() throws Exception {
        if (getSessionStore() != null) {
            if (getSessionStore().isInitializable()) {
                getSessionStore().initialize();
            }
            if (!isClusterEnabled()) {
                int restoredSessions = getSessionStore().getAllSessions().size();
                if (restoredSessions > 0) {
                    getStatistics().sessionCreated(restoredSessions);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Restored {} sessions from {}", restoredSessions, getSessionStoreName());
                    }
                }
            }
        }
    }

    @Override
    protected void doDestroy() throws Exception {
        // loop over all the sessions in memory (a few times if necessary to catch sessions that have been
        // added while we're running
        int loop = 100;
        while (!sessions.isEmpty() && loop-- >= 0) {
            for (ManagedSession session : sessions.values()) {
                // if we have a backing store so give the session to it to write out if necessary
                if (getSessionStore() != null) {
                    // remove attributes excluded from serialization
                    if (getSessionStore().getNonPersistentAttributes() != null) {
                        for (String name : getSessionStore().getNonPersistentAttributes()) {
                            try {
                                session.removeAttribute(name);
                            } catch (Exception e) {
                                logger.warn("Failed to remove non-persistent attribute: {}", name, e);
                            }
                        }
                    }
                    try (AutoLock ignored = session.lock()) {
                        for (Map.Entry<String, Object> entry : session.getSessionData().getAllAttributes().entrySet()) {
                            String name = entry.getKey();
                            Object value = entry.getValue();
                            if (value instanceof NonPersistent) {
                                try {
                                    Object old = session.getSessionData().removeAttribute(name);
                                    if (old != null) {
                                        Object oldValue = NonPersistentValue.unwrap(old);
                                        session.onSessionAttributeUpdate(name, oldValue, null);
                                    }
                                } catch (Exception e) {
                                    logger.warn("Failed to remove non-persistent attribute: {}", name, e);
                                }
                            }
                        }
                    }
                    try {
                        getSessionStore().save(session.getId(), session.getSessionData());
                    } catch (Exception e) {
                        logger.warn("Failed to save session data of session id={}", session.getId(), e);
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

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("maxActiveSessions", getMaxActiveSessions());
        tsb.append("evictionIdleSecs", getEvictionIdleSecs());
        tsb.append("evictionIdleSecsForNew", getEvictionIdleSecsForNew());
        tsb.appendForce("saveOnCreate", isSaveOnCreate());
        tsb.appendForce("saveOnInactiveEviction", isSaveOnInactiveEviction());
        tsb.appendForce("clusterEnabled", isClusterEnabled());
        tsb.append("store", getSessionStoreName());
        return tsb.toString();
    }

}
