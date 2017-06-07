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
import com.aspectran.core.component.session.BasicSessionData;
import com.aspectran.core.util.thread.Locker;
import com.aspectran.core.util.thread.Locker.Lock;

/**
 * The Class BasicSessionAdapter.
 * 
 * @since 2.3.0
 */
public class BasicSessionAdapter extends AbstractSessionAdapter {

    private Locker locker = new Locker();

    /**
     * Instantiates a new BasicSessionAdapter.
     */
    public BasicSessionAdapter(BasicSessionData sessionData) {
        super(sessionData);
    }

    @Override
    public String getId() {
        return ((BasicSessionData)adaptee).getId();
    }

    @Override
    public long getCreationTime() {
        return ((BasicSessionData)adaptee).getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        try (Lock lock = locker.lockIfNotHeld()) {
            return ((BasicSessionData)adaptee).getLastAccessedTime();
        }
    }

    public void updateLastAccessedTime() {
        try (Lock lock = locker.lockIfNotHeld()) {
            ((BasicSessionData)adaptee).updateLastAccessedTime();
        }
    }

    @Override
    public int getMaxInactiveInterval() {
        try (Lock lock = locker.lockIfNotHeld()) {
            return ((BasicSessionData)adaptee).getMaxInactiveInterval();
        }
    }

    public void setMaxInactiveInterval(int secs) {
        try (Lock lock = locker.lockIfNotHeld()) {
            ((BasicSessionData)adaptee).setMaxInactiveInterval(secs);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        try (Lock lock = locker.lockIfNotHeld()) {
            return ((BasicSessionData)adaptee).getAttribute(name);
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        try (Lock lock = locker.lockIfNotHeld()) {
            ((BasicSessionData)adaptee).setAttribute(name, value);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        try (Lock lock = locker.lockIfNotHeld()) {
            return ((BasicSessionData)adaptee).getAttributeNames();
        }
    }

    @Override
    public void removeAttribute(String name) {
        try (Lock lock = locker.lockIfNotHeld()) {
            ((BasicSessionData)adaptee).removeAttribute(name);
        }
    }

    @Override
    public void invalidate() {
        try (Lock lock = locker.lockIfNotHeld()) {
            ((BasicSessionData)adaptee).invalidate();
        }
    }

    @Override
    public SessionScope getSessionScope() {
        return ((BasicSessionData)adaptee).getSessionScope();
    }

}
