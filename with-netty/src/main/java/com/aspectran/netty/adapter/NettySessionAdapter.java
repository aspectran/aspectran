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
package com.aspectran.netty.adapter;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.component.session.ManagedSession;
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.netty.activity.NettyActivity;
import com.aspectran.netty.server.session.NettySessionConfig;
import com.aspectran.netty.service.AbstractNettyService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Enumeration;

/**
 * An adapter that exposes session management in a Netty environment
 * via the {@link com.aspectran.core.adapter.SessionAdapter} interface.
 * <p>Integrates with Aspectran's {@link SessionManager} and manages HTTP session cookies.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettySessionAdapter extends AbstractSessionAdapter {

    private final FullHttpRequest request;

    private final NettyResponseAdapter responseAdapter;

    private final SessionManager sessionManager;

    private final NettySessionConfig sessionConfig;

    private Session session;

    private boolean newSession;

    public NettySessionAdapter(
            ChannelHandlerContext ctx,
            FullHttpRequest request,
            NettyResponseAdapter responseAdapter,
            @NonNull NettyActivity activity) {
        super(request);
        this.request = request;
        this.responseAdapter = responseAdapter;

        if (activity.getNettyService() instanceof AbstractNettyService service) {
            this.sessionManager = service.getSessionManager();
            this.sessionConfig = service.getSessionConfig();
        } else {
            this.sessionManager = null;
            this.sessionConfig = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T) getSession(true);
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
        return (sess != null ? (T) sess.getAttribute(name) : null);
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
    public void invalidate() {
        Session sess = getSession(false);
        if (sess != null) {
            sess.invalidate();
            session = null;
            if (sessionConfig != null && responseAdapter != null) {
                responseAdapter.addHeader(HttpHeaderNames.SET_COOKIE.toString(), sessionConfig.encodeExpiredCookie());
            }
        }
    }

    @Override
    public boolean isValid() {
        Session sess = getSession(false);
        return (sess != null && sess.isValid());
    }

    @Override
    public boolean isNew() {
        Session sess = getSession(false);
        return (sess == null || newSession);
    }

    public void access() {
        Session sess = getSession(false);
        if (sess != null) {
            sess.access();
        }
    }

    public void complete() {
        Session sess = getSession(false);
        if (sess != null) {
            sess.complete();
        }
    }

    public Session getSession(boolean create) {
        if (session != null) {
            if (session.isValid()) {
                return session;
            } else {
                session = null;
            }
        }

        if (sessionManager == null || sessionConfig == null) {
            return null;
        }

        String sessionId = sessionConfig.findSessionId(request);
        if (sessionId != null) {
            ManagedSession existingSession = sessionManager.getSession(sessionId);
            if (existingSession != null && existingSession.isValid()) {
                session = existingSession;
                return session;
            }
        }

        if (create) {
            String newId = sessionManager.createSessionId();
            session = sessionManager.createSession(newId);
            newSession = true;

            if (responseAdapter != null) {
                responseAdapter.addHeader(HttpHeaderNames.SET_COOKIE.toString(), sessionConfig.encodeCookie(newId));
            }
            return session;
        }

        return null;
    }

}
