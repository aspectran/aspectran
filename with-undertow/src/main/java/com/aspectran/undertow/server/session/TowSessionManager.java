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
package com.aspectran.undertow.server.session;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.session.DefaultSession;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.component.session.SessionStoreFactory;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.util.AttachmentKey;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Created: 2019-08-07</p>
 */
public class TowSessionManager implements SessionManager, ApplicationAdapterAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TowSessionManager.class);

    private final AttachmentKey<TowSessionBridge> NEW_SESSION = AttachmentKey.create(TowSessionBridge.class);

    private final Map<SessionListener, TowSessionListenerBridge> sessionListenerMappings = new ConcurrentHashMap<>();

    private final DefaultSessionManager sessionManager = new DefaultSessionManager();

    private volatile long startTime;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setSessionManagerConfig(sessionManagerConfig);
    }

    public void setSessionStoreFactory(SessionStoreFactory sessionStoreFactory) {
        sessionManager.setSessionStoreFactory(sessionStoreFactory);
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
            sessionManager.initialize();
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing TowSessionManager", e);
        }
    }

    @Override
    public void stop() {
        try {
            sessionManager.destroy();
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
        TowSessionBridge sessionBridge = newTowSessionBridge(session);
        sessionConfig.setSessionId(exchange, session.getId());
        exchange.putAttachment(NEW_SESSION, sessionBridge);
        return sessionBridge;
    }

    @Override
    public Session getSession(HttpServerExchange exchange, SessionConfig sessionConfig) {
        if (exchange != null) {
            TowSessionBridge newSession = exchange.getAttachment(NEW_SESSION);
            if (newSession != null) {
                return newSession;
            }
        }
        String sessionId;
        try {
            sessionId = sessionConfig.findSessionId(exchange);
        } catch (Exception e) {
            logger.error("Unable to retrieve session due to failure to find session ID", e);
            return null;
        }
        TowSessionBridge sessionWrapper = (TowSessionBridge)getSession(sessionId);
        if (sessionWrapper != null && exchange != null) {
            sessionWrapper.requestStarted(exchange);
        }
        return sessionWrapper;
    }

    @Override
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        DefaultSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            return newTowSessionBridge(session);
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
        return getAllSessions();
    }

    @Override
    public Set<String> getActiveSessions() {
        return getAllSessions();
    }

    @Override
    public Set<String> getAllSessions() {
        return new HashSet<>(sessionManager.getSessionCache().getAllSessions());
    }

    @Override
    public SessionManagerStatistics getStatistics() {
        return new SessionManagerStatistics() {
            @Override
            public long getCreatedSessionCount() {
                return sessionManager.getSessionCache().getCreatedSessionCount();
            }

            @Override
            public long getMaxActiveSessions() {
                return sessionManager.getSessionCache().getMaxSessions();
            }

            @Override
            public long getHighestSessionCount() {
                return sessionManager.getSessionCache().getHighestSessionCount();
            }

            @Override
            public long getActiveSessionCount() {
                return sessionManager.getSessionCache().getActiveSessionCount();
            }

            @Override
            public long getExpiredSessionCount() {
                return sessionManager.getSessionCache().getExpiredSessionCount();
            }

            @Override
            public long getRejectedSessions() {
                return sessionManager.getSessionCache().getRejectedSessionCount();
            }

            @Override
            public long getMaxSessionAliveTime() {
                return sessionManager.getSessionHandler().getSessionTimeMax();
            }

            @Override
            public long getAverageSessionAliveTime() {
                return sessionManager.getSessionHandler().getSessionTimeMean();
            }

            @Override
            public long getStartTime() {
                return startTime;
            }
        };
    }

    TowSessionBridge newTowSessionBridge(com.aspectran.core.component.session.Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        return new TowSessionBridge(session, this);
    }

}
