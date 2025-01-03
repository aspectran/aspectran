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
package com.aspectran.undertow.server.handler;

import com.aspectran.undertow.server.handler.resource.TowResourceHandler;
import com.aspectran.undertow.server.handler.session.SessionAttachmentHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;

/**
 * <p>Created: 2019-08-04</p>
 */
public class DefaultRequestHandlerFactory extends AbstractRequestHandlerFactory {

    private ServletContainer servletContainer;

    public ServletContainer getServletContainer() {
        return servletContainer;
    }

    public void setServletContainer(ServletContainer servletContainer) {
        if (servletContainer == null) {
            throw new IllegalArgumentException("servletContainer must not be null");
        }
        this.servletContainer = servletContainer;
    }

    public HttpHandler createHandler() throws Exception {
        HttpHandler rootHandler;
        if (servletContainer != null) {
            PathHandler pathHandler = new PathHandler();
            for (String deploymentName : servletContainer.listDeployments()) {
                DeploymentManager manager = servletContainer.getDeployment(deploymentName);
                HttpHandler handler = manager.start();

                SessionManager sessionManager = manager.getDeployment().getSessionManager();
                if (sessionManager != null) {
                    SessionConfig sessionConfig = manager.getDeployment().getServletContext().getSessionConfig();
                    handler = new SessionAttachmentHandler(handler, sessionManager, sessionConfig);
                }

                DeploymentInfo info = manager.getDeployment().getDeploymentInfo();
                String contextPath = info.getContextPath();
                ResourceManager resourceManager = info.getResourceManager();

                if (resourceManager != null) {
                    TowResourceHandler resourceHandler = new TowResourceHandler(resourceManager, handler);
                    String pathPrefix = contextPath;
                    if (pathPrefix != null && pathPrefix.endsWith("/")) {
                        pathPrefix = pathPrefix.substring(0, pathPrefix.length() - 1);
                    }
                    resourceHandler.autoDetect(pathPrefix);
                    if (resourceHandler.hasPatterns()) {
                        handler = resourceHandler;
                    }
                }

                pathHandler.addPrefixPath(contextPath, handler);
            }
            rootHandler = pathHandler;
        } else {
            rootHandler = ResponseCodeHandler.HANDLE_404;
        }
        return wrapHandler(rootHandler);
    }

}
