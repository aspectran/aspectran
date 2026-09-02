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

import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.netty.server.NettyContext;
import com.aspectran.netty.server.NettyServer;
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
public class NettyServerEndpointExporter implements InitializableBean, ActivityContextAware {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerEndpointExporter.class);

    private ActivityContext activityContext;

    private NettyServer nettyServer;

    private NettyContext nettyContext;

    @Nullable
    private List<Class<?>> annotatedEndpointClasses;

    public NettyServerEndpointExporter() {
    }

    public NettyServerEndpointExporter(@NonNull NettyContext nettyContext) {
        this.nettyContext = nettyContext;
    }

    public NettyServerEndpointExporter(@NonNull NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    public void setNettyServer(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    public void setNettyContext(NettyContext nettyContext) {
        this.nettyContext = nettyContext;
    }

    public void setAnnotatedEndpointClasses(Class<?>... annotatedEndpointClasses) {
        this.annotatedEndpointClasses = Arrays.asList(annotatedEndpointClasses);
    }

    @Override
    public void initialize() throws Exception {
        Assert.state(activityContext != null, "activityContext must not be null");

        if (nettyServer == null && nettyContext == null) {
            try {
                nettyServer = activityContext.getBeanRegistry().getBean(NettyServer.class);
            } catch (Exception ignore) {
            }
        }

        registerEndpoints();
    }

    public Set<Class<?>> registerEndpoints() {
        Set<Class<?>> endpointClasses = new LinkedHashSet<>();
        if (annotatedEndpointClasses != null) {
            endpointClasses.addAll(annotatedEndpointClasses);
        } else if (activityContext != null) {
            endpointClasses.addAll(findServerEndpointClasses());
        }

        for (Class<?> endpointClass : endpointClasses) {
            registerEndpoint(endpointClass);
        }

        if (activityContext != null) {
            ServerEndpointConfig[] endpointConfigs = findServerEndpointConfigs();
            if (endpointConfigs != null) {
                for (ServerEndpointConfig endpointConfig : endpointConfigs) {
                    registerEndpoint(endpointConfig);
                    endpointClasses.add(endpointConfig.getEndpointClass());
                }
            }
        }

        return endpointClasses;
    }

    public void registerEndpoint(@NonNull Class<?> endpointClass) {
        ServerEndpoint annotation = endpointClass.getAnnotation(ServerEndpoint.class);
        Assert.notNull(annotation, "Class must be annotated with @ServerEndpoint: " + endpointClass.getName());

        String path = annotation.value();
        Object endpointInstance = null;
        if (activityContext != null && activityContext.getBeanRegistry().containsBean(endpointClass)) {
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

    public void registerEndpoint(@NonNull ServerEndpointConfig endpointConfig) {
        Class<?> endpointClass = endpointConfig.getEndpointClass();
        Object endpointInstance = null;
        if (activityContext != null && activityContext.getBeanRegistry().containsBean(endpointClass)) {
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
        NettyContext targetContext = resolveTargetContext(path);
        Assert.state(targetContext != null, "No matching NettyContext found for WebSocket path: " + path);

        String relativePath = path;
        String contextPath = targetContext.getContextPath();
        if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
            relativePath = path.substring(contextPath.length());
        }
        if (relativePath.isEmpty()) {
            relativePath = "/";
        }

        JsrWebSocketEndpointAdapter adapter = new JsrWebSocketEndpointAdapter(endpointInstance, endpointConfig);
        targetContext.addWebSocketEndpoint(relativePath, adapter);

        if (logger.isInfoEnabled()) {
            logger.info("Registered JSR-356 @ServerEndpoint [{}] on NettyContext [{}] with path [{}]",
                    endpointInstance.getClass().getName(), targetContext.getDisplayContextPath(), relativePath);
        }
    }

    private NettyContext resolveTargetContext(String path) {
        if (nettyContext != null) {
            return nettyContext;
        }
        if (nettyServer != null && nettyServer.getContextRouter() != null) {
            NettyContext matched = nettyServer.getContextRouter().match(path);
            if (matched != null) {
                return matched;
            }
        }
        return null;
    }

    private Collection<Class<?>> findServerEndpointClasses() {
        return activityContext.getBeanRegistry().findConfigBeanClassesWithAnnotation(ServerEndpoint.class);
    }

    private ServerEndpointConfig[] findServerEndpointConfigs() {
        return activityContext.getBeanRegistry().getBeansOfType(ServerEndpointConfig.class);
    }

}
