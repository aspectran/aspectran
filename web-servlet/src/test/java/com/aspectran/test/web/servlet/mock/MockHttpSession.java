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
package com.aspectran.test.web.servlet.mock;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock implementation of {@link HttpSession}.
 *
 * <p>Created: 2026. 11. 25.</p>
 */
public class MockHttpSession implements HttpSession {

    private final String id;
    private final long creationTime;
    private final Map<String, Object> attributes = new HashMap<>();
    private ServletContext servletContext;
    private int maxInactiveInterval;
    private boolean valid = true;
    private boolean isNew = true;

    public MockHttpSession() {
        this(UUID.randomUUID().toString(), null);
    }

    public MockHttpSession(ServletContext servletContext) {
        this(UUID.randomUUID().toString(), servletContext);
    }

    public MockHttpSession(String id, ServletContext servletContext) {
        this.id = id;
        this.servletContext = servletContext;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public long getCreationTime() {
        checkValid();
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        checkValid();
        return creationTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value != null) {
            attributes.put(name, value);
        } else {
            removeAttribute(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        attributes.remove(name);
    }

    @Override
    public void invalidate() {
        checkValid();
        valid = false;
        attributes.clear();
    }

    @Override
    public boolean isNew() {
        checkValid();
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    private void checkValid() {
        if (!valid) {
            throw new IllegalStateException("Session is already invalidated");
        }
    }

}
