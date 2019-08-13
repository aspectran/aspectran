package com.aspectran.undertow.server.session;

import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionListener;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2019-08-11</p>
 */
public class SessionListenerWrapper implements SessionListener {

    private final io.undertow.server.session.SessionListener listener;

    private final TowSessionManager towSessionManager;

    public SessionListenerWrapper(io.undertow.server.session.SessionListener listener, TowSessionManager towSessionManager) {
        this.listener = listener;
        this.towSessionManager = towSessionManager;
    }

    @Override
    public void sessionCreated(Session session) {
        HttpServerExchange exchange = towSessionManager.getCurrentExchange();
        if (exchange == null) {
            throw new IllegalStateException("No HttpServerExchange is currently active");
        }
        listener.sessionCreated(newTowSession(session), exchange);
    }

    @Override
    public void sessionDestroyed(Session session) {
        HttpServerExchange exchange = towSessionManager.getCurrentExchange();
        io.undertow.server.session.SessionListener.SessionDestroyedReason reason = null;
        switch (session.getDestroyedReason()) {
            case INVALIDATED:
                reason = io.undertow.server.session.SessionListener.SessionDestroyedReason.INVALIDATED;
                break;
            case TIMEOUT:
                reason = io.undertow.server.session.SessionListener.SessionDestroyedReason.TIMEOUT;
                break;
            case UNDEPLOY:
                reason = io.undertow.server.session.SessionListener.SessionDestroyedReason.UNDEPLOY;
                break;
        }
        listener.sessionDestroyed(newTowSession(session), exchange, reason);
    }

    @Override
    public void attributeAdded(Session session, String name, Object value) {
        listener.attributeAdded(newTowSession(session), name, value);
    }

    @Override
    public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
        listener.attributeUpdated(newTowSession(session), name, newValue, oldValue);
    }

    @Override
    public void attributeRemoved(Session session, String name, Object oldValue) {
        listener.attributeRemoved(newTowSession(session), name, oldValue);
    }

    @Override
    public void sessionIdChanged(Session session, String oldSessionId) {
        listener.sessionIdChanged(newTowSession(session), oldSessionId);
    }

    private SessionWrapper newTowSession(com.aspectran.core.component.session.Session session) {
        return towSessionManager.newSessionWrapper(session);
    }

}
