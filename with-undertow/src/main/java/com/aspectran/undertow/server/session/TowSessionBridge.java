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
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.AttachmentKey;

import java.util.Set;

/**
 * Class that bridges between Aspectran native session and Undertow one.
 *
 * <p>Created: 2019-08-11</p>
 */
final class TowSessionBridge implements io.undertow.server.session.Session {

    private final AttachmentKey<Boolean> FIRST_REQUEST_ACCESSED = AttachmentKey.create(Boolean.class);

    private final Session session;

    private final TowSessionManager sessionManager;

    TowSessionBridge(Session session, TowSessionManager sessionManager) {
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
            config.setSessionId(exchange, sessionId);
        }
        return sessionId;
    }

}
