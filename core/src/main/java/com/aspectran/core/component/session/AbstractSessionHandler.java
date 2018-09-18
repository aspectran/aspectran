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

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.statistic.CounterStatistic;
import com.aspectran.core.util.statistic.SampleStatistic;
import com.aspectran.core.util.thread.ScheduledExecutorScheduler;
import com.aspectran.core.util.thread.Scheduler;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.round;

/**
 * Abstract Implementation of SessionHandler.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public abstract class AbstractSessionHandler extends AbstractComponent implements SessionHandler {

    private static final Log log = LogFactory.getLog(AbstractSessionHandler.class);

    protected final SampleStatistic sessionTimeStats = new SampleStatistic();

    protected final CounterStatistic sessionsCreatedStats = new CounterStatistic();

    private final List<SessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    private final List<SessionAttributeListener> sessionAttributeListeners = new CopyOnWriteArrayList<>();

    private final Scheduler scheduler = new ScheduledExecutorScheduler();

    private SessionIdGenerator sessionIdGenerator;

    private SessionCache sessionCache;

    private int defaultMaxIdleSecs = -1;

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
     * Called by the {@link SessionHandler} when a session is first accessed by a request.
     *
     * @param session the session object
     * @see #complete(Session)
     */
    public void access(Session session) {
        long now = System.currentTimeMillis();
        session.access(now);
    }

    /**
     * Called by the {@link SessionHandler} when a session is last accessed by a request.
     *
     * @param session the session object
     * @see #access(Session)
     */
    public void complete(Session session) {
        try {
            session.complete();
            sessionCache.put(session.getId(), session);
        } catch (Exception e) {
            log.warn("Session failed to complete", e);
        }
    }

    /**
     * Create an entirely new Session.
     *
     * @param id identity of session to create
     * @return the new session object
     */
    @Override
    public Session newSession(String id) {
        long created = System.currentTimeMillis();
        Session session = sessionCache.newSession(id, created, (defaultMaxIdleSecs > 0 ? defaultMaxIdleSecs * 1000L : -1));
        try {
            sessionCache.put(id, session);
            sessionsCreatedStats.increment();

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
    public Session getSession(String id) {
        try {
            Session session = sessionCache.get(id);
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
        } catch (UnreadableSessionDataException e) {
            log.warn(e.getMessage(), e);
            return null;
        } catch (Exception other) {
            log.warn(other.getMessage(), other);
            return null;
        }
    }

    @Override
    public void invalidate(String id) {
        Session session = removeSession(id);
        if (session != null) {
            sessionTimeStats.set(round((System.currentTimeMillis() - session.getSessionData().getCreationTime()) / 1000.0));
            session.finishInvalidate();
        }
    }

    /**
     * Remove session from manager.
     *
     * @param id the session to remove
     * @return if the session was removed
     */
    private Session removeSession(String id) {
        try {
            Session session = sessionCache.delete(id);
            if (session != null) {
                session.beginInvalidate();
                for (int i = sessionListeners.size() - 1; i >= 0; i--) {
                    sessionListeners.get(i).sessionDestroyed(session);
                }
            }
            return session;
        } catch (Exception e) {
            log.warn("Failed to delete session", e);
            return null;
        }
    }


    @Override
    public String newSessionId(long seedTerm) {
        return sessionIdGenerator.newSessionId(seedTerm);
    }

    /**
     * Adds an event listener for session-related events.
     *
     * @param listener the session event listener
     * @see #removeEventListener(EventListener)
     */
    @Override
    public void addEventListener(EventListener listener) {
        if (listener instanceof SessionListener) {
            sessionListeners.add((SessionListener)listener);
        }
        if (listener instanceof SessionAttributeListener) {
            sessionAttributeListeners.add((SessionAttributeListener)listener);
        }
    }

    /**
     * Removes an event listener for for session-related events.
     *
     * @param listener the session event listener to remove
     * @see #addEventListener(EventListener)
     */
    @Override
    public void removeEventListener(EventListener listener) {
        if (listener instanceof SessionListener) {
            sessionListeners.remove(listener);
        }
        if (listener instanceof SessionAttributeListener) {
            sessionAttributeListeners.remove(listener);
        }
    }

    /**
     * Removes all event listeners for session-related events.
     *
     * @see #removeEventListener(EventListener)
     */
    @Override
    public void clearEventListeners() {
        sessionListeners.clear();
        sessionAttributeListeners.clear();
    }

    @Override
    public void sessionAttributeChanged(Session session, String name, Object oldValue, Object newValue) {
        if (newValue == null || !newValue.equals(oldValue)) {
            if (oldValue != null) {
                unbindValue(session, name, oldValue);
            }
            if (newValue != null) {
                bindValue(session, name, newValue);
            }
        }
        if (!sessionAttributeListeners.isEmpty()) {
            for (SessionAttributeListener listener : sessionAttributeListeners) {
                if (oldValue == null) {
                    listener.attributeAdded(session, name, newValue);
                } else if (newValue == null) {
                    listener.attributeRemoved(session, name, oldValue);
                } else {
                    listener.attributeReplaced(session, name, oldValue);
                }
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
    private void unbindValue(Session session, String name, Object value) {
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
    private void bindValue(Session session, String name, Object value) {
        if (value instanceof SessionBindingListener) {
            ((SessionBindingListener)value).valueBound(session, name, value);
        }
    }

    @Override
    public void didActivate(Session session) {
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
    public void willPassivate(Session session) {
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
    public double getSessionTimeMean() {
        return sessionTimeStats.getMean();
    }

    @Override
    public double getSessionTimeStdDev() {
        return sessionTimeStats.getStdDev();
    }

    @Override
    public int getSessionsCreated() {
        return (int)sessionsCreatedStats.getCurrent();
    }

    /**
     * Resets the session usage statistics.
     */
    public void statsReset() {
        sessionsCreatedStats.reset();
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
