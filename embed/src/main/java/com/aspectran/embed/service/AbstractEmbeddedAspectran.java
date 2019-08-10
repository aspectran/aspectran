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
package com.aspectran.embed.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.config.EmbedConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.embed.adapter.AspectranSessionAdapter;

/**
 * Provides an interface that can be used by embedding Aspectran in Java applications.
 *
 * @since 3.0.0
 */
public abstract class AbstractEmbeddedAspectran extends AspectranCoreService implements EmbeddedAspectran {

    private SessionManager sessionManager;

    private SessionAgent sessionAgent;

    public AbstractEmbeddedAspectran() {
        super();
        determineBasePath();
    }

    @Override
    public boolean isExposable(String transletName) {
        return super.isExposable(transletName);
    }

    @Override
    public void release() {
        stop();
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new AspectranSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    protected void initSessionManager() {
        EmbedConfig embedConfig = getAspectranConfig().getEmbedConfig();
        if (embedConfig != null) {
            SessionManagerConfig sessionManagerConfig = embedConfig.getSessionManagerConfig();
            if (sessionManagerConfig != null && sessionManagerConfig.isStartup()) {
                try {
                    String workerName = this.hashCode() + "_";
                    sessionManager = DefaultSessionManager.create(getActivityContext(), sessionManagerConfig, workerName);
                    sessionManager.initialize();
                    sessionAgent = new SessionAgent(sessionManager.getSessionHandler());
                } catch (Exception e) {
                    throw new AspectranServiceException("Failed to initialize session manager", e);
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

}
