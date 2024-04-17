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
package com.aspectran.undertow.server.handler;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.TowServer;
import com.aspectran.undertow.server.handler.resource.TowResourceHandler;
import com.aspectran.undertow.service.DefaultTowServiceBuilder;
import com.aspectran.undertow.service.TowService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.lifecycle.LifeCycle;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

/**
 * <p>Created: 06/10/2019</p>
 */
public class LightRequestHandlerFactory extends AbstractRequestHandlerFactory
    implements ActivityContextAware, DisposableBean {

    private ActivityContext context;

    private TowServer towServer;

    private ResourceManager resourceManager;

    private SessionManager sessionManager;

    private SessionConfig sessionConfig;

    private AspectranConfig aspectranConfig;

    private TowService towService;

    @NonNull
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    @AvoidAdvice
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    public void setTowServer(TowServer towServer) {
        this.towServer = towServer;
    }

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

    public HttpHandler createHandler() throws Exception {
        TowService towService = createTowService();

        if (sessionManager != null) {
            sessionManager.start();
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

    private TowService createTowService() throws Exception {
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
            towService.getServiceLifeCycle().start();
        }
        if (towServer != null) {
            towServer.addLifeCycleListener(new LifeCycle.Listener() {
                @Override
                public void lifeCycleStopping(LifeCycle event) {
                    destroyTowService();
                }
            });
        }
        return towService;
    }

    private void destroyTowService() {
        if (towService != null) {
            if (towService.getServiceLifeCycle().isActive()) {
                towService.getServiceLifeCycle().stop();
                towService.leaveFromRootService();
            }
            towService = null;
        }
    }

    @Override
    public void destroy() throws Exception {
        destroyTowService();
        if (sessionManager != null) {
            sessionManager.stop();
        }
    }

}
