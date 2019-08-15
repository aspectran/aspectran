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
package com.aspectran.undertow.server.session;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.session.BasicSession;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.context.rule.type.SessionStoreType;
import com.aspectran.core.util.StringUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.util.AttachmentKey;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Created: 2019-08-07</p>
 */
public class TowSessionManager implements SessionManager, ApplicationAdapterAware {

    private final AttachmentKey<TowSessionBridge> NEW_SESSION = AttachmentKey.create(TowSessionBridge.class);

    private final Map<SessionListener, TowSessionListenerBridge> sessionListenerMappings = new ConcurrentHashMap<>();

    private final DefaultSessionManager sessionManager;

    private ApplicationAdapter applicationAdapter;

    private volatile long startTime;

    public TowSessionManager() {
        this(null);
    }

    public TowSessionManager(String deploymentName) {
        DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
        defaultSessionManager.setWorkerName(deploymentName);
        this.sessionManager = defaultSessionManager;
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public SessionHandler getSessionHandler() {
        return sessionManager.getSessionHandler();
    }

    @Override
    public String getDeploymentName() {
        return sessionManager.getWorkerName();
    }

    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) throws IOException {
        if (sessionManagerConfig != null) {
            if (applicationAdapter != null) {
                String storeType = sessionManagerConfig.getStoreType();
                SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
                if (sessionStoreType == SessionStoreType.FILE) {
                    SessionFileStoreConfig fileStoreConfig = sessionManagerConfig.getFileStoreConfig();
                    if (fileStoreConfig != null) {
                        String storeDir = fileStoreConfig.getStoreDir();
                        if (StringUtils.hasText(storeDir)) {
                            String basePath = applicationAdapter.getBasePath();
                            String canonPath = new File(basePath, storeDir).getCanonicalPath();
                            fileStoreConfig.setStoreDir(canonPath);
                        }
                    }
                }
            }
            sessionManager.setSessionManagerConfig(sessionManagerConfig);
        }
    }

    public void setSessionManagerConfigWithApon(String apon) throws IOException {
        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
        try {
            sessionManagerConfig.readFrom(apon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setSessionManagerConfig(sessionManagerConfig);
    }

    @Override
    public void start() {
        try {
            sessionManager.initialize();
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            sessionManager.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Session createSession(HttpServerExchange serverExchange, SessionConfig sessionConfig) {
        String sessionId = sessionConfig.findSessionId(serverExchange);
        if (sessionId == null) {
            sessionId = sessionManager.createSessionId(hashCode());
        }
        BasicSession session = sessionManager.createSession(sessionId);
        TowSessionBridge sessionWrapper = newTowSessionBridge(session);
        sessionConfig.setSessionId(serverExchange, session.getId());
        serverExchange.putAttachment(NEW_SESSION, sessionWrapper);
        return sessionWrapper;
    }

    @Override
    public Session getSession(HttpServerExchange serverExchange, SessionConfig sessionConfig) {
        if (serverExchange != null) {
            TowSessionBridge newSession = serverExchange.getAttachment(NEW_SESSION);
            if (newSession != null) {
                return newSession;
            }
        }
        String sessionId = sessionConfig.findSessionId(serverExchange);
        TowSessionBridge sessionWrapper = (TowSessionBridge)getSession(sessionId);
        if (sessionWrapper != null && serverExchange != null) {
            sessionWrapper.requestStarted(serverExchange);
        }
        return sessionWrapper;
    }

    @Override
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        BasicSession session = sessionManager.getSession(sessionId);
        if (session != null) {
            return newTowSessionBridge(session);
        } else {
            return null;
        }
    }

    @Override
    public void registerSessionListener(SessionListener listener) {
        TowSessionListenerBridge sessionListenerBridge = new TowSessionListenerBridge(listener, this);
        sessionListenerMappings.put(listener, sessionListenerBridge);
        sessionManager.addSessionListener(sessionListenerBridge);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
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
        return new TowSessionBridge(session, this);
    }

}
