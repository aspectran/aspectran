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
package com.aspectran.core.context.config;

import com.aspectran.core.component.session.SessionCache;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.AponParseException;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;
import com.aspectran.core.util.apon.VariableParameters;

public class SessionManagerConfig extends AbstractParameters {

    private static final ParameterKey workerName;
    private static final ParameterKey maxActiveSessions;
    private static final ParameterKey maxIdleSeconds;
    private static final ParameterKey evictionIdleSeconds;
    private static final ParameterKey scavengingIntervalSeconds;
    private static final ParameterKey saveOnCreate;
    private static final ParameterKey saveOnInactiveEviction;
    private static final ParameterKey removeUnloadableSessions;
    private static final ParameterKey fileStore;
    private static final ParameterKey clusterEnabled;
    private static final ParameterKey enabled;

    private static final ParameterKey[] parameterKeys;

    static {
        workerName = new ParameterKey("workerName", ValueType.STRING);
        maxActiveSessions = new ParameterKey("maxActiveSessions", ValueType.INT);
        maxIdleSeconds = new ParameterKey("maxIdleSeconds", ValueType.INT);
        evictionIdleSeconds = new ParameterKey("evictionIdleSeconds", ValueType.INT);
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
                scavengingIntervalSeconds,
                saveOnCreate,
                saveOnInactiveEviction,
                removeUnloadableSessions,
                fileStore,
                clusterEnabled,
                enabled
        };
    }

    public SessionManagerConfig() {
        super(parameterKeys);
    }

    public SessionManagerConfig(String apon) throws AponParseException {
        super(parameterKeys);
        readFrom(apon);
    }

    public SessionManagerConfig(VariableParameters parameters) throws AponParseException {
        this();
        readFrom(parameters);
    }

    public String getWorkerName() {
        return getString(workerName);
    }

    public SessionManagerConfig setWorkerName(String workerName) {
        putValue(SessionManagerConfig.workerName, workerName);
        return this;
    }

    public boolean hasWorkerName() {
        return hasValue(workerName);
    }

    public int getMaxActiveSessions() {
        return getInt(maxActiveSessions, 0);
    }

    public SessionManagerConfig setMaxActiveSessions(int maxActiveSessions) {
        putValue(SessionManagerConfig.maxActiveSessions, maxActiveSessions);
        return this;
    }

    public boolean hasMaxActiveSessions() {
        return hasValue(maxActiveSessions);
    }

    public int getMaxIdleSeconds() {
        return getInt(maxIdleSeconds, -1);
    }

    public SessionManagerConfig setMaxIdleSeconds(int maxIdleSeconds) {
        putValue(SessionManagerConfig.maxIdleSeconds, maxIdleSeconds);
        return this;
    }

    public boolean hasMaxIdleSeconds() {
        return hasValue(maxIdleSeconds);
    }

    public int getEvictionIdleSeconds() {
        return getInt(evictionIdleSeconds, SessionCache.NEVER_EVICT);
    }

    public SessionManagerConfig setEvictionIdleSeconds(int evictionIdleSeconds) {
        putValue(SessionManagerConfig.evictionIdleSeconds, evictionIdleSeconds);
        return this;
    }

    public boolean hasEvictionIdleSeconds() {
        return hasValue(evictionIdleSeconds);
    }

    public int getScavengingIntervalSeconds() {
        return getInt(scavengingIntervalSeconds, 0);
    }

    public SessionManagerConfig setScavengingIntervalSeconds(int scavengingIntervalSeconds) {
        putValue(SessionManagerConfig.scavengingIntervalSeconds, scavengingIntervalSeconds);
        return this;
    }

    public boolean hasScavengingIntervalSeconds() {
        return hasValue(scavengingIntervalSeconds);
    }

    public boolean getSaveOnCreate() {
        return getBoolean(saveOnCreate, false);
    }

    public SessionManagerConfig setSaveOnCreate(boolean saveOnCreate) {
        putValue(SessionManagerConfig.saveOnCreate, saveOnCreate);
        return this;
    }

    public boolean hasSaveOnCreate() {
        return hasValue(saveOnCreate);
    }

    public boolean getSaveOnInactiveEviction() {
        return getBoolean(saveOnInactiveEviction, false);
    }

    public SessionManagerConfig setSaveOnInactiveEviction(boolean saveOnInactiveEviction) {
        putValue(SessionManagerConfig.saveOnInactiveEviction, saveOnInactiveEviction);
        return this;
    }

    public boolean hasSaveOnInactiveEviction() {
        return hasValue(saveOnInactiveEviction);
    }

    public boolean getRemoveUnloadableSessions() {
        return getBoolean(removeUnloadableSessions, false);
    }

    public SessionManagerConfig setRemoveUnloadableSessions(boolean removeUnloadableSessions) {
        putValue(SessionManagerConfig.removeUnloadableSessions, removeUnloadableSessions);
        return this;
    }

    public boolean hasRemoveUnloadableSessions() {
        return hasValue(removeUnloadableSessions);
    }

    public SessionFileStoreConfig getFileStoreConfig() {
        return getParameters(fileStore);
    }

    public SessionFileStoreConfig newFileStoreConfig() {
        return newParameters(fileStore);
    }

    public SessionFileStoreConfig touchFileStoreConfig() {
        return touchParameters(fileStore);
    }

    public boolean isClusterEnabled() {
        return getBoolean(clusterEnabled, false);
    }

    public SessionManagerConfig setClusterEnabled(boolean clusterEnabled) {
        putValue(SessionManagerConfig.clusterEnabled, clusterEnabled);
        return this;
    }

    public boolean isEnabled() {
        return getBoolean(enabled, true);
    }

    public SessionManagerConfig setEnabled(boolean enabled) {
        putValue(SessionManagerConfig.enabled, enabled);
        return this;
    }

}
