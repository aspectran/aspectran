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
package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.undertow.server.TowServer;
import com.aspectran.undertow.server.resource.StaticResourceHandler;
import com.aspectran.undertow.service.DefaultTowService;
import com.aspectran.undertow.service.TowService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.lifecycle.LifeCycle;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Created: 06/10/2019</p>
 */
public class HttpHybridHandlerFactory implements ActivityContextAware, DisposableBean {

    private ActivityContext context;

    private TowServer towServer;

    private ResourceManager resourceManager;

    private StaticResourceHandler staticResourceHandler;

    private SessionManager sessionManager;

    private SessionConfig sessionConfig;

    private List<HandlerWrapper> outerHandlerChainWrappers;

    private AspectranConfig aspectranConfig;

    private TowService towService;

    @Override
    @AvoidAdvice
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public void setTowServer(TowServer towServer) {
        this.towServer = towServer;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void setStaticResourceHandler(StaticResourceHandler staticResourceHandler) {
        this.staticResourceHandler = staticResourceHandler;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public void setOuterHandlerChainWrappers(HandlerWrapper[] wrappers) {
        if (wrappers != null && wrappers.length > 0) {
            this.outerHandlerChainWrappers = Arrays.asList(wrappers);
        } else {
            this.outerHandlerChainWrappers = null;
        }
    }

    public void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    public HttpHandler createHandler() {
        TowService towService = createTowService();

        if (sessionManager != null) {
            if (sessionConfig == null) {
                setSessionConfig(new SessionCookieConfig());
            }
            sessionManager.start();
        }

        HttpHybridHandler httpHybridHandler = new HttpHybridHandler(resourceManager);
        httpHybridHandler.setStaticResourceHandler(staticResourceHandler);
        httpHybridHandler.setSessionManager(sessionManager);
        httpHybridHandler.setSessionConfig(sessionConfig);
        httpHybridHandler.setTowService(towService);

        if (outerHandlerChainWrappers != null) {
            return wrapHandlers(httpHybridHandler, outerHandlerChainWrappers);
        } else {
            return httpHybridHandler;
        }
    }

    private TowService createTowService() {
        Assert.state(towService == null, "TowService is already configured");
        if (aspectranConfig == null) {
            towService = DefaultTowService.create(context.getRootService());
        } else {
            ContextConfig contextConfig = aspectranConfig.getContextConfig();
            if (contextConfig != null) {
                String basePath = contextConfig.getBasePath();
                if (basePath == null) {
                    contextConfig.setBasePath(context.getApplicationAdapter().getBasePath());
                }
            }
            towService = DefaultTowService.create(aspectranConfig);
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
            if (towService.getServiceController().isActive()) {
                towService.getServiceController().stop();
                if (towService.isDerived()) {
                    towService.leaveFromRootService();
                }
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

    private static HttpHandler wrapHandlers(HttpHandler wrapee, @NonNull List<HandlerWrapper> wrappers) {
        HttpHandler current = wrapee;
        for (HandlerWrapper wrapper : wrappers) {
            current = wrapper.wrap(current);
        }
        return current;
    }

}
