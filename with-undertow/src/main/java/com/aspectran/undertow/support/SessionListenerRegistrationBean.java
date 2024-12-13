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
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.core.component.session.SessionListenerRegistration;
import com.aspectran.undertow.server.TowServer;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * A Bean to register session listener in session manager.
 *
 * <p>Created: 2020/05/09</p>
 *
 * @since 6.7.0
 */
public class SessionListenerRegistrationBean extends InstantActivitySupport implements SessionListenerRegistration {

    private final String towServerId;

    private final String deploymentName;

    public SessionListenerRegistrationBean(String towServerId, String deploymentName) {
        this.towServerId = towServerId;
        this.deploymentName = deploymentName;
    }

    @Override
    public void register(SessionListener listener) {
        getSessionHandler().addSessionListener(listener);
    }

    @Override
    public void register(SessionListener listener, String deploymentName) {
        getSessionHandler(deploymentName).addSessionListener(listener);
    }

    @Override
    public void remove(SessionListener listener) {
        getSessionHandler().removeSessionListener(listener);
    }

    @Override
    public void remove(SessionListener listener, String deploymentName) {
        getSessionHandler(deploymentName).removeSessionListener(listener);
    }

    private SessionHandler getSessionHandler() {
        return getTowServer().getSessionHandler(deploymentName);
    }

    private SessionHandler getSessionHandler(String deploymentName) {
        return getTowServer().getSessionHandler(deploymentName);
    }

    @NonNull
    private TowServer getTowServer() {
        TowServer towServer = getBeanRegistry().getBean(towServerId);
        if (towServer == null) {
            throw new IllegalArgumentException("No TowServer named '" + towServerId + "'");
        }
        return towServer;
    }

}
