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

import com.aspectran.utils.CustomObjectInputStream;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the complete state of a session, including its metadata and attributes.
 * This class is designed to be serializable so that it can be persisted by a
 * {@link SessionStore}.
 *
 * <p>Created: 2017. 6. 6.</p>
 */
public class SessionData implements Serializable {

    @Serial
    private static final long serialVersionUID = -6253355753257200708L;

    /** The session's attributes */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /** The unique identifier of the session */
    private String id;

    /** The timestamp when the session was created */
    private final long created;

    /** The timestamp of the last access */
    private long accessed;

    /** The timestamp of the access before the current one */
    private long lastAccessed;

    /** The maximum time in milliseconds the session can be inactive */
    private long inactiveInterval;

    /** The amount of inactive interval that was temporarily reduced */
    private long extraInactiveInterval;

    /** The pre-calculated timestamp when the session will expire */
    private long expiry;

    /** A flag indicating if the session's attributes have changed */
    private boolean dirty;

    /** The timestamp when the session was last saved to the store */
    private long lastSaved;

    /**
     * Instantiates a new SessionData.
     * @param id the session ID
     * @param created the creation timestamp
     * @param inactiveInterval the maximum inactive interval in milliseconds
     */
    protected SessionData(String id, long created, long inactiveInterval) {
        this(id, created, created, created, inactiveInterval);
        calcAndSetExpiry(created);
    }

    /**
     * Instantiates a new SessionData.
     * @param id the session ID
     * @param created the creation timestamp
     * @param accessed the last accessed timestamp
     * @param lastAccessed the previously accessed timestamp
     * @param inactiveInterval the maximum inactive interval in milliseconds
     */
    protected SessionData(String id, long created, long accessed, long lastAccessed, long inactiveInterval) {
        this.id = id;
        this.created = created;
        this.accessed = accessed;
        this.lastAccessed = lastAccessed;
        this.inactiveInterval = inactiveInterval;
    }

    /**
     * Returns the session's unique identifier.
     * @return the session ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the session's unique identifier.
     * @param id the session ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the time the session was created.
     * @return the creation timestamp
     */
    public long getCreated() {
        return created;
    }

    /**
     * Returns the last time the session was accessed.
     * @return the last accessed timestamp
     */
    public long getAccessed() {
        return accessed;
    }

    /**
     * Sets the last time the session was accessed.
     * @param accessed the last accessed timestamp
     */
    public void setAccessed(long accessed) {
        this.accessed = accessed;
    }

    /**
     * Returns the time before the last access.
     * @return the previous access timestamp
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Sets the time before the last access.
     * @param lastAccessed the previous access timestamp
     */
    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    /**
     * Returns the maximum time the session can be inactive.
     * @return the max inactive interval in milliseconds
     */
    public long getInactiveInterval() {
        return inactiveInterval;
    }

    /**
     * Sets the maximum time the session can be inactive.
     * @param inactiveInterval the max inactive interval in milliseconds
     */
    public void setInactiveInterval(long inactiveInterval) {
        this.inactiveInterval = inactiveInterval;
    }

    protected long getExtraInactiveInterval() {
        return extraInactiveInterval;
    }

    protected void setExtraInactiveInterval(long extraInactiveInterval) {
        this.extraInactiveInterval = extraInactiveInterval;
    }

    /**
     * Reduces the inactive interval and stores the reduced amount.
     * @param inactiveInterval the new, shorter inactive interval
     */
    public void reduceInactiveInterval(long inactiveInterval) {
        if (this.inactiveInterval > inactiveInterval) {
            this.extraInactiveInterval = this.inactiveInterval - inactiveInterval;
            this.inactiveInterval = inactiveInterval;
        } else {
            this.extraInactiveInterval = 0L;
        }
    }

