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
package com.aspectran.core.component.session;

import com.aspectran.core.util.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The data associated with a session.
 *
 * <p>Created: 2017. 6. 6.</p>
 */
public class SessionData implements Serializable {

    private static final long serialVersionUID = -6253355753257200708L;

    private String id;

    private final Map<String, Object> attributes;

    private final long creationTime;

    /** the time of the last access */
    private long accessedTime;

    /** the time of the last access excluding this one */
    private long lastAccessedTime;

    private long maxInactiveInterval;

    private long expiryTime;

    private boolean dirty;

    private long lastSaved; // time in msec since last save

    public SessionData(String id, long creationTime, long accessedTime, long lastAccessedTime, long maxInactiveInterval) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.creationTime = creationTime;
        this.accessedTime = accessedTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
        calcAndSetExpiryTime(creationTime);
        this.attributes = new ConcurrentHashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)attributes.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T setAttribute(String name, Object value) {
        T old;
        if (value == null) {
            // noinspection unchecked
            old = (T)attributes.remove(name);
        } else {
            // noinspection unchecked
            old = (T)attributes.put(name, value);
        }
        if (value == null && old == null) {
            return null;
        }
        dirty = true;
        return old;
    }

    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    public Set<String> getKeys() {
        return attributes.keySet();
    }

    public void removeAttribute(String name) {
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

    public long getCreationTime() {
        return creationTime;
    }

    public long getAccessedTime() {
        return accessedTime;
    }

    public void setAccessedTime(long accessedTime) {
        this.accessedTime = accessedTime;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public long getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(long maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public long calcExpiryTime() {
        return calcExpiryTime(System.currentTimeMillis());
    }

    public long calcExpiryTime(long time) {
        return (maxInactiveInterval <= 0L ? 0L : (time + maxInactiveInterval));
    }

    public void calcAndSetExpiryTime() {
        setExpiryTime(calcExpiryTime());
    }

    public void calcAndSetExpiryTime(long time) {
        setExpiryTime(calcExpiryTime(time));
    }

    public boolean isExpiredAt(long time) {
        if (maxInactiveInterval <= 0L) {
            return false; // never expires
        }
        return (expiryTime <= time);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public long getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(long lastSaved) {
        this.lastSaved = lastSaved;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", getId());
        tsb.append("createdTime", getCreationTime());
        tsb.append("accessedTime", getLastAccessedTime());
        tsb.append("lastAccessedTime", getLastAccessedTime());
        tsb.append("maxInactiveInterval", getMaxInactiveInterval());
        tsb.append("expiryTime", getExpiryTime());
        return tsb.toString();
    }

}
