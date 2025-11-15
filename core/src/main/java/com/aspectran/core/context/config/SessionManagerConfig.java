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
package com.aspectran.core.context.config;

import com.aspectran.core.component.session.SessionCache;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;
import com.aspectran.utils.apon.VariableParameters;

/**
 * Configuration for the session manager.
 * <p>This class holds settings for session timeout, persistence, clustering, etc.</p>
 */
public class SessionManagerConfig extends DefaultParameters {

    /** The name of the session worker. */
    private static final ParameterKey workerName;

    /** The maximum number of active sessions. */
    private static final ParameterKey maxActiveSessions;

    /** The maximum time in seconds a session can be idle before it is invalidated. */
    private static final ParameterKey maxIdleSeconds;

    /** The time in seconds after which an idle session is eligible for eviction. */
    private static final ParameterKey evictionIdleSeconds;

    /** The maximum time in seconds a new session can be idle. */
    private static final ParameterKey maxIdleSecondsForNew;

    /** The time in seconds after which a new idle session is eligible for eviction. */
    private static final ParameterKey evictionIdleSecondsForNew;

    /** The interval in seconds at which to scavenge for expired sessions. */
    private static final ParameterKey scavengingIntervalSeconds;

    /** Whether to save session data on creation. */
    private static final ParameterKey saveOnCreate;

    /** Whether to save session data when it is evicted for being inactive. */
    private static final ParameterKey saveOnInactiveEviction;

    /** Whether to remove session files that cannot be restored. */
    private static final ParameterKey removeUnloadableSessions;

    /** The configuration for the file-based session store. */
    private static final ParameterKey fileStore;

    /** Whether session clustering is enabled. */
    private static final ParameterKey clusterEnabled;

    /** Whether the session manager is enabled. */
    private static final ParameterKey enabled;

    private static final ParameterKey[] parameterKeys;

    static {
        workerName = new ParameterKey("workerName", ValueType.STRING);
        maxActiveSessions = new ParameterKey("maxActiveSessions", ValueType.INT);
        maxIdleSeconds = new ParameterKey("maxIdleSeconds", ValueType.INT);
        evictionIdleSeconds = new ParameterKey("evictionIdleSeconds", ValueType.INT);
        maxIdleSecondsForNew = new ParameterKey("maxIdleSecondsForNew", ValueType.INT);
        evictionIdleSecondsForNew = new ParameterKey("evictionIdleSecondsForNew", ValueType.INT);
        scavengingIntervalSeconds = new ParameterKey("scavengingIntervalSeconds", ValueType.INT);
        saveOnCreate = new ParameterKey("saveOnCreate", ValueType.BOOLEAN);
        saveOnInactiveEviction = new ParameterKey("saveOnInactiveEviction", ValueType.BOOLEAN);
        removeUnloadableSessions = new ParameterKey("removeUnloadableSessions", ValueType.BOOLEAN);
        fileStore = new ParameterKey("fileStore", SessionFileStoreConfig.class);
        clusterEnabled = new ParameterKey("clusterEnabled", ValueType.BOOLEAN);
        enabled = new ParameterKey("enabled", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                workerName,
                maxActiveSessions,
                maxIdleSeconds,
                evictionIdleSeconds,
                maxIdleSecondsForNew,
                evictionIdleSecondsForNew,
                scavengingIntervalSeconds,
                saveOnCreate,
                saveOnInactiveEviction,
                removeUnloadableSessions,
                fileStore,
                clusterEnabled,
                enabled
        };
    }

    /**
     * Instantiates a new SessionManagerConfig.
     */
    public SessionManagerConfig() {
        super(parameterKeys);
    }

    /**
     * Instantiates a new SessionManagerConfig with the specified APON text.
     * @param apon the APON text
     * @throws AponParseException if the APON text is invalid
     */
    public SessionManagerConfig(String apon) throws AponParseException {
        super(parameterKeys);
        readFrom(apon);
    }

    /**
     * Instantiates a new SessionManagerConfig with the specified parameters.
     * @param parameters the parameters
     * @throws AponParseException if the parameters are invalid
     */
    public SessionManagerConfig(VariableParameters parameters) throws AponParseException {
        this();
        readFrom(parameters);
    }

    /**
     * Returns whether the worker name is set.
     * @return true if the worker name is set, false otherwise
     */
    public boolean hasWorkerName() {
        return hasValue(workerName);
    }

