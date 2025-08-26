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
package com.aspectran.undertow.server;

import com.aspectran.core.component.session.SessionManager;
import com.aspectran.utils.lifecycle.LifeCycle;
import io.undertow.Undertow;
import io.undertow.Version;
import io.undertow.servlet.api.DeploymentManager;

/**
 * Defines the contract for an embedded Undertow server managed by Aspectran.
 * <p>"Tow" is an abbreviation for Undertow. This interface extends Aspectran's
 * {@link LifeCycle} to manage the server's start and stop operations. It provides
 * access to the underlying native {@link Undertow} server instance and its
 * deployment managers.</p>
 *
 * <p>Created: 11/25/23</p>
 */
public interface TowServer extends LifeCycle {

    /**
     * Returns the version of the underlying Undertow server library.
     * @return the Undertow version string
     */
    static String getVersion() {
        return Version.getVersionString();
    }

    /**
     * Returns the underlying native {@link Undertow} server instance.
     * This can be used for advanced, low-level configuration.
     * @return the Undertow server instance
     */
    Undertow getUndertow();

    /**
     * Retrieves the Undertow {@link DeploymentManager} for a web application by its deployment name.
     * @param deploymentName the name of the deployment
     * @return the deployment manager, or {@code null} if not found
     */
    DeploymentManager getDeploymentManager(String deploymentName);

    /**
     * Retrieves the Undertow {@link DeploymentManager} for a web application by its context path.
     * @param path the context path of the web application
     * @return the deployment manager, or {@code null} if not found
     */
    DeploymentManager getDeploymentManagerByPath(String path);

    /**
     * Retrieves the Aspectran {@link SessionManager} for a specific deployment.
     * @param deploymentName the name of the deployment
     * @return the session manager, or {@code null} if not found
     */
    SessionManager getSessionManager(String deploymentName);

    /**
     * Retrieves the Aspectran {@link SessionManager} for a web application by its context path.
     * @param path the context path of the web application
     * @return the session manager, or {@code null} if not found
     */
    SessionManager getSessionManagerByPath(String path);

}
