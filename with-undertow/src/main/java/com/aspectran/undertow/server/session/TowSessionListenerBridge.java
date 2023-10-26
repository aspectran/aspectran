/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;

/**
 * Class that bridges between Aspectran native session listener and Undertow ones.
 *
 * <p>Created: 2019-08-11</p>
 */
final class TowSessionListenerBridge implements SessionListener {

    private final io.undertow.server.session.SessionListener listener;

    private final TowSessionManager towSessionManager;

    TowSessionListenerBridge(io.undertow.server.session.SessionListener listener, TowSessionManager towSessionManager) {
        this.listener = listener;
        this.towSessionManager = towSessionManager;
    }

    @Override
    public void sessionCreated(Session session) {
        listener.sessionCreated(wrapSession(session), getCurrentExchange());
    }

    @Override
    public void sessionDestroyed(Session session) {
        io.undertow.server.session.SessionListener.SessionDestroyedReason reason = null;
        if (session != null && session.getDestroyedReason() != null) {
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
        }
        listener.sessionDestroyed(wrapSession(session), getCurrentExchange(), reason);
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

    static HttpServerExchange getCurrentExchange() {
        ServletRequestContext current = ServletRequestContext.current();
        return current != null ? current.getExchange() : null;
    }

}
