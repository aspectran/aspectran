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
package com.aspectran.core.adapter;

import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionAgent;

import java.util.Enumeration;

/**
 * The Class BasicSessionAdapter.
 * 
 * @since 2.3.0
 */
public class BasicSessionAdapter extends AbstractSessionAdapter {

    /**
     * Instantiates a new BasicSessionAdapter.
     *
     * @param agent the session agent
     */
    public BasicSessionAdapter(SessionAgent agent) {
        super(agent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Session getAdaptee() {
        return ((SessionAgent)adaptee).getSession(true);
    }

    @Override
    public String getId() {
        return ((SessionAgent)adaptee).getId();
    }

    @Override
    public boolean isNew() {
        return ((SessionAgent)adaptee).isNew();
    }

    @Override
    public long getCreationTime() {
        return ((SessionAgent)adaptee).getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return ((SessionAgent)adaptee).getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        return ((SessionAgent)adaptee).getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int secs) {
        ((SessionAgent)adaptee).setMaxInactiveInterval(secs);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return ((SessionAgent)adaptee).getAttributeNames();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return ((SessionAgent)adaptee).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        ((SessionAgent)adaptee).setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        ((SessionAgent)adaptee).removeAttribute(name);
    }

    @Override
    public void invalidate() {
        ((SessionAgent)adaptee).invalidate();
    }

    @Override
    public SessionScope getSessionScope() {
        return ((SessionAgent)adaptee).getSessionScope();
    }

    public SessionAgent getSessionAgent() {
        return (SessionAgent)adaptee;
    }

}
