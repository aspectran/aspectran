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

import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.SessionConfig;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.rule.type.SessionStoreType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.VariableParameters;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager extends AbstractSessionHandler implements SessionManager {

    private final ActivityContext context;

    private String workerName;

    private SessionConfig sessionConfig;

    private SessionDataStore sessionDataStore;

    public DefaultSessionManager() {
        this(null);
    }

    public DefaultSessionManager(ActivityContext context) {
        this.context = context;
    }

    @Override
    public String getWorkerName() {
        return workerName;
    }

    @Override
    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    @Override
    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    @Override
    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public void setSessionConfig(VariableParameters parameters) {
        SessionConfig sessionConfig = new SessionConfig();
        try {
            sessionConfig.readFrom(parameters.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setSessionConfig(sessionConfig);
    }

    @Override
    public SessionDataStore getSessionDataStore() {
        return sessionDataStore;
    }

    @Override
    public void setSessionDataStore(SessionDataStore sessionDataStore) {
        this.sessionDataStore = sessionDataStore;
    }

    @Override
    public SessionHandler getSessionHandler() {
        return this;
    }

    @Override
    public SessionAgent newSessionAgent() {
        return new SessionAgent(this);
    }

    @Override
    protected void doInitialize() throws Exception {
        if (getSessionIdGenerator() == null) {
            SessionIdGenerator sessionIdGenerator = new SessionIdGenerator(workerName);
            setSessionIdGenerator(sessionIdGenerator);
        }

        if (getSessionCache() == null) {
            SessionCache sessionCache = new DefaultSessionCache(this);
            setSessionCache(sessionCache);
        }

        if (sessionDataStore != null) {
            getSessionCache().setSessionDataStore(sessionDataStore);
        }

        if (sessionConfig != null) {
            if (sessionConfig.hasTimeout()) {
                int timeout = sessionConfig.getTimeout();
                setDefaultMaxIdleSecs(timeout);
            }

            if (getSessionCache().getSessionDataStore() == null) {
                String storeType = sessionConfig.getStoreType();
                SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
                if (storeType != null && sessionStoreType == null) {
                    throw new IllegalArgumentException("Unknown session store type: " + storeType);
                }
                if (sessionStoreType == SessionStoreType.FILE) {
                    FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
                    SessionFileStoreConfig fileStoreConfig = sessionConfig.getFileStoreConfig();

                    String storeDir = fileStoreConfig.getStoreDir();
                    if (StringUtils.hasText(storeDir)) {
                        if (context != null) {
                            String basePath = context.getEnvironment().getBasePath();
                            fileSessionDataStore.setStoreDir(new File(basePath, storeDir));
                        } else {
                            fileSessionDataStore.setStoreDir(new File(storeDir));
                        }
                    }

                    boolean deleteUnrestorableFiles = fileStoreConfig.isDeleteUnrestorableFiles();
                    if (deleteUnrestorableFiles) {
                        fileSessionDataStore.setDeleteUnrestorableFiles(true);
                    }

                    fileSessionDataStore.initialize();
                    getSessionCache().setSessionDataStore(fileSessionDataStore);
                }
            }
        }

        if (context != null) {
            final SessionScopeAdvisor sessionScopeAdvisor = SessionScopeAdvisor.create(context);
            if (sessionScopeAdvisor != null) {
                addEventListener(new SessionListener() {
                    @Override
                    public void sessionCreated(Session session) {
                        sessionScopeAdvisor.executeBeforeAdvice();
                    }

                    @Override
                    public void sessionDestroyed(Session session) {
                        sessionScopeAdvisor.executeAfterAdvice();
                    }
                });
            }
        }

        super.doInitialize();
    }

    @Override
    protected void doDestroy() throws Exception {
        getSessionCache().clear();
        super.doDestroy();
    }

    public static DefaultSessionManager create(ActivityContext context, SessionConfig sessionConfig, String workerName) {
        DefaultSessionManager sessionManager = new DefaultSessionManager(context);
        sessionManager.setWorkerName(workerName);
        if (sessionConfig != null) {
            sessionManager.setSessionConfig(sessionConfig);
        }
        return sessionManager;
    }

}