    /**
     * Returns the name of the session worker.
     * @return the worker name
     */
    public String getWorkerName() {
        return getString(workerName);
    }

    /**
     * Sets the name of the session worker.
     * @param workerName the worker name
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setWorkerName(String workerName) {
        putValue(SessionManagerConfig.workerName, workerName);
        return this;
    }

    /**
     * Returns whether the maximum number of active sessions is set.
     * @return true if the max active sessions is set, false otherwise
     */
    public boolean hasMaxActiveSessions() {
        return hasValue(maxActiveSessions);
    }

    /**
     * Returns the maximum number of active sessions.
     * @return the max active sessions
     */
    public int getMaxActiveSessions() {
        return getInt(maxActiveSessions, 0);
    }

    /**
     * Sets the maximum number of active sessions.
     * @param maxActiveSessions the max active sessions
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setMaxActiveSessions(int maxActiveSessions) {
        putValue(SessionManagerConfig.maxActiveSessions, maxActiveSessions);
        return this;
    }

    /**
     * Returns whether the maximum idle time is set.
     * @return true if the max idle time is set, false otherwise
     */
    public boolean hasMaxIdleSeconds() {
        return hasValue(maxIdleSeconds);
    }

    /**
     * Returns the maximum time in seconds a session can be idle.
     * @return the max idle time in seconds
     */
    public int getMaxIdleSeconds() {
        return getInt(maxIdleSeconds, -1);
    }

    /**
     * Sets the maximum time in seconds a session can be idle.
     * @param maxIdleSeconds the max idle time in seconds
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setMaxIdleSeconds(int maxIdleSeconds) {
        putValue(SessionManagerConfig.maxIdleSeconds, maxIdleSeconds);
        return this;
    }

    /**
     * Returns whether the eviction time for idle sessions is set.
     * @return true if the eviction time is set, false otherwise
     */
    public boolean hasEvictionIdleSeconds() {
        return hasValue(evictionIdleSeconds);
    }

    /**
     * Returns the time in seconds after which an idle session is eligible for eviction.
     * @return the eviction time in seconds
     */
    public int getEvictionIdleSeconds() {
        return getInt(evictionIdleSeconds, SessionCache.NEVER_EVICT);
    }

    /**
     * Sets the time in seconds after which an idle session is eligible for eviction.
     * @param evictionIdleSeconds the eviction time in seconds
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setEvictionIdleSeconds(int evictionIdleSeconds) {
        putValue(SessionManagerConfig.evictionIdleSeconds, evictionIdleSeconds);
        return this;
    }

    /**
     * Returns whether the maximum idle time for new sessions is set.
     * @return true if the max idle time for new sessions is set, false otherwise
     */
    public boolean hasMaxIdleSecondsForNew() {
        return hasValue(maxIdleSecondsForNew);
    }

    /**
     * Returns the maximum time in seconds a new session can be idle.
     * @return the max idle time in seconds for new sessions
     */
    public int getMaxIdleSecondsForNew() {
        return getInt(maxIdleSecondsForNew, 0);
    }

    /**
     * Sets the maximum time in seconds a new session can be idle.
     * @param maxIdleSecondsForNew the max idle time in seconds for new sessions
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setMaxIdleSecondsForNew(int maxIdleSecondsForNew) {
        putValue(SessionManagerConfig.maxIdleSecondsForNew, maxIdleSecondsForNew);
        return this;
    }

    /**
     * Returns whether the eviction time for new idle sessions is set.
     * @return true if the eviction time is set, false otherwise
     */
    public boolean hasEvictionIdleSecondsForNew() {
        return hasValue(evictionIdleSecondsForNew);
    }

    /**
     * Returns the time in seconds after which a new idle session is eligible for eviction.
     * @return the eviction time in seconds for new sessions
     */
    public int getEvictionIdleSecondsForNew() {
        return getInt(evictionIdleSecondsForNew, SessionCache.NEVER_EVICT);
    }

    /**
     * Sets the time in seconds after which a new idle session is eligible for eviction.
     * @param evictionIdleSecondsForNew the eviction time in seconds for new sessions
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setEvictionIdleSecondsForNew(int evictionIdleSecondsForNew) {
        putValue(SessionManagerConfig.evictionIdleSecondsForNew, evictionIdleSecondsForNew);
        return this;
    }

    /**
     * Returns whether the scavenging interval is set.
     * @return true if the scavenging interval is set, false otherwise
     */
    public boolean hasScavengingIntervalSeconds() {
        return hasValue(scavengingIntervalSeconds);
    }

