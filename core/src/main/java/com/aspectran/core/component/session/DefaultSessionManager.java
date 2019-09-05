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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.context.rule.type.SessionStoreType;

import java.io.IOException;

/**
 * Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager extends AbstractSessionHandler implements SessionManager {

    private ApplicationAdapter applicationAdapter;

    private SessionManagerConfig sessionManagerConfig;

    public DefaultSessionManager() {
        super();
    }

    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    @Override
    public SessionManagerConfig getSessionManagerConfig() {
        return sessionManagerConfig;
    }

    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) {
        this.sessionManagerConfig = sessionManagerConfig;
    }

    public void setSessionManagerConfigWithApon(String apon) {
        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
        try {
            sessionManagerConfig.readFrom(apon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setSessionManagerConfig(sessionManagerConfig);
    }

    @Override
    public SessionHandler getSessionHandler() {
        if (!isInitialized()) {
            throw new IllegalStateException("DefaultSessionManager is not yet initialized");
        }
        return this;
    }

    @Override
    protected void doInitialize() throws Exception {
        if (sessionManagerConfig != null) {
            if (sessionManagerConfig.hasWorkerName()) {
                setWorkerName(sessionManagerConfig.getWorkerName());
            } else {
                setWorkerName("node0");
            }
            if (sessionManagerConfig.hasMaxIdleSeconds()) {
                int secs = sessionManagerConfig.getMaxIdleSeconds();
                setDefaultMaxIdleSecs(secs);
            }
            if (sessionManagerConfig.hasScavengingIntervalSeconds()) {
                int secs = sessionManagerConfig.getScavengingIntervalSeconds();
                if (secs > 0) {
                    HouseKeeper houseKeeper = new HouseKeeper(this);
                    houseKeeper.setScavengingInterval(secs);
                    setHouseKeeper(houseKeeper);
                }
            } else {
                setHouseKeeper(new HouseKeeper(this));
            }
        }

        if (getSessionIdGenerator() == null) {
            SessionIdGenerator sessionIdGenerator = new SessionIdGenerator(getWorkerName());
            setSessionIdGenerator(sessionIdGenerator);
        }

        if (getSessionCache() == null) {
            SessionDataStore sessionDataStore = null;
            if (sessionManagerConfig != null) {
                String storeType = sessionManagerConfig.getStoreType();
                SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
                if (storeType != null && sessionStoreType == null) {
                    throw new IllegalArgumentException("Unknown session store type: " + storeType);
                }
                if (sessionStoreType == SessionStoreType.FILE) {
                    sessionDataStore = SessionDataStoreFactory.createFileSessionDataStore(
                        sessionManagerConfig.getFileStoreConfig(), getApplicationAdapter());
                }
            }

            DefaultSessionCache sessionCache = new DefaultSessionCache(this, sessionDataStore);
            if (sessionManagerConfig != null) {
                if (sessionManagerConfig.hasMaxSessions()) {
                    int maxSessions = sessionManagerConfig.getMaxSessions();
                    sessionCache.setMaxSessions(maxSessions);
                }
                if (sessionManagerConfig.hasEvictionIdleSeconds()) {
                    int secs = sessionManagerConfig.getEvictionIdleSeconds();
                    sessionCache.setEvictionIdleSecs(secs);
                }
                if (sessionManagerConfig.hasSaveOnCreate()) {
                    boolean saveOnCreate = sessionManagerConfig.getSaveOnCreate();
                    sessionCache.setSaveOnCreate(saveOnCreate);
                }
                if (sessionManagerConfig.hasSaveOnInactiveEviction()) {
                    boolean saveOnInactiveEviction = sessionManagerConfig.getSaveOnInactiveEviction();
                    sessionCache.setSaveOnInactiveEviction(saveOnInactiveEviction);
                }
                if (sessionManagerConfig.hasRemoveUnloadableSessions()) {
                    boolean removeUnloadableSessions = sessionManagerConfig.getRemoveUnloadableSessions();
                    sessionCache.setRemoveUnloadableSessions(removeUnloadableSessions);
                }
            }
            sessionCache.initialize();
            setSessionCache(sessionCache);
        }

        super.doInitialize();
    }

}
