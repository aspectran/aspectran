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
package com.aspectran.core.adapter;

import com.aspectran.core.component.session.SessionAgent;

import java.util.Enumeration;

/**
 * Adapt {@link SessionAgent} to Default {@link SessionAdapter}.
 *
 * @since 2.3.0
 */
public class DefaultSessionAdapter extends AbstractSessionAdapter {

    /**
     * Instantiates a new DefaultSessionAdapter.
     * @param agent the session agent
     */
    public DefaultSessionAdapter(SessionAgent agent) {
        super(agent);
    }

    @Override
    public String getId() {
        return getSessionAgent().getId();
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

    @Override
    public boolean isValid() {
        return getSessionAgent().isValid();
    }

    @Override
    public boolean isNew() {
        return getSessionAgent().isNew();
    }

    public SessionAgent getSessionAgent() {
        return super.getAdaptee();
    }

}
