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
import com.aspectran.undertow.server.session.TowSession;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

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

    private final HttpServerExchange exchange;

    private final SessionManager sessionManager;

    private final SessionConfig sessionConfig;

    private boolean newSession;

    /**
     * Creates a new {@code TowSessionAdapter}.
     * @param exchange the native {@link HttpServerExchange} from which the session is obtained
     */
    public TowSessionAdapter(@NonNull HttpServerExchange exchange) {
        super(exchange);
        this.exchange = exchange;
        this.sessionManager = exchange.getAttachment(SessionManager.ATTACHMENT_KEY);
        this.sessionConfig = exchange.getAttachment(SessionConfig.ATTACHMENT_KEY);
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
    public Set<String> getAttributeNameSet() {
        Session sess = getSession(false);
        return (sess != null ? sess.getAttributeNames() : Collections.emptySet());
    }

    @Override
    public boolean containsAttribute(String name) {
        return (getAttribute(name) != null);
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

    @Override
    public void clear() {
        Session sess = getSession(false);
        if (sess != null) {
            for (String name : sess.getAttributeNames()) {
                sess.removeAttribute(name);
            }
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
            sess.invalidate(exchange);
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

    @Override
    public long getRemainingInactiveInterval() {
        Session sess = getSession(false);
        if (sess instanceof TowSession towSession) {
            return towSession.getRemainingInactiveInterval();
        }
        return super.getRemainingInactiveInterval();
    }

    @Override
    public String changeSessionId() {
        Session sess = getSession(false);
        if (sess != null && sessionConfig != null) {
            return sess.changeSessionId(exchange, sessionConfig);
        }
        return null;
    }

    /**
     * Gets the underlying Undertow {@link Session}, creating it if necessary.
     * @param create {@code true} to create a new session if one does not exist
     * @return the session, or {@code null} if {@code create} is false and no session exists
     */
    public Session getSession(boolean create) {
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
