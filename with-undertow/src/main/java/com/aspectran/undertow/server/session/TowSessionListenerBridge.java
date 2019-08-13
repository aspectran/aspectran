package com.aspectran.undertow.server.session;

import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionListener;

/**
 * Class that bridges between Aspectran native session listeners and Undertow ones.
 *
 * <p>Created: 2019-08-11</p>
 */
public class TowSessionListenerBridge implements SessionListener {

    private final io.undertow.server.session.SessionListener listener;

    private final TowSessionManager towSessionManager;

    public TowSessionListenerBridge(io.undertow.server.session.SessionListener listener, TowSessionManager towSessionManager) {
        this.listener = listener;
        this.towSessionManager = towSessionManager;
    }

    @Override
    public void sessionCreated(Session session) {
        listener.sessionCreated(wrapSession(session), null);
    }

    @Override
    public void sessionDestroyed(Session session) {
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
        listener.sessionDestroyed(wrapSession(session), null, reason);
    }

    @Override
    public void attributeAdded(Session session, String name, Object value) {
        listener.attributeAdded(wrapSession(session), name, value);
    }

    @Override
    public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
        listener.attributeUpdated(wrapSession(session), name, newValue, oldValue);
    }

    @Override
    public void attributeRemoved(Session session, String name, Object oldValue) {
        listener.attributeRemoved(wrapSession(session), name, oldValue);
    }

    @Override
    public void sessionIdChanged(Session session, String oldSessionId) {
        listener.sessionIdChanged(wrapSession(session), oldSessionId);
    }

    private TowSessionBridge wrapSession(com.aspectran.core.component.session.Session session) {
        return towSessionManager.newTowSessionBridge(session);
    }

}
