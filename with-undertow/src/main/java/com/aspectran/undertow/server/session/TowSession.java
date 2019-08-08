package com.aspectran.undertow.server.session;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.AttachmentKey;

import java.util.Set;

public class TowSession implements Session {

    private final AttachmentKey<Long> FIRST_REQUEST_ACCESS = AttachmentKey.create(Long.class);

    private final com.aspectran.core.component.session.Session session;

    private final TowSessionManager sessionManager;

    public TowSession(com.aspectran.core.component.session.Session session, TowSessionManager sessionManager) {
        this.session = session;
        this.sessionManager = sessionManager;
    }

    public com.aspectran.core.component.session.Session getSession() {
        return session;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    void requestStarted(HttpServerExchange serverExchange) {
        Long existing = serverExchange.getAttachment(FIRST_REQUEST_ACCESS);
        if (existing == null && sessionManager.getSessionHandler().access(session)) {
            serverExchange.putAttachment(FIRST_REQUEST_ACCESS, System.currentTimeMillis());
        }
    }

    @Override
    public void requestDone(HttpServerExchange serverExchange) {
        Long existing = serverExchange.getAttachment(FIRST_REQUEST_ACCESS);
        if (existing != null) {
            session.getSessionData().setLastAccessedTime(existing);
        }
        sessionManager.getSessionHandler().complete(session);
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
        final String oldId = session.getId();
        String newId = sessionManager.getSessionHandler().createSessionId(hashCode());
        String sessionId = sessionManager.getSessionHandler().renewSessionId(oldId, newId);
        if (sessionId != null) {
            config.setSessionId(exchange, this.getId());
        }
        return sessionId;
    }

}
