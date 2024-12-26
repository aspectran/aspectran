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
package com.aspectran.undertow.server.session;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.component.session.SessionStore;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Created: 2024-04-27</p>
 */
public abstract class AbstractSessionManager implements ApplicationAdapterAware, InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionManager.class);

    private final DefaultSessionManager sessionManager = new DefaultSessionManager();

    private final AtomicInteger startCount = new AtomicInteger();

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        sessionManager.setApplicationAdapter(applicationAdapter);
    }

    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) {
        sessionManager.setSessionManagerConfig(sessionManagerConfig);
    }

    public void setSessionManagerConfigWithApon(String apon) {
        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
        try {
            sessionManagerConfig.readFrom(apon);
        } catch (AponParseException e) {
            throw new RuntimeException(e);
        }
        setSessionManagerConfig(sessionManagerConfig);
    }

    public void setSessionStore(SessionStore sessionStore) {
        sessionManager.setSessionStore(sessionStore);
    }

    public SessionHandler getSessionHandler() {
        return sessionManager.getSessionHandler();
    }

    protected DefaultSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public void initialize() throws Exception {
        sessionManager.initialize();
    }

    public void start() {
        startCount.getAndIncrement();
    }

    public void stop() {
        int count = startCount.decrementAndGet();
        if (count == 0) {
            try {
                destroy();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (sessionManager.isAvailable()) {
            try {
                sessionManager.destroy();
            } catch (Exception e) {
                throw new RuntimeException("Error destroying TowSessionManager", e);
            }
        }
    }

}
