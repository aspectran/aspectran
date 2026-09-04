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
package com.aspectran.netty.server.websocket.jsr356;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.netty.server.NettyContext;
import com.aspectran.utils.Assert;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects beans annotated with {@link ServerEndpoint} or instances of
 * {@link ServerEndpointConfig} in the Aspectran bean registry and registers them
 * with the corresponding {@link NettyContext} as JSR-356 compatible endpoints.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyServerEndpointExporter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerEndpointExporter.class);

    private final ActivityContext activityContext;

    private final NettyContext nettyContext;

    @Nullable
    private List<Class<?>> annotatedEndpointClasses;

    /**
     * Creates a new exporter with the given activity context and Netty context.
     * @param activityContext the activity context containing bean definitions
     * @param nettyContext the Netty context to register endpoints with
     */
    public NettyServerEndpointExporter(@NonNull ActivityContext activityContext, @NonNull NettyContext nettyContext) {
        Assert.notNull(activityContext, "activityContext must not be null");
        Assert.notNull(nettyContext, "nettyContext must not be null");
        this.activityContext = activityContext;
        this.nettyContext = nettyContext;
    }

    /**
     * Creates a new exporter resolving the activity context from the given Netty context.
     * @param nettyContext the Netty context to register endpoints with
     */
    public NettyServerEndpointExporter(@NonNull NettyContext nettyContext) {
        Assert.notNull(nettyContext, "nettyContext must not be null");
        Assert.notNull(nettyContext.getActivityContext(), "activityContext must not be null for NettyContext");
        this.activityContext = nettyContext.getActivityContext();
        this.nettyContext = nettyContext;
    }

    /**
     * Explicitly specifies the classes annotated with {@link ServerEndpoint} to register,
     * bypassing automatic classpath/bean scanning.
     * @param annotatedEndpointClasses the endpoint classes to export
     */
    public void setAnnotatedEndpointClasses(Class<?>... annotatedEndpointClasses) {
        this.annotatedEndpointClasses = Arrays.asList(annotatedEndpointClasses);
    }

    /**
     * Scans for and registers all discovered {@link ServerEndpoint} annotated classes
     * and {@link ServerEndpointConfig} beans into the {@link NettyContext}.
     * @return the set of registered endpoint classes
     */
    public Set<Class<?>> registerEndpoints() {
        Set<Class<?>> endpointClasses = new LinkedHashSet<>();
        if (annotatedEndpointClasses != null) {
            endpointClasses.addAll(annotatedEndpointClasses);
        } else {
            endpointClasses.addAll(findServerEndpointClasses());
        }

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

    /**
     * Registers a single endpoint class annotated with {@link ServerEndpoint}.
     * @param endpointClass the annotated endpoint class
     */
    public void registerEndpoint(@NonNull Class<?> endpointClass) {
        ServerEndpoint annotation = endpointClass.getAnnotation(ServerEndpoint.class);
        Assert.notNull(annotation, "Class must be annotated with @ServerEndpoint: " + endpointClass.getName());

        String path = annotation.value();
        Object endpointInstance;
        if (activityContext.getBeanRegistry().containsBean(endpointClass)) {
            endpointInstance = activityContext.getBeanRegistry().getBean(endpointClass);
        } else {
            try {
                endpointInstance = endpointClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate @ServerEndpoint class: " + endpointClass.getName(), e);
            }
        }

        ServerEndpointConfig.Configurator configurator = null;
        if (annotation.configurator() != null && annotation.configurator() != ServerEndpointConfig.Configurator.class) {
            try {
                configurator = annotation.configurator().getDeclaredConstructor().newInstance();
            } catch (Exception ignore) {
            }
        }

        ServerEndpointConfig endpointConfig = new DefaultServerEndpointConfig(
                endpointClass,
                path,
                Arrays.asList(annotation.encoders()),
                Arrays.asList(annotation.decoders()),
                Arrays.asList(annotation.subprotocols()),
                null,
                configurator
        );

        registerEndpoint(endpointInstance, path, endpointConfig);
    }

    /**
     * Registers an endpoint defined by a programmatic {@link ServerEndpointConfig}.
     * @param endpointConfig the server endpoint configuration
     */
    public void registerEndpoint(@NonNull ServerEndpointConfig endpointConfig) {
        Class<?> endpointClass = endpointConfig.getEndpointClass();
        Object endpointInstance;
        if (activityContext.getBeanRegistry().containsBean(endpointClass)) {
            endpointInstance = activityContext.getBeanRegistry().getBean(endpointClass);
        } else {
            try {
                endpointInstance = endpointClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate endpoint class: " + endpointClass.getName(), e);
            }
        }

        registerEndpoint(endpointInstance, endpointConfig.getPath(), endpointConfig);
    }

    private void registerEndpoint(Object endpointInstance, String path, ServerEndpointConfig endpointConfig) {
        String relativePath = path;
        String contextPath = nettyContext.getContextPath();
        if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
            relativePath = path.substring(contextPath.length());
        }
        if (relativePath.isEmpty()) {
            relativePath = "/";
        }

        JsrWebSocketEndpointAdapter adapter = new JsrWebSocketEndpointAdapter(endpointInstance, endpointConfig);
        nettyContext.addWebSocketEndpoint(relativePath, adapter);

        if (logger.isInfoEnabled()) {
            logger.info("Registered JSR-356 @ServerEndpoint [{}] on NettyContext [{}] with path [{}]",
                    endpointInstance.getClass().getName(), nettyContext.getDisplayContextPath(), relativePath);
        }
    }

    private Collection<Class<?>> findServerEndpointClasses() {
        return activityContext.getBeanRegistry().findConfigBeanClassesWithAnnotation(ServerEndpoint.class);
    }

    private ServerEndpointConfig[] findServerEndpointConfigs() {
        return activityContext.getBeanRegistry().getBeansOfType(ServerEndpointConfig.class);
    }

}
