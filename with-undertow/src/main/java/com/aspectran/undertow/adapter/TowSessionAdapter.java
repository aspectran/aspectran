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
package com.aspectran.undertow.adapter;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import java.util.Collections;
import java.util.Enumeration;

/**
 * An adapter that wraps an {@link HttpServerExchange} to expose session management
 * capabilities via the {@link com.aspectran.core.adapter.SessionAdapter} interface.
 * <p>This class uses the {@link SessionManager} and {@link SessionConfig} attached to the
 * exchange to lazily retrieve and manage the underlying Undertow {@link Session}.
 * </p>
 *
 * @author Juho Jeong
 * @since 2019-07-27
 */
public class TowSessionAdapter extends AbstractSessionAdapter {

    private boolean newSession;

    /**
     * Creates a new {@code TowSessionAdapter}.
     * @param exchange the native {@link HttpServerExchange} from which the session is obtained
     */
    public TowSessionAdapter(HttpServerExchange exchange) {
        super(exchange);
    }

    /**
     * {@inheritDoc}
     * <p>Returns the underlying Undertow {@link Session}, creating it if necessary.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)getSession(true);
    }

    @Override
    public String getId() {
        Session sess = getSession(true);
        return (sess != null ? sess.getId() : null);
    }

    @Override
    public long getCreationTime() {
        Session sess = getSession(true);
        return (sess != null ? sess.getCreationTime() : 0L);
    }

    @Override
    public long getLastAccessedTime() {
        Session sess = getSession(true);
        return (sess != null ? sess.getLastAccessedTime() : 0L);
    }

    @Override
    public int getMaxInactiveInterval() {
        Session sess = getSession(true);
        return (sess != null ? sess.getMaxInactiveInterval() : 0);
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        Session sess = getSession(true);
        if (sess != null) {
            sess.setMaxInactiveInterval(interval);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Session sess = getSession(false);
        return (sess != null ? Collections.enumeration(sess.getAttributeNames()) : Collections.emptyEnumeration());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        Session sess = getSession(false);
        return (sess != null ? (T)sess.getAttribute(name) : null);
    }

    @Override
    public void setAttribute(String name, Object value) {
        Session sess = getSession(true);
        if (sess != null) {
            sess.setAttribute(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Session sess = getSession(false);
        if (sess != null) {
            sess.removeAttribute(name);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Does not create a session if one does not exist.
     */
    @Override
    public void invalidate() {
        Session sess = getSession(false);
        if (sess != null) {
            sess.invalidate(getAdaptee());
        }
    }

    /**
     * {@inheritDoc}
     * <p>A session is considered valid if it exists.
     */
    @Override
    public boolean isValid() {
        return (getSession(false) != null);
    }

    @Override
    public boolean isNew() {
        Session sess = getSession(false);
        return (sess == null || newSession);
    }

    /**
     * Gets the underlying Undertow {@link Session}, creating it if necessary.
     * @param create {@code true} to create a new session if one does not exist
     * @return the session, or {@code null} if {@code create} is false and no session exists
     */
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
