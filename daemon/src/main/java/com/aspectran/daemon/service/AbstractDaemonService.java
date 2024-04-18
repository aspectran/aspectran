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
package com.aspectran.daemon.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.daemon.adapter.DaemonSessionAdapter;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Abstract base class for {@code DaemonService} implementations.
 *
 * @since 5.1.0
 */
public abstract class AbstractDaemonService extends AspectranCoreService implements DaemonService {

    private SessionManager sessionManager;

    private SessionAgent sessionAgent;

    AbstractDaemonService() {
        super();
        checkDirectoryStructure();
    }

    @Override
    public boolean isExposable(String requestName) {
        return super.isExposable(requestName);
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new DaemonSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    protected void createSessionManager() {
        Assert.state(this.sessionManager == null, "Session Manager is already exists for daemon service");
        DaemonConfig daemonConfig = getAspectranConfig().getDaemonConfig();
        if (daemonConfig != null) {
            SessionManagerConfig sessionManagerConfig = daemonConfig.getSessionManagerConfig();
            if (sessionManagerConfig != null && sessionManagerConfig.isEnabled()) {
                try {
                    DefaultSessionManager sessionManager = new DefaultSessionManager();
                    sessionManager.setApplicationAdapter(getActivityContext().getApplicationAdapter());
                    sessionManager.setSessionManagerConfig(sessionManagerConfig);
                    sessionManager.initialize();
                    this.sessionManager = sessionManager;
                    this.sessionAgent = new SessionAgent(sessionManager.getSessionHandler());
                } catch (Exception e) {
                    throw new AspectranServiceException("Failed to create session manager for daemon service", e);
                }
            }
        }
    }

    protected void destroySessionManager() {
        if (sessionAgent != null) {
            sessionAgent.invalidate();
            sessionAgent = null;
        }
        if (sessionManager != null) {
            sessionManager.destroy();
            sessionManager = null;
        }
    }

    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        DaemonConfig daemonConfig = aspectranConfig.getDaemonConfig();
        if (daemonConfig != null) {
            configure(daemonConfig);
        }
        super.configure(aspectranConfig);
    }

    private void configure(@NonNull DaemonConfig daemonConfig) {
        ExposalsConfig exposalsConfig = daemonConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            setExposals(includePatterns, excludePatterns);
        }
    }

}
