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
package com.aspectran.embed.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
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
 * Abstract base class for {@link EmbeddedAspectran} implementations.
 * <p>This class extends {@link DefaultCoreService} and provides the foundational
 * infrastructure for embedding Aspectran within another application. It implements
 * the {@link EmbeddedAspectran} interface by delegating calls to the underlying
 * {@link com.aspectran.core.context.ActivityContext} for bean access, message lookup,
 * and session management.
 */
public abstract class AbstractEmbeddedAspectran extends DefaultCoreService implements EmbeddedAspectran {

    private DefaultSessionManager sessionManager;

    private SessionAgent sessionAgent;

    /**
     * Default constructor for instantiation.
     */
    AbstractEmbeddedAspectran() {
        super();
    }

    /**
     * Creates and returns a session adapter based on the current session agent.
     * If a session agent is available, it wraps it in an Aspectran-specific session adapter.
     * Otherwise, returns null.
     * @return a new {@code SessionAdapter} instance, or null if no session agent is configured
     */
    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new AspectranSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    /**
     * Initializes the session manager using configuration from the embedded Aspectran setup.
     * This method creates a {@code DefaultSessionManager} and configures it with the provided
     * session manager configuration and activity context. If the session manager is already
     * created, this method will throw an exception.
     * @throws CoreServiceException if session manager creation fails
     */
    protected void createSessionManager() {
        Assert.state(this.sessionManager == null,
                "Session Manager is already exists for " + getServiceName());
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
                    this.sessionAgent = new SessionAgent(sessionManager);
                } catch (Exception e) {
                    throw new CoreServiceException("Failed to create session manager for " + getServiceName(), e);
                }
            }
        }
    }

    /**
     * Destroys the session manager and its associated session agent.
     * This method invalidates the session agent and releases resources.
     * Should be called during application shutdown or when the embedded aspect is no longer needed.
     */
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
