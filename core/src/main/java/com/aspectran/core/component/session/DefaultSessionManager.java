/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import java.io.File;

/**
 * Implementation of SessionManager.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public class DefaultSessionManager extends AbstractSessionHandler implements SessionManager {

    private final ActivityContext context;

    private String groupName;

    private SessionConfig sessionConfig;

    private SessionDataStore sessionDataStore;

    public DefaultSessionManager() {
        this(null);
    }

    public DefaultSessionManager(ActivityContext context) {
        this.context = context;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    @Override
    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
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
            SessionIdGenerator sessionIdGenerator = new SessionIdGenerator(groupName);
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
            if (sessionConfig.isValueAssigned(SessionConfig.timeout)) {
                int timeout = sessionConfig.getInt(SessionConfig.timeout);
                setDefaultMaxIdleSecs(timeout);
            }

            if (getSessionCache().getSessionDataStore() != null) {
                String storeType = sessionConfig.getString(SessionConfig.storeType);
                SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
                if (sessionStoreType == SessionStoreType.FILE) {
                    FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
                    SessionFileStoreConfig fileStoreConfig = sessionConfig.getParameters(SessionConfig.fileStore);

                    String path = fileStoreConfig.getString(SessionFileStoreConfig.path);
                    if (StringUtils.hasText(path)) {
                        fileSessionDataStore.setStoreDir(new File(path));
                    }

                    Boolean deleteUnrestorableFiles = fileStoreConfig.getBoolean(SessionFileStoreConfig.deleteUnrestorableFiles);
                    if (deleteUnrestorableFiles != null) {
                        fileSessionDataStore.setDeleteUnrestorableFiles(deleteUnrestorableFiles);
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

}
