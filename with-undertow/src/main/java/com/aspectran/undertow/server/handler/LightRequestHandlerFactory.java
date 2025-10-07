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
package com.aspectran.undertow.server.handler;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.handler.resource.TowResourceHandler;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.undertow.service.DefaultTowService;
import com.aspectran.undertow.service.DefaultTowServiceBuilder;
import com.aspectran.utils.Assert;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.ServletContainer;

/**
 * A factory for creating the root {@link HttpHandler} for a lightweight, non-servlet environment.
 * <p>This factory is responsible for:
 * <ul>
 *   <li>Creating and managing the lifecycle of the underlying {@link DefaultTowService}.</li>
 *   <li>Initializing the session manager.</li>
 *   <li>Creating a {@link LightRequestHandler} to process dynamic requests.</li>
 *   <li>Optionally creating a {@link TowResourceHandler} to serve static resources.</li>
 *   <li>Chaining all handlers together using the configured {@code HandlerWrapper}s.</li>
 * </ul></p>
 *
 * <p>Created: 06/10/2019</p>
 */
public class LightRequestHandlerFactory extends AbstractRequestHandlerFactory implements RequestHandlerFactory {

    private ResourceManager resourceManager;

    private SessionManager sessionManager;

    private SessionConfig sessionConfig;

    private AspectranConfig aspectranConfig;

    private DefaultTowService towService;

    /**
     * Sets the resource manager for serving static files.
     * @param resourceManager the static resource manager
     */
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Sets the session manager for handling user sessions.
     * @param sessionManager the session manager
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Sets the session configuration (e.g., for cookie handling).
     * @param sessionConfig the session configuration
     */
    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    /**
     * Sets the Aspectran configuration to build an embedded service.
     * @param aspectranConfig the Aspectran configuration
     */
    public void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    /**
     * Creates the root {@link HttpHandler} for the server.
     * <p>This method assembles the full handler chain, including static resources,
     * session management, and the core Aspectran request handler.</p>
     * @return the root HTTP handler
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public HttpHandler createHandler() throws Exception {
        createTowService();

        if (sessionManager != null) {
            if (sessionManager instanceof TowSessionManager towSessionManager) {
                try {
                    towSessionManager.initialize();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            sessionManager.start();
        } else {
            towService.setSessionAdaptable(false);
        }

        LightRequestHandler requestHandler = new LightRequestHandler(towService, sessionManager, sessionConfig);
        HttpHandler rootHandler = requestHandler;

        if (resourceManager != null) {
            TowResourceHandler resourceHandler = new TowResourceHandler(resourceManager, requestHandler);
            resourceHandler.autoDetect(null);
            if (resourceHandler.hasPatterns()) {
                rootHandler = resourceHandler;
            }
        }

        return wrapHandler(rootHandler);
    }

    /**
     * Throws {@link UnsupportedOperationException} as this factory does not support servlet containers.
     * @return never returns
     */
    @Override
    public ServletContainer getServletContainer() {
        throw new UnsupportedOperationException("Not support servlet container");
    }

    /**
     * Disposes of the created {@link DefaultTowService} and stops the session manager.
     * @throws Exception if an error occurs during disposal
     */
    @Override
    public void dispose() throws Exception {
        destroyTowService();
        if (sessionManager != null) {
            sessionManager.stop();
        }
    }

    /**
     * Creates and starts the embedded {@link DefaultTowService}.
     * @throws Exception if the service fails to start
     */
    private void createTowService() throws Exception {
        Assert.state(towService == null, "TowService is already configured");
        CoreService masterService = getActivityContext().getMasterService();
        if (aspectranConfig == null) {
            towService = DefaultTowServiceBuilder.build(masterService, resourceManager);
        } else {
            ContextConfig contextConfig = aspectranConfig.getContextConfig();
            if (contextConfig != null) {
                String basePath = contextConfig.getBasePath();
                if (basePath == null) {
                    contextConfig.setBasePath(getActivityContext().getApplicationAdapter().getBasePathString());
                }
            }
            towService = DefaultTowServiceBuilder.build(masterService, aspectranConfig, resourceManager);
        }
        if (towService.isOrphan()) {
            towService.start();
        }
    }

    /**
     * Stops and withdraws the embedded {@link DefaultTowService}.
     */
    private void destroyTowService() {
        if (towService != null) {
            if (towService.isActive()) {
                towService.stop();
            }
            towService.withdraw();
            towService = null;
        }
    }

}
