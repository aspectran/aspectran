package com.aspectran.undertow.server.session;

import com.aspectran.core.component.session.BasicSession;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionListener;

/**
 * <p>Created: 2019-08-11</p>
 */
public class SessionListenerWrapper implements com.aspectran.core.component.session.SessionListener {

    private final SessionListener listener;

    private final TowSessionManager towSessionManager;

    public SessionListenerWrapper(SessionListener listener, TowSessionManager towSessionManager) {
        this.listener = listener;
        this.towSessionManager = towSessionManager;
    }

    @Override
    public void sessionCreated(BasicSession session) {
        HttpServerExchange exchange = towSessionManager.getCurrentExchange();
        if (exchange == null) {
            throw new IllegalStateException("No HttpServerExchange is currently active");
        }
        listener.sessionCreated(newTowSession(session), exchange);
    }

    @Override
    public void sessionDestroyed(BasicSession session) {
        HttpServerExchange exchange = towSessionManager.getCurrentExchange();
        listener.sessionDestroyed(newTowSession(session), exchange, SessionListener.SessionDestroyedReason.INVALIDATED);
    }

    @Override
    public void attributeAdded(BasicSession session, String name, Object value) {
        listener.attributeAdded(newTowSession(session), name, value);
    }

    @Override
    public void attributeUpdated(BasicSession session, String name, Object newValue, Object oldValue) {
        listener.attributeUpdated(newTowSession(session), name, newValue, oldValue);
    }

    @Override
    public void attributeRemoved(BasicSession session, String name, Object oldValue) {
        listener.attributeRemoved(newTowSession(session), name, oldValue);
    }

    @Override
    public void sessionIdChanged(BasicSession session, String oldSessionId) {
        listener.sessionIdChanged(newTowSession(session), oldSessionId);
    }

    private SessionWrapper newTowSession(BasicSession session) {
        return towSessionManager.newSessionWrapper(session);
    }

}
