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
package com.aspectran.netty.server;

import com.aspectran.core.component.Component;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.netty.server.handler.resource.NettyResourceHandler;
import com.aspectran.netty.server.session.NettySessionConfig;
import com.aspectran.netty.server.session.NettySessionManager;
import com.aspectran.netty.server.websocket.NettyWebSocketConfig;
import com.aspectran.netty.server.websocket.NettyWebSocketListener;
import com.aspectran.netty.server.websocket.NettyWebSocketServerContainerInitializer;
import com.aspectran.netty.server.websocket.WebSocketEndpointMatch;
import com.aspectran.netty.server.websocket.WebSocketEndpointTemplate;
import com.aspectran.netty.server.websocket.jsr356.NettyServerEndpointExporter;
import com.aspectran.netty.service.DefaultNettyService;
import com.aspectran.netty.service.DefaultNettyServiceBuilder;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an application context deployed at a specific context path on a {@link NettyServer}.
 * <p>Each context maintains its own context path, configuration, and {@link DefaultNettyService}
 * with an isolated {@link ActivityContext}. In accordance with the shared-nothing model between peers,
 * sibling child contexts cannot access or reference each other's beans.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyContext extends AbstractLifeCycle implements ActivityContextAware {

    private static final Logger logger = LoggerFactory.getLogger(NettyContext.class);

    private ActivityContext activityContext;

    private DefaultNettyService nettyService;

    private String name;

    private String contextPath = "";

    private String aspectranConfigFile;

    private AspectranConfig aspectranConfig;

    private SessionManager sessionManager;

    private NettySessionConfig sessionConfig;

    private NettyResourceHandler resourceHandler;

    private final Map<String, NettyWebSocketListener> exactWebSocketEndpoints = new ConcurrentHashMap<>();

    private final List<WebSocketEndpointTemplate> templateWebSocketEndpoints = new CopyOnWriteArrayList<>();

    private NettyWebSocketServerContainerInitializer webSocketServerContainerInitializer;

    private NettyWebSocketConfig webSocketConfig;

    private String loggingGroup;

    private Boolean proxyAddressForwarding;

    /**
     * Constructs a new {@code NettyContext} with the default root context path.
     */
    public NettyContext() {
    }

    /**
     * Constructs a new {@code NettyContext} with the specified context path.
     * @param contextPath the context path
     */
    public NettyContext(String contextPath) {
        setContextPath(contextPath);
    }

    /**
     * Constructs a new {@code NettyContext} with the specified context path and configuration file.
     * @param contextPath the context path
     * @param aspectranConfigFile the Aspectran configuration file path
     */
    public NettyContext(String contextPath, String aspectranConfigFile) {
        setContextPath(contextPath);
        this.aspectranConfigFile = aspectranConfigFile;
    }

    /**
     * Constructs a new {@code NettyContext} with the specified name, context path, and configuration file.
     * @param name the context name
     * @param contextPath the context path
     * @param aspectranConfigFile the Aspectran configuration file path
     */
    public NettyContext(String name, String contextPath, String aspectranConfigFile) {
        this.name = name;
        setContextPath(contextPath);
        this.aspectranConfigFile = aspectranConfigFile;
    }

    /**
     * Returns the name of this context.
     * <p>If not explicitly set, attempts to resolve the name from the underlying
     * {@link ActivityContext}, or returns {@code "root"} if this is the root context,
     * or the context path without the leading slash.</p>
     * @return the name of this context
     */
    @NonNull
    public String getName() {
        if (name != null) {
            return name;
        }
        if (nettyService != null && nettyService.getActivityContext() != null) {
            String ctxName = nettyService.getActivityContext().getName();
            if (StringUtils.hasText(ctxName)) {
                return ctxName;
            }
        }
        if (isRootContext()) {
            return "root";
        }
        return (contextPath.startsWith("/") ? contextPath.substring(1) : contextPath);
    }

    /**
     * Sets the name of this context.
     * @param name the context name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether this context is the root context (i.e. empty context path or {@code "/"}).
     * @return {@code true} if this context is the root context, {@code false} otherwise
     */
    public boolean isRootContext() {
        return (contextPath.isEmpty() || "/".equals(contextPath));
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.activityContext = context;
    }

    /**
     * Returns the {@link DefaultNettyService} associated with this context.
     * @return the Netty service, or {@code null} if not yet initialized
     */
    public DefaultNettyService getNettyService() {
        return nettyService;
    }

    /**
     * Returns the {@link ActivityContext} of this Netty context.
     * @return the activity context, or {@code null} if not yet available
     */
    @Nullable
    public ActivityContext getActivityContext() {
        if (nettyService != null && nettyService.getActivityContext() != null) {
            return nettyService.getActivityContext();
        }
        return activityContext;
    }

    /**
     * Returns the context path for this context.
     * <p>For the root context, this returns an empty string.</p>
     * @return the context path
     */
    @NonNull
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path for this context.
     * <p>The path is normalized so that it starts with a leading slash and contains
     * no trailing slash, except that the root context is stored as an empty string.</p>
     * @param contextPath the context path
     */
    public void setContextPath(String contextPath) {
        this.contextPath = StringUtils.nullToEmpty(contextPath);
        if (this.contextPath.equals("/")) {
            this.contextPath = "";
        } else if (!this.contextPath.isEmpty()) {
            if (!this.contextPath.startsWith("/")) {
                this.contextPath = "/" + this.contextPath;
            }
            if (this.contextPath.endsWith("/")) {
                this.contextPath = this.contextPath.substring(0, this.contextPath.length() - 1);
            }
        }
        if (this.resourceHandler != null && this.resourceHandler.getContextPath() == null) {
            this.resourceHandler.setContextPath(this.contextPath);
        }
    }

    /**
     * Returns the display context path, which returns {@code "/"} for the root context
     * instead of an empty string.
     * @return the display context path
     */
    public String getDisplayContextPath() {
        return (contextPath.isEmpty() ? "/" : contextPath);
    }

    /**
     * Returns the Aspectran configuration file location.
     * @return the configuration file location, or {@code null} if not specified
     */
    public String getAspectranConfigFile() {
        return aspectranConfigFile;
    }

    /**
     * Sets the Aspectran configuration file location.
     * @param aspectranConfigFile the configuration file location
     */
    public void setAspectranConfigFile(String aspectranConfigFile) {
        this.aspectranConfigFile = aspectranConfigFile;
    }

    /**
     * Returns the {@link AspectranConfig} applied to this context.
     * @return the Aspectran configuration, or {@code null} if not configured
     */
    public AspectranConfig getAspectranConfig() {
        return aspectranConfig;
    }

    /**
     * Sets the {@link AspectranConfig} for this context.
     * @param aspectranConfig the Aspectran configuration
     */
    public void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    /**
     * Returns the session manager for this context.
     * <p>If a custom session manager is not directly set on this context,
     * attempts to delegate to the underlying {@link DefaultNettyService}.</p>
     * @return the session manager, or {@code null} if not configured
     */
    public SessionManager getSessionManager() {
        if (sessionManager != null) {
            return sessionManager;
        }
        if (nettyService != null) {
            return nettyService.getSessionManager();
        }
        return null;
    }

    /**
     * Sets the session manager for this context.
     * @param sessionManager the session manager
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Returns the session configuration for this context.
     * @return the session configuration, or {@code null} if not configured
     */
    public NettySessionConfig getSessionConfig() {
        return sessionConfig;
    }

    /**
     * Sets the session configuration for this context.
     * @param sessionConfig the session configuration
     */
    public void setSessionConfig(NettySessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    /**
     * Returns the static resource handler for this context.
     * @return the resource handler, or {@code null} if not configured
     */
    public NettyResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    /**
     * Sets the static resource handler for this context.
     * @param resourceHandler the resource handler
     */
    public void setResourceHandler(NettyResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
        if (resourceHandler != null && resourceHandler.getContextPath() == null) {
            resourceHandler.setContextPath(this.contextPath);
        }
    }

    /**
     * Returns an unmodifiable map of all registered WebSocket endpoints
     * (both exact paths and URI template patterns) and their associated listeners.
     * @return an unmodifiable map of WebSocket endpoints
     */
    public Map<String, NettyWebSocketListener> getWebSocketEndpoints() {
        Map<String, NettyWebSocketListener> map = new LinkedHashMap<>(exactWebSocketEndpoints);
        for (WebSocketEndpointTemplate template : templateWebSocketEndpoints) {
            map.put(template.getPattern(), template.getListener());
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Sets the WebSocket endpoints for this context.
     * @param endpoints a map of path patterns to WebSocket listeners
     */
    public void setWebSocketEndpoints(Map<String, NettyWebSocketListener> endpoints) {
        this.exactWebSocketEndpoints.clear();
        this.templateWebSocketEndpoints.clear();
        if (endpoints != null) {
            endpoints.forEach(this::addWebSocketEndpoint);
        }
    }

    /**
     * Registers a WebSocket endpoint at the specified path.
     * <p>If the path contains URI template variables (e.g. {@code {token}}),
     * it is registered as a template endpoint.</p>
     * @param path the endpoint path or URI template pattern
     * @param listener the WebSocket listener to handle events
     */
    public void addWebSocketEndpoint(String path, NettyWebSocketListener listener) {
        Assert.notNull(path, "path must not be null");
        Assert.notNull(listener, "listener must not be null");
        String normalizedPath = (path.startsWith("/") ? path : "/" + path);
        if (normalizedPath.contains("{") && normalizedPath.contains("}")) {
            templateWebSocketEndpoints.add(new WebSocketEndpointTemplate(normalizedPath, listener));
            Collections.sort(templateWebSocketEndpoints);
        } else {
            exactWebSocketEndpoints.put(normalizedPath, listener);
        }
    }

    /**
     * Matches the specified request path against registered WebSocket endpoints.
     * @param path the request path to match
     * @return the matching endpoint and extracted path parameters, or {@code null} if no match found
     */
    @Nullable
    public WebSocketEndpointMatch matchWebSocketEndpoint(@NonNull String path) {
        String normalizedPath = (path.startsWith("/") ? path : "/" + path);
        NettyWebSocketListener exact = exactWebSocketEndpoints.get(normalizedPath);
        if (exact != null) {
            return new WebSocketEndpointMatch(exact, Collections.emptyMap());
        }
        for (WebSocketEndpointTemplate template : templateWebSocketEndpoints) {
            Map<String, String> params = template.match(normalizedPath);
            if (params != null) {
                return new WebSocketEndpointMatch(template.getListener(), params);
            }
        }
        return null;
    }

    /**
     * Returns the {@link NettyWebSocketListener} matching the specified path.
     * @param path the request path
     * @return the matching WebSocket listener, or {@code null} if not found
     */
    @Nullable
    public NettyWebSocketListener getWebSocketEndpoint(@NonNull String path) {
        WebSocketEndpointMatch match = matchWebSocketEndpoint(path);
        return (match != null ? match.getListener() : null);
    }

    /**
     * Returns whether any WebSocket endpoints are registered in this context.
     * @return {@code true} if there are registered WebSocket endpoints, {@code false} otherwise
     */
    public boolean hasWebSocketEndpoints() {
        return !exactWebSocketEndpoints.isEmpty() || !templateWebSocketEndpoints.isEmpty();
    }

    /**
     * Returns the WebSocket configuration for this context.
     * @return the WebSocket configuration, or {@code null} if not configured
     */
    public NettyWebSocketConfig getWebSocketConfig() {
        return webSocketConfig;
    }

    /**
     * Sets the WebSocket configuration for this context.
     * @param webSocketConfig the WebSocket configuration
     */
    public void setWebSocketConfig(NettyWebSocketConfig webSocketConfig) {
        this.webSocketConfig = webSocketConfig;
    }

    /**
     * Returns the WebSocket server container initializer for this context.
     * @return the WebSocket server container initializer, or {@code null} if not configured
     */
    public NettyWebSocketServerContainerInitializer getWebSocketServerContainerInitializer() {
        return webSocketServerContainerInitializer;
    }

    /**
     * Sets the WebSocket server container initializer for this context.
     * @param webSocketServerContainerInitializer the WebSocket server container initializer
     */
    public void setWebSocketServerContainerInitializer(
            NettyWebSocketServerContainerInitializer webSocketServerContainerInitializer) {
        this.webSocketServerContainerInitializer = webSocketServerContainerInitializer;
    }

    /**
     * Returns whether WebSocket support is configured for this context.
     * @return true if WebSocket configuration or container initializer is present
     */
    public boolean hasWebSocketConfig() {
        return (webSocketConfig != null || webSocketServerContainerInitializer != null);
    }

    /**
     * Returns whether proxy address forwarding is enabled for this context.
     * @return {@code true} if proxy address forwarding is enabled, {@code false} otherwise
     */
    public boolean isProxyAddressForwarding() {
        return Boolean.TRUE.equals(proxyAddressForwarding);
    }

    /**
     * Sets whether proxy address forwarding is enabled for this context.
     * @param proxyAddressForwarding {@code true} to enable, {@code false} to disable
     */
    public void setProxyAddressForwarding(boolean proxyAddressForwarding) {
        this.proxyAddressForwarding = proxyAddressForwarding;
    }

    /**
     * Returns the logging group name for this context.
     * <p>If not explicitly specified, attempts to resolve the name from the underlying
     * {@link ActivityContext}, or the context path without the leading slash.</p>
     * @return the logging group name, or {@code null} if not resolved
     */
    @Nullable
    public String getLoggingGroup() {
        if (loggingGroup != null) {
            return loggingGroup;
        }
        if (nettyService != null && nettyService.getActivityContext() != null) {
            String name = nettyService.getActivityContext().getName();
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        if (contextPath != null && !contextPath.isEmpty() && !"/".equals(contextPath)) {
            return contextPath.startsWith("/") ? contextPath.substring(1) : contextPath;
        }
        return null;
    }

    /**
     * Sets the logging group name for this context.
     * @param loggingGroup the logging group name
     */
    public void setLoggingGroup(String loggingGroup) {
        this.loggingGroup = loggingGroup;
    }

    @Override
    protected void doStart() throws Exception {
        createNettyService();
        initSessionManager();

        if (webSocketServerContainerInitializer != null) {
            webSocketServerContainerInitializer.initialize(this);
        }

        if (nettyService.isOrphan() && !nettyService.isActive()) {
            nettyService.start();
        }

        if (resourceHandler != null && resourceHandler.getContextPath() == null) {
            resourceHandler.setContextPath(contextPath);
        }
    }

    @Override
    protected void doStop() throws Exception {
        destroyNettyService();
        destroySessionManager();
        exactWebSocketEndpoints.clear();
        templateWebSocketEndpoints.clear();
    }

    private void createNettyService() throws Exception {
        CoreService masterService = (activityContext != null ? activityContext.getMasterService() : null);
        if (aspectranConfig == null && aspectranConfigFile != null) {
            aspectranConfig = loadAspectranConfig(aspectranConfigFile);
        }
        if (aspectranConfig != null) {
            nettyService = DefaultNettyServiceBuilder.build(masterService, aspectranConfig);
        } else if (masterService != null) {
            nettyService = DefaultNettyServiceBuilder.build(masterService);
        } else {
            throw new IllegalStateException("Neither aspectranConfig nor masterService is available for NettyContext [" +
                    getDisplayContextPath() + "]");
        }

        nettyService.setContextPath(contextPath);
        nettyService.setNettyContext(this);
        if (proxyAddressForwarding != null) {
            nettyService.setProxyAddressForwarding(proxyAddressForwarding);
        }
    }

    private void destroyNettyService() {
        if (nettyService != null) {
            if (nettyService.isActive()) {
                nettyService.stop();
            }
            nettyService.withdraw();
            nettyService = null;
        }
    }

    private void initSessionManager() throws Exception {
        if (sessionManager instanceof NettySessionManager nettySessionManager) {
            if (sessionConfig == null) {
                sessionConfig = nettySessionManager.getSessionConfig();
            }
        }
        if (sessionConfig != null) {
            if (sessionConfig.getCookiePath() == null) {
                sessionConfig.setCookiePath(getDisplayContextPath());
            }
            nettyService.setSessionConfig(sessionConfig);
        } else if (nettyService.getSessionConfig() != null && nettyService.getSessionConfig().getCookiePath() == null) {
            nettyService.getSessionConfig().setCookiePath(getDisplayContextPath());
        }

        if (sessionManager != null) {
            if (sessionManager instanceof Component component) {
                if (!component.isInitialized()) {
                    component.initialize();
                }
            } else if (sessionManager instanceof InitializableBean initializable) {
                initializable.initialize();
            }
            nettyService.setSessionManager(sessionManager);
        }
    }

    private void destroySessionManager() throws Exception {
        if (sessionManager != null) {
            if (sessionManager instanceof Component component) {
                if (component.isInitialized() && !component.isDestroyed()) {
                    component.destroy();
                }
            } else if (sessionManager instanceof DisposableBean disposable) {
                disposable.destroy();
            }
        }
    }

    /**
     * Scans and exports all {@link jakarta.websocket.server.ServerEndpoint} annotated classes
     * within this context's {@link ActivityContext} to this Netty context.
     */
    public void exportServerEndpoints() {
        if (!hasWebSocketConfig()) {
            return;
        }
        if (nettyService != null && nettyService.getActivityContext() != null) {
            try {
                NettyServerEndpointExporter exporter = new NettyServerEndpointExporter(nettyService.getActivityContext(), this);
                Set<Class<?>> endpointClasses = exporter.registerEndpoints();
                for (Class<?> endpointClass : endpointClasses) {
                    CoreServiceHolder.hold(endpointClass, nettyService);
                }
            } catch (Exception e) {
                logger.warn("Failed to auto-register @ServerEndpoint for NettyContext [{}]", getDisplayContextPath(), e);
            }
        }
    }

    /**
     * Loads the {@link AspectranConfig} from the specified location.
     * @param location the configuration file location or resource path
     * @return the loaded Aspectran configuration, or {@code null} if {@code location} is {@code null}
     * @throws IOException if an error occurs while reading the configuration
     */
    protected AspectranConfig loadAspectranConfig(String location) throws IOException {
        if (location == null) {
            return null;
        }
        if (location.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String resourcePath = location.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()).stripLeading();
            try (Reader reader = ResourceUtils.getResourceAsReader(resourcePath)) {
                return new AspectranConfig(reader);
            }
        } else if (location.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            String filePath = location.substring(ResourceUtils.FILE_URL_PREFIX.length()).stripLeading();
            return new AspectranConfig(new File(filePath));
        } else {
            if (ClassUtils.getDefaultClassLoader().getResource(location) != null) {
                try (Reader reader = ResourceUtils.getResourceAsReader(location)) {
                    return new AspectranConfig(reader);
                }
            }
            File file = new File(location);
            if (file.isFile()) {
                return new AspectranConfig(file);
            }
            return new AspectranConfig(location);
        }
    }

    @Override
    public String toString() {
        return "NettyContext[" + getDisplayContextPath() + "]";
    }

}
