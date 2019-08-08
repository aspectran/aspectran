/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.component.session;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.component.bean.scope.SessionScope;

import java.util.Collections;
import java.util.Enumeration;

/**
 * Session processing delegate by request.
 *
 * <p>Created: 2017. 9. 10.</p>
 */
public class SessionAgent {

    private static final String SESSION_SCOPE_ATTRIBUTE_NAME = SessionAgent.class.getName() + ".SESSION_SCOPE";

    private final SessionHandler sessionHandler;

    private final String id;

    private Session session;

    private SessionScope sessionScope;

    private boolean requestStarted;

    public SessionAgent(SessionHandler sessionHandler, String id) {
        this.sessionHandler = sessionHandler;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isNew() {
        return getSession(true).isNew();
    }

    public long getCreationTime() {
        return getSession(true).getCreationTime();
    }

    public long getLastAccessedTime() {
        return getSession(true).getLastAccessedTime();
    }

    public int getMaxInactiveInterval() {
        return getSession(true).getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int secs) {
        getSession(true).setMaxInactiveInterval(secs);
    }

    public Enumeration<String> getAttributeNames() {
        Session session = getSession(false);
        if (session == null) {
            return null;
        }
        return Collections.enumeration(session.getAttributeNames());
    }

    public <T> T getAttribute(String name) {
        Session session = getSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        getSession(true).setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        Session session = getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    public SessionScope getSessionScope() {
        if (this.sessionScope == null) {
            this.sessionScope = getAttribute(SESSION_SCOPE_ATTRIBUTE_NAME);
            if (this.sessionScope == null) {
                newSessionScope();
            }
        }
        return this.sessionScope;
    }

    public Session getSession(boolean create) {
        if (!requestStarted) {
            requestStarted = true;
            if (session != null) {
                if (!sessionHandler.access(session)) {
                    return null;
                }
            }
        }
        if (session != null) {
            if (session.isValid()) {
                return session;
            }
            session = null;
        }
        if (!create) {
            return null;
        }
        session = sessionHandler.createSession(id);
        return session;
    }

    public void invalidate() {
        Session session = getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Called by the {@link CoreActivity}
     * when a session is last accessed by a request.
     */
    public void complete() {
        Session session = getSession(false);
        if (session != null) {
            sessionHandler.complete(session);
        }
    }

    /**
     * Creates a new HTTP session scope.
     */
    private void newSessionScope() {
        SessionScopeAdvisor advisor = SessionScopeAdvisor.create(context);
        this.sessionScope = new DefaultSessionScope(advisor);
        setAttribute(SESSION_SCOPE_ATTRIBUTE_NAME, this.sessionScope);
    }

}
