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
 * <p>Created: 06/10/2019</p>
 */
public class LightRequestHandlerFactory extends AbstractRequestHandlerFactory implements RequestHandlerFactory {

    private ResourceManager resourceManager;

    private SessionManager sessionManager;

    private SessionConfig sessionConfig;

    private AspectranConfig aspectranConfig;

    private DefaultTowService towService;

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

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

    @Override
    public ServletContainer getServletContainer() {
        throw new UnsupportedOperationException("Not support servlet container");
    }

    @Override
    public void dispose() throws Exception {
        destroyTowService();
        if (sessionManager != null) {
            sessionManager.stop();
        }
    }

    private void createTowService() throws Exception {
        Assert.state(towService == null, "TowService is already configured");
        CoreService masterService = getActivityContext().getMasterService();
        if (aspectranConfig == null) {
            towService = DefaultTowServiceBuilder.build(masterService);
        } else {
            ContextConfig contextConfig = aspectranConfig.getContextConfig();
            if (contextConfig != null) {
                String basePath = contextConfig.getBasePath();
                if (basePath == null) {
                    contextConfig.setBasePath(getActivityContext().getApplicationAdapter().getBasePath());
                }
            }
            towService = DefaultTowServiceBuilder.build(masterService, aspectranConfig);
        }
        if (towService.isOrphan()) {
            towService.start();
        }
    }

    private void destroyTowService() {
        if (towService != null) {
            if (towService.isActive()) {
                towService.stop();
                towService.withdraw();
            }
            towService = null;
        }
    }

}
