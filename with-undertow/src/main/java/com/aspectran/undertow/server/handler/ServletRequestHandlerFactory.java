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

import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.handler.logging.LoggingGroupAssistHandlerWrapper;
import com.aspectran.undertow.server.handler.resource.TowResourceHandler;
import com.aspectran.undertow.server.handler.session.SessionAttachmentHandler;
import com.aspectran.undertow.server.servlet.TowServletContext;
import com.aspectran.undertow.server.servlet.TowWebSocketServerContainerInitializer;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.utils.Assert;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import com.aspectran.web.service.WebService;
import com.aspectran.web.service.WebServiceClassLoader;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.core.ServletContainerImpl;
import jakarta.servlet.ServletContext;
import org.jspecify.annotations.NonNull;

/**
 * A factory for creating a root {@link HttpHandler} that manages a full servlet environment.
 * <p>This factory creates an Undertow {@link ServletContainer} and deploys one or more
 * web applications (defined by {@link TowServletContext} beans) into it. It uses a
 * {@link PathHandler} to route incoming requests to the appropriate web application
 * based on its context path.</p>
 *
 * <p>Created: 2019-08-04</p>
 */
public class ServletRequestHandlerFactory extends AbstractRequestHandlerFactory implements RequestHandlerFactory {

    private TowServletContext[] towServletContexts;

    private ServletContainer servletContainer;

    /**
     * Sets the servlet contexts (web applications) to be deployed.
     * @param servletContexts an array of {@link TowServletContext} configurations
     */
    public void setServletContexts(TowServletContext... servletContexts) {
        Assert.notNull(servletContexts, "servletContexts must not be null");
        this.towServletContexts = servletContexts;
    }

    /**
     * Creates the root {@link HttpHandler} which is a {@link PathHandler} that routes
     * requests to the appropriate deployed web application.
     * @return the root HTTP handler
     * @throws Exception if an error occurs during deployment
     */
    @Override
    public HttpHandler createHandler() throws Exception {
        createServletContainer();

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

        return wrapHandler(pathHandler);
    }

    @Override
    public ServletContainer getServletContainer() {
        Assert.notNull(servletContainer, "servletContainer not yet created");
        return servletContainer;
    }

    /**
     * Disposes of the servlet container, undeploying all web applications.
     * @throws Exception if an error occurs during disposal
     */
    @Override
    public void dispose() throws Exception {
        disposeServletContainer();
    }

    /**
     * Creates and configures the {@link ServletContainer}, deploying all specified
     * {@link TowServletContext}s.
     * @throws Exception if an error occurs during deployment
     */
    private void createServletContainer() throws Exception {
        Assert.state(servletContainer == null, "ServletContainer is already configured");
        servletContainer = new ServletContainerImpl();
        if (towServletContexts == null) {
            towServletContexts = getActivityContext().getBeanRegistry().getBeansOfType(TowServletContext.class);
        }
        if (towServletContexts != null) {
            for (TowServletContext towServletContext : towServletContexts) {
                ClassLoader webServiceClassLoader = new WebServiceClassLoader(getActivityContext().getClassLoader());
                towServletContext.setClassLoader(webServiceClassLoader);

                if (hasLoggingGroupHandlerWrapper()) {
                    towServletContext.addOuterHandlerChainWrapper(new LoggingGroupAssistHandlerWrapper());
                }

                DeploymentManager manager = servletContainer.addDeployment(towServletContext);
                manager.deploy();

                ServletContext servletContext = manager.getDeployment().getServletContext();
                DefaultWebService rootWebService = createRootWebService(servletContext);
                if (towServletContext.getTowSessionManager() != null) {
                    towServletContext.getTowSessionManager().start(); // for lazy stop
                } else {
                    rootWebService.setSessionAdaptable(false);
                }
            }
        }
    }

    /**
     * Stops and undeploys all applications from the servlet container.
     * @throws Exception if an error occurs during undeployment
     */
    private void disposeServletContainer() throws Exception {
        for (String deploymentName : getServletContainer().listDeployments()) {
            DeploymentManager manager = getServletContainer().getDeployment(deploymentName);
            if (manager != null) {
                Deployment deployment = manager.getDeployment();
                SessionManager sessionManager = deployment.getSessionManager();
                ServletContext servletContext = deployment.getServletContext();

                DefaultWebService webService = WebService.findWebService(servletContext);
                if (webService.isActive()) {
                    webService.pause();
                }

                TowWebSocketServerContainerInitializer.destroy(deployment);

                manager.stop();
                manager.undeploy();

                disposeRootWebService(webService);

                if (sessionManager instanceof TowSessionManager towSessionManager) {
                    towSessionManager.stop(); // for lazy stop
                }
            }
        }
        servletContainer = null;
    }

    /**
     * Creates and starts the root {@link WebService} for a given servlet context.
     * @param servletContext the servlet context
     * @return the created and started web service
     * @throws Exception if the service fails to start
     */
    @NonNull
    private DefaultWebService createRootWebService(ServletContext servletContext) throws Exception {
        CoreService masterService = getActivityContext().getMasterService();
        DefaultWebService rootWebService = DefaultWebServiceBuilder.build(servletContext, masterService);
        if (rootWebService.isOrphan()) {
            rootWebService.start();
        }
        return rootWebService;
    }

    /**
     * Stops and withdraws a {@link WebService}.
     * @param webService the web service to dispose of
     */
    private void disposeRootWebService(@NonNull DefaultWebService webService) {
        if (webService.isActive()) {
            webService.stop();
        }
        webService.withdraw();
    }

}
