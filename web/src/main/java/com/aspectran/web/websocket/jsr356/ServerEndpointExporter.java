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
package com.aspectran.web.websocket.jsr356;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.service.WebService;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.Arrays;
import java.util.Collection;
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

    private final WebService webService;

    private final ServerContainer serverContainer;

    @Nullable
    private List<Class<?>> annotatedEndpointClasses;

    public ServerEndpointExporter(@NonNull WebService webService) {
        this.webService = webService;
        this.serverContainer = (ServerContainer) webService.getServletContext().getAttribute(ServerContainer.class.getName());
    }

    /**
     * Return the JSR-356 {@link ServerContainer} to use for endpoint registration.
     */
    @Nullable
    public ServerContainer getServerContainer() {
        return this.serverContainer;
    }

    public boolean hasServerContainer() {
        return (this.serverContainer != null);
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
    public Set<Class<?>> registerEndpoints() {
        Assert.state(getServerContainer() != null,
            "jakarta.websocket.server.ServerContainer not available");

        Set<Class<?>> endpointClasses = new LinkedHashSet<>();
        if (this.annotatedEndpointClasses != null) {
            endpointClasses.addAll(this.annotatedEndpointClasses);
        }
        endpointClasses.addAll(findServerEndpointClasses());
        for (Class<?> endpointClass : endpointClasses) {
            registerEndpoint(endpointClass);
        }

        ServerEndpointConfig[] endpointConfigs = findServerEndpointConfigs();
        if (endpointConfigs != null) {
            for (ServerEndpointConfig endpointConfig : endpointConfigs) {
                registerEndpoint(endpointConfig);
                endpointClasses.add(endpointConfig.getEndpointClass());
            }
        }
        return endpointClasses;
    }

    private void registerEndpoint(Class<?> endpointClass) {
        ServerContainer serverContainer = getServerContainer();
        Assert.state(serverContainer != null,
                "No ServerContainer set. Most likely the server's own WebSocket ServletContainerInitializer " +
                        "has not run yet.");
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering @ServerEndpoint: " + endpointClass);
            }
            serverContainer.addEndpoint(endpointClass);
        } catch (DeploymentException ex) {
            throw new IllegalStateException("Failed to register @ServerEndpoint: " + endpointClass, ex);
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

    private Collection<Class<?>> findServerEndpointClasses() {
        return webService.getActivityContext().getBeanRegistry().findConfigBeanClassesWithAnnotation(ServerEndpoint.class);
    }

    private ServerEndpointConfig[] findServerEndpointConfigs() {
        return webService.getActivityContext().getBeanRegistry().getBeansOfType(ServerEndpointConfig.class);
    }

}
