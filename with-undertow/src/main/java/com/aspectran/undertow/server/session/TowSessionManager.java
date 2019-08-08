package com.aspectran.undertow.server.session;

import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.util.AttachmentKey;

import java.util.Set;

/**
 * <p>Created: 2019-08-07</p>
 */
public class TowSessionManager implements SessionManager {

    private final AttachmentKey<TowSession> NEW_SESSION = AttachmentKey.create(TowSession.class);

    private final DefaultSessionManager sessionManager;

    public TowSessionManager(String deploymentName) {
        DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
        defaultSessionManager.setWorkerName(deploymentName);
        this.sessionManager = defaultSessionManager;
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
        com.aspectran.core.component.session.Session session = sessionManager.createSession(sessionId);
        TowSession servletSession = new TowSession(session, this);
        sessionConfig.setSessionId(serverExchange, session.getId());
        serverExchange.putAttachment(NEW_SESSION, servletSession);
        return servletSession;
    }

    @Override
    public Session getSession(HttpServerExchange serverExchange, SessionConfig sessionConfig) {
        if (serverExchange != null) {
            TowSession newSession = serverExchange.getAttachment(NEW_SESSION);
            if(newSession != null) {
                return newSession;
            }
        }
        String sessionId = sessionConfig.findSessionId(serverExchange);
        TowSession servletSession = (TowSession)getSession(sessionId);
        if(servletSession != null && serverExchange != null) {
            servletSession.requestStarted(serverExchange);
        }
        return servletSession;
    }

    @Override
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        com.aspectran.core.component.session.Session session = sessionManager.getSession(sessionId);
        if (session != null) {
            return new TowSession(session, this);
        } else {
            return null;
        }
    }

    @Override
    public void registerSessionListener(SessionListener listener) {

    }

    @Override
    public void removeSessionListener(SessionListener listener) {

    }

    @Override
    public void setDefaultSessionTimeout(int timeout) {
        sessionManager.setDefaultMaxIdleSecs(timeout);
    }

    @Override
    public Set<String> getTransientSessions() {
        return null;
    }

    @Override
    public Set<String> getActiveSessions() {
        return null;
    }

    @Override
    public Set<String> getAllSessions() {
        return null;
    }

    @Override
    public SessionManagerStatistics getStatistics() {
        return null;
    }

}
