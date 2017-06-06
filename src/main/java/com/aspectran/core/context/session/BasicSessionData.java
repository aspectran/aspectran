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
package com.aspectran.core.context.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.aspectran.core.context.bean.scope.SessionScope;
import com.aspectran.core.util.ToStringBuilder;

/**
 * <p>Created: 2017. 6. 6.</p>
 */
public class BasicSessionData {

    private final String id;

    private final SessionScope sessionScope = new SessionScope();

    private final Map<String, Object> attributes = new HashMap<>();

    private final long creationTime = System.currentTimeMillis();

    private long lastAccessedTime = creationTime;

    private long maxInactiveIntervalMS;

    public BasicSessionData() {
        this.id = generateSessionId();
    }

    public BasicSessionData(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Argument 'id' must not be null");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public long updateLastAccessedTime() {
        lastAccessedTime = System.currentTimeMillis();
        return lastAccessedTime;
    }

    public int getMaxInactiveInterval() {
        if (maxInactiveIntervalMS > 0) {
            return (int)(maxInactiveIntervalMS / 1000L);
        } else {
            return -1;
        }
    }

    public void setMaxInactiveInterval(int secs) {
        maxInactiveIntervalMS = secs * 1000L;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        updateLastAccessedTime();
        return (T)attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        updateLastAccessedTime();

        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    public Enumeration<String> getAttributeNames() {
        updateLastAccessedTime();
        return Collections.enumeration(attributes.keySet());
    }

    public void removeAttribute(String name) {
        updateLastAccessedTime();
        attributes.remove(name);
    }

    /**
     * Returns an unmodifiable map of the attributes.
     *
     * @return an unmodifiable map of the attributes
     */
    public Map<String,Object> getAllAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Copies all of the mappings from the specified attributes.
     *
     * @param attributes the specified attributes
     */
    public void putAllAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }

    /**
     * Removes all attributes.
     */
    public void clearAllAttributes() {
        attributes.clear();
    }

    public void invalidate() {
        updateLastAccessedTime();
    }

    public SessionScope getSessionScope() {
        return sessionScope;
    }

    private String generateSessionId() {
        long seed = creationTime;
        seed ^= Runtime.getRuntime().freeMemory();

        Random rnd = new Random(seed);

        return Long.toString(Math.abs(rnd.nextLong()),16);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", getId());
        tsb.append("creationTime", getCreationTime());
        tsb.append("lastAccessedTime", getLastAccessedTime());
        tsb.append("maxInactiveInterval", getMaxInactiveInterval());
        tsb.append("attributeNames", getAttributeNames());
        return tsb.toString();
    }

}
