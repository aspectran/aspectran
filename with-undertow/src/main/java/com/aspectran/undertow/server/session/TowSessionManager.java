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
package com.aspectran.undertow.server.session;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.ManagedSession;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.component.session.SessionStore;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.util.AttachmentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages Undertow sessions by delegating to an Aspectran {@link SessionManager}.
 *
 * <p>Created: 2019-08-07</p>
 */
public class TowSessionManager implements ActivityContextAware, DisposableBean, io.undertow.server.session.SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(TowSessionManager.class);

    private final AttachmentKey<TowSession> NEW_SESSION = AttachmentKey.create(TowSession.class);

    private final AttachmentKey<Boolean> FIRST_ACCESS = AttachmentKey.create(Boolean.class);

    private final DefaultSessionManager sessionManager = new DefaultSessionManager();

    private final Map<SessionListener, TowSessionListener> sessionListenerMappings = new ConcurrentHashMap<>();

    private final AtomicInteger startCount = new AtomicInteger();

    private int defaultSessionTimeout = Integer.MIN_VALUE;

    @Override
    public void setActivityContext(ActivityContext context) {
        sessionManager.setActivityContext(context);
    }

    /**
     * Sets the class loader to be used by the session manager.
     * @param classLoader the class loader
     */
    public void setClassLoader(ClassLoader classLoader) {
        sessionManager.setClassLoader(classLoader);
    }

    /**
     * Returns the underlying Aspectran session manager.
     * @return the session manager
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Returns the configuration for the underlying Aspectran session manager.
     * @return the session manager configuration
     */
    public SessionManagerConfig getSessionManagerConfig() {
        return sessionManager.getSessionManagerConfig();
    }

    /**
     * Sets the configuration for the underlying Aspectran session manager.
     * @param sessionManagerConfig the session manager configuration
     */
    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) {
        sessionManager.setSessionManagerConfig(sessionManagerConfig);
    }

    /**
     * Sets the session store for the underlying Aspectran session manager.
     * @param sessionStore the session store
     */
    public void setSessionStore(SessionStore sessionStore) {
        sessionManager.setSessionStore(sessionStore);
    }

    /**
     * Initializes the underlying Aspectran session manager.
     * @throws Exception if initialization fails
     */
    public void initialize() throws Exception {
        if (sessionManager.isInitializable()) {
            sessionManager.initialize();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (sessionManager.isAvailable()) {
            try {
                sessionManager.destroy();
            } catch (Exception e) {
                throw new RuntimeException("Error destroying TowSessionManager", e);
            }
        }
    }

    @Override
    public String getDeploymentName() {
        return sessionManager.getWorkerName();
    }

    @Override
    public void start() {
        startCount.getAndIncrement();
    }

    @Override
    public void stop() {
        int count = startCount.decrementAndGet();
        if (count == 0) {
            try {
                destroy();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a new, unique session ID.
     * @return a unique session ID
     */
    public String createSessionId() {
        return sessionManager.createSessionId();
    }

    /**
     * Renews the ID of an existing session.
     * @param oldId the current session ID
     * @param newId the new session ID to assign
     * @return the new session ID
     */
    public String renewSessionId(String oldId, String newId) {
        return sessionManager.renewSessionId(oldId, newId);
    }

    @Override
    public TowSession createSession(@NonNull HttpServerExchange exchange, @NonNull SessionConfig sessionConfig) {
        String sessionId;
        try {
            sessionId = sessionConfig.findSessionId(exchange);
        } catch (Exception e) {
            logger.error("Unable to create new session due to failure to find session ID", e);
            return null;
        }
        if (sessionId == null) {
            sessionId = sessionManager.createSessionId();
        }

        sessionConfig.setSessionId(exchange, sessionId);

        ManagedSession session = sessionManager.createSession(sessionId);
        TowSession towSession = wrapSession(session);
        exchange.putAttachment(NEW_SESSION, towSession);
        return towSession;
    }

    @Override
    public TowSession getSession(@NonNull HttpServerExchange exchange, SessionConfig sessionConfig) {
        TowSession newSession = exchange.getAttachment(NEW_SESSION);
        if (newSession != null) {
            return newSession;
        }

        if (sessionConfig == null) {
            throw new IllegalStateException("Could not find session config in the request");
        }

        String sessionId;
        try {
            sessionId = sessionConfig.findSessionId(exchange);
        } catch (Exception e) {
            logger.error("Unable to retrieve session due to failure to find session ID", e);
            return null;
        }

        TowSession towSession = getSession(sessionId);
        if (towSession != null) {
            towSession.requestStarted(exchange);
        }
        return towSession;
    }

    @Override
    public TowSession getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        ManagedSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            return wrapSession(session);
        } else {
            return null;
        }
    }

    @Override
    public void registerSessionListener(SessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        TowSessionListener towSessionListener = new TowSessionListener(this, listener);
        sessionListenerMappings.put(listener, towSessionListener);
        sessionManager.addSessionListener(towSessionListener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        TowSessionListener towSessionListener = sessionListenerMappings.remove(listener);
        if (towSessionListener != null) {
            sessionManager.removeSessionListener(towSessionListener);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Note that the initial timeout value set by Undertow may be overridden by
     * Aspectran's specific session configuration to ensure consistency.
     * Subsequent calls will update the timeout for all new sessions.</p>
     */
    @Override
    public void setDefaultSessionTimeout(int timeout) {
        /*
         * This logic is intentionally complex to handle a lifecycle interaction with Undertow.
         * Undertow may call this method with a default value early in the startup sequence.
         * We want to ignore this initial call and prioritize the timeout value set in Aspectran's
         * own configuration (`sessionManagerConfig`). Therefore, on the first call, if an
         * Aspectran config value exists, we only store it locally. Subsequent calls are
         * treated as runtime changes and are passed through to the underlying session manager.
         */
        if (defaultSessionTimeout == Integer.MIN_VALUE &&
                getSessionManagerConfig() != null &&
                getSessionManagerConfig().hasMaxIdleSeconds()) {
            defaultSessionTimeout = getSessionManagerConfig().getMaxIdleSeconds();
        } else {
            defaultSessionTimeout = timeout;
            sessionManager.setDefaultMaxIdleSecs(timeout);
        }
    }

    @Override
    public Set<String> getTransientSessions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getActiveSessions() {
        return sessionManager.getActiveSessions();
    }

    @Override
    public Set<String> getAllSessions() {
        return sessionManager.getAllSessions();
    }

    @Override
    public SessionManagerStatistics getStatistics() {
        return new SessionManagerStatistics() {
            @Override
            public long getCreatedSessionCount() {
                return sessionManager.getStatistics().getNumberOfCreated();
            }

            @Override
            public long getMaxActiveSessions() {
                return sessionManager.getSessionCache().getMaxActiveSessions();
            }

            @Override
            public long getHighestSessionCount() {
                return sessionManager.getStatistics().getHighestNumberOfActives();
            }

            @Override
            public long getActiveSessionCount() {
                return sessionManager.getStatistics().getNumberOfActives();
            }

            @Override
            public long getExpiredSessionCount() {
                return sessionManager.getStatistics().getNumberOfExpired();
            }

            @Override
            public long getRejectedSessions() {
                return sessionManager.getStatistics().getNumberOfRejected();
            }

            @Override
            public long getMaxSessionAliveTime() {
                return sessionManager.getStatistics().getMaxSessionAliveTime();
            }

            @Override
            public long getAverageSessionAliveTime() {
                return sessionManager.getStatistics().getAverageSessionAliveTime();
            }

            @Override
            public long getStartTime() {
                return sessionManager.getStatistics().getStartTime();
            }
        };
    }

    /**
     * Wraps the given Aspectran session in an Undertow session.
     * @param session the Aspectran session
     * @return the Undertow session
     */
    TowSession wrapSession(@NonNull com.aspectran.core.component.session.Session session) {
        return new TowSession(this, session);
    }

    /**
     * Clears the session from the exchange and the session config.
     * @param exchange the HTTP server exchange
     * @param sessionId the session ID
     */
    void clearSession(HttpServerExchange exchange, String sessionId) {
        if (exchange != null) {
            SessionConfig sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
            if (sessionConfig != null) {
                sessionConfig.clearSession(exchange, sessionId);
            }
            exchange.removeAttachment(NEW_SESSION);
        }
    }

    /**
     * Checks if this is the first access for the current request.
     * @param exchange the HTTP server exchange
     * @return true if it is the first access, false otherwise
     */
    boolean checkFirstAccess(@NonNull HttpServerExchange exchange) {
        if (exchange.getAttachment(FIRST_ACCESS) == null) {
            exchange.putAttachment(FIRST_ACCESS, true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the session has been accessed during the current request.
     * @param exchange the HTTP server exchange
     * @return true if the session has been accessed, false otherwise
     */
    boolean hasBeenAccessed(@NonNull HttpServerExchange exchange) {
        return (exchange.getAttachment(FIRST_ACCESS) != null || exchange.getAttachment(NEW_SESSION) != null);
    }

}
