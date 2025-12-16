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
package com.aspectran.daemon.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.context.config.AcceptableConfig;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.service.RequestAcceptor;
import com.aspectran.daemon.adapter.DaemonSessionAdapter;
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;

/**
 * Abstract base class for {@link DaemonService} implementations.
 * <p>This class extends {@link DefaultCoreService} to provide common wiring for
 * daemon environments. It adds support for session management in a non-web context
 * and handles daemon-specific configuration from {@link DaemonConfig}.
 *
 * @since 5.1.0
 */
public abstract class AbstractDaemonService extends DefaultCoreService implements DaemonService {

    private DefaultSessionManager sessionManager;

    private SessionAgent sessionAgent;

    /**
     * Instantiates a new Abstract daemon service.
     */
    AbstractDaemonService() {
        super();
    }

    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new DaemonSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    /**
     * Creates and initializes a session manager for the daemon service.
     * The session manager is configured based on the {@link SessionManagerConfig}
     * within the daemon configuration.
     * @throws CoreServiceException if the session manager fails to initialize
     */
    protected void createSessionManager() {
        Assert.state(this.sessionManager == null,
                "Session Manager is already exists for " + getServiceName());
        DaemonConfig daemonConfig = getAspectranConfig().getDaemonConfig();
        if (daemonConfig != null) {
            SessionManagerConfig sessionManagerConfig = daemonConfig.getSessionManagerConfig();
            if (sessionManagerConfig != null && sessionManagerConfig.isEnabled()) {
                try {
                    DefaultSessionManager sessionManager = new DefaultSessionManager();
                    sessionManager.setActivityContext(getActivityContext());
                    sessionManager.setSessionManagerConfig(sessionManagerConfig);
                    sessionManager.initialize();
                    this.sessionManager = sessionManager;
                    this.sessionAgent = new SessionAgent(sessionManager);
                } catch (Exception e) {
                    throw new CoreServiceException("Failed to create a session manager", e);
                }
            }
        }
    }

    /**
     * Destroys the session manager and releases its resources.
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

    /**
     * Applies daemon-specific configuration, then delegates to the base class.
     * <p>
     * If present, {@link DaemonConfig} is extracted from the provided
     * {@link AspectranConfig} and applied via {@link #configure(DaemonConfig)}.
     * </p>
     * @param aspectranConfig the root service configuration (never {@code null})
     */
    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        DaemonConfig daemonConfig = aspectranConfig.getDaemonConfig();
        if (daemonConfig != null) {
            configure(daemonConfig);
        }
        super.configure(aspectranConfig);
    }

    /**
     * Applies {@link DaemonConfig}-specific options for this service.
     * <p>
     * Currently, it sets a {@link RequestAcceptor} based on the daemon's
     * {@link AcceptableConfig}, allowing the daemon to restrict which requests
     * are considered acceptable/exposed.
     * </p>
     * @param daemonConfig the daemon-specific configuration (never {@code null})
     */
    private void configure(@NonNull DaemonConfig daemonConfig) {
        AcceptableConfig acceptableConfig = daemonConfig.getAcceptableConfig();
        if (acceptableConfig != null) {
            setRequestAcceptor(new RequestAcceptor(acceptableConfig));
        }
    }

}
