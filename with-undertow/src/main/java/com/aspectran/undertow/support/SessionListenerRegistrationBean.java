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
package com.aspectran.undertow.support;

import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.core.component.session.SessionListenerRegistration;
import com.aspectran.undertow.server.TowServer;

/**
 * A Bean to register session listener in session manager.
 *
 * <p>Created: 2020/05/09</p>
 *
 * @since 6.7.0
 */
public class SessionListenerRegistrationBean extends InstantActivitySupport
        implements SessionListenerRegistration, InitializableBean {

    private final String towServerId;

    private final String deploymentName;

    private SessionHandler sessionHandler;

    public SessionListenerRegistrationBean(String towServerId, String deploymentName) {
        this.towServerId = towServerId;
        this.deploymentName = deploymentName;
    }

    @Override
    public void register(SessionListener listener) {
        sessionHandler.addSessionListener(listener);
    }

    @Override
    public void remove(SessionListener listener) {
        sessionHandler.removeSessionListener(listener);
    }

    private SessionHandler getSessionHandler() {
        TowServer towServer = getBeanRegistry().getBean(towServerId);
        if (towServer == null) {
            throw new IllegalArgumentException("No TowServer named '" + towServerId + "'");
        }
        return towServer.getSessionHandler(deploymentName);
    }

    @Override
    public void initialize() throws Exception {
        sessionHandler = getSessionHandler();
    }

}
