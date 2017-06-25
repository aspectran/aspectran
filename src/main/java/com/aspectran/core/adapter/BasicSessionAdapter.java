/*
 * Copyright 2008-2017 Juho Jeong
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

import java.util.Enumeration;

import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.component.session.BasicSession;

/**
 * The Class BasicSessionAdapter.
 * 
 * @since 2.3.0
 */
public class BasicSessionAdapter extends AbstractSessionAdapter {

    /**
     * Instantiates a new BasicSessionAdapter.
     *
     * @param session the basic session
     */
    public BasicSessionAdapter(BasicSession session) {
        super(session);
    }

    @Override
    public String getId() {
        return ((BasicSession)adaptee).getId();
    }

    @Override
    public boolean isNew() {
        return ((BasicSession)adaptee).isNew();
    }

    @Override
    public long getCreationTime() {
        return ((BasicSession)adaptee).getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return ((BasicSession)adaptee).getLastAccessedTime();
    }

    @Override
    public int getMaxInactiveInterval() {
        return ((BasicSession)adaptee).getMaxInactiveInterval();
    }

    public void setMaxInactiveInterval(int secs) {
        ((BasicSession)adaptee).setMaxInactiveInterval(secs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return ((BasicSession)adaptee).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        ((BasicSession)adaptee).setAttribute(name, value);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return ((BasicSession)adaptee).getAttributeNames();
    }

    @Override
    public void removeAttribute(String name) {
        ((BasicSession)adaptee).removeAttribute(name);
    }

    @Override
    public void invalidate() {
        ((BasicSession)adaptee).invalidate();
    }

    @Override
    public SessionScope getSessionScope() {
        return ((BasicSession)adaptee).getSessionScope();
    }

    @Override
    public boolean isBasicSession() {
        return true;
    }

}
