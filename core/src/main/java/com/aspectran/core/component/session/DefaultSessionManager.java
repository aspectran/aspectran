/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.thread.ScheduledExecutorScheduler;
import com.aspectran.utils.thread.Scheduler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager extends AbstractSessionHandler
        implements SessionManager, ApplicationAdapterAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);

    private static final String UNNAMED_WORKER_PREFIX = "unnamed";

    private static final AtomicInteger uniqueNumberIssuer = new AtomicInteger();

    private ApplicationAdapter applicationAdapter;

    private SessionManagerConfig sessionManagerConfig;

    private SessionStore sessionStore;

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

    public void setSessionStore(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
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
        if (sessionManagerConfig == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing " + getComponentName());
            }
            setWorkerName(UNNAMED_WORKER_PREFIX + uniqueNumberIssuer.getAndIncrement());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing " + ToStringBuilder.toString(getComponentName(), sessionManagerConfig));
            }
            if (sessionManagerConfig.isClusterEnabled()) {
                clusterEnabled = true;
            }
            if (sessionManagerConfig.hasWorkerName()) {
                uniqueNumberIssuer.getAndIncrement();
                setWorkerName(sessionManagerConfig.getWorkerName());
            } else {
                setWorkerName(UNNAMED_WORKER_PREFIX + uniqueNumberIssuer.getAndIncrement());
            }
            if (sessionManagerConfig.hasMaxIdleSeconds()) {
                setDefaultMaxIdleSecs(sessionManagerConfig.getMaxIdleSeconds());
            }
            if (sessionManagerConfig.hasScavengingIntervalSeconds()) {
                scavengingIntervalSeconds = sessionManagerConfig.getScavengingIntervalSeconds();
            } else {
                scavengingIntervalSeconds = 0;
            }
        }

        if (getScheduler() == null) {
            String schedulerName;
            if (getWorkerName() != null) {
                schedulerName = "Scheduler-" + getWorkerName();
            } else {
                schedulerName = String.format("Scheduler@%x", hashCode());
            }
            Scheduler scheduler = new ScheduledExecutorScheduler(schedulerName, false);
            setScheduler(scheduler);
        }

        if (scavengingIntervalSeconds > -1) {
            HouseKeeper houseKeeper = new HouseKeeper(this, scavengingIntervalSeconds);
            setHouseKeeper(houseKeeper);
        }

        if (getSessionIdGenerator() == null) {
            SessionIdGenerator sessionIdGenerator = new SessionIdGenerator(getWorkerName());
            setSessionIdGenerator(sessionIdGenerator);
        }

        if (getSessionCache() == null) {
            if (sessionStore == null && sessionManagerConfig != null) {
                SessionFileStoreConfig fileStoreConfig = sessionManagerConfig.getFileStoreConfig();
                if (fileStoreConfig != null) {
                    FileSessionStoreFactory fileSessionStoreFactory = new FileSessionStoreFactory();
                    fileSessionStoreFactory.setApplicationAdapter(applicationAdapter);
                    String storeDir = fileStoreConfig.getStoreDir();
                    if (StringUtils.hasText(storeDir)) {
                        fileSessionStoreFactory.setStoreDir(storeDir);
                    }
                    if (fileStoreConfig.hasGracePeriodSeconds()) {
                        fileSessionStoreFactory.setGracePeriodSecs(fileStoreConfig.getGracePeriodSeconds());
                    }
                    if (fileStoreConfig.hasSavePeriodSeconds()) {
                        fileSessionStoreFactory.setSavePeriodSecs(fileStoreConfig.getSavePeriodSeconds());
                    }
                    if (fileStoreConfig.hasDeleteUnrestorableFiles()) {
                        fileSessionStoreFactory.setDeleteUnrestorableFiles(fileStoreConfig.isDeleteUnrestorableFiles());
                    }
                    if (fileStoreConfig.hasNonPersistentAttributes()) {
                        fileSessionStoreFactory.setNonPersistentAttributes(fileStoreConfig.getNonPersistentAttributes());
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
