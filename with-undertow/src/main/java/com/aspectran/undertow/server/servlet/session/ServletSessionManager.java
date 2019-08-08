package com.aspectran.undertow.server.servlet.session;

import com.aspectran.core.component.session.DefaultSessionManager;
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
public class ServletSessionManager implements SessionManager {

    private final AttachmentKey<ServletSession> NEW_SESSION = AttachmentKey.create(ServletSession.class);

    private final DefaultSessionManager defaultSessionManager;

    public ServletSessionManager(String deploymentName) {
        DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
        defaultSessionManager.setWorkerName(deploymentName);
        this.defaultSessionManager = defaultSessionManager;
    }

    @Override
    public String getDeploymentName() {
        return defaultSessionManager.getWorkerName();
    }

    @Override
    public void start() {
        try {
            defaultSessionManager.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            defaultSessionManager.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Session createSession(HttpServerExchange serverExchange, SessionConfig sessionConfig) {
        String sessionId = sessionConfig.findSessionId(serverExchange);
        if (sessionId == null) {
            sessionId = defaultSessionManager.createSessionId(hashCode());
        }
        com.aspectran.core.component.session.Session session = defaultSessionManager.createSession(sessionId);
        ServletSession servletSession = new ServletSession(session, defaultSessionManager);
        sessionConfig.setSessionId(serverExchange, session.getId());
        serverExchange.putAttachment(NEW_SESSION, servletSession);
        return servletSession;
    }

    @Override
    public Session getSession(HttpServerExchange serverExchange, SessionConfig sessionConfig) {
        if (serverExchange != null) {
            ServletSession newSession = serverExchange.getAttachment(NEW_SESSION);
            if(newSession != null) {
                return newSession;
            }
        }
        String sessionId = sessionConfig.findSessionId(serverExchange);
        ServletSession servletSession = (ServletSession)getSession(sessionId);
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
        com.aspectran.core.component.session.Session session = defaultSessionManager.getSession(sessionId);
        if (session != null) {
            return new ServletSession(session, defaultSessionManager);
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
        defaultSessionManager.setDefaultMaxIdleSecs(timeout);
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