    /**
     * Restores the inactive interval to its original value.
     * @return true if the interval was restored, false otherwise
     */
    public boolean restoreInactiveInterval() {
        if (this.extraInactiveInterval > 0) {
            this.inactiveInterval += this.extraInactiveInterval;
            this.extraInactiveInterval = 0L;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the time the session will expire.
     * @return the expiry timestamp
     */
    public long getExpiry() {
        return expiry;
    }

    private void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    /**
     * Calculates the expiry time based on a given time.
     * @param time the base time for calculation
     * @return the calculated expiry timestamp
     */
    protected long calcExpiry(long time) {
        return (inactiveInterval <= 0L ? 0L : (time + inactiveInterval));
    }

    /**
     * Calculates and sets the expiry time based on a given time.
     * @param time the base time for calculation
     */
    public void calcAndSetExpiry(long time) {
        setExpiry(calcExpiry(time));
    }

    /**
     * Checks if the session has expired at a given time.
     * @param time the time to check against
     * @return true if the session has expired, false otherwise
     */
    public boolean isExpiredAt(long time) {
        if (inactiveInterval <= 0L) {
            return false; // never expires
        } else {
            return (expiry <= time);
        }
    }

    /**
     * Checks if the session data has been modified.
     * @return true if the session needs to be saved to the store
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets whether the session data has been modified.
     * @param dirty true if the session has been modified
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Returns the time the session was last saved.
     * @return the last saved timestamp
     */
    public long getLastSaved() {
        return lastSaved;
    }

    /**
     * Sets the time the session was last saved.
     * @param lastSaved the last saved timestamp
     */
    public void setLastSaved(long lastSaved) {
        this.lastSaved = lastSaved;
    }

    /**
     * Retrieves an attribute by name.
     * @param <T> the type of the attribute
     * @param name the name of the attribute
     * @return the attribute value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)attributes.get(name);
    }

    /**
     * Sets an attribute, replacing any existing value.
     * @param <T> the type of the old attribute value
     * @param name the name of the attribute
     * @param value the new value for the attribute
     * @return the previous value of the attribute, or null
     */
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
     * Removes an attribute from the session.
     * @param <T> the type of the removed attribute
     * @param name the name of the attribute to remove
     * @return the removed value, or null if not found
     */
    public <T> T removeAttribute(String name) {
        return setAttribute(name, null);
    }

    /**
     * Returns the set of all attribute names.
     * @return a set of attribute names
     */
    public Set<String> getKeys() {
        return attributes.keySet();
    }

    /**
     * Returns an unmodifiable map of all attributes.
     * @return an unmodifiable map of attributes
     */
    public Map<String, Object> getAllAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Copies all mappings from the specified map to this session's attributes.
     * @param attributes the attributes to add
     */
    public void putAllAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }

    /**
     * Removes all attributes from the session.
     */
    public void clearAllAttributes() {
        attributes.clear();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("id", id);
        tsb.append("created", created);
        tsb.append("accessed", accessed);
        tsb.append("lastAccessed", lastAccessed);
        tsb.append("inactiveInterval", inactiveInterval);
        if (extraInactiveInterval > 0) {
            tsb.append("extraInactiveInterval", extraInactiveInterval);
        }
        tsb.append("expiry", expiry);
        return tsb.toString();
    }

    /**
     * Serializes the session data to an output stream.
     * @param data the session data to serialize
     * @param outputStream the stream to write to
     * @param nonPersistentAttributes a set of attribute names to exclude from serialization
     * @throws IOException if an I/O error occurs
     */
    public static void serialize(
            @NonNull SessionData data, OutputStream outputStream, Set<String> nonPersistentAttributes)
            throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeUTF(data.getId());
        dataOutputStream.writeLong(data.getCreated());
        dataOutputStream.writeLong(data.getAccessed());
        dataOutputStream.writeLong(data.getLastAccessed());
        dataOutputStream.writeLong(data.getInactiveInterval());
        dataOutputStream.writeLong(data.getExtraInactiveInterval());
        dataOutputStream.writeLong(data.getExpiry());

        Set<String> keys = data.getKeys();
        if (keys.isEmpty()) {
            dataOutputStream.writeInt(0);
            return;
        }

        List<String> attrKeys = new ArrayList<>(keys.size());
        for (String key : keys) {
            // Remove all attributes specified to be excluded from serialization
            if ((nonPersistentAttributes == null || !nonPersistentAttributes.contains(key)) &&
                    !(data.getAttribute(key) instanceof NonPersistent)) {
                attrKeys.add(key);
            }
        }

        dataOutputStream.writeInt(attrKeys.size());

        if (!attrKeys.isEmpty()) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(dataOutputStream);
            for (String name : attrKeys) {
                Object value = data.getAttribute(name);
                objectOutputStream.writeUTF(name);
                objectOutputStream.writeObject(value);
            }
        }
    }

    /**
     * Deserializes session data from an input stream.
     * @param inputStream the stream to read from
     * @return a new SessionData instance
     * @throws Exception if an error occurs during deserialization
     */
    @NonNull
    public static SessionData deserialize(InputStream inputStream) throws Exception {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        String id = dataInputStream.readUTF(); // the actual id from inside the file
        long created = dataInputStream.readLong();
        long accessed = dataInputStream.readLong();
        long lastAccessed = dataInputStream.readLong();
        long inactiveInterval = dataInputStream.readLong();
        long extraInactiveInterval = dataInputStream.readLong();
        long expiry = dataInputStream.readLong();
        int entries = dataInputStream.readInt();

        SessionData data = new SessionData(id, created, accessed, lastAccessed, inactiveInterval);
        data.setExtraInactiveInterval(extraInactiveInterval);
        data.setExpiry(expiry);

        // Load all attributes
        if (entries > 0) {
            Map<String, Object> attributes = new ConcurrentHashMap<>();
            // input stream should not be closed here
            ObjectInputStream objectInputStream =  new CustomObjectInputStream(dataInputStream);
            for (int i = 0; i < entries; i++) {
                String key = objectInputStream.readUTF();
                Object value = objectInputStream.readObject();
                attributes.put(key, value);
            }
            data.putAllAttributes(attributes);
        }

        return data;
    }

}
