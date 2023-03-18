/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.websocket.jsr356;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects beans of type {@link ServerEndpointConfig} and registers with the standard
 * Java WebSocket runtime. Also detects beans annotated with {@link ServerEndpoint} and
 * registers them as well. Although not required, it is likely annotated endpoints should
 * have their {@code configurator} property set to {@link AspectranConfigurator}.
 *
 * <p>Created: 29/09/2019</p>
 */
public class ServerEndpointExporter {

    private static final Logger logger = LoggerFactory.getLogger(ServerEndpointExporter.class);

    private final ActivityContext context;

    @Nullable
    private ServerContainer serverContainer;

    @Nullable
    private List<Class<?>> annotatedEndpointClasses;

    public ServerEndpointExporter(ActivityContext context) {
        this.context = context;
    }

    /**
     * Return the JSR-356 {@link ServerContainer} to use for endpoint registration.
     */
    @Nullable
    public ServerContainer getServerContainer() {
        return this.serverContainer;
    }

    /**
     * Set the JSR-356 {@link ServerContainer} to use for endpoint registration.
     * If not set, the container is going to be retrieved via the {@code ServletContext}.
     */
    public void setServerContainer(@Nullable ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
    }

    public void initServletContext(ServletContext servletContext) {
        if (this.serverContainer == null) {
            this.serverContainer =
                    (ServerContainer)servletContext.getAttribute(ServerContainer.class.getName());
        }
    }

    /**
     * Explicitly list annotated endpoint types that should be registered on startup. This
     * can be done if you wish to turn off a Servlet container's scan for endpoints, which
     * goes through all 3rd party jars in the, and rely on Spring configuration instead.
     * @param annotatedEndpointClasses {@link ServerEndpoint}-annotated types
     */
    public void setAnnotatedEndpointClasses(Class<?>... annotatedEndpointClasses) {
        this.annotatedEndpointClasses = Arrays.asList(annotatedEndpointClasses);
    }

    /**
     * Actually register the endpoints.
     */
    public void registerEndpoints() {
        Assert.state(getServerContainer() != null, "javax.websocket.server.ServerContainer not available");

        Set<Class<?>> endpointClasses = new LinkedHashSet<>();
        if (this.annotatedEndpointClasses != null) {
            endpointClasses.addAll(this.annotatedEndpointClasses);
        }
        endpointClasses.addAll(context.getBeanRegistry().findConfigBeanClassesWithAnnotation(ServerEndpoint.class));
        for (Class<?> endpointClass : endpointClasses) {
            registerEndpoint(endpointClass);
        }

        ServerEndpointConfig[] endpointConfigs = context.getBeanRegistry().getBeansOfType(ServerEndpointConfig.class);
        if (endpointConfigs != null) {
            for (ServerEndpointConfig endpointConfig : endpointConfigs) {
                registerEndpoint(endpointConfig);
            }
        }
    }

    private void registerEndpoint(Class<?> endpointClass) {
        ServerContainer serverContainer = getServerContainer();
        Assert.state(serverContainer != null,
                "No ServerContainer set. Most likely the server's own WebSocket ServletContainerInitializer " +
                        "has not run yet.");
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering @ServerEndpoint class: " + endpointClass);
            }
            serverContainer.addEndpoint(endpointClass);
        } catch (DeploymentException ex) {
            throw new IllegalStateException("Failed to register @ServerEndpoint class: " + endpointClass, ex);
        }
    }

    private void registerEndpoint(ServerEndpointConfig endpointConfig) {
        ServerContainer serverContainer = getServerContainer();
        Assert.state(serverContainer != null, "No ServerContainer set");
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering ServerEndpointConfig: " + endpointConfig);
            }
            serverContainer.addEndpoint(endpointConfig);
        } catch (DeploymentException ex) {
            throw new IllegalStateException("Failed to register ServerEndpointConfig: " + endpointConfig, ex);
        }
    }

}
