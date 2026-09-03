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
import com.aspectran.netty.server.handler.NettyResourceHandler;
import com.aspectran.netty.server.session.NettySessionConfig;
import com.aspectran.netty.server.session.NettySessionManager;
import com.aspectran.netty.server.websocket.NettyWebSocketConfig;
import com.aspectran.netty.server.websocket.NettyWebSocketListener;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private String contextPath = "";

    private String aspectranConfigFile;

    private AspectranConfig aspectranConfig;

    private DefaultNettyService nettyService;

    private SessionManager sessionManager;

    private NettySessionConfig sessionConfig;

    private NettyResourceHandler resourceHandler;

    private boolean sessionAdaptable = true;

    private boolean trailingSlashRedirect = true;

    private final Map<String, NettyWebSocketListener> webSocketEndpoints = new ConcurrentHashMap<>();

    private NettyWebSocketConfig webSocketConfig;

    private String loggingGroup;

    private Boolean derived;

    private Boolean proxyAddressForwarding;

    public NettyContext() {
    }

    public NettyContext(String contextPath) {
        setContextPath(contextPath);
    }

    public NettyContext(String contextPath, String aspectranConfigFile) {
        setContextPath(contextPath);
        this.aspectranConfigFile = aspectranConfigFile;
    }

    public NettyContext(String contextPath, DefaultNettyService nettyService) {
        setContextPath(contextPath);
        this.nettyService = nettyService;
    }

    @NonNull
    public String getContextPath() {
        return contextPath;
    }

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
    }

    public String getDisplayContextPath() {
        return (contextPath.isEmpty() ? "/" : contextPath);
    }

    public String getAspectranConfigFile() {
        return aspectranConfigFile;
    }

    public void setAspectranConfigFile(String aspectranConfigFile) {
        this.aspectranConfigFile = aspectranConfigFile;
    }

    public AspectranConfig getAspectranConfig() {
        return aspectranConfig;
    }

    public void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    public DefaultNettyService getNettyService() {
        return nettyService;
    }

    public void setNettyService(DefaultNettyService nettyService) {
        this.nettyService = nettyService;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public NettySessionConfig getSessionConfig() {
        return sessionConfig;
    }

    public void setSessionConfig(NettySessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public NettyResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    public void setResourceHandler(NettyResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
    }

    public boolean isSessionAdaptable() {
        return sessionAdaptable;
    }

    public void setSessionAdaptable(boolean sessionAdaptable) {
        this.sessionAdaptable = sessionAdaptable;
    }

    public boolean isTrailingSlashRedirect() {
        return trailingSlashRedirect;
    }

    public void setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        this.trailingSlashRedirect = trailingSlashRedirect;
    }

    public Map<String, NettyWebSocketListener> getWebSocketEndpoints() {
        return webSocketEndpoints;
    }

    public void setWebSocketEndpoints(Map<String, NettyWebSocketListener> endpoints) {
        this.webSocketEndpoints.clear();
        if (endpoints != null) {
            endpoints.forEach(this::addWebSocketEndpoint);
        }
    }

    public void addWebSocketEndpoint(String path, NettyWebSocketListener listener) {
        Assert.notNull(path, "path must not be null");
        Assert.notNull(listener, "listener must not be null");
        String normalizedPath = (path.startsWith("/") ? path : "/" + path);
        webSocketEndpoints.put(normalizedPath, listener);
    }

    public NettyWebSocketListener getWebSocketEndpoint(@NonNull String path) {
        String normalizedPath = (path.startsWith("/") ? path : "/" + path);
        return webSocketEndpoints.get(normalizedPath);
    }

    public boolean hasWebSocketEndpoints() {
        return !webSocketEndpoints.isEmpty();
    }

    public NettyWebSocketConfig getWebSocketConfig() {
        return webSocketConfig;
    }

    public void setWebSocketConfig(NettyWebSocketConfig webSocketConfig) {
        this.webSocketConfig = webSocketConfig;
    }

    public boolean isDerived() {
        return Boolean.TRUE.equals(derived);
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public boolean isProxyAddressForwarding() {
        return Boolean.TRUE.equals(proxyAddressForwarding);
    }

    public void setProxyAddressForwarding(boolean proxyAddressForwarding) {
        this.proxyAddressForwarding = proxyAddressForwarding;
    }

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
        if (activityContext != null && StringUtils.hasText(activityContext.getName())) {
            return activityContext.getName();
        }
        if (contextPath != null && !contextPath.isEmpty() && !"/".equals(contextPath)) {
            return contextPath.startsWith("/") ? contextPath.substring(1) : contextPath;
        }
        return "root";
    }

    public void setLoggingGroup(String loggingGroup) {
        this.loggingGroup = loggingGroup;
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.activityContext = context;
    }

    @Override
    protected void doStart() throws Exception {
        if (nettyService == null) {
            CoreService masterService = (activityContext != null ? activityContext.getMasterService() : null);
            boolean isDerived = (derived != null ? derived : (aspectranConfig == null && aspectranConfigFile == null));
            if (isDerived) {
                Assert.notNull(masterService, "masterService must not be null for derived NettyContext [" +
                        getDisplayContextPath() + "]");
                nettyService = DefaultNettyServiceBuilder.build(masterService);
            } else {
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
            }
        }

        nettyService.setContextPath(contextPath);
        nettyService.setSessionAdaptable(sessionAdaptable);
        nettyService.setTrailingSlashRedirect(trailingSlashRedirect);
        if (proxyAddressForwarding != null) {
            nettyService.setProxyAddressForwarding(proxyAddressForwarding);
        }

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
            if (sessionManager instanceof Component component && !component.isInitialized()) {
                component.initialize();
            } else if (sessionManager instanceof InitializableBean initializable) {
                initializable.initialize();
            }
            nettyService.setSessionManager(sessionManager);
        }

        if (nettyService.isOrphan() && !nettyService.isActive()) {
            nettyService.start();
        }

        if (nettyService.getActivityContext() != null) {
            try {
                NettyServerEndpointExporter exporter = new NettyServerEndpointExporter(this);
                exporter.setActivityContext(nettyService.getActivityContext());
                exporter.registerEndpoints();
            } catch (Exception e) {
                logger.warn("Failed to auto-register @ServerEndpoint for NettyContext [{}]", getDisplayContextPath(), e);
            }
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (nettyService != null && nettyService.isActive()) {
            nettyService.stop();
        }
        if (sessionManager != null) {
            if (sessionManager instanceof Component component && component.isInitialized() && !component.isDestroyed()) {
                component.destroy();
            } else if (sessionManager instanceof DisposableBean disposable) {
                disposable.destroy();
            }
        }
    }

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
