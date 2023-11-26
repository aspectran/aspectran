/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Abstract Implementation for SessionStore.
 *
 * <p>Created: 2017. 9. 10.</p>
 */
public abstract class AbstractSessionStore extends AbstractComponent implements SessionStore {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionStore.class);

    public static final int DEFAULT_GRACE_PERIOD_SECS = 60 * 5; // default of 5min

    public static final int DEFAULT_SAVE_PERIOD_SECS = 0;

    private int gracePeriodSecs = DEFAULT_GRACE_PERIOD_SECS;

    private int savePeriodSecs = DEFAULT_SAVE_PERIOD_SECS; // time in seconds between saves

    private Set<String> nonPersistentAttributes;

    private long lastExpiryCheckTime = 0L; // last time in ms that getExpired was called

    public int getGracePeriodSecs() {
        return gracePeriodSecs;
    }

    /**
     * Sets the interval in secs to prevent too eager session scavenging.
     * @param gracePeriodSecs interval in secs to prevent too eager session scavenging
     */
    public void setGracePeriodSecs(int gracePeriodSecs) {
        this.gracePeriodSecs = gracePeriodSecs;
    }

    /**
     * @return the time in seconds between saves
     */
    public int getSavePeriodSecs() {
        return savePeriodSecs;
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
        if (nonPersistentAttributes != null && nonPersistentAttributes.length > 0) {
            Set<String> attrNames = new HashSet<>();
            Collections.addAll(attrNames, nonPersistentAttributes);
            this.nonPersistentAttributes = Collections.unmodifiableSet(attrNames);
        } else {
            this.nonPersistentAttributes = null;
        }
    }

    public boolean isNonPersistentAttributes(String attrName) {
        if (nonPersistentAttributes != null) {
            return nonPersistentAttributes.contains(attrName);
        } else {
            return false;
        }
    }

    @Override
    public void save(String id, SessionData data) throws Exception {
        checkInitialized();
        if (data == null) {
            return;
        }

        long lastSaveMs = data.getLastSaved();
        long savePeriodMs = (savePeriodSecs <= 0 ? 0 : TimeUnit.SECONDS.toMillis(savePeriodSecs));

        if (logger.isTraceEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Store session");
            tsb.append("id", id);
            tsb.append("dirty", data.isDirty());
            tsb.append("lastSaved", data.getLastSaved());
            tsb.append("savePeriod", savePeriodMs);
            tsb.append("elapsed", System.currentTimeMillis() - lastSaveMs);
            logger.trace(tsb.toString());
        }

        // save session if attribute changed or never been saved or time between saves exceeds threshold
        if (data.isDirty() || lastSaveMs <= 0 || (System.currentTimeMillis() - lastSaveMs) > savePeriodMs) {
            // set the last saved time to now
            data.setLastSaved(System.currentTimeMillis());
            try {
                // call the specific store method, passing in previous save time
                doSave(id, data);
                data.setDirty(false); // only undo the dirty setting if we saved it
            } catch (Exception e) {
                // reset last save time if save failed
                data.setLastSaved(lastSaveMs);
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
        checkInitialized();

        // check the backing store to find other sessions
        // that expired long ago (ie cannot be actively managed by any node)
        long now = System.currentTimeMillis();
        Set<String> expiredSessions = null;
        try {
            long time = 0L;
            // if we have never checked for old expired sessions, then only find
            // those that are very old so we don't find sessions that other nodes
            // that are also starting up find
            if (lastExpiryCheckTime <= 0L) {
                time = now - TimeUnit.SECONDS.toMillis(gracePeriodSecs * 3L);
            } else {
                // only do the check once every gracePeriod to avoid expensive searches,
                // and find sessions that expired at least one gracePeriod ago
                if (now > (lastExpiryCheckTime + TimeUnit.SECONDS.toMillis(gracePeriodSecs))) {
                    time = now - TimeUnit.SECONDS.toMillis(gracePeriodSecs);
                }
            }
            if (time > 0L) {
                expiredSessions = doGetExpired(time);
                for (String id : candidates) {
                    if (!expiredSessions.contains(id)) {
                        boolean expired = false;
                        try {
                            SessionData data = load(id);
                            if (data != null) {
                                if (data.getExpiry() > 0 && data.getExpiry() <= time) {
                                    expired = true;
                                }
                            }
                        } catch (Exception ignored) {
                            // no grace period unless session data is valid
                        }
                        if (expired) {
                            expiredSessions.add(id);
                        }
                    }
                }
            }
        } finally {
            lastExpiryCheckTime = now;
        }
        return expiredSessions;
    }

    /**
     * Implemented by subclasses to resolve which sessions this node
     * should attempt to expire.
     * @param time the upper limit of expiry times to check
     * @return the reconciled set of session ids that this node should attempt to expire
     */
    public abstract Set<String> doGetExpired (long time);

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
        checkInitialized();
        doCleanOrphans(time);
    }

    protected void checkInitialized() throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
    }

    protected void checkAlreadyInitialized() throws IllegalStateException {
        if (isInitialized()) {
            throw new IllegalStateException("Already initialized");
        }
    }

}
