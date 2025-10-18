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
package com.aspectran.undertow.support;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.core.component.session.SessionListenerRegistration;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.undertow.server.TowServer;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bean that facilitates the registration of {@link SessionListener}s to a specific
 * {@link SessionManager} associated with an Undertow deployment.
 * <p>This class is typically configured as a bean within an Aspectran context. It looks up
 * a {@link TowServer} instance and uses it to find the target {@code SessionManager}
 * by deployment name, then adds or removes listeners. This provides a declarative way
 * to manage session listeners for embedded Undertow servers.</p>
 *
 * <p>Created: 2020/05/09</p>
 *
 * @since 6.7.0
 */
public class SessionListenerRegistrationBean extends InstantActivitySupport implements SessionListenerRegistration {

    private static final Logger logger = LoggerFactory.getLogger(SessionListenerRegistrationBean.class);

    private final String towServerId;

    private final String deploymentName;

    /**
     * Instantiates a new SessionListenerRegistrationBean.
     */
    public SessionListenerRegistrationBean() {
        this(null, null);
    }

    /**
     * Instantiates a new SessionListenerRegistrationBean.
     * @param towServerId the bean ID of the {@link TowServer}
     */
    public SessionListenerRegistrationBean(String towServerId) {
        this(towServerId, null);
    }

    /**
     * Instantiates a new SessionListenerRegistrationBean.
     * @param towServerId the bean ID of the {@link TowServer}
     * @param deploymentName the name of the deployment
     */
    public SessionListenerRegistrationBean(String towServerId, String deploymentName) {
        this.towServerId = towServerId;
        this.deploymentName = deploymentName;
    }

    @Override
    public void register(SessionListener listener) {
        register(listener, deploymentName);
    }

    @Override
    public void register(SessionListener listener, String deploymentName) {
        Assert.notNull(listener, "listener must not be null");
        Assert.notNull(deploymentName, "deploymentName must not be null");
        SessionManager sessionManager = getSessionManager(deploymentName);
        if (sessionManager != null) {
            sessionManager.addSessionListener(listener);
        } else {
            logger.warn("Unable to register {}. Cause: No session manager found for deployment '{}'",
                    ObjectUtils.simpleIdentityToString(listener), deploymentName);
        }
    }

    @Override
    public void remove(SessionListener listener) {
        remove(listener, deploymentName);
    }

    @Override
    public void remove(SessionListener listener, String deploymentName) {
        Assert.notNull(listener, "listener must not be null");
        Assert.notNull(deploymentName, "deploymentName must not be null");
        if (getBeanRegistry().isAvailable()) {
            SessionManager sessionManager = getSessionManager(deploymentName);
            if (sessionManager != null) {
                sessionManager.removeSessionListener(listener);
            } else {
                logger.warn("Unable to remove {}. Cause: No session manager found for deployment '{}'",
                        ObjectUtils.simpleIdentityToString(listener), deploymentName);
            }
        }
    }

    /**
     * Finds the {@link SessionManager} for a given deployment name.
     * <p>It looks up the {@link TowServer} bean (either by a configured ID or as a unique
     * bean of its type) and then retrieves the session manager for the specified deployment.</p>
     * @param deploymentName the name of the deployment
     * @return the {@link SessionManager} instance
     * @throws IllegalStateException if the {@code TowServer} cannot be found
     */
    private SessionManager getSessionManager(String deploymentName) {
        Assert.notNull(deploymentName, "deploymentName must not be null");
        TowServer towServer = null;
        if (towServerId != null) {
            if (getBeanRegistry().containsBean(TowServer.class, towServerId)) {
                towServer = getBeanRegistry().getBean(TowServer.class, towServerId);
            }
            if (towServer == null) {
                throw new IllegalStateException("No TowServer named '" + towServerId + "'");
            }
        } else {
            if (getBeanRegistry().containsBean(TowServer.class)) {
                towServer = getBeanRegistry().getBean(TowServer.class);
            }
            if (towServer == null) {
                throw new IllegalStateException("No TowServer");
            }
        }
        return towServer.getSessionManager(deploymentName);
    }

}
