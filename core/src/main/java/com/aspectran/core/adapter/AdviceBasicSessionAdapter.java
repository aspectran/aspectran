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

import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.component.session.Session;

import java.util.Set;

/**
 * Adapt {@link Session} for AOP Advice to Core {@link SessionAdapter}.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public class AdviceBasicSessionAdapter extends AbstractSessionAdapter {

    /**
     * Instantiates a new AdviceHttpSessionAdapter.
     *
     * @param session the HTTP session
     */
    public AdviceBasicSessionAdapter(Session session) {
        super(session);
    }

    @Override
    public SessionScope getSessionScope() {
        throw new UnsupportedOperationException("Default session scope can not be created during after advice");
    }

    @Override
    public String getId() {
        return ((Session)getAdaptee()).getId();
    }

    @Override
    public boolean isNew() {
        return ((Session)getAdaptee()).isNew();
    }

    @Override
    public long getCreationTime() {
        return ((Session)getAdaptee()).getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return ((Session)getAdaptee()).getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        return ((Session)getAdaptee()).getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int interval) {
        ((Session)getAdaptee()).setMaxInactiveInterval(interval);
    }

    @Override
    public Set<String> getAttributeNames() {
        return ((Session)getAdaptee()).getAttributeNames();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)((Session)getAdaptee()).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        ((Session)getAdaptee()).setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        ((Session)getAdaptee()).removeAttribute(name);
    }

    @Override
    public void invalidate() {
        ((Session)getAdaptee()).invalidate();
    }

}
