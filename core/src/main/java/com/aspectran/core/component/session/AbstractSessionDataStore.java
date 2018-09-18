/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>Created: 2017. 9. 10.</p>
 */
public abstract class AbstractSessionDataStore extends AbstractComponent implements SessionDataStore {

    private static final Log log = LogFactory.getLog(AbstractSessionDataStore.class);

    protected int gracePeriodSec = 60 * 60; // default of 1hr

    protected long lastExpiryCheckTime = 0; // last time in ms that getExpired was called

    protected int savePeriodSec = 0; // time in sec between saves

    /**
     * Store the session data persistently.
     *
     * @param id identity of session to store
     * @param data info of the session
     * @param lastSaveTime time of previous save or 0 if never saved
     * @throws Exception if unable to store data
     */
    public abstract void doStore(String id, SessionData data, long lastSaveTime) throws Exception;

    /**
     * Implemented by subclasses to resolve which sessions this node
     * should attempt to expire.
     *
     * @param candidates the ids of sessions the SessionDataStore thinks has expired
     * @return the reconciled set of session ids that this node should attempt to expire
     */
    public abstract Set<String> doGetExpired (Set<String> candidates);

    @Override
    public void store(String id, SessionData data) throws Exception {
        if (data == null) {
            return;
        }

        long lastSave = data.getLastSaved();
        long savePeriodMs = (savePeriodSec <= 0 ? 0 : TimeUnit.SECONDS.toMillis(savePeriodSec));

        if (log.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Store");
            tsb.append("id", id);
            tsb.append("dirty", data.isDirty());
            tsb.append("lastSaved", data.getLastSaved());
            tsb.append("period", savePeriodMs);
            tsb.append("elapsed", System.currentTimeMillis() - lastSave);
            log.debug(tsb.toString());
        }

        // save session if attribute changed or never been saved or time between saves exceeds threshold
        if (data.isDirty() || (lastSave <= 0) || ((System.currentTimeMillis() - lastSave) > savePeriodMs)) {
            // set the last saved time to now
            data.setLastSaved(System.currentTimeMillis());
            try {
                // call the specific store method, passing in previous save time
                doStore(id, data, lastSave);
                data.setDirty(false); // only undo the dirty setting if we saved it
            } catch (Exception e) {
                // reset last save time if save failed
                data.setLastSaved(lastSave);
                throw e;
            }
        }
    }

    @Override
    public Set<String> getExpired(Set<String> candidates) {
        try {
            return doGetExpired(candidates);
        } finally {
            lastExpiryCheckTime = System.currentTimeMillis();
        }
    }

    @Override
    public SessionData newSessionData(String id, long createdTime, long accessedTime, long lastAccessedTime,
                                      long maxInactiveIntervalMS) {
        return new SessionData(id, createdTime, accessedTime, lastAccessedTime, maxInactiveIntervalMS);
    }

    public int getGracePeriodSec() {
        return gracePeriodSec;
    }

    /**
     * Sets the interval in secs to prevent too eager session scavenging.
     *
     * @param sec interval in secs to prevent too eager session scavenging
     */
    public void setGracePeriodSec(int sec) {
        gracePeriodSec = sec;
    }

    public int getSavePeriodSec() {
        return savePeriodSec;
    }

    /**
     * The minimum time in seconds between save operations.
     * Saves normally occur every time the last request
     * exits as session. If nothing changes on the session
     * except for the access time and the persistence technology
     * is slow, this can cause delays.
     *
     * <p>By default the value is 0, which means we save
     * after the last request exists. A non zero value
     * means that we will skip doing the save if the
     * session isn't dirty if the elapsed time since
     * the session was last saved does not exceed this
     * value.</p>
     *
     * @param savePeriodSec the savePeriodSec to set
     */
    public void setSavePeriodSec(int savePeriodSec) {
        this.savePeriodSec = savePeriodSec;
    }

    @Override
    protected void doInitialize() throws Exception {
    }

    @Override
    protected void doDestroy() {
    }

    protected void checkInitialized() throws IllegalStateException {
        if (isInitialized()) {
            throw new IllegalStateException("Already initialized");
        }
    }

}