/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.core.ServletContainerImpl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContainer extends ServletContainerImpl {

    private final Map<String, DeploymentManager> deploymentManagers = new LinkedHashMap<>();

    public DeploymentManager[] getDeploymentManagers() {
        if (deploymentManagers.isEmpty()) {
            return null;
        } else {
            return deploymentManagers.values().toArray(new DeploymentManager[0]);
        }
    }

    public void setTowServletContexts(TowServletContext... towServletContexts) {
        if (towServletContexts != null) {
            for (DeploymentInfo deploymentInfo : towServletContexts) {
                addDeployment(deploymentInfo);
            }
        }
    }

    @Override
    public DeploymentManager addDeployment(DeploymentInfo deploymentInfo) {
        DeploymentManager manager = super.addDeployment(deploymentInfo);
        deploymentManagers.put(deploymentInfo.getDeploymentName(), manager);
        return manager;
    }

    @Override
    public void removeDeployment(DeploymentInfo deploymentInfo) {
        super.removeDeployment(deploymentInfo);
        deploymentManagers.remove(deploymentInfo.getDeploymentName());
    }

}
