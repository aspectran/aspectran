/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.resource.StaticResourceHandler;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.WebService;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletContext;
import java.util.Collection;

import static com.aspectran.web.service.WebService.ROOT_WEB_SERVICE_ATTRIBUTE;

/**
 * <p>Created: 2019-08-04</p>
 */
public class ServletHandlerFactory implements ActivityContextAware {

    private ActivityContext context;

    private TowServletContainer towServletContainer;

    private StaticResourceHandler staticResourceHandler;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public TowServletContainer getTowServletContainer() {
        return towServletContainer;
    }

    public void setTowServletContainer(TowServletContainer towServletContainer) {
        this.towServletContainer = towServletContainer;
    }

    public StaticResourceHandler getStaticResourceHandler() {
        return staticResourceHandler;
    }

    public void setStaticResourceHandler(StaticResourceHandler staticResourceHandler) {
        this.staticResourceHandler = staticResourceHandler;
    }

    public HttpHandler createServletHandler() throws Exception {
        if (towServletContainer != null) {
            PathHandler pathHandler = new PathHandler();
            Collection<String> deploymentNames = towServletContainer.listDeployments();
            for (String deploymentName : deploymentNames) {
                DeploymentManager manager = towServletContainer.getDeployment(deploymentName);
                manager.deploy();

                ServletContext servletContext = manager.getDeployment().getServletContext();
                Object attr = servletContext.getAttribute(TowServletContext.DERIVED_WEB_SERVICE_ATTRIBUTE);
                servletContext.removeAttribute(TowServletContext.DERIVED_WEB_SERVICE_ATTRIBUTE);
                if ("true".equals(attr)) {
                    CoreService rootService = context.getRootService();
                    WebService webService = DefaultWebService.create(servletContext, rootService);
                    servletContext.setAttribute(ROOT_WEB_SERVICE_ATTRIBUTE, webService);
                }

                HttpHandler handler = manager.start();
                String contextPath = manager.getDeployment().getDeploymentInfo().getContextPath();
                pathHandler.addPrefixPath(contextPath, handler);
            }
            if (staticResourceHandler != null && staticResourceHandler.hasPatterns()) {
                return exchange -> {
                    if (staticResourceHandler != null) {
                        staticResourceHandler.handleRequest(exchange);
                        if (!exchange.isDispatched() && !exchange.isComplete()) {
                            pathHandler.handleRequest(exchange);
                        }
                    }
                };
            } else {
                return pathHandler;
            }
        } else {
            return null;
        }
    }

}