    /**
     * Returns the interval in seconds at which to scavenge for expired sessions.
     * @return the scavenging interval in seconds
     */
    public int getScavengingIntervalSeconds() {
        return getInt(scavengingIntervalSeconds, 0);
    }

    /**
     * Sets the interval in seconds at which to scavenge for expired sessions.
     * @param scavengingIntervalSeconds the scavenging interval in seconds
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setScavengingIntervalSeconds(int scavengingIntervalSeconds) {
        putValue(SessionManagerConfig.scavengingIntervalSeconds, scavengingIntervalSeconds);
        return this;
    }

    /**
     * Returns whether to save session data on creation.
     * @return true to save on creation, false otherwise
     */
    public boolean hasSaveOnCreate() {
        return hasValue(saveOnCreate);
    }

    /**
     * Returns whether to save session data on creation.
     * @return true to save on creation, false otherwise
     */
    public boolean getSaveOnCreate() {
        return getBoolean(saveOnCreate, false);
    }

    /**
     * Sets whether to save session data on creation.
     * @param saveOnCreate true to save on creation, false otherwise
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setSaveOnCreate(boolean saveOnCreate) {
        putValue(SessionManagerConfig.saveOnCreate, saveOnCreate);
        return this;
    }

    /**
     * Returns whether to save session data on inactive eviction.
     * @return true to save on eviction, false otherwise
     */
    public boolean hasSaveOnInactiveEviction() {
        return hasValue(saveOnInactiveEviction);
    }

    /**
     * Returns whether to save session data when it is evicted for being inactive.
     * @return true to save on eviction, false otherwise
     */
    public boolean getSaveOnInactiveEviction() {
        return getBoolean(saveOnInactiveEviction, false);
    }

    /**
     * Sets whether to save session data when it is evicted for being inactive.
     * @param saveOnInactiveEviction true to save on eviction, false otherwise
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setSaveOnInactiveEviction(boolean saveOnInactiveEviction) {
        putValue(SessionManagerConfig.saveOnInactiveEviction, saveOnInactiveEviction);
        return this;
    }

    /**
     * Returns whether to remove unloadable sessions.
     * @return true to remove unloadable sessions, false otherwise
     */
    public boolean hasRemoveUnloadableSessions() {
        return hasValue(removeUnloadableSessions);
    }

    /**
     * Returns whether to remove session files that cannot be restored.
     * @return true to remove unloadable sessions, false otherwise
     */
    public boolean getRemoveUnloadableSessions() {
        return getBoolean(removeUnloadableSessions, false);
    }

    /**
     * Sets whether to remove session files that cannot be restored.
     * @param removeUnloadableSessions true to remove unloadable sessions, false otherwise
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setRemoveUnloadableSessions(boolean removeUnloadableSessions) {
        putValue(SessionManagerConfig.removeUnloadableSessions, removeUnloadableSessions);
        return this;
    }

    /**
     * Returns the configuration for the file-based session store.
     * @return the {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig getFileStoreConfig() {
        return getParameters(fileStore);
    }

    /**
     * Returns the existing configuration for the file-based session store
     * or creates a new one if it does not exist.
     * @return a non-null {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig touchFileStoreConfig() {
        return touchParameters(fileStore);
    }

    /**
     * Returns whether session clustering is enabled.
     * @return true if clustering is enabled, false otherwise
     */
    public boolean isClusterEnabled() {
        return getBoolean(clusterEnabled, false);
    }

    /**
     * Sets whether session clustering is enabled.
     * @param clusterEnabled true to enable clustering, false otherwise
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setClusterEnabled(boolean clusterEnabled) {
        putValue(SessionManagerConfig.clusterEnabled, clusterEnabled);
        return this;
    }

    /**
     * Returns whether the session manager is enabled.
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return getBoolean(enabled, true);
    }

    /**
     * Sets whether the session manager is enabled.
     * @param enabled true to enable, false otherwise
     * @return this {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig setEnabled(boolean enabled) {
        putValue(SessionManagerConfig.enabled, enabled);
        return this;
    }

}
