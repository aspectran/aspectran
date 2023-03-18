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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.util.StringUtils;

import java.io.IOException;

/**
 * Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager extends AbstractSessionHandler
        implements SessionManager, ApplicationAdapterAware, DisposableBean {

    private ApplicationAdapter applicationAdapter;

    private SessionManagerConfig sessionManagerConfig;

    private SessionStoreFactory sessionStoreFactory;

    public DefaultSessionManager() {
        super();
    }

    public DefaultSessionManager(String workerName) {
        super();
        setWorkerName(workerName);
    }

    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
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

    public void setSessionStoreFactory(SessionStoreFactory sessionStoreFactory) {
        this.sessionStoreFactory = sessionStoreFactory;
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
        boolean clusterEnabled = false;
        if (sessionManagerConfig != null) {
            if (sessionManagerConfig.isClusterEnabled()) {
                clusterEnabled = true;
            }
            if (sessionManagerConfig.hasWorkerName()) {
                setWorkerName(sessionManagerConfig.getWorkerName());
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
            SessionStore sessionStore = null;
            if (sessionStoreFactory != null) {
                sessionStore = sessionStoreFactory.getSessionStore();
            } else if (sessionManagerConfig != null) {
                SessionFileStoreConfig fileStoreConfig = sessionManagerConfig.getFileStoreConfig();
                if (fileStoreConfig != null) {
                    FileSessionStoreFactory fileSessionStoreFactory = new FileSessionStoreFactory();
                    fileSessionStoreFactory.setApplicationAdapter(applicationAdapter);
                    String storeDir = fileStoreConfig.getStoreDir();
                    if (StringUtils.hasText(storeDir)) {
                        fileSessionStoreFactory.setStoreDir(storeDir);
                    }
                    boolean deleteUnrestorableFiles = fileStoreConfig.isDeleteUnrestorableFiles();
                    if (deleteUnrestorableFiles) {
                        fileSessionStoreFactory.setDeleteUnrestorableFiles(true);
                    }
                    String[] nonPersistentAttributes = fileStoreConfig.getNonPersistentAttributes();
                    if (nonPersistentAttributes != null) {
                        fileSessionStoreFactory.setNonPersistentAttributes(nonPersistentAttributes);
                    }
                    sessionStoreFactory = fileSessionStoreFactory;
                    sessionStore = fileSessionStoreFactory.getSessionStore();
                }
            }

            DefaultSessionCache sessionCache = new DefaultSessionCache(this, sessionStore, clusterEnabled);
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
