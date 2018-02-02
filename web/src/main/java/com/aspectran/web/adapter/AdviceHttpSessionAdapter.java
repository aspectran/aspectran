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

import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.component.bean.scope.SessionScope;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * The Class AdviceHttpSessionAdapter.
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
        return ((HttpSession)adaptee).getId();
    }

    @Override
    public boolean isNew() {
        return ((HttpSession)adaptee).isNew();
    }

    @Override
    public long getCreationTime() {
        return ((HttpSession)adaptee).getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return ((HttpSession)adaptee).getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        return ((HttpSession)adaptee).getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int interval) {
        ((HttpSession)adaptee).setMaxInactiveInterval(interval);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return ((HttpSession)adaptee).getAttributeNames();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)((HttpSession)adaptee).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        ((HttpSession)adaptee).setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        ((HttpSession)adaptee).removeAttribute(name);
    }

    @Override
    public void invalidate() {
        ((HttpSession)adaptee).invalidate();
    }

}
