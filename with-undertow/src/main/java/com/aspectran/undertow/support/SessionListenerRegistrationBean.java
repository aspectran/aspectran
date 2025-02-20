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
 * A Bean to register session listener in session manager.
 *
 * <p>Created: 2020/05/09</p>
 *
 * @since 6.7.0
 */
public class SessionListenerRegistrationBean extends InstantActivitySupport implements SessionListenerRegistration {

    private static final Logger logger = LoggerFactory.getLogger(SessionListenerRegistrationBean.class);

    private final String towServerId;

    private final String deploymentName;

    public SessionListenerRegistrationBean(String towServerId) {
        this(towServerId, null);
    }

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
            logger.warn("Unable to register " + ObjectUtils.simpleIdentityToString(listener) +
                    ". Cause: No session manager found for deployment '" + deploymentName + "'");
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
                logger.warn("Unable to remove " + ObjectUtils.simpleIdentityToString(listener) +
                        ". Cause: No session manager found for deployment '" + deploymentName + "'");
            }
        }
    }

    private SessionManager getSessionManager(String deploymentName) {
        Assert.notNull(towServerId, "towServerId must not be null");
        Assert.notNull(deploymentName, "deploymentName must not be null");
        TowServer towServer = getBeanRegistry().getBean(towServerId);
        if (towServer == null) {
            throw new IllegalStateException("No TowServer named '" + towServerId + "'");
        }
        return towServer.getSessionManager(deploymentName);
    }

}
