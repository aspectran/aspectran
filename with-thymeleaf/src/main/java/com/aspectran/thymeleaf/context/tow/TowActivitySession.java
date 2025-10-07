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
package com.aspectran.thymeleaf.context.tow;

import com.aspectran.core.adapter.SessionAdapter;
import org.thymeleaf.web.IWebSession;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Thymeleaf {@link IWebSession} implementation for Aspectran's non-servlet
 * web environment.
 *
 * <p>This class acts as a bridge between Aspectran's abstract {@link SessionAdapter}
 * and Thymeleaf's {@code IWebSession} interface. It allows Thymeleaf to interact
 * with session-scoped attributes in a consistent manner, regardless of the
 * underlying session management implementation (e.g., Undertow's native sessions).</p>
 *
 * <p>Created: 2025-10-07</p>
 */
public class TowActivitySession implements IWebSession {

    private final SessionAdapter sessionAdapter;

    TowActivitySession(SessionAdapter sessionAdapter) {
        this.sessionAdapter = sessionAdapter;
    }

    @Override
    public boolean exists() {
        return sessionAdapter.isValid();
    }

    @Override
    public boolean containsAttribute(String name) {
        return (sessionAdapter.getAttribute(name) != null);
    }

    @Override
    public int getAttributeCount() {
        Enumeration<String> enumer = sessionAdapter.getAttributeNames();
        if (enumer == null) {
            return 0;
        }
        int count = 0;
        while (enumer.hasMoreElements()) {
            enumer.nextElement();
            count++;
        }
        return count;
    }

    @Override
    public Set<String> getAllAttributeNames() {
        Enumeration<String> enumer = sessionAdapter.getAttributeNames();
        if (enumer == null) {
            return Collections.emptySet();
        }
        Set<String> attributeNames = new LinkedHashSet<>(10);
        while (enumer.hasMoreElements()) {
            attributeNames.add(enumer.nextElement());
        }
        return Collections.unmodifiableSet(attributeNames);
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        Enumeration<String> enumer = sessionAdapter.getAttributeNames();
        if (enumer == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> attributeMap = new LinkedHashMap<>(10);
        String attributeName;
        while (enumer.hasMoreElements()) {
            attributeName = enumer.nextElement();
            attributeMap.put(attributeName, getAttributeValue(attributeName));
        }
        return Collections.unmodifiableMap(attributeMap);
    }

    @Override
    public Object getAttributeValue(String name) {
        return sessionAdapter.getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        sessionAdapter.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        sessionAdapter.removeAttribute(name);
    }

}
