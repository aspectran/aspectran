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
package com.aspectran.undertow.server.servlet;

import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.core.ServletContainerImpl;
import jakarta.servlet.ServletContext;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContainer extends ServletContainerImpl {

    public void setServletContexts(TowServletContext... towServletContexts) throws Exception {
        if (towServletContexts != null) {
            for (TowServletContext towServletContext : towServletContexts) {
                DeploymentManager manager = addDeployment(towServletContext);
                manager.deploy();
                Deployment deployment = manager.getDeployment();
                ServletContext servletContext = deployment.getServletContext();
                towServletContext.createRootWebService(servletContext);
            }
        }
    }

}
