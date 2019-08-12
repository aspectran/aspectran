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
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.statistic.SampleStatistic;
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.ScheduledExecutorScheduler;
import com.aspectran.core.util.thread.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.round;

/**
 * Abstract Implementation of SessionHandler.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public abstract class AbstractSessionHandler extends AbstractComponent implements SessionHandler {

    private static final Log log = LogFactory.getLog(AbstractSessionHandler.class);

    private final SampleStatistic sessionTimeStats = new SampleStatistic();

    private final List<SessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    private final Scheduler scheduler = new ScheduledExecutorScheduler();

    private SessionIdGenerator sessionIdGenerator;

    private SessionCache sessionCache;

    /** 30 minute default */
    private volatile int defaultMaxIdleSecs = 30 * 60;

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

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public int getDefaultMaxIdleSecs() {
        return defaultMaxIdleSecs;
    }

    @Override
    public void setDefaultMaxIdleSecs(int defaultMaxIdleSecs) {
        this.defaultMaxIdleSecs = defaultMaxIdleSecs;
        if (log.isDebugEnabled()) {
            if (defaultMaxIdleSecs <= 0) {
                log.debug("Sessions created by this manager are immortal (default maxInactiveInterval="
                        + defaultMaxIdleSecs + ")");
            } else {
                log.debug("SessionHandler default maxInactiveInterval=" + defaultMaxIdleSecs);
            }
        }
    }

    /**
     * Create an entirely new Session.
     *
     * @param id identity of session to create
     * @return the new session object
     */
    @Override
    public BasicSession createSession(String id) {
        long created = System.currentTimeMillis();
        long maxIdleSecs = (defaultMaxIdleSecs > 0 ? defaultMaxIdleSecs * 1000L : -1);
        BasicSession session = sessionCache.createSession(id, created, maxIdleSecs);
        try {
            sessionCache.put(id, session);
            for (SessionListener listener : sessionListeners) {
                listener.sessionCreated(session);
            }
            return session;
        } catch (Exception e) {
            log.warn("Failed to create a new session", e);
            return null;
        }
    }

    @Override
    public BasicSession getSession(String id) {
        try {
            BasicSession session = sessionCache.get(id);
            if (session != null) {
                // if the session we got back has expired
                if (session.isExpiredAt(System.currentTimeMillis())) {
                    // expire the session
                    try {
                        session.invalidate();
                    } catch (Exception e) {
                        log.warn("Invalidating session " + id + " found to be expired when requested", e);
                    }
                    return null;
                }
            }
            return session;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void saveSession(BasicSession session) {
        try {
            sessionCache.put(session.getId(), session);
        } catch (Exception e) {
            log.warn("Session failed to save", e);
        }
    }

    /**
     * Remove session from manager.
     *
     * @param id the session to remove
     * @return if the session was removed
     */
    private BasicSession removeSession(String id) {
        try {
            BasicSession session = sessionCache.delete(id);
            if (session != null) {
                session.beginInvalidate();
                // We need to create our own snapshot to safely iterate over a concurrent list in reverse
                List<SessionListener> listeners = new ArrayList<>(sessionListeners);
                ListIterator<SessionListener> iterator = listeners.listIterator(listeners.size());
                while (iterator.hasPrevious()) {
                    iterator.previous().sessionDestroyed(session);
                }
            }
            return session;
        } catch (Exception e) {
            log.warn("Failed to delete session", e);
            return null;
        }
    }

    @Override
    public String createSessionId(long seedTerm) {
        return sessionIdGenerator.createSessionId(seedTerm);
    }

    @Override
    public String renewSessionId(String oldId, String newId) {
        try {
            BasicSession session = sessionCache.renewSessionId(oldId, newId);
            for (SessionListener listener : sessionListeners) {
                listener.sessionIdChanged(session, oldId);
            }
            return session.getId();
        } catch (Exception e) {
            log.warn("Failed to renew session", e);
        }
        return null;
    }

    @Override
    public void invalidate(String id) {
        BasicSession session = removeSession(id);
        if (session != null) {
            sessionTimeStats.set(round((System.currentTimeMillis() - session.getSessionData().getCreationTime()) / 1000.0));
            session.finishInvalidate();
        }
    }

    /**
     * Each session has a timer that is configured to go off
     * when either the session has not been accessed for a
     * configurable amount of time, or the session itself
     * has passed its expiry.
     *
     * If it has passed its expiry, then we will mark it for
     * scavenging by next run of the HouseKeeper; if it has
     * been idle longer than the configured eviction period,
     * we evict from the cache.
     *
     * If none of the above are true, then the System timer
     * is inconsistent and the caller of this method will
     * need to reset the timer.
     *
     * @param session the basic session
     * @param now the time at which to check for expiry
     */
    @Override
    public void sessionInactivityTimerExpired(BasicSession session, long now) {
        if (session == null) {
            return;
        }

        // check if the session is:
        // 1. valid
        // 2. expired
        // 3. idle
        try (Locker.Lock ignored = session.lock()) {
            if (session.getRequests() > 0) {
                return; // session can't expire or be idle if there is a request in it
            }
            if (log.isDebugEnabled()) {
                log.debug("Inspecting session " + session.getId() + ", valid=" + session.isValid());
            }
            if (!session.isValid()) {
                return; // do nothing, session is no longer valid
            }
            if (session.isExpiredAt(now)) {
                invalidate(session.getId());
            } else {
                //possibly evict the session
                sessionCache.checkInactiveSession(session);
            }
        }
    }

    /**
     * Adds an event listener for session-related events.
     *
     * @param listener the session listener
     * @see #removeSessionListener(SessionListener)
     */
    @Override
    public void addSessionListener(SessionListener listener) {
        if (log.isDebugEnabled()) {
            log.debug("Registered session listener " + listener);
        }
        sessionListeners.add(listener);
    }

    /**
     * Removes an event listener for for session-related events.
     *
     * @param listener the session event listener to remove
     * @see #addSessionListener(SessionListener)
     */
    @Override
    public void removeSessionListener(SessionListener listener) {
        if (log.isDebugEnabled()) {
            log.debug("Removed session listener " + listener);
        }
        sessionListeners.remove(listener);
    }

    /**
     * Removes all event listeners for session-related events.
     *
     * @see #removeSessionListener(SessionListener)
     */
    @Override
    public void clearSessionListeners() {
        sessionListeners.clear();
    }

    @Override
    public void attributeChanged(BasicSession session, String name, Object oldValue, Object newValue) {
        if (newValue == null || !newValue.equals(oldValue)) {
            if (oldValue != null) {
                unbindValue(session, name, oldValue);
            }
            if (newValue != null) {
                bindValue(session, name, newValue);
            }
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

    /**
     * Unbind value if value implements {@link SessionBindingListener}
     * (calls {@link SessionBindingListener#valueUnbound(Session, String, Object)})
     *
     * @param session the basic session
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    private void unbindValue(BasicSession session, String name, Object value) {
        if (value instanceof SessionBindingListener) {
            ((SessionBindingListener)value).valueUnbound(session, name, value);
        }
    }

    /**
     * Bind value if value implements {@link SessionBindingListener}
     * (calls {@link SessionBindingListener#valueBound(Session, String, Object)})
     *
     * @param session the basic session
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    private void bindValue(BasicSession session, String name, Object value) {
        if (value instanceof SessionBindingListener) {
            ((SessionBindingListener)value).valueBound(session, name, value);
        }
    }

    @Override
    public void didActivate(BasicSession session) {
        SessionData sessionData = session.getSessionData();
        if (sessionData != null) {
            for (String key : sessionData.getKeys()) {
                Object value = sessionData.getAttribute(key);
                if (value instanceof SessionActivationListener) {
                    SessionActivationListener listener = (SessionActivationListener)value;
                    listener.sessionDidActivate(session);
                }
            }
        }
    }

    @Override
    public void willPassivate(BasicSession session) {
        SessionData sessionData = session.getSessionData();
        if (sessionData != null) {
            for (String key : sessionData.getKeys()) {
                Object value = sessionData.getAttribute(key);
                if (value instanceof SessionActivationListener) {
                    SessionActivationListener listener = (SessionActivationListener)value;
                    listener.sessionWillPassivate(session);
                }
            }
        }
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

    /**
     * Resets the session usage statistics.
     */
    @Override
    public void statsReset() {
        sessionTimeStats.reset();
    }

    @Override
    protected void doInitialize() throws Exception {
        scheduler.start();
    }

    @Override
    protected void doDestroy() throws Exception {
        scheduler.stop();
        sessionCache.clear();
    }

}
