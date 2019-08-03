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
package com.aspectran.core.context.config;

import com.aspectran.core.context.rule.type.SessionStoreType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;
import com.aspectran.core.util.apon.VariableParameters;

import java.io.IOException;

public class SessionConfig extends AbstractParameters {

    private static final ParameterKey timeout;
    private static final ParameterKey maxSessions;
    private static final ParameterKey evictionPolicy;
    private static final ParameterKey saveOnCreate;
    private static final ParameterKey saveOnInactiveEviction;
    private static final ParameterKey removeUnloadableSessions;
    private static final ParameterKey storeType;
    private static final ParameterKey fileStore;
    private static final ParameterKey startup;

    private static final ParameterKey[] parameterKeys;

    static {
        timeout = new ParameterKey("timeout", ValueType.INT);
        maxSessions = new ParameterKey("maxSessions", ValueType.INT);
        evictionPolicy = new ParameterKey("evictionPolicy", ValueType.INT);
        saveOnCreate = new ParameterKey("saveOnCreate", ValueType.BOOLEAN);
        saveOnInactiveEviction = new ParameterKey("saveOnInactiveEviction", ValueType.BOOLEAN);
        removeUnloadableSessions = new ParameterKey("removeUnloadableSessions", ValueType.BOOLEAN);
        storeType = new ParameterKey("storeType", ValueType.STRING);
        fileStore = new ParameterKey("fileStore", SessionFileStoreConfig.class);
        startup = new ParameterKey("startup", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                timeout,
                maxSessions,
                evictionPolicy,
                saveOnCreate,
                saveOnInactiveEviction,
                removeUnloadableSessions,
                storeType,
                fileStore,
                startup
        };
    }

    public SessionConfig() {
        super(parameterKeys);
    }

    public SessionConfig(String apon) throws IOException {
        super(parameterKeys);
        readFrom(apon);
    }

    public SessionConfig(VariableParameters parameters) throws IOException {
        this(parameters.toString());
    }

    public boolean isStartup() {
        return BooleanUtils.toBoolean(getBoolean(startup));
    }

    public void setStartup(boolean startup) {
        putValue(SessionConfig.startup, startup);
    }

    public int getTimeout() {
        return getInt(timeout, -1);
    }

    public SessionConfig setTimeout(int timeout) {
        putValue(SessionConfig.timeout, timeout);
        return this;
    }

    public int getMaxSessions() {
        return getInt(maxSessions, 0);
    }

    public SessionConfig setMaxSessions(int maxSessions) {
        putValue(SessionConfig.maxSessions, maxSessions);
        return this;
    }

    public boolean hasTimeout() {
        return hasValue(timeout);
    }

    public String getStoreType() {
        return getString(storeType);
    }

    public SessionConfig setStoreType(SessionStoreType sessionStoreType) {
        putValue(storeType, sessionStoreType.toString());
        return this;
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

}
