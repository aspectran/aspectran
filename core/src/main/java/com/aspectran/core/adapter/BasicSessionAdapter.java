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
package com.aspectran.core.adapter;

import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.activity.aspect.SessionScopeAdvisorFactory;
import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.component.session.DefaultSessionScope;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.context.ActivityContext;

import java.util.Enumeration;

/**
 * The Class BasicSessionAdapter.
 *
 * @since 2.3.0
 */
public class BasicSessionAdapter extends AbstractSessionAdapter {

    private static final String SESSION_SCOPE_ATTRIBUTE_NAME = BasicSessionAdapter.class.getName() + ".SESSION_SCOPE";

    private final ActivityContext context;

    private volatile SessionScope sessionScope;

    /**
     * Instantiates a new BasicSessionAdapter.
     *
     * @param agent the session agent
     * @param context the current activity context
     */
    public BasicSessionAdapter(SessionAgent agent, ActivityContext context) {
        super(agent);
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)getSessionAgent().getSession(true);
    }

    @Override
    public SessionScope getSessionScope() {
        if (this.sessionScope == null) {
            this.sessionScope = getAttribute(SESSION_SCOPE_ATTRIBUTE_NAME);
            if (this.sessionScope == null) {
                newDefaultSessionScope();
            }
        }
        return this.sessionScope;
    }

    /**
     * Creates a new default session scope.
     */
    private void newDefaultSessionScope() {
        SessionScopeAdvisor advisor = SessionScopeAdvisorFactory.create(context);
        this.sessionScope = new DefaultSessionScope(advisor);
        setAttribute(SESSION_SCOPE_ATTRIBUTE_NAME, this.sessionScope);
    }

    @Override
    public String getId() {
        return getSessionAgent().getId();
    }

    @Override
    public boolean isNew() {
        return getSessionAgent().isNew();
    }

    @Override
    public long getCreationTime() {
        return getSessionAgent().getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return getSessionAgent().getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        return getSessionAgent().getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int secs) {
        getSessionAgent().setMaxInactiveInterval(secs);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return getSessionAgent().getAttributeNames();
    }

    @Override
    public <T> T getAttribute(String name) {
        return getSessionAgent().getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        getSessionAgent().setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        getSessionAgent().removeAttribute(name);
    }

    @Override
    public void invalidate() {
        getSessionAgent().invalidate();
    }

    protected SessionAgent getSessionAgent() {
        return super.getAdaptee();
    }

}
