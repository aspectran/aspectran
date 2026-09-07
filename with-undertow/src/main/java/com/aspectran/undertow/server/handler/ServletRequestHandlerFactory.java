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
import com.aspectran.undertow.server.handler.resource.TowResourceManager;
import com.aspectran.undertow.server.handler.session.SessionAttachmentHandler;
import com.aspectran.undertow.server.servlet.TowServletContext;
import com.aspectran.undertow.server.servlet.TowWebSocketServerContainerInitializer;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.utils.Assert;
import com.aspectran.utils.PathUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.service.WebService;
import com.aspectran.web.service.WebServiceClassLoader;
import com.aspectran.web.servlet.service.DefaultServletWebService;
import com.aspectran.web.servlet.service.DefaultServletWebServiceBuilder;
import com.aspectran.web.servlet.service.ServletWebService;
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

import java.util.Arrays;
import java.util.Map;

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
            if (contextPath == null) {
                contextPath = "";
            }
            ResourceManager resourceManager = info.getResourceManager();

            if (resourceManager != null) {
                TowResourceHandler resourceHandler = new TowResourceHandler(resourceManager, handler);
                String pathPrefix = contextPath;
                if (pathPrefix.endsWith("/")) {
                    pathPrefix = pathPrefix.substring(0, pathPrefix.length() - 1);
                }
                resourceHandler.autoDetect(pathPrefix);
                if (resourceHandler.hasPatterns()) {
                    handler = resourceHandler;
                }
            }

            pathHandler.addPrefixPath(contextPath.isEmpty() ? "/" : contextPath, handler);

            if (resourceManager instanceof TowResourceManager trm) {
                Map<String, String> mappings = trm.getResourceMappings();
                if (mappings != null) {
                    for (Map.Entry<String, String> entry : mappings.entrySet()) {
                        String path = entry.getKey();
                        String base = PathUtils.cleanPath(entry.getValue());

                        TowResourceManager newTrm = new TowResourceManager();
                        newTrm.setApplicationAdapter(trm.getApplicationAdapter());
                        newTrm.setBase(base);

                        TowResourceHandler resourceHandler = new TowResourceHandler(newTrm);
                        String fullPath = contextPath;
                        if (StringUtils.hasLength(path)) {
                            if (!fullPath.endsWith("/") && !path.startsWith("/")) {
                                fullPath += "/";
                            } else if (fullPath.endsWith("/") && path.startsWith("/")) {
                                path = path.substring(1);
                            }
                            fullPath += path;
                        }
                        if (fullPath.isEmpty()) {
                            fullPath = "/";
                        }
                        resourceHandler.autoDetect(fullPath);
                        pathHandler.addPrefixPath(fullPath, resourceHandler);
                    }
                }
            }
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
            // Sort contexts: root context takes priority, then by order ascending
            Arrays.sort(towServletContexts, (c1, c2) -> {
                boolean root1 = c1.isRootContext();
                boolean root2 = c2.isRootContext();
                if (root1 != root2) {
                    return root1 ? -1 : 1;
                }
                return Integer.compare(c1.getOrder(), c2.getOrder());
            });

            DeploymentManager[] managers = new DeploymentManager[towServletContexts.length];
            for (int i = 0; i < towServletContexts.length; i++) {
                TowServletContext towServletContext = towServletContexts[i];
                ClassLoader webServiceClassLoader = new WebServiceClassLoader(getActivityContext().getClassLoader());
                towServletContext.setClassLoader(webServiceClassLoader);

                if (hasLoggingGroupHandlerWrapper()) {
                    towServletContext.addOuterHandlerChainWrapper(new LoggingGroupAssistHandlerWrapper());
                }

                DeploymentManager manager = servletContainer.addDeployment(towServletContext);
                manager.deploy();
                managers[i] = manager;
            }
            for (int i = 0; i < towServletContexts.length; i++) {
                TowServletContext towServletContext = towServletContexts[i];
                DeploymentManager manager = managers[i];
                ServletContext servletContext = manager.getDeployment().getServletContext();
                DefaultServletWebService rootWebService = createRootWebService(servletContext);
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
        if (servletContainer == null) {
            return;
        }
        if (towServletContexts != null && towServletContexts.length > 0) {
            // Stop and undeploy child contexts first in reverse order of startup (LIFO)
            for (int i = towServletContexts.length - 1; i > 0; i--) {
                TowServletContext towServletContext = towServletContexts[i];
                disposeDeployment(towServletContext.getDeploymentName());
            }
            // Finally stop and undeploy the root context
            disposeDeployment(towServletContexts[0].getDeploymentName());
        }
        servletContainer = null;
    }

    private void disposeDeployment(String deploymentName) throws Exception {
        if (servletContainer == null || deploymentName == null) {
            return;
        }
        DeploymentManager manager = servletContainer.getDeployment(deploymentName);
        if (manager != null && manager.getState() != DeploymentManager.State.UNDEPLOYED) {
            Deployment deployment = manager.getDeployment();
            SessionManager sessionManager = (deployment != null ? deployment.getSessionManager() : null);
            ServletContext servletContext = (deployment != null ? deployment.getServletContext() : null);

            DefaultServletWebService webService = null;
            if (servletContext != null) {
                try {
                    webService = ServletWebService.findWebService(servletContext);
                } catch (IllegalStateException e) {
                    // ignored if webService was not created or bound
                }
            }
            if (webService != null && webService.isActive()) {
                webService.pause();
            }

            if (deployment != null) {
                TowWebSocketServerContainerInitializer.destroy(deployment);
            }

            if (manager.getState() == DeploymentManager.State.STARTED) {
                manager.stop();
            }
            manager.undeploy();

            if (webService != null) {
                disposeRootWebService(webService);
            }

            if (sessionManager instanceof TowSessionManager towSessionManager) {
                towSessionManager.stop(); // for lazy stop
            }
        }
    }

    /**
     * Creates and starts the root {@link WebService} for a given servlet context.
     * @param servletContext the servlet context
     * @return the created and started web service
     * @throws Exception if the service fails to start
     */
    @NonNull
    private DefaultServletWebService createRootWebService(ServletContext servletContext) throws Exception {
        CoreService masterService = getActivityContext().getMasterService();
        DefaultServletWebService rootWebService = DefaultServletWebServiceBuilder.build(servletContext, masterService);
        if (rootWebService.isOrphan()) {
            rootWebService.start();
        }
        return rootWebService;
    }

    /**
     * Stops and withdraws a {@link WebService}.
     * @param webService the web service to dispose of
     */
    private void disposeRootWebService(@NonNull DefaultServletWebService webService) {
        if (webService.isActive()) {
            webService.stop();
        }
        webService.withdraw();
    }

}
