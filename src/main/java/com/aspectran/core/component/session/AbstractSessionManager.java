/*
 * Copyright 2008-2017 Juho Jeong
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

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.ScheduledExecutorScheduler;
import com.aspectran.core.util.thread.Scheduler;

/**
 * Abstract Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public abstract class AbstractSessionManager implements SessionManager {

    private static final Log log = LogFactory.getLog(AbstractSessionManager.class);

    protected final Locker locker = new Locker();

    private final SessionIdGenerator sessionIdGenerator;

    private final SessionDataStore sessionDataStore;

    private final SessionCache sessionCache;

    private final List<SessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    private final List<SessionAttributeListener> sessionAttributeListeners = new CopyOnWriteArrayList<>();

    private final Scheduler scheduler = new ScheduledExecutorScheduler();

    private final SessionScavenger sessionScavenger;

    public AbstractSessionManager(String serviceName, SessionDataStore sessionDataStore) {
        this.sessionIdGenerator = new SessionIdGenerator(serviceName);
        this.sessionDataStore = sessionDataStore;
        this.sessionCache = new SessionCache();
        this.sessionScavenger = new SessionScavenger(this);
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public BasicSession getSession(String sessionId) {
        return getSession(sessionId, true);
    }

    @Override
    public BasicSession getSession(String id, boolean create) {
        try (Locker.Lock ignored = locker.lockIfNotHeld()) {
            BasicSession session = sessionCache.get(id);
            if (session != null) {
                session.notNewSession();
                if (session.isValid() && session.isResident()) {
                    return session;
                }
            }
            session = newSession(id);
            session.setResident(true);
            sessionCache.put(id, session);
            return session;
        }
    }

    private BasicSession newSession(String id) {
        BasicSession session = new BasicSession(this, id);
        try {
            for (SessionListener listener : sessionListeners) {
                listener.sessionCreated(session);
            }
        } catch(Exception e) {
            log.warn(e.getMessage(), e);
        }
        return session;
    }

    @Override
    public void invalidate(String id) {
        BasicSession session = removeSession(id);
        if (session != null) {
            session.finishInvalidate();
        }
    }

    private BasicSession removeSession(String id) {
        BasicSession session = sessionCache.remove(id);
        if (session != null) {
            session.setResident(false);
        }
        try {
            sessionDataStore.delete(id);
        } catch(Exception e) {
            log.warn("Failed to delete session data", e);
        }
        try {
            for (int i = sessionListeners.size() - 1; i >= 0; i--) {
                sessionListeners.get(i).sessionDestroyed(session);
            }
        } catch(Exception e) {
            log.warn(e.getMessage(), e);
        }
        return session;
    }

    @Override
    public SessionData loadSessionData(String id, boolean create) {
        SessionData data;
        try {
            data = sessionDataStore.load(id);
        } catch(Exception e) {
            log.warn("Failed to load session data", e);
            return null;
        }
        if (data == null && create) {
            data = newSessionData(id);
        }
        return data;
    }

    @Override
    public void storeSessionData(String id, SessionData sessionData) {
        try {
            sessionDataStore.store(id, sessionData);
        } catch (Exception e) {
            log.warn("Failed to store session data", e);
        }
    }

    private SessionData newSessionData(String id) {
        long now = System.currentTimeMillis();
        return new SessionData(id, now);
    }

    @Override
    public String newSessionId(long seedTerm) {
        return sessionIdGenerator.newSessionId(seedTerm);
    }

    @Override
    public void destroy() {
        sessionCache.clear();
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
    public void sessionAttributeChanged(BasicSession session, String name, Object oldValue, Object newValue) {
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
     * (calls {@link SessionBindingListener#valueUnbound(BasicSession, String, Object)})
     *
     * @param session the basic session
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    private void unbindValue(BasicSession session, String name, Object value) {
        if (value != null && value instanceof SessionBindingListener) {
            ((SessionBindingListener)value).valueUnbound(session, name, value);
        }
    }

    /**
     * Bind value if value implements {@link SessionBindingListener}
     * (calls {@link SessionBindingListener#valueBound(BasicSession, String, Object)})
     *
     * @param session the basic session
     * @param name the name with which the object is bound or unbound
     * @param value the bound value
     */
    private void bindValue(BasicSession session, String name, Object value) {
        if (value != null&&value instanceof SessionBindingListener) {
            ((SessionBindingListener)value).valueBound(session, name, value);
        }
    }

}
