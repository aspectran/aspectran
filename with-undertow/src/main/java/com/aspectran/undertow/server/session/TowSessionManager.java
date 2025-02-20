/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

    public void setClassLoader(ClassLoader classLoader) {
        sessionManager.setClassLoader(classLoader);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public SessionManagerConfig getSessionManagerConfig() {
        return sessionManager.getSessionManagerConfig();
    }

    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) {
        sessionManager.setSessionManagerConfig(sessionManagerConfig);
    }

    public void setSessionStore(SessionStore sessionStore) {
        sessionManager.setSessionStore(sessionStore);
    }

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

    public String createSessionId(long seedTerm) {
        return sessionManager.createSessionId(seedTerm);
    }

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
            sessionId = sessionManager.createSessionId(hashCode());
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

    @Override
    public void setDefaultSessionTimeout(int timeout) {
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

    TowSession wrapSession(@NonNull com.aspectran.core.component.session.Session session) {
        return new TowSession(this, session);
    }

    void clearSession(HttpServerExchange exchange, String sessionId) {
        if (exchange != null) {
            SessionConfig sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
            if (sessionConfig != null) {
                sessionConfig.clearSession(exchange, sessionId);
            }
            exchange.removeAttachment(NEW_SESSION);
        }
    }

    boolean checkFirstAccess(@NonNull HttpServerExchange exchange) {
        if (exchange.getAttachment(FIRST_ACCESS) == null) {
            exchange.putAttachment(FIRST_ACCESS, true);
            return true;
        } else {
            return false;
        }
    }

    boolean hasBeenAccessed(@NonNull HttpServerExchange exchange) {
        return (exchange.getAttachment(FIRST_ACCESS) != null || exchange.getAttachment(NEW_SESSION) != null);
    }

}
