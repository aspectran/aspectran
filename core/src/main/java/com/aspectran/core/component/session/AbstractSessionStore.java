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

import com.aspectran.core.component.AbstractComponent;
import com.aspectran.utils.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * An abstract base implementation of the {@link SessionStore} interface.
 * This class provides common functionality for session stores, such as handling
 * grace periods for session expiration and periodic saving of session data.
 * Subclasses must implement the persistence-specific methods.
 *
 * <p>Created: 2017. 9. 10.</p>
 */
public abstract class AbstractSessionStore extends AbstractComponent implements SessionStore {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionStore.class);

    /** Default grace period in seconds (1 minute). */
    public static final int DEFAULT_GRACE_PERIOD_SECS = 60;

    /** Default save period in seconds (0, meaning save on every change). */
    public static final int DEFAULT_SAVE_PERIOD_SECS = 0;

    /** The grace period in seconds to avoid premature session scavenging. */
    private int gracePeriodSecs = DEFAULT_GRACE_PERIOD_SECS;

    /** The minimum time in seconds between save operations. */
    private int savePeriodSecs = DEFAULT_SAVE_PERIOD_SECS;

    /** The set of attribute names that should not be persisted. */
    private Set<String> nonPersistentAttributes;

    /** The last time in milliseconds that expired sessions were checked. */
    private long lastExpiryCheckTime = 0L;

    /**
     * Returns the grace period in seconds.
     * This is an interval to prevent overly aggressive session scavenging.
     * @return the grace period in seconds
     */
    public int getGracePeriodSecs() {
        return gracePeriodSecs;
    }

    /**
     * Calculates the grace period in milliseconds, multiplied by a weight.
     * @param weight the multiplier for the grace period
     * @return the grace period in milliseconds
     */
    public long getGracePeriodMillis(float weight) {
        if (gracePeriodSecs > 0) {
            return TimeUnit.SECONDS.toMillis((long)(gracePeriodSecs * weight));
        } else {
            return 0L;
        }
    }

    /**
     * Sets the interval in secs to prevent too eager session scavenging.
     * @param gracePeriodSecs interval in secs to prevent too eager session scavenging
     */
    public void setGracePeriodSecs(int gracePeriodSecs) {
        this.gracePeriodSecs = gracePeriodSecs;
    }

    /**
     * Returns the minimum time in seconds between save operations.
     * @return the time in seconds between saves
     */
    public int getSavePeriodSecs() {
        return savePeriodSecs;
    }

    /**
     * Returns the minimum time in milliseconds between save operations.
     * @return the save period in milliseconds
     */
    public long getSavePeriodMillis() {
        if (savePeriodSecs > 0) {
            return TimeUnit.SECONDS.toMillis(savePeriodSecs);
        } else {
            return 0L;
        }
    }

    /**
     * The minimum time in seconds between save operations.
     * Saves normally occur every time the last request
     * exits as session. If nothing changes on the session
     * except for the access time and the persistence technology
     * is slow, this can cause delays.
     * <p>By default the value is 0, which means we save
     * after the last request exists. A non zero value
     * means that we will skip doing the save if the
     * session isn't dirty if the elapsed time since
     * the session was last saved does not exceed this
     * value.</p>
     * @param savePeriodSecs the savePeriodSecs to set
     */
    public void setSavePeriodSecs(int savePeriodSecs) {
        this.savePeriodSecs = savePeriodSecs;
    }

    @Override
    public Set<String> getNonPersistentAttributes() {
        return nonPersistentAttributes;
    }

    /**
     * Specifies attributes that should be excluded from serialization.
     * @param nonPersistentAttributes the attribute names to exclude from serialization
     */
    public void setNonPersistentAttributes(String... nonPersistentAttributes) {
        checkInitializable();
        if (nonPersistentAttributes != null && nonPersistentAttributes.length > 0) {
            Set<String> attrNames = new HashSet<>();
            Collections.addAll(attrNames, nonPersistentAttributes);
            this.nonPersistentAttributes = Collections.unmodifiableSet(attrNames);
        } else {
            this.nonPersistentAttributes = null;
        }
    }

    /**
     * Checks if an attribute should be excluded from persistence.
     * @param attrName the name of the attribute
     * @return true if the attribute is non-persistent, false otherwise
     */
    public boolean isNonPersistentAttribute(String attrName) {
        if (nonPersistentAttributes != null) {
            return nonPersistentAttributes.contains(attrName);
        } else {
            return false;
        }
    }

    @Override
    public void save(String id, SessionData data) throws Exception {
        checkAvailable();

        if (data == null) {
            return;
        }

        long lastSaved = data.getLastSaved();
        long savePeriodMs = getSavePeriodMillis();
        long now = System.currentTimeMillis();
        long elapsed = now - lastSaved;

        if (logger.isTraceEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Store session");
            tsb.append("id", id);
            tsb.append("dirty", data.isDirty());
            tsb.append("lastSaved", data.getLastSaved());
            tsb.append("savePeriod", savePeriodMs);
            tsb.append("elapsed", elapsed);
            logger.trace(tsb.toString());
        }

        // save session if attribute changed or never been saved or time between saves exceeds threshold
        if (data.isDirty() || lastSaved <= 0 || elapsed > savePeriodMs) {
            // set the last saved time to now
            data.setLastSaved(now);
            try {
                // call the specific store method, passing in previous save time
                doSave(id, data);
                data.setDirty(false); // only undo the dirty setting if we saved it
            } catch (Exception e) {
                // reset last save time if save failed
                data.setLastSaved(lastSaved);
                throw e;
            }
        }
    }

    /**
     * Store the session data persistently.
     * @param id identity of session to store
     * @param data info of the session
     * @throws Exception if unable to store data
     */
    public abstract void doSave(String id, SessionData data) throws Exception;

    @Override
    public Set<String> getExpired(Set<String> candidates) {
        if (candidates == null) {
            throw new IllegalArgumentException("candidates must not be null");
        }

        checkAvailable();

        // check the backing store to find other sessions
        // that expired long ago (ie cannot be actively managed by any node)
        long now = System.currentTimeMillis();
        Set<String> expired = null;

        // if we have never checked for old expired sessions, then only find
        // those that are very old so we don't find sessions that other nodes
        // that are also starting up find
        long time = 0L;
        if (lastExpiryCheckTime <= 0L) {
            time = now - getGracePeriodMillis(3);
        } else {
            // only do the check once every gracePeriod to avoid expensive searches,
            // and find sessions that expired at least one gracePeriod ago
            long gracePeriod = getGracePeriodMillis(1);
            if (now > lastExpiryCheckTime + gracePeriod) {
                time = now - gracePeriod;
            }
        }
        if (time > 0L) {
            expired = doGetExpired(time);
            for (String id : candidates) {
                if (!expired.contains(id) && !checkExpiry(id, time)) {
                    expired.add(id);
                }
            }
            lastExpiryCheckTime = time;
        }
        return expired;
    }

    private boolean checkExpiry(String id, long time) {
        SessionData data = null;
        try {
            data = load(id);
        } catch (Exception ignored) {
            // ignore
        }
        return checkExpiry(data, time);
    }

    /**
     * Checks whether the given session has not expired.
     * @param data the session data
     * @param time the time before which the sessions must have expired
     * @return true if the session has not yet expired, false otherwise
     */
    protected boolean checkExpiry(SessionData data, long time) {
        if (data != null) {
            long expiry = data.getExpiry();
            return (expiry <= 0L || expiry > time);
        } else {
            return false;
        }
    }

    /**
     * Implemented by subclasses to resolve which sessions this store
     * should attempt to expire.
     * @param time the upper limit of expiry times to check
     * @return the reconciled set of session ids that this store should attempt to expire
     */
    public abstract Set<String> doGetExpired(long time);

    /**
     * Implemented by subclasses to delete unmanaged sessions that expired
     * before a specified time. This is to remove 'orphaned' sessions that are
     * no longer actively managed on any node, while sessions that are
     * explicitly managed on each node are handled by other mechanisms such
     * as doGetExpired.
     * <p>This is called only periodically to avoid placing
     * excessive load on the store.</p>
     * @param time the upper limit of the expiry time to check in msec
     */
    public abstract void doCleanOrphans(long time);

    @Override
    public void cleanOrphans(long time) {
        checkAvailable();
        doCleanOrphans(time);
    }

}
