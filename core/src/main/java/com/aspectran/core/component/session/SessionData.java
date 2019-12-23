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

import com.aspectran.core.util.CustomObjectInputStream;
import com.aspectran.core.util.ToStringBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private final long creationTime;

    /** the time of the last access */
    private long accessedTime;

    /** the time of the last access excluding this one */
    private long lastAccessedTime;

    private long maxInactiveInterval;

    /** precalculated time of expiry in ms since epoch */
    private long expiryTime;

    private boolean dirty;

    /** time in ms since last save */
    private long lastSavedTime;

    public SessionData(String id, long creationTime, long accessedTime, long lastAccessedTime, long maxInactiveInterval) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.creationTime = creationTime;
        this.accessedTime = accessedTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
        calcAndSetExpiry(creationTime);
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
            old = (T)attributes.remove(name);
        } else {
            old = (T)attributes.put(name, value);
        }
        if (value == null && old == null) {
            return null;
        }
        setDirty(true);
        return old;
    }

    /**
     * @return a Set of attribute names
     */
    public Set<String> getKeys() {
        return attributes.keySet();
    }

    /**
     * Returns an unmodifiable map of the attributes.
     *
     * @return an unmodifiable map of the attributes
     */
    public Map<String, Object> getAllAttributes() {
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

    public long calcExpiry() {
        return calcExpiry(System.currentTimeMillis());
    }

    public long calcExpiry(long time) {
        return (maxInactiveInterval <= 0L ? 0L : (time + maxInactiveInterval));
    }

    public void calcAndSetExpiry() {
        setExpiryTime(calcExpiry());
    }

    public void calcAndSetExpiry(long time) {
        setExpiryTime(calcExpiry(time));
    }

    public boolean isExpiredAt(long time) {
        if (maxInactiveInterval <= 0L) {
            return false; // never expires
        }
        return (expiryTime <= time);
    }

    /**
     * @return true if a session needs to be written out
     */
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public long getLastSavedTime() {
        return lastSavedTime;
    }

    public void setLastSavedTime(long lastSavedTime) {
        this.lastSavedTime = lastSavedTime;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", getId());
        tsb.append("created", getCreationTime());
        tsb.append("accessed", getLastAccessedTime());
        tsb.append("lastAccessed", getLastAccessedTime());
        tsb.append("maxInactiveInterval", getMaxInactiveInterval());
        tsb.append("expiry", getExpiryTime());
        return tsb.toString();
    }

    /**
     * Save the session data.
     *
     * @param os the output stream to save to
     * @param nonPersistentAttributes the attribute names to be excluded from serialization
     * @throws IOException if an I/O error has occurred
     */
    public static void serialize(SessionData data, OutputStream os,
                                 Set<String> nonPersistentAttributes) throws IOException {
        DataOutputStream out = new DataOutputStream(os);
        out.writeUTF(data.getId());
        out.writeLong(data.getCreationTime());
        out.writeLong(data.getAccessedTime());
        out.writeLong(data.getLastAccessedTime());
        out.writeLong(data.getExpiryTime());
        out.writeLong(data.getMaxInactiveInterval());

        Set<String> keys = data.getKeys();
        if (keys.isEmpty()) {
            out.writeInt(0);
            return;
        }

        List<String> attrKeys = new ArrayList<>(keys);
        // remove attributes excluded from serialization
        if (nonPersistentAttributes != null && !attrKeys.isEmpty()) {
            attrKeys.removeAll(nonPersistentAttributes);
        }
        for (String name : attrKeys) {
            Object value = data.getAttribute(name);
            if (value instanceof NonPersistent) {
                attrKeys.remove(name);
            }
        }
        out.writeInt(attrKeys.size());
        if (!attrKeys.isEmpty()) {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            for (String name : attrKeys) {
                Object value = data.getAttribute(name);
                oos.writeUTF(name);
                oos.writeObject(value);
            }
        }
    }

    /**
     * Load session data from an input stream that contains session data.
     *
     * @param is the input stream containing session data
     * @return the session data
     * @throws Exception if the session data could not be read from the file
     */
    public static SessionData deserialize(InputStream is) throws Exception {
        DataInputStream dis = new DataInputStream(is);
        String id = dis.readUTF(); // the actual id from inside the file
        long created = dis.readLong();
        long accessed = dis.readLong();
        long lastAccessed = dis.readLong();
        long expiry = dis.readLong();
        long maxInactive = dis.readLong();
        int entries = dis.readInt();

        SessionData data = new SessionData(id, created, accessed, lastAccessed, maxInactive);
        data.setExpiryTime(expiry);
        data.setMaxInactiveInterval(maxInactive);

        // Attributes
        restoreAttributes(dis, entries, data);

        return data;
    }

    /**
     * Load attributes from an input stream that contains session data.
     *
     * @param is the input stream containing session data
     * @param entries number of attributes
     * @param data the data to restore to
     * @throws Exception if the input stream is invalid or fails to read
     */
    private static void restoreAttributes(InputStream is, int entries, SessionData data) throws Exception {
        if (entries > 0) {
            // input stream should not be closed here
            Map<String, Object> attributes = new HashMap<>();
            ObjectInputStream ois =  new CustomObjectInputStream(is);
            for (int i = 0; i < entries; i++) {
                String key = ois.readUTF();
                Object value = ois.readObject();
                attributes.put(key, value);
            }
            data.putAllAttributes(attributes);
        }
    }

}
