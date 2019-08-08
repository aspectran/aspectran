package com.aspectran.undertow.server.servlet.session;

import com.aspectran.core.component.session.SessionHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.AttachmentKey;

import java.util.Set;

public class ServletSession implements Session {

    private final AttachmentKey<Boolean> FIRST_REQUEST_ACCESSED = AttachmentKey.create(Boolean.class);

    private final com.aspectran.core.component.session.Session session;

    private final SessionHandler sessionHandler;

    public ServletSession(com.aspectran.core.component.session.Session session, SessionHandler sessionHandler) {
        this.session = session;
        this.sessionHandler = sessionHandler;
    }

    public com.aspectran.core.component.session.Session getSession() {
        return session;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    void requestStarted(HttpServerExchange serverExchange) {
        Boolean existing = serverExchange.getAttachment(FIRST_REQUEST_ACCESSED);
        if (existing == null && session.isValid()) {
            sessionHandler.access(session);
            serverExchange.putAttachment(FIRST_REQUEST_ACCESSED, Boolean.TRUE);
        }
    }

    @Override
    public void requestDone(HttpServerExchange serverExchange) {
        sessionHandler.complete(session);
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
        return null;
    }

    @Override
    public String changeSessionId(HttpServerExchange exchange, SessionConfig config) {
        return null;
    }

}
