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
package com.aspectran.netty.support;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.core.component.session.SessionListenerRegistration;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.netty.server.NettyServer;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bean that facilitates the registration of {@link SessionListener}s to a specific
 * {@link SessionManager} associated with a Netty context.
 * <p>This class is typically configured as a bean within an Aspectran context. It looks up
 * a {@link NettyServer} instance and uses it to find the target {@code SessionManager}
 * by context path, then adds or removes listeners. This provides a declarative way
 * to manage session listeners for embedded Netty servers.</p>
 *
 * <p>Created: 2026-09-04</p>
 */
public class SessionListenerRegistrationBean extends InstantActivitySupport implements SessionListenerRegistration {

    private static final Logger logger = LoggerFactory.getLogger(SessionListenerRegistrationBean.class);

    private final String nettyServerId;

    private final String contextPath;

    /**
     * Instantiates a new SessionListenerRegistrationBean.
     */
    public SessionListenerRegistrationBean() {
        this(null, null);
    }

    /**
     * Instantiates a new SessionListenerRegistrationBean.
     * @param nettyServerId the bean ID of the {@link NettyServer}
     */
    public SessionListenerRegistrationBean(String nettyServerId) {
        this(nettyServerId, null);
    }

    /**
     * Instantiates a new SessionListenerRegistrationBean.
     * @param nettyServerId the bean ID of the {@link NettyServer}
     * @param contextPath the context path
     */
    public SessionListenerRegistrationBean(String nettyServerId, String contextPath) {
        this.nettyServerId = nettyServerId;
        this.contextPath = contextPath;
    }

    @Override
    public void register(SessionListener listener) {
        register(listener, contextPath);
    }

    @Override
    public void register(SessionListener listener, String deploymentName) {
        Assert.notNull(listener, "listener must not be null");
        String targetPath = (deploymentName != null ? deploymentName : contextPath);
        SessionManager sessionManager = getSessionManager(targetPath);
        if (sessionManager != null) {
            sessionManager.addSessionListener(listener);
        } else {
            String displayPath = (targetPath != null && !targetPath.isEmpty() ? targetPath : "/");
            logger.warn("Unable to register {}. Cause: No session manager found for context path '{}'",
                    ObjectUtils.simpleIdentityToString(listener), displayPath);
        }
    }

    @Override
    public void remove(SessionListener listener) {
        remove(listener, contextPath);
    }

    @Override
    public void remove(SessionListener listener, String deploymentName) {
        Assert.notNull(listener, "listener must not be null");
        if (getBeanRegistry().isAvailable()) {
            String targetPath = (deploymentName != null ? deploymentName : contextPath);
            SessionManager sessionManager = getSessionManager(targetPath);
            if (sessionManager != null) {
                sessionManager.removeSessionListener(listener);
            } else {
                String displayPath = (targetPath != null && !targetPath.isEmpty() ? targetPath : "/");
                logger.warn("Unable to remove {}. Cause: No session manager found for context path '{}'",
                        ObjectUtils.simpleIdentityToString(listener), displayPath);
            }
        }
    }

    /**
     * Finds the {@link SessionManager} for a given context path.
     * <p>It looks up the {@link NettyServer} bean (either by a configured ID or as a unique
     * bean of its type) and then retrieves the session manager for the specified context path.</p>
     * @param contextPath the context path
     * @return the {@link SessionManager} instance
     * @throws IllegalStateException if the {@code NettyServer} cannot be found
     */
    private SessionManager getSessionManager(String contextPath) {
        NettyServer nettyServer = null;
        if (nettyServerId != null) {
            if (getBeanRegistry().containsBean(NettyServer.class, nettyServerId)) {
                nettyServer = getBeanRegistry().getBean(NettyServer.class, nettyServerId);
            }
            if (nettyServer == null) {
                throw new IllegalStateException("No NettyServer named '" + nettyServerId + "'");
            }
        } else {
            if (getBeanRegistry().containsBean(NettyServer.class)) {
                nettyServer = getBeanRegistry().getBean(NettyServer.class);
            }
            if (nettyServer == null) {
                throw new IllegalStateException("No NettyServer");
            }
        }
        return nettyServer.getSessionManager(contextPath);
    }

}
