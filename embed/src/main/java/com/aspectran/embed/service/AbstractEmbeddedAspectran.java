/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.support.i18n.message.NoSuchMessageException;
import com.aspectran.embed.adapter.AspectranSessionAdapter;
import com.aspectran.utils.Assert;

import java.util.Locale;

/**
 * Provides an interface that can be used by embedding Aspectran in Java applications.
 *
 * @since 3.0.0
 */
public abstract class AbstractEmbeddedAspectran extends DefaultCoreService implements EmbeddedAspectran {

    private SessionManager sessionManager;

    private SessionAgent sessionAgent;

    AbstractEmbeddedAspectran() {
        super();
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new AspectranSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    protected void createSessionManager() {
        Assert.state(this.sessionManager == null,
                "Session Manager is already exists for embedded aspectran");
        EmbedConfig embedConfig = getAspectranConfig().getEmbedConfig();
        if (embedConfig != null) {
            SessionManagerConfig sessionManagerConfig = embedConfig.getSessionManagerConfig();
            if (sessionManagerConfig != null && sessionManagerConfig.isEnabled()) {
                try {
                    DefaultSessionManager sessionManager = new DefaultSessionManager();
                    sessionManager.setActivityContext(getActivityContext());
                    sessionManager.setSessionManagerConfig(sessionManagerConfig);
                    sessionManager.initialize();
                    this.sessionManager = sessionManager;
                    this.sessionAgent = new SessionAgent(sessionManager.getSessionHandler());
                } catch (Exception e) {
                    throw new CoreServiceException("Failed to create session manager for embedded aspectran", e);
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
    public Environment getEnvironment() {
        return getActivityContext().getEnvironment();
    }

    @Override
    public void release() {
        stop();
    }

    //---------------------------------------------------------------------
    // Implementation of BeanRegistry interface
    //---------------------------------------------------------------------

    @Override
    public <V> V getBean(String id) {
        return getActivityContext().getBeanRegistry().getBean(id);
    }

    @Override
    public <V> V getBean(Class<V> type) {
        return getActivityContext().getBeanRegistry().getBean(type);
    }

    @Override
    public <V> V getBean(Class<V> type, String id) {
        return getActivityContext().getBeanRegistry().getBean(type, id);
    }

    @Override
    public boolean containsBean(String id) {
        return getActivityContext().getBeanRegistry().containsBean(id);
    }

    @Override
    public boolean containsBean(Class<?> type) {
        return getActivityContext().getBeanRegistry().containsBean(type);
    }

    @Override
    public boolean containsBean(Class<?> type, String id) {
        return getActivityContext().getBeanRegistry().containsBean(type, id);
    }

    //---------------------------------------------------------------------
    // Implementation of MessageSource interface
    //---------------------------------------------------------------------

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return getActivityContext().getMessageSource().getMessage(code, args, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return getActivityContext().getMessageSource().getMessage(code, args, defaultMessage, locale);
    }

}
