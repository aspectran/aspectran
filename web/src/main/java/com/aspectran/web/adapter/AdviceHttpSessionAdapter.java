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
package com.aspectran.web.adapter;

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.scope.SessionScope;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * Adapt {@link HttpSession} for AOP Advice to Core {@link SessionAdapter}.
 *
 * <p>Created: 2017. 6. 24.</p>
 */
public class AdviceHttpSessionAdapter extends AbstractSessionAdapter {

    /**
     * Instantiates a new AdviceHttpSessionAdapter.
     *
     * @param session the HTTP session
     */
    public AdviceHttpSessionAdapter(HttpSession session) {
        super(session);
    }

    @Override
    public SessionScope getSessionScope() {
        throw new UnsupportedOperationException("HTTP session scope can not be created during after advice");
    }

    @Override
    public String getId() {
        return ((HttpSession)getAdaptee()).getId();
    }

    @Override
    public boolean isNew() {
        return ((HttpSession)getAdaptee()).isNew();
    }

    @Override
    public long getCreationTime() {
        return ((HttpSession)getAdaptee()).getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return ((HttpSession)getAdaptee()).getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        return ((HttpSession)getAdaptee()).getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int interval) {
        ((HttpSession)getAdaptee()).setMaxInactiveInterval(interval);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return ((HttpSession)getAdaptee()).getAttributeNames();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)((HttpSession)getAdaptee()).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        ((HttpSession)getAdaptee()).setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        ((HttpSession)getAdaptee()).removeAttribute(name);
    }

    @Override
    public void invalidate() {
        ((HttpSession)getAdaptee()).invalidate();
    }

}
