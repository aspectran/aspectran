package com.aspectran.undertow.server.session;

import com.aspectran.core.component.session.Session;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.AttachmentKey;

import java.util.Set;

/**
 * Class that bridges between Aspectran native session and Undertow ones.
 *
 * <p>Created: 2019-08-11</p>
 */
public class TowSessionBridge implements io.undertow.server.session.Session {

    private final AttachmentKey<Boolean> FIRST_REQUEST_ACCESSED = AttachmentKey.create(Boolean.class);

    private final Session session;

    private final TowSessionManager sessionManager;

    public TowSessionBridge(Session session, TowSessionManager sessionManager) {
        this.session = session;
        this.sessionManager = sessionManager;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    void requestStarted(HttpServerExchange serverExchange) {
        Boolean existing = serverExchange.getAttachment(FIRST_REQUEST_ACCESSED);
        if (existing == null) {
            serverExchange.putAttachment(FIRST_REQUEST_ACCESSED, true);
            session.access();
        }
    }

    @Override
    public void requestDone(HttpServerExchange serverExchange) {
        session.complete();
    }

    @Override
    public long getCreationTime() {
        return session.getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        session.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    @Override
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    @Override
    public Set<String> getAttributeNames() {
        return session.getAttributeNames();
    }

    @Override
    public Object setAttribute(String name, Object value) {
        return session.setAttribute(name, value);
    }

    @Override
    public Object removeAttribute(String name) {
        return session.removeAttribute(name);
    }

    @Override
    public void invalidate(HttpServerExchange exchange) {
        session.invalidate();
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public String changeSessionId(HttpServerExchange exchange, SessionConfig config) {
        String oldId = session.getId();
        String newId = sessionManager.getSessionHandler().createSessionId(hashCode());
        String sessionId = sessionManager.getSessionHandler().renewSessionId(oldId, newId);
        if (sessionId != null) {
            config.setSessionId(exchange, this.getId());
        }
        return sessionId;
    }

}
