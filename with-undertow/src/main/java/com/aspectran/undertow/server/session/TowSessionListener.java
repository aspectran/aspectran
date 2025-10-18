/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;

/**
 * An Aspectran {@link SessionListener} implementation that wraps and delegates
 * events to an Undertow {@link io.undertow.server.session.SessionListener}.
 *
 * <p>Created: 2019-08-11</p>
 */
public final class TowSessionListener implements SessionListener {

    private final TowSessionManager towSessionManager;

    private final io.undertow.server.session.SessionListener listener;

    /**
     * Creates a new Undertow session listener that wraps the given Aspectran session listener.
     * @param towSessionManager the session manager
     * @param listener the Undertow session listener
     */
    TowSessionListener(TowSessionManager towSessionManager, io.undertow.server.session.SessionListener listener) {
        this.towSessionManager = towSessionManager;
        this.listener = listener;
    }

    @Override
    public void sessionCreated(Session session) {
        listener.sessionCreated(towSessionManager.wrapSession(session), getCurrentExchange());
    }

    @Override
    public void sessionDestroyed(@NonNull Session session) {
        io.undertow.server.session.SessionListener.SessionDestroyedReason reason = null;
        if (session.getDestroyedReason() != null) {
            reason = switch (session.getDestroyedReason()) {
                case INVALIDATED -> io.undertow.server.session.SessionListener.SessionDestroyedReason.INVALIDATED;
                case TIMEOUT -> io.undertow.server.session.SessionListener.SessionDestroyedReason.TIMEOUT;
                case UNDEPLOY -> io.undertow.server.session.SessionListener.SessionDestroyedReason.UNDEPLOY;
            };
        }
        listener.sessionDestroyed(towSessionManager.wrapSession(session), getCurrentExchange(), reason);
    }

    @Override
    public void attributeAdded(Session session, String name, Object value) {
        listener.attributeAdded(towSessionManager.wrapSession(session), name, value);
    }

    @Override
    public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
        listener.attributeUpdated(towSessionManager.wrapSession(session), name, newValue, oldValue);
    }

    @Override
    public void attributeRemoved(Session session, String name, Object oldValue) {
        listener.attributeRemoved(towSessionManager.wrapSession(session), name, oldValue);
    }

    @Override
    public void sessionIdChanged(Session session, String oldSessionId) {
        listener.sessionIdChanged(towSessionManager.wrapSession(session), oldSessionId);
    }

    /**
     * Returns the current HTTP server exchange.
     * @return the current HTTP server exchange, or {@code null} if not available
     */
    @Nullable
    static HttpServerExchange getCurrentExchange() {
        ServletRequestContext current = ServletRequestContext.current();
        return (current != null ? current.getExchange() : null);
    }

}
