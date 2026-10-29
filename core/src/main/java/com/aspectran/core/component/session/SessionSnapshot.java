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
package com.aspectran.core.component.session;

import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An immutable, read-only snapshot of a {@link Session} captured at a specific point in time.
 * <p>All reading operations are completely lock-free, while state-mutating methods throw
 * {@link UnsupportedOperationException}.</p>
 *
 * <p>Created: 2026. 10. 30.</p>
 */
public class SessionSnapshot implements Session, Serializable {

    @Serial
    private static final long serialVersionUID = 7129583162817293845L;

    private final String id;

    private final long creationTime;

    private final long lastAccessedTime;

    private final int maxInactiveInterval;

    private final long remainingInactiveInterval;

    private final boolean newSession;

    private final boolean tempResident;

    private final Map<String, Object> attributes;

    public SessionSnapshot(
            String id,
            long creationTime,
            long lastAccessedTime,
            int maxInactiveInterval,
            long remainingInactiveInterval,
            boolean newSession,
            boolean tempResident,
            @Nullable Map<String, Object> attributes) {
        this.id = id;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
        this.remainingInactiveInterval = remainingInactiveInterval;
        this.newSession = newSession;
        this.tempResident = tempResident;
        // Note: Use new HashMap<>(attributes) instead of Map.copyOf(attributes) because
        // Map.copyOf strictly disallows null values with a NullPointerException.
        // Although standard session attributes typically avoid null values, defensive
        // copying via HashMap guarantees complete robustness and tolerance against null values
        // in custom session stores, mock objects, or test environments.
        this.attributes = (attributes != null && !attributes.isEmpty() ?
                Collections.unmodifiableMap(new HashMap<>(attributes)) : Collections.emptyMap());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)NonPersistentValue.unwrap(attributes.get(name));
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    @Override
    public Object setAttribute(String name, Object value) {
        throw new UnsupportedOperationException("SessionSnapshot is read-only");
    }

    @Override
    public Object removeAttribute(String name) {
        throw new UnsupportedOperationException("SessionSnapshot is read-only");
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int secs) {
        throw new UnsupportedOperationException("SessionSnapshot is read-only");
    }

    @Override
    public long getRemainingInactiveInterval() {
        return remainingInactiveInterval;
    }

    @Override
    public boolean isNew() {
        return newSession;
    }

    @Override
    public boolean isTempResident() {
        return tempResident;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean access() {
        throw new UnsupportedOperationException("SessionSnapshot is read-only");
    }

    @Override
    public void complete() {
        throw new UnsupportedOperationException("SessionSnapshot is read-only");
    }

    @Override
    public void invalidate() {
        throw new UnsupportedOperationException("SessionSnapshot is read-only");
    }

    @Override
    public DestroyedReason getDestroyedReason() {
        return null;
    }

    @Override
    public <T> T read(Function<Session, T> action) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        }
        return action.apply(this);
    }

    @Override
    public void read(Consumer<Session> action) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        }
        action.accept(this);
    }

    @Override
    public SessionSnapshot getSnapshot() {
        return this;
    }

    /**
     * Returns an unmodifiable map of all attributes in this snapshot.
     * @return the attributes map
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Returns whether an attribute with the specified name exists.
     * @param name the name of the attribute
     * @return true if the attribute exists, false otherwise
     */
    public boolean containsAttribute(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", id);
        tsb.append("creationTime", creationTime);
        tsb.append("lastAccessedTime", lastAccessedTime);
        tsb.append("maxInactiveInterval", maxInactiveInterval);
        tsb.append("remainingInactiveInterval", remainingInactiveInterval);
        tsb.append("newSession", newSession);
        tsb.append("tempResident", tempResident);
        tsb.append("attributes", attributes.keySet());
        return tsb.toString();
    }

}
