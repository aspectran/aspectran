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
package com.aspectran.undertow.server.servlet;

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.resource.StaticResourceHandler;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.WebService;
import com.aspectran.websocket.jsr356.ServerEndpointExporter;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import jakarta.servlet.ServletContext;

import java.util.Arrays;
import java.util.List;

import static com.aspectran.web.service.WebService.ROOT_WEB_SERVICE_ATTR_NAME;

/**
 * <p>Created: 2019-08-04</p>
 */
public class ServletHybridHandlerFactory implements ActivityContextAware {

    private ActivityContext context;

    private ServletContainer servletContainer;

    private StaticResourceHandler staticResourceHandler;

    private List<HandlerWrapper> outerHandlerChainWrappers;

    @Override
    @AvoidAdvice
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public ServletContainer getServletContainer() {
        return servletContainer;
    }

    public void setServletContainer(ServletContainer servletContainer) {
        if (servletContainer == null) {
            throw new IllegalArgumentException("servletContainer must not be null");
        }
        this.servletContainer = servletContainer;
    }

    public StaticResourceHandler getStaticResourceHandler() {
        return staticResourceHandler;
    }

    public void setStaticResourceHandler(StaticResourceHandler staticResourceHandler) {
        if (staticResourceHandler == null) {
            throw new IllegalArgumentException("staticResourceHandler must not be null");
        }
        this.staticResourceHandler = staticResourceHandler;
    }

    public void setOuterHandlerChainWrappers(HandlerWrapper[] handlerWrappers) {
        if (handlerWrappers == null || handlerWrappers.length == 0) {
            throw new IllegalArgumentException("handlerWrappers must not be null or empty");
        }
        this.outerHandlerChainWrappers = Arrays.asList(handlerWrappers);
    }

    public HttpHandler createHandler() throws Exception {
        HttpHandler rootHandler;
        if (servletContainer != null) {
            PathHandler pathHandler = new PathHandler();
            for (String deploymentName : servletContainer.listDeployments()) {
                DeploymentManager manager = servletContainer.getDeployment(deploymentName);
                manager.deploy();
                Deployment deployment = manager.getDeployment();
                ServletContext servletContext = deployment.getServletContext();

                // Create a root web service
                CoreService rootService = context.getRootService();
                WebService rootWebService = DefaultWebService.create(servletContext, rootService);
                servletContext.setAttribute(ROOT_WEB_SERVICE_ATTR_NAME, rootWebService);

                // Required for any websocket support in undertow
                ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter(context);
                serverEndpointExporter.setServerContainer(servletContext);
                if (serverEndpointExporter.hasServerContainer()) {
                    serverEndpointExporter.registerEndpoints();
                }

                HttpHandler handler = manager.start();
                String contextPath = deployment.getDeploymentInfo().getContextPath();
                pathHandler.addPrefixPath(contextPath, handler);
            }
            rootHandler = pathHandler;
        } else {
            rootHandler = ResponseCodeHandler.HANDLE_404;
        }
        if (staticResourceHandler != null && staticResourceHandler.hasPatterns()) {
            rootHandler = new ServletHybridHandler(rootHandler, staticResourceHandler);
        }
        if (outerHandlerChainWrappers != null) {
            rootHandler = wrapHandlers(rootHandler, outerHandlerChainWrappers);
        }
        return rootHandler;
    }

    private static HttpHandler wrapHandlers(HttpHandler wrapee, List<HandlerWrapper> wrappers) {
        HttpHandler current = wrapee;
        for (HandlerWrapper wrapper : wrappers) {
            current = wrapper.wrap(current);
        }
        return current;
    }

}
