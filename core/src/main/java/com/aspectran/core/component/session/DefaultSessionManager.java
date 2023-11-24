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
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.thread.ScheduledExecutorScheduler;
import com.aspectran.core.util.thread.Scheduler;

import java.io.IOException;

/**
 * Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager extends AbstractSessionHandler
        implements SessionManager, ApplicationAdapterAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);

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
        if (sessionManagerConfig != null) {
            if (logger.isDebugEnabled()) {
                ToStringBuilder tsb = new ToStringBuilder("Configuring SessionManager", sessionManagerConfig);
                logger.debug(tsb.toString());
            }
        }
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
        int scavengingIntervalSeconds = -1;
        if (sessionManagerConfig != null) {
            if (sessionManagerConfig.isClusterEnabled()) {
                clusterEnabled = true;
            }
            if (sessionManagerConfig.hasWorkerName()) {
                setWorkerName(sessionManagerConfig.getWorkerName());
            }
            if (sessionManagerConfig.hasMaxIdleSeconds()) {
                setDefaultMaxIdleSecs(sessionManagerConfig.getMaxIdleSeconds());
            }
            if (sessionManagerConfig.hasScavengingIntervalSeconds()) {
                scavengingIntervalSeconds = sessionManagerConfig.getScavengingIntervalSeconds();
            }
        }

        if (getScheduler() == null) {
            String schedulerName;
            if (getWorkerName() != null) {
                schedulerName = "SessionScheduler-" + getWorkerName();
            } else {
                schedulerName = String.format("SessionScheduler-%x", hashCode());
            }
            Scheduler scheduler = new ScheduledExecutorScheduler(schedulerName, false);
            setScheduler(scheduler);
        }

        if (scavengingIntervalSeconds >= 0) {
            HouseKeeper houseKeeper = new HouseKeeper(this);
            if (scavengingIntervalSeconds > 0) {
                houseKeeper.setScavengingInterval(scavengingIntervalSeconds);
            }
            setHouseKeeper(houseKeeper);
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
                    sessionStore = fileSessionStoreFactory.getSessionStore();
                }
            }

            DefaultSessionCache sessionCache = new DefaultSessionCache(this, sessionStore, clusterEnabled);
            if (sessionManagerConfig != null) {
                if (sessionManagerConfig.hasMaxActiveSessions()) {
                    int maxActiveSessions = sessionManagerConfig.getMaxActiveSessions();
                    sessionCache.setMaxActiveSessions(maxActiveSessions);
                }
                if (sessionManagerConfig.hasEvictionIdleSeconds()) {
                    int secs = sessionManagerConfig.getEvictionIdleSeconds();
                    if (sessionStore == null && secs != SessionCache.NEVER_EVICT) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Fixed to evictionIdleSeconds=-1 because there is no session store");
                        }
                        secs = SessionCache.NEVER_EVICT;
                    }
                    sessionCache.setEvictionIdleSecs(secs);
                }
                if (sessionManagerConfig.hasSaveOnCreate()) {
                    boolean saveOnCreate = sessionManagerConfig.getSaveOnCreate();
                    if (sessionStore == null && saveOnCreate) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Fixed to saveOnCreate=false because there is no session store");
                        }
                        saveOnCreate = false;
                    }
                    sessionCache.setSaveOnCreate(saveOnCreate);
                }
                if (sessionManagerConfig.hasSaveOnInactiveEviction()) {
                    boolean saveOnInactiveEviction = sessionManagerConfig.getSaveOnInactiveEviction();
                    if (sessionStore == null && saveOnInactiveEviction) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Fixed to saveOnInactiveEviction=false because there is no session store");
                        }
                        saveOnInactiveEviction = false;
                    }
                    sessionCache.setSaveOnInactiveEviction(saveOnInactiveEviction);
                }
                if (sessionManagerConfig.hasRemoveUnloadableSessions()) {
                    boolean removeUnloadableSessions = sessionManagerConfig.getRemoveUnloadableSessions();
                    if (sessionStore == null && removeUnloadableSessions) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Fixed to removeUnloadableSessions=false because there is no session store");
                        }
                        removeUnloadableSessions = false;
                    }
                    sessionCache.setRemoveUnloadableSessions(removeUnloadableSessions);
                } else if (sessionStore != null) {
                    sessionCache.setRemoveUnloadableSessions(true);
                }
            }
            sessionCache.initialize();
            setSessionCache(sessionCache);
        }

        super.doInitialize();
    }

}
