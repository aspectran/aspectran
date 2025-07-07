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
 * <p>Created: 11/25/23</p>
 */
public interface TowServer extends LifeCycle {

    static String getVersion() {
        return Version.getVersionString();
    }

    Undertow getUndertow();

    DeploymentManager getDeploymentManager(String deploymentName);

    DeploymentManager getDeploymentManagerByPath(String path);

    SessionManager getSessionManager(String deploymentName);

    SessionManager getSessionManagerByPath(String path);

}
