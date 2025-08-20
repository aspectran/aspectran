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
 * The data associated with a session.
 *
 * <p>Created: 2017. 6. 6.</p>
 */
public class SessionData implements Serializable {

    @Serial
    private static final long serialVersionUID = -6253355753257200708L;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private String id;

    private final long created;

    /** the time of the last access */
    private long accessed;

    /** the time of the last access excluding this one */
    private long lastAccessed;

    private long inactiveInterval;

    private long extraInactiveInterval;

    /** precalculated time of expiry in ms since epoch */
    private long expiry;

    private boolean dirty;

    /** time in ms since last save */
    private long lastSaved;

    protected SessionData(String id, long created, long inactiveInterval) {
        this(id, created, created, created, inactiveInterval);
        calcAndSetExpiry(created);
    }

    protected SessionData(String id, long created, long accessed, long lastAccessed, long inactiveInterval) {
        this.id = id;
        this.created = created;
        this.accessed = accessed;
        this.lastAccessed = lastAccessed;
        this.inactiveInterval = inactiveInterval;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public long getAccessed() {
        return accessed;
    }

    public void setAccessed(long accessed) {
        this.accessed = accessed;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public long getInactiveInterval() {
        return inactiveInterval;
    }

    public void setInactiveInterval(long inactiveInterval) {
        this.inactiveInterval = inactiveInterval;
    }

    protected long getExtraInactiveInterval() {
        return extraInactiveInterval;
    }

    protected void setExtraInactiveInterval(long extraInactiveInterval) {
        this.extraInactiveInterval = extraInactiveInterval;
    }

    public void reduceInactiveInterval(long inactiveInterval) {
        if (this.inactiveInterval > inactiveInterval) {
            this.extraInactiveInterval = this.inactiveInterval - inactiveInterval;
            this.inactiveInterval = inactiveInterval;
        } else {
            this.extraInactiveInterval = 0L;
        }
    }

    public boolean restoreInactiveInterval() {
        if (this.extraInactiveInterval > 0) {
            this.inactiveInterval += this.extraInactiveInterval;
            this.extraInactiveInterval = 0L;
            return true;
        } else {
            return false;
        }
    }

    public long getExpiry() {
        return expiry;
    }

    private void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    protected long calcExpiry(long time) {
        return (inactiveInterval <= 0L ? 0L : (time + inactiveInterval));
    }

    public void calcAndSetExpiry(long time) {
        setExpiry(calcExpiry(time));
    }

    public boolean isExpiredAt(long time) {
        if (inactiveInterval <= 0L) {
            return false; // never expires
        } else {
            return (expiry <= time);
        }
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

    public long getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(long lastSaved) {
        this.lastSaved = lastSaved;
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

    public <T> T removeAttribute(String name) {
        return setAttribute(name, null);
    }

    /**
     * @return a Set of attribute names
     */
    public Set<String> getKeys() {
        return attributes.keySet();
    }

    /**
     * Returns an unmodifiable map of the attributes.
     * @return an unmodifiable map of the attributes
     */
    public Map<String, Object> getAllAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Copies all of the mappings from the specified attributes.
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
     * Save the session data.
     * @param outputStream the output stream to save to
     * @param nonPersistentAttributes the attribute names to be excluded from serialization
     * @throws IOException if an I/O error has occurred
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
     * Load session data from an input stream that contains session data.
     * @param inputStream the input stream containing session data
     * @return the session data
     * @throws Exception if the session data could not be read from the file
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
