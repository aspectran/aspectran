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

import com.aspectran.core.component.session.NonPersistentValue;
import com.aspectran.core.component.session.Session;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import java.util.Set;

/**
 * Class that bridges between Aspectran native session and Undertow one.
 *
 * <p>Created: 2019-08-11</p>
 */
public final class TowSession implements io.undertow.server.session.Session {

    private final TowSessionManager sessionManager;

    private final Session session;

    TowSession(TowSessionManager sessionManager, Session session) {
        this.sessionManager = sessionManager;
        this.session = session;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    void requestStarted(@NonNull HttpServerExchange exchange) {
        if (sessionManager.checkFirstAccess(exchange)) {
            session.access();
        }
    }

    @Override
    public void requestDone(@NonNull HttpServerExchange exchange) {
        if (sessionManager.hasBeenAccessed(exchange)) {
            session.complete();
        }
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
        Assert.notNull(name, "name must not be null");
        if (value != null && isNonPersistent(name)) {
            return session.setAttribute(name, NonPersistentValue.wrap(value));
        } else {
            return session.setAttribute(name, value);
        }
    }

    @Override
    public Object removeAttribute(String name) {
        return session.removeAttribute(name);
    }

    @Override
    public void invalidate(HttpServerExchange exchange) {
        session.invalidate();
        if (exchange != null) {
            sessionManager.clearSession(exchange, session.getId());
        }
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    @Nullable
    public String changeSessionId(HttpServerExchange exchange, SessionConfig config) {
        synchronized (session) {
            if (!session.isValid()) {
                return null;
            }
            String oldId = session.getId();
            String newId = sessionManager.createSessionId(hashCode());
            String newIdToUse = sessionManager.renewSessionId(oldId, newId);
            if (newIdToUse != null) {
                config.setSessionId(exchange, newIdToUse);
            }
            return newIdToUse;
        }
    }

    private boolean isNonPersistent(@NonNull String name) {
        return name.startsWith("io.undertow.");
    }

}
