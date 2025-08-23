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
package com.aspectran.web.adapter;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.component.bean.scope.SessionScope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;

/**
 * An adapter that wraps an {@link HttpServletRequest} to expose session management
 * capabilities via the {@link com.aspectran.core.adapter.SessionAdapter} interface.
 * <p>This class lazily retrieves the underlying {@link HttpSession} from the request
 * as needed, providing a bridge between the Servlet API and the Aspectran core.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public class HttpSessionAdapter extends AbstractSessionAdapter {

    /**
     * Creates a new {@code HttpSessionAdapter}.
     * @param request the native {@link HttpServletRequest} from which the session is obtained
     */
    public HttpSessionAdapter(HttpServletRequest request) {
        super(request);
    }

    /**
     * {@inheritDoc}
     * <p>Returns the underlying {@link HttpSession}, creating it if necessary.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)getSession(true);
    }

    /**
     * {@inheritDoc}
     * <p>Returns a new {@link HttpSessionScope} instance.
     */
    @Override
    public SessionScope createSessionScope() {
        return new HttpSessionScope();
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

    /**
     * {@inheritDoc}
     * <p>Does not create a session if one does not exist.
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        HttpSession session = getSession(false);
        return (session != null ? session.getAttributeNames() : null);
    }

    /**
     * {@inheritDoc}
     * <p>Does not create a session if one does not exist.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        HttpSession session = getSession(false);
        return (session != null ? (T)session.getAttribute(name) : null);
    }

    /**
     * {@inheritDoc}
     * <p>Creates a session if one does not exist and the value is not null.
     */
    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            getSession(true).setAttribute(name, value);
        } else {
            HttpSession session = getSession(false);
            if (session != null) {
                session.removeAttribute(name);
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        HttpSession session = getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    /**
     * {@inheritDoc}
     * <p>Does not create a session if one does not exist.
     */
    @Override
    public void invalidate() {
        HttpSession session = getSession(false);
        if (session != null) {
            session.invalidate();
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
        HttpSession session = getSession(false);
        return (session == null || session.isNew());
    }

    /**
     * Gets the underlying {@link HttpSession}, creating it if necessary.
     * @param create {@code true} to create a new session if one does not exist
     * @return the session, or {@code null} if {@code create} is false and no session exists
     */
    public HttpSession getSession(boolean create) {
        return ((HttpServletRequest)super.getAdaptee()).getSession(create);
    }

}
