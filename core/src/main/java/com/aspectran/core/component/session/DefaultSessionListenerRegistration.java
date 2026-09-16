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
package com.aspectran.core.component.session;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link SessionListenerRegistration} that registers or removes
 * {@link SessionListener}s using a {@link SessionManagerProvider}.
 *
 * <p>Created: 2026-09-16</p>
 */
public class DefaultSessionListenerRegistration extends InstantActivitySupport implements SessionListenerRegistration {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionListenerRegistration.class);

    private final SessionManagerProvider sessionManagerProvider;

    private final String sessionManagerProviderId;

    private final String nameOrPath;

    /**
     * Instantiates a new DefaultSessionListenerRegistration.
     */
    public DefaultSessionListenerRegistration() {
        this(null, null, null);
    }

    /**
     * Instantiates a new DefaultSessionListenerRegistration.
     * @param sessionManagerProviderId the bean ID of the {@link SessionManagerProvider}
     */
    public DefaultSessionListenerRegistration(String sessionManagerProviderId) {
        this(null, sessionManagerProviderId, null);
    }

    /**
     * Instantiates a new DefaultSessionListenerRegistration.
     * @param sessionManagerProviderId the bean ID of the {@link SessionManagerProvider}
     * @param nameOrPath the default deployment name or context path
     */
    public DefaultSessionListenerRegistration(String sessionManagerProviderId, String nameOrPath) {
        this(null, sessionManagerProviderId, nameOrPath);
    }

    /**
     * Instantiates a new DefaultSessionListenerRegistration.
     * @param sessionManagerProvider the {@link SessionManagerProvider} instance
     */
    public DefaultSessionListenerRegistration(SessionManagerProvider sessionManagerProvider) {
        this(sessionManagerProvider, null, null);
    }

    /**
     * Instantiates a new DefaultSessionListenerRegistration.
     * @param sessionManagerProvider the {@link SessionManagerProvider} instance
     * @param nameOrPath the default deployment name or context path
     */
    public DefaultSessionListenerRegistration(SessionManagerProvider sessionManagerProvider, String nameOrPath) {
        this(sessionManagerProvider, null, nameOrPath);
    }

    private DefaultSessionListenerRegistration(
            SessionManagerProvider sessionManagerProvider,
            String sessionManagerProviderId,
            String nameOrPath) {
        this.sessionManagerProvider = sessionManagerProvider;
        this.sessionManagerProviderId = sessionManagerProviderId;
        this.nameOrPath = nameOrPath;
    }

    @Override
    public void register(SessionListener listener) {
        register(listener, nameOrPath);
    }

    @Override
    public void register(SessionListener listener, String nameOrPath) {
        Assert.notNull(listener, "listener must not be null");
        String target = (nameOrPath != null ? nameOrPath : this.nameOrPath);
        SessionManager sessionManager = getSessionManager(target);
        if (sessionManager != null) {
            sessionManager.addSessionListener(listener);
        } else {
            String display = (target != null && !target.isEmpty() ? target : "/");
            logger.warn("Unable to register {}. Cause: No session manager found for '{}'",
                    ObjectUtils.simpleIdentityToString(listener), display);
        }
    }

    @Override
    public void remove(SessionListener listener) {
        remove(listener, nameOrPath);
    }

    @Override
    public void remove(SessionListener listener, String nameOrPath) {
        Assert.notNull(listener, "listener must not be null");
        if (sessionManagerProvider != null || (getBeanRegistry() != null && getBeanRegistry().isAvailable())) {
            String target = (nameOrPath != null ? nameOrPath : this.nameOrPath);
            SessionManager sessionManager = getSessionManager(target);
            if (sessionManager != null) {
                sessionManager.removeSessionListener(listener);
            } else {
                String display = (target != null && !target.isEmpty() ? target : "/");
                logger.warn("Unable to remove {}. Cause: No session manager found for '{}'",
                        ObjectUtils.simpleIdentityToString(listener), display);
            }
        }
    }

    @Nullable
    private SessionManager getSessionManager(String nameOrPath) {
        SessionManagerProvider provider = resolveSessionManagerProvider();
        if (provider == null) {
            throw new IllegalStateException("No SessionManagerProvider available");
        }
        if (nameOrPath == null || nameOrPath.isEmpty() || "/".equals(nameOrPath) || "root".equalsIgnoreCase(nameOrPath)) {
            SessionManager sm = provider.getSessionManager();
            if (sm != null) {
                return sm;
            }
        }
        SessionManager sm = provider.getSessionManager(nameOrPath);
        if (sm == null) {
            sm = provider.getSessionManagerByPath(nameOrPath);
        }
        return sm;
    }

    private SessionManagerProvider resolveSessionManagerProvider() {
        if (sessionManagerProvider != null) {
            return sessionManagerProvider;
        }
        if (sessionManagerProviderId != null) {
            if (getBeanRegistry().containsBean(SessionManagerProvider.class, sessionManagerProviderId)) {
                return getBeanRegistry().getBean(SessionManagerProvider.class, sessionManagerProviderId);
            }
            throw new IllegalStateException("No SessionManagerProvider named '" + sessionManagerProviderId + "'");
        } else {
            if (getBeanRegistry().containsBean(SessionManagerProvider.class)) {
                return getBeanRegistry().getBean(SessionManagerProvider.class);
            }
            throw new IllegalStateException("No SessionManagerProvider found");
        }
    }

}
