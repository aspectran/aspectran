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
package com.aspectran.undertow.server;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.lang.NonNull;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.lifecycle.AbstractLifeCycle;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.undertow.server.session.TowSessionManager;
import io.undertow.Undertow;
import io.undertow.Version;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import org.xnio.Option;
import org.xnio.OptionMap;

import java.io.IOException;

/**
 * The Undertow Server managed by Aspectran.
 *
 * @see <a href="http://undertow.io">Undertow</a>
 * @since 6.3.0
 */
public class TowServer extends AbstractLifeCycle implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TowServer.class);

    private final Undertow.Builder builder = Undertow.builder();

    private boolean autoStart;

    private int shutdownTimeout;

    private ServletContainer servletContainer;

    private HttpHandler handler;

    private Undertow server;

    /**
     * Returns whether the server starts automatically.
     * @return true if the server should be started
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Specifies whether the server should start automatically.
     * @param autoStart if the server should be started
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public int getShutdownTimeout() {
        return shutdownTimeout;
    }

    public void setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public void setHttpListeners(HttpListenerConfig... httpListenerConfigs) {
        if (httpListenerConfigs == null) {
            throw new IllegalArgumentException("httpListenerConfigs must not be null");
        }
        for (HttpListenerConfig listenerConfig : httpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setHttpsListeners(HttpsListenerConfig... httpsListenerConfigs) throws IOException {
        if (httpsListenerConfigs == null) {
            throw new IllegalArgumentException("httpsListenerConfigs must not be null");
        }
        for (HttpsListenerConfig listenerConfig : httpsListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setAjpListeners(AjpListenerConfig... ajpListenerConfigs) {
        if (ajpListenerConfigs == null) {
            throw new IllegalArgumentException("ajpListenerConfigs must not be null");
        }
        for (AjpListenerConfig listenerConfig : ajpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
        builder.setHandler(handler);
    }

    public void setBufferSize(int bufferSize) {
        builder.setBufferSize(bufferSize);
    }

    public void setIoThreads(int ioThreads) {
        builder.setIoThreads(ioThreads);
    }

    public void setWorkerThreads(int workerThreads) {
        builder.setWorkerThreads(workerThreads);
    }

    public void setDirectBuffers(final boolean directBuffers) {
        builder.setDirectBuffers(directBuffers);
    }

    public <T> void setServerOption(final Option<T> option, final T value) {
        builder.setServerOption(option, value);
    }

    public <T> void setSocketOption(final Option<T> option, final T value) {
        builder.setSocketOption(option, value);
    }

    public <T> void setWorkerOption(final Option<T> option, final T value) {
        builder.setWorkerOption(option, value);
    }

    public Undertow.Builder getBuilder() {
        return builder;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setServerOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setServerOption(option, optionMap.get(option));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setSocketOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setSocketOption(option, optionMap.get(option));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setWorkerOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setWorkerOption(option, optionMap.get(option));
            }
        }
    }

    public ServletContainer getServletContainer() {
        return servletContainer;
    }

    public void setServletContainer(ServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }

    public DeploymentManager getDeploymentManager(String deploymentName) {
        checkStarted();
        if (servletContainer == null) {
            throw new IllegalStateException("servletContainer is not set");
        }
        if (deploymentName == null) {
            throw new IllegalArgumentException("deploymentName must not be null");
        }
        return servletContainer.getDeployment(deploymentName);
    }

    public DeploymentManager getDeploymentManagerByPath(String path) {
        checkStarted();
        if (servletContainer == null) {
            throw new IllegalStateException("servletContainer is not set");
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        return servletContainer.getDeploymentByPath(path);
    }

    public SessionHandler getSessionHandler(String deploymentName) {
        DeploymentManager deploymentManager = getDeploymentManager(deploymentName);
        if (deploymentManager == null) {
            throw new IllegalStateException("Deployment named '" + deploymentName + "' not found");
        }
        SessionHandler sessionHandler = getSessionHandler(deploymentManager);
        if (sessionHandler == null) {
            throw new IllegalStateException("No session handler for the deployment named '" + deploymentName + "'");
        }
        return sessionHandler;
    }

    public SessionHandler getSessionHandlerByPath(String path) {
        DeploymentManager deploymentManager = getDeploymentManagerByPath(path);
        if (deploymentManager == null) {
            throw new IllegalStateException("Deployment with path '" + path + "' not found");
        }
        SessionHandler sessionHandler = getSessionHandler(deploymentManager);
        if (sessionHandler == null) {
            throw new IllegalStateException("No session handler for the deployment with path " + path + "'");
        }
        return sessionHandler;
    }

    @Nullable
    private SessionHandler getSessionHandler(@NonNull DeploymentManager deploymentManager) {
        Deployment deployment = deploymentManager.getDeployment();
        if (deployment != null) {
            SessionManager sessionManager = deployment.getSessionManager();
            if (sessionManager instanceof TowSessionManager) {
                return ((TowSessionManager) sessionManager).getSessionHandler();
            }
        }
        return null;
    }

    @Override
    public void doStart() throws Exception {
        try {
            server = builder.build();
            server.start();
            logger.info("Undertow " + Version.getVersionString() + " started");
        } catch (Exception e) {
            try {
                if (server != null) {
                    server.stop();
                    server = null;
                }
            } catch (Exception ex) {
                // ignore
            }
            throw new Exception("Unable to start Undertow server", e);
        }
    }

    @Override
    public void doStop() {
        try {
            if (server != null) {
                if (handler instanceof GracefulShutdownHandler) {
                    ((GracefulShutdownHandler)handler).shutdown();
                    try {
                        if (shutdownTimeout > 0) {
                            // Wait "30" seconds before make a force shutdown
                            boolean result = ((GracefulShutdownHandler)handler).awaitShutdown(shutdownTimeout * 1000L);
                            if (!result) {
                                logger.warn("Undertow server did not shut down gracefully within " +
                                        shutdownTimeout + " seconds. Proceeding with forceful shutdown");
                            }
                        } else {
                            ((GracefulShutdownHandler)handler).awaitShutdown();
                        }
                    } catch (Exception ex) {
                        logger.error("Unable to gracefully stop Undertow server");
                    }
                }
                if (servletContainer != null) {
                    for (String deploymentName : servletContainer.listDeployments()) {
                        DeploymentManager manager = servletContainer.getDeployment(deploymentName);
                        if (manager != null) {
                            manager.stop();
                            manager.undeploy();
                        }
                    }
                }
                server.stop();
                server = null;
                logger.info("Undertow " + Version.getVersionString() + " stopped");
            }
        } catch (Exception e) {
            logger.error("Unable to stop Undertow server", e);
        }
    }

    @Override
    public void initialize() throws Exception {
        if (autoStart) {
            start();
        }
    }

    @Override
    public void destroy() {
        try {
            stop();
        } catch (Exception e) {
            logger.error("Error while stopping Undertow server: " + e.getMessage(), e);
        }
    }

    protected void checkStarted() throws IllegalStateException {
        if (!isStarted()) {
            throw new IllegalStateException("Not started");
        }
    }

}
