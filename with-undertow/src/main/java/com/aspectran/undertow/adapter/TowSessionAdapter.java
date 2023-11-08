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
package com.aspectran.undertow.adapter;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.scope.SessionScope;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import java.util.Collections;
import java.util.Enumeration;

/**
 * Adapt {@link HttpServerExchange} to Core {@link SessionAdapter}.
 *
 * @since 2011. 3. 13.
 */
public class TowSessionAdapter extends AbstractSessionAdapter {

    private boolean newSession;

    public TowSessionAdapter(HttpServerExchange exchange) {
        super(exchange);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)getSession(true);
    }

    @Override
    public SessionScope newSessionScope() {
        return new SessionScope();
    }

    @Override
    public String getId() {
        return getSession(true).getId();
    }

    @Override
    public long getCreationTime() {
        return getSession(true).getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return getSession(true).getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        return getSession(true).getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int interval) {
        getSession(true).setMaxInactiveInterval(interval);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Session session = getSession(false);
        if (session != null) {
            return Collections.enumeration(session.getAttributeNames());
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        Session session = getSession(false);
        if (session != null) {
            return (T)session.getAttribute(name);
        } else {
            return null;
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            Session session = getSession(true);
            session.setAttribute(name, value);
        } else {
            Session session = getSession(false);
            if (session != null) {
                session.removeAttribute(name);
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        Session session = getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    @Override
    public void invalidate() {
        Session session = getSession(false);
        if (session != null) {
            session.invalidate(getAdaptee());
        }
    }

    @Override
    public boolean isNew() {
        Session session = getSession(false);
        return (session == null || newSession);
    }

    public Session getSession(boolean create) {
        HttpServerExchange exchange = super.getAdaptee();
        SessionManager sessionManager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
        SessionConfig sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
        if (sessionConfig == null || sessionManager == null) {
            return null;
        }

        Session session = sessionManager.getSession(exchange, sessionConfig);
        if (session == null && create) {
            newSession = true;
            return sessionManager.createSession(exchange, sessionConfig);
        }
        return session;
    }

}
