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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.context.rule.type.SessionStoreType;
import com.aspectran.core.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager extends AbstractSessionHandler implements SessionManager {

    private String workerName;

    private SessionManagerConfig sessionManagerConfig;

    public DefaultSessionManager() {
        super();
    }

    @Override
    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
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
        if (getSessionIdGenerator() == null) {
            SessionIdGenerator sessionIdGenerator = new SessionIdGenerator(workerName);
            setSessionIdGenerator(sessionIdGenerator);
        }

        if (getSessionCache() == null) {
            DefaultSessionCache sessionCache = new DefaultSessionCache(this);
            if (sessionManagerConfig != null) {
                sessionCache.setMaxSessions(sessionManagerConfig.getMaxSessions());
            }
            setSessionCache(sessionCache);
        }

        if (sessionManagerConfig != null) {
            if (sessionManagerConfig.hasTimeout()) {
                int timeout = sessionManagerConfig.getTimeout();
                setDefaultMaxIdleSecs(timeout);
            }
            if (getSessionCache().getSessionDataStore() == null) {
                String storeType = sessionManagerConfig.getStoreType();
                SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
                if (storeType != null && sessionStoreType == null) {
                    throw new IllegalArgumentException("Unknown session store type: " + storeType);
                }
                if (sessionStoreType == SessionStoreType.FILE) {
                    FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
                    SessionFileStoreConfig fileStoreConfig = sessionManagerConfig.getFileStoreConfig();
                    if (fileStoreConfig != null) {
                        String storeDir = fileStoreConfig.getStoreDir();
                        if (StringUtils.hasText(storeDir)) {
                            fileSessionDataStore.setStoreDir(new File(storeDir));
                        }
                        boolean deleteUnrestorableFiles = fileStoreConfig.isDeleteUnrestorableFiles();
                        if (deleteUnrestorableFiles) {
                            fileSessionDataStore.setDeleteUnrestorableFiles(true);
                        }
                    }
                    fileSessionDataStore.initialize();
                    getSessionCache().setSessionDataStore(fileSessionDataStore);
                }
            }
        }

        super.doInitialize();
    }

    @Override
    protected void doDestroy() throws Exception {
        getSessionCache().clear();
        super.doDestroy();
    }

    public static DefaultSessionManager create(ActivityContext context, SessionManagerConfig sessionManagerConfig,
                                               String workerName) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }

        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setWorkerName(workerName);
        if (sessionManagerConfig != null) {
            sessionManager.setSessionManagerConfig(sessionManagerConfig);
            String storeType = sessionManagerConfig.getStoreType();
            SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
            if (sessionStoreType == SessionStoreType.FILE) {
                SessionFileStoreConfig fileStoreConfig = sessionManagerConfig.getFileStoreConfig();
                if (fileStoreConfig != null) {
                    String storeDir = fileStoreConfig.getStoreDir();
                    if (StringUtils.hasText(storeDir)) {
                        String basePath = context.getApplicationAdapter().getBasePath();
                        String canonPath = new File(basePath, storeDir).getCanonicalPath();
                        fileStoreConfig.setStoreDir(canonPath);
                    }
                }
            }
        }
        return sessionManager;
    }

}
