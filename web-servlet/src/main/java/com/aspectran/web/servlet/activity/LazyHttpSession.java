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
package com.aspectran.web.servlet.activity;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Enumeration;

/**
 * A lazy {@link HttpSession} that delays actual session creation until
 * a state-changing operation (such as setting an attribute) is performed.
 * <p>Read operations against this session check for an existing session
 * without creating a new one, returning empty or default values if none exists.
 * This prevents premature session creation (e.g., during JSP page evaluation
 * with the default {@code session="true"} directive).</p>
 *
 * <p>Created: 2026. 11. 25.</p>
 */
public class LazyHttpSession implements HttpSession {

    /**
     * Prefix for JSTL internal configuration and formatting attributes.
     * JSTL tags (such as {@code <fmt:formatNumber>} or {@code <fmt:setLocale>})
     * attempt to store response character encodings or locale settings in the
     * session under this namespace, which should not trigger premature session creation.
     */
    private static final String JSTL_PREFIX = "jakarta.servlet.jsp.jstl.";

    private final ActivityRequestWrapper wrapper;

    public LazyHttpSession(@NonNull ActivityRequestWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Nullable
    private HttpSession getSession(boolean create) {
        return wrapper.getRealSession(create);
    }

    @Override
    public long getCreationTime() {
        HttpSession session = getSession(false);
        if (session != null) {
            return session.getCreationTime();
        }
        throw new IllegalStateException("No session exists");
    }

    @Override
    public String getId() {
        HttpSession session = getSession(false);
        return (session != null ? session.getId() : null);
    }

    @Override
    public long getLastAccessedTime() {
        HttpSession session = getSession(false);
        if (session != null) {
            return session.getLastAccessedTime();
        }
        throw new IllegalStateException("No session exists");
    }

    @Override
    public ServletContext getServletContext() {
        return wrapper.getServletContext();
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        HttpSession session = getSession(true);
        if (session != null) {
            session.setMaxInactiveInterval(interval);
        }
    }

    @Override
    public int getMaxInactiveInterval() {
        HttpSession session = getSession(false);
        return (session != null ? session.getMaxInactiveInterval() : 0);
    }

    @Override
    public Object getAttribute(String name) {
        HttpSession session = getSession(false);
        return (session != null ? session.getAttribute(name) : null);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        HttpSession session = getSession(false);
        return (session != null ? session.getAttributeNames() : Collections.emptyEnumeration());
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            // Ignore JSTL internal attributes (e.g., request charset caching by fmt tags)
            // if no real session exists yet, preventing unwanted session creation.
            if (name != null && name.startsWith(JSTL_PREFIX) && getSession(false) == null) {
                return;
            }
            HttpSession session = getSession(true);
            if (session != null) {
                session.setAttribute(name, value);
            }
        } else {
            removeAttribute(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        HttpSession session = getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    @Override
    public void invalidate() {
        HttpSession session = getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public boolean isNew() {
        HttpSession session = getSession(false);
        return (session != null && session.isNew());
    }

}
