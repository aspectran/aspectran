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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.scheduling.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default, concrete implementation of the {@link SessionManager} interface.
 *
 * <p>This class is responsible for reading a {@link SessionManagerConfig},
 * initializing and wiring together all the necessary session components such as
 * {@link SessionCache}, {@link SessionStore}, {@link HouseKeeper}, and
 * {@link SessionIdGenerator}. It manages the complete lifecycle of the session
 * management system.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager
        extends AbstractSessionManager implements ActivityContextAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);

    private static final String UNNAMED_WORKER_PREFIX = "unnamed";

    private static final AtomicInteger uniqueNumberIssuer = new AtomicInteger();

    private ApplicationAdapter applicationAdapter;

    private ClassLoader classLoader;

    private SessionManagerConfig sessionManagerConfig;

    private SessionStore sessionStore;

    public DefaultSessionManager() {
        super();
    }

    public DefaultSessionManager(String workerName) {
        super();
        setWorkerName(workerName);
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        checkInitializable();
        this.applicationAdapter = context.getApplicationAdapter();
        this.classLoader = context.getClassLoader();
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        checkInitializable();
        this.classLoader = classLoader;
    }

    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    public SessionManagerConfig getSessionManagerConfig() {
        return sessionManagerConfig;
    }

    /**
     * Sets the session manager configuration.
     * This is typically injected by the Aspectran container.
     * @param sessionManagerConfig the session manager configuration
     */
    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) {
        checkInitializable();
        this.sessionManagerConfig = sessionManagerConfig;
    }

    /**
     * Sets the session manager configuration from an APON-formatted string.
     * @param apon the APON string containing the configuration
     */
    public void setSessionManagerConfigWithApon(String apon) {
        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
        try {
            sessionManagerConfig.readFrom(apon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setSessionManagerConfig(sessionManagerConfig);
    }

    /**
     * Sets the session store to be used for persistence.
     * This is typically injected by the Aspectran container, often using a factory bean.
     * @param sessionStore the session store implementation
     */
    public void setSessionStore(SessionStore sessionStore) {
        checkInitializable();
        this.sessionStore = sessionStore;
    }

    /**
     * Initializes the session manager and all its components based on the provided configuration.
     * This method performs the following steps:
     * <ol>
     *   <li>Reads settings from {@link SessionManagerConfig}.</li>
     *   <li>Sets the worker name and session timeout policies.</li>
     *   <li>Initializes the {@link Scheduler} for background tasks.</li>
     *   <li>Creates and starts the {@link HouseKeeper} for session scavenging.</li>
     *   <li>Initializes the {@link SessionIdGenerator}.</li>
     *   <li>Conditionally creates a {@link FileSessionStore} from configuration if no other store is provided.</li>
     *   <li>Creates and configures the {@link DefaultSessionCache} with eviction and persistence policies.</li>
     *   <li>Completes the lifecycle initialization.</li>
     * </ol>
     * @throws Exception if an error occurs during initialization
     */
    @Override
    protected void doInitialize() throws Exception {
        boolean clusterEnabled = false;
        int scavengingIntervalSeconds = -1;
        if (sessionManagerConfig == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing {}", getComponentName());
            }
            setWorkerName(UNNAMED_WORKER_PREFIX + uniqueNumberIssuer.getAndIncrement());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing {}", ToStringBuilder.toString(getComponentName(), sessionManagerConfig));
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
                setDefaultMaxIdleSecs(Math.max(sessionManagerConfig.getMaxIdleSeconds(), -1));
            }
            if (sessionManagerConfig.hasMaxIdleSecondsForNew()) {
                setMaxIdleSecsForNew(Math.max(sessionManagerConfig.getMaxIdleSecondsForNew(), -1));
            }
            if (sessionManagerConfig.hasScavengingIntervalSeconds()) {
                scavengingIntervalSeconds = Math.max(sessionManagerConfig.getScavengingIntervalSeconds(), 0);
            } else {
                scavengingIntervalSeconds = 0;
            }
        }

        if (getScheduler() == null) {
            String schedulerName;
            if (getWorkerName() != null) {
                schedulerName = "SM Worker(" + getWorkerName() + ")";
            } else {
                schedulerName = String.format("SM Worker(@%x)", hashCode());
            }
            Scheduler scheduler = new SessionScheduler(schedulerName, classLoader);
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
                    fileSessionStoreFactory.setApplicationAdapter(getApplicationAdapter());
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
                    sessionStore = fileSessionStoreFactory.createSessionStore();
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
                    sessionCache.setEvictionIdleSecs(Math.max(secs, -1));
                }
                if (sessionManagerConfig.hasEvictionIdleSecondsForNew()) {
                    int secs = sessionManagerConfig.getEvictionIdleSecondsForNew();
                    if (sessionStore == null && secs != SessionCache.NEVER_EVICT) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Fixed to evictionIdleSecondsForNew=0 because there is no session store");
                        }
                        secs = SessionCache.NEVER_EVICT;
                    }
                    sessionCache.setEvictionIdleSecsForNew(Math.max(secs, -1));
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
            setSessionCache(sessionCache);
        }

        super.doInitialize();
    }

}
