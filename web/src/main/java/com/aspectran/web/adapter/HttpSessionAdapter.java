/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.Locker.Lock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * The Class HttpSessionAdapter.
 * 
 * @since 2011. 3. 13.
 */
public class HttpSessionAdapter extends AbstractSessionAdapter {

    private static final String SESSION_SCOPE_ATTRIBUTE_NAME = HttpSessionScope.class.getName() + ".SESSION_SCOPE";

    private Locker locker = new Locker();

    private volatile SessionScope sessionScope;

    private ActivityContext context;

    /**
     * Instantiates a new HttpSessionAdapter.
     *
     * @param request the HTTP request
     * @param context the current activity context
     */
    public HttpSessionAdapter(HttpServletRequest request, ActivityContext context) {
        super(request);
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpSession getAdaptee() {
        return getSession(true);
    }

    @Override
    public SessionScope getSessionScope() {
        if (this.sessionScope == null) {
            try (Lock ignored = locker.lockIfNotHeld()) {
                this.sessionScope = getAttribute(SESSION_SCOPE_ATTRIBUTE_NAME);
                if (this.sessionScope == null) {
                    newHttpSessionScope();
                }
            }
        }
        return this.sessionScope;
    }

    @Override
    public String getId() {
        HttpSession session = getSession(true);
        return session.getId();
    }

    @Override
    public boolean isNew() {
        HttpSession session = getSession(true);
        return session.isNew();
    }

    @Override
    public long getCreationTime() {
        HttpSession session = getSession(true);
        return session.getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        HttpSession session = getSession(true);
        return session.getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        HttpSession session = getSession(true);
        return session.getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int interval) {
        HttpSession session = getSession(true);
        session.setMaxInactiveInterval(interval);
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

    /**
     * Creates a new HTTP session scope.
     */
    private void newHttpSessionScope() {
        SessionScopeAdvisor advisor = SessionScopeAdvisor.create(context);
        this.sessionScope = new HttpSessionScope(advisor);
        setAttribute(SESSION_SCOPE_ATTRIBUTE_NAME, this.sessionScope);
    }

    protected HttpSession getSession(boolean create) {
        if (adaptee == null) {
            throw new IllegalStateException("Session has been expired or not yet initialized");
        }
        return ((HttpServletRequest)adaptee).getSession(create);
    }

}
