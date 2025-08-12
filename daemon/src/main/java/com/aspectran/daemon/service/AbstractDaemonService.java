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
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Base support class for daemon-specific {@link DaemonService} implementations.
 * <p>
 * Extends {@link com.aspectran.core.service.DefaultCoreService} to provide common
 * wiring for daemon environments, including optional session management backed by
 * {@link com.aspectran.core.component.session.DefaultSessionManager} and request
 * acceptability configuration through {@link com.aspectran.core.service.RequestAcceptor}.
 * Subclasses can call {@link #createSessionManager()} and {@link #destroySessionManager()}
 * at appropriate lifecycle phases to manage session infrastructure.
 * </p>
 *
 * @since 5.1.0
 */
public abstract class AbstractDaemonService extends DefaultCoreService implements DaemonService {

    private DefaultSessionManager sessionManager;

    private SessionAgent sessionAgent;

    /**
     * Constructs the service base.
     * <p>
     * Protected/package-private to restrict direct instantiation to the framework
     * and subclasses within the daemon module.
     * </p>
     */
    AbstractDaemonService() {
        super();
    }

    /**
     * Creates a new {@link SessionAdapter} bound to this service's session infrastructure.
     * <p>
     * Returns {@code null} when session management is not configured or has not been initialized
     * (i.e., when no {@link SessionAgent} is available).
     * </p>
     * @return a new {@code SessionAdapter}, or {@code null} if sessions are not available
     */
    @Override
    public SessionAdapter newSessionAdapter() {
        if (sessionAgent != null) {
            return new DaemonSessionAdapter(sessionAgent);
        } else {
            return null;
        }
    }

    /**
         * Initializes session management if enabled by daemon configuration.
         * <p>
         * Reads {@link DaemonConfig} from the current {@link AspectranConfig} and, when a
         * {@link SessionManagerConfig} is present and enabled, instantiates and initializes a
         * {@link DefaultSessionManager} bound to this service's activity context. Also creates a
         * {@link SessionAgent} for adapter creation.
         * </p>
         * @throws CoreServiceException if initialization fails for any reason
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
                    throw new CoreServiceException("Failed to create session manager for " + getServiceName(), e);
                }
            }
        }
    }

    /**
     * Shuts down and clears session infrastructure if previously initialized.
     * <p>
     * This method invalidates the {@link SessionAgent} (if any) and destroys the
     * underlying {@link DefaultSessionManager}. It is safe to call multiple times.
     * </p>
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
