/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.session.DefaultSession;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.component.session.SessionStore;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.util.AttachmentKey;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Created: 2019-08-07</p>
 */
public class TowSessionManager implements SessionManager, ApplicationAdapterAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TowSessionManager.class);

    private final AttachmentKey<TowSessionBridge> SESSION_BRIDGE = AttachmentKey.create(TowSessionBridge.class);

    private final Map<SessionListener, TowSessionListenerBridge> sessionListenerMappings = new ConcurrentHashMap<>();

    private final DefaultSessionManager sessionManager = new DefaultSessionManager();

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        sessionManager.setApplicationAdapter(applicationAdapter);
    }

    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) {
        sessionManager.setSessionManagerConfig(sessionManagerConfig);
    }

    public void setSessionManagerConfigWithApon(String apon) {
        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
        try {
            sessionManagerConfig.readFrom(apon);
        } catch (AponParseException e) {
            throw new RuntimeException(e);
        }
        setSessionManagerConfig(sessionManagerConfig);
    }

    public void setSessionStore(SessionStore sessionStore) {
        sessionManager.setSessionStore(sessionStore);
    }

    public SessionHandler getSessionHandler() {
        return sessionManager.getSessionHandler();
    }

    @Override
    public String getDeploymentName() {
        return sessionManager.getWorkerName();
    }

    @Override
    public void start() {
        try {
            if (!sessionManager.isInitialized()) {
                sessionManager.initialize();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing TowSessionManager", e);
        }
    }

    @Override
    public void stop() {
        try {
            if (!sessionManager.isAvailable()) {
                sessionManager.destroy();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error destroying TowSessionManager", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public Session createSession(HttpServerExchange exchange, SessionConfig sessionConfig) {
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
        DefaultSession session = sessionManager.createSession(sessionId);
        TowSessionBridge sessionBridge = createTowSessionBridge(session);
        sessionConfig.setSessionId(exchange, session.getId());
        exchange.putAttachment(SESSION_BRIDGE, sessionBridge);
        return sessionBridge;
    }

    @Override
    public Session getSession(HttpServerExchange exchange, SessionConfig sessionConfig) {
        if (exchange != null) {
            TowSessionBridge bridged = exchange.getAttachment(SESSION_BRIDGE);
            if (bridged != null) {
                return bridged;
            }
        }
        String sessionId;
        try {
            sessionId = sessionConfig.findSessionId(exchange);
        } catch (Exception e) {
            logger.error("Unable to retrieve session due to failure to find session ID", e);
            return null;
        }
        TowSessionBridge sessionBridge = (TowSessionBridge)getSession(sessionId);
        if (sessionBridge != null && exchange != null) {
            exchange.putAttachment(SESSION_BRIDGE, sessionBridge);
            sessionBridge.requestStarted(exchange);
        }
        return sessionBridge;
    }

    @Override
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        DefaultSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            return createTowSessionBridge(session);
        } else {
            return null;
        }
    }

    @Override
    public void registerSessionListener(SessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        TowSessionListenerBridge sessionListenerBridge = new TowSessionListenerBridge(listener, this);
        sessionListenerMappings.put(listener, sessionListenerBridge);
        sessionManager.addSessionListener(sessionListenerBridge);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        TowSessionListenerBridge sessionListenerBridge = sessionListenerMappings.remove(listener);
        if (sessionListenerBridge != null) {
            sessionManager.removeSessionListener(sessionListenerBridge);
        }
    }

    @Override
    public void setDefaultSessionTimeout(int timeout) {
        sessionManager.setDefaultMaxIdleSecs(timeout);
    }

    @Override
    public Set<String> getTransientSessions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getActiveSessions() {
        return getSessionHandler().getActiveSessions();
    }

    @Override
    public Set<String> getAllSessions() {
        return getSessionHandler().getAllSessions();
    }

    @Override
    public SessionManagerStatistics getStatistics() {
        return new SessionManagerStatistics() {
            @Override
            public long getCreatedSessionCount() {
                return sessionManager.getStatistics().getCreatedSessions();
            }

            @Override
            public long getMaxActiveSessions() {
                return sessionManager.getSessionCache().getMaxActiveSessions();
            }

            @Override
            public long getHighestSessionCount() {
                return sessionManager.getStatistics().getHighestActiveSessions();
            }

            @Override
            public long getActiveSessionCount() {
                return sessionManager.getStatistics().getActiveSessions();
            }

            @Override
            public long getExpiredSessionCount() {
                return sessionManager.getStatistics().getExpiredSessions();
            }

            @Override
            public long getRejectedSessions() {
                return sessionManager.getStatistics().getRejectedSessions();
            }

            @Override
            public long getMaxSessionAliveTime() {
                return sessionManager.getStatistics().getSessionTimeMax();
            }

            @Override
            public long getAverageSessionAliveTime() {
                return sessionManager.getStatistics().getSessionTimeMean();
            }

            @Override
            public long getStartTime() {
                return sessionManager.getStatistics().getStartTime();
            }
        };
    }

    TowSessionBridge createTowSessionBridge(com.aspectran.core.component.session.Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        return new TowSessionBridge(session, this);
    }

}
