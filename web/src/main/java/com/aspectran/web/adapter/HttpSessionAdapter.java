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
package com.aspectran.web.adapter;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.scope.SessionScope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;

/**
 * Adapt {@link HttpServletRequest} to Core {@link SessionAdapter}.
 *
 * @since 2011. 3. 13.
 */
public class HttpSessionAdapter extends AbstractSessionAdapter {

    /**
     * Instantiates a new HttpSessionAdapter.
     * @param request the HTTP request
     */
    public HttpSessionAdapter(HttpServletRequest request) {
        super(request);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)getSession(true);
    }

    @Override
    public SessionScope newSessionScope() {
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

    @Override
    public Enumeration<String> getAttributeNames() {
        HttpSession session = getSession(false);
        if (session != null) {
            return session.getAttributeNames();
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        HttpSession session = getSession(false);
        if (session != null) {
            return (T)session.getAttribute(name);
        } else {
            return null;
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            HttpSession session = getSession(true);
            session.setAttribute(name, value);
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

    @Override
    public void invalidate() {
        HttpSession session = getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public boolean isNew() {
        HttpSession session = getSession(false);
        if (session == null) {
            return true;
        }
        return session.isNew();
    }

    public HttpSession getSession(boolean create) {
        return ((HttpServletRequest)super.getAdaptee()).getSession(create);
    }

}
