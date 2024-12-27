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

import com.aspectran.core.component.session.ManagedSession;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import io.undertow.server.HttpServerExchange;
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
public class TowSessionManager extends AbstractSessionManager implements SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(TowSessionManager.class);

    protected final AttachmentKey<TowSession> NEW_SESSION = AttachmentKey.create(TowSession.class);

    protected final AttachmentKey<Boolean> FIRST_ACCESS = AttachmentKey.create(Boolean.class);

    private final Map<SessionListener, TowSessionListener> sessionListenerMappings = new ConcurrentHashMap<>();

    @Override
    public String getDeploymentName() {
        return getSessionManager().getWorkerName();
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
            sessionId = getSessionManager().createSessionId(hashCode());
        }

        sessionConfig.setSessionId(exchange, sessionId);

        ManagedSession session = getSessionManager().createSession(sessionId);
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
        ManagedSession session = getSessionManager().getSession(sessionId);
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
        getSessionManager().addSessionListener(towSessionListener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        TowSessionListener towSessionListener = sessionListenerMappings.remove(listener);
        if (towSessionListener != null) {
            getSessionManager().removeSessionListener(towSessionListener);
        }
    }

    @Override
    public void setDefaultSessionTimeout(int timeout) {
        getSessionManager().setDefaultMaxIdleSecs(timeout);
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
                return getSessionManager().getStatistics().getNumberOfCreated();
            }

            @Override
            public long getMaxActiveSessions() {
                return getSessionManager().getSessionCache().getMaxActiveSessions();
            }

            @Override
            public long getHighestSessionCount() {
                return getSessionManager().getStatistics().getHighestNumberOfActives();
            }

            @Override
            public long getActiveSessionCount() {
                return getSessionManager().getStatistics().getNumberOfActives();
            }

            @Override
            public long getExpiredSessionCount() {
                return getSessionManager().getStatistics().getNumberOfExpired();
            }

            @Override
            public long getRejectedSessions() {
                return getSessionManager().getStatistics().getNumberOfRejected();
            }

            @Override
            public long getMaxSessionAliveTime() {
                return getSessionManager().getStatistics().getMaxSessionAliveTime();
            }

            @Override
            public long getAverageSessionAliveTime() {
                return getSessionManager().getStatistics().getAverageSessionAliveTime();
            }

            @Override
            public long getStartTime() {
                return getSessionManager().getStatistics().getStartTime();
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
