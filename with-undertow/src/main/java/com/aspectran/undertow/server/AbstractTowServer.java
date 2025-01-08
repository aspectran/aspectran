/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.undertow.server.handler.RequestHandlerFactory;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import org.xnio.Option;
import org.xnio.OptionMap;

import java.io.IOException;

/**
 * <p>Created: 11/25/23</p>
 */
public abstract class AbstractTowServer extends AbstractLifeCycle implements TowServer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTowServer.class);

    private final Undertow.Builder builder = Undertow.builder();

    private boolean autoStart;

    private boolean shutdownGracefully = true;

    private int shutdownTimeoutSecs;

    private RequestHandlerFactory requestHandlerFactory;

    private HttpHandler handler;

    /**
     * Returns whether the server starts automatically.
     * @return true if the server should be started
     */
    protected boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Specifies whether the server should start automatically.
     * @param autoStart if the server should be started
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    protected boolean isShutdownGracefully() {
        return shutdownGracefully;
    }

    public void setShutdownGracefully(boolean shutdownGracefully) {
        this.shutdownGracefully = shutdownGracefully;
    }

    protected int getShutdownTimeoutSecs() {
        return shutdownTimeoutSecs;
    }

    public void setShutdownTimeoutSecs(int shutdownTimeoutSecs) {
        this.shutdownTimeoutSecs = shutdownTimeoutSecs;
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public void setHttpListeners(HttpListenerConfig... httpListenerConfigs) {
        Assert.notNull(httpListenerConfigs, "httpListenerConfigs must not be null");
        for (HttpListenerConfig listenerConfig : httpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setHttpsListeners(HttpsListenerConfig... httpsListenerConfigs) throws IOException {
        Assert.notNull(httpsListenerConfigs, "httpsListenerConfigs must not be null");
        for (HttpsListenerConfig listenerConfig : httpsListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setAjpListeners(AjpListenerConfig... ajpListenerConfigs) {
        Assert.notNull(ajpListenerConfigs, "ajpListenerConfigs must not be null");
        for (AjpListenerConfig listenerConfig : ajpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
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

    protected RequestHandlerFactory getRequestHandlerFactory() {
        Assert.state(requestHandlerFactory != null, "requestHandlerFactory is not set");
        return requestHandlerFactory;
    }

    public void setRequestHandlerFactory(RequestHandlerFactory requestHandlerFactory) {
        this.requestHandlerFactory = requestHandlerFactory;
    }

    protected Undertow buildServer() throws Exception {
        HttpHandler handler = getRequestHandlerFactory().createHandler();
        if (isShutdownGracefully()) {
            handler = new GracefulShutdownHandler(handler);
        }

        this.handler = handler;

        builder.setHandler(handler);
        return builder.build();
    }

    protected HttpHandler getHandler() {
        Assert.state(handler != null, "handler is not set");
        return handler;
    }

    protected void shutdown() throws Exception {
        if (getHandler() instanceof GracefulShutdownHandler shutdownHandler) {
            shutdownHandler.shutdown();
            try {
                if (getShutdownTimeoutSecs() > 0) {
                    boolean result = shutdownHandler.awaitShutdown(getShutdownTimeoutSecs() * 1000L);
                    if (!result) {
                        logger.warn("Undertow server did not shut down gracefully within " +
                                getShutdownTimeoutSecs() + " seconds. Proceeding with forceful shutdown");
                    }
                } else {
                    shutdownHandler.awaitShutdown();
                }
            } catch (Exception ex) {
                logger.error("Unable to gracefully stop Undertow server");
            }
        }
        getRequestHandlerFactory().dispose();
    }

    @Override
    public DeploymentManager getDeploymentManager(String deploymentName) {
        Assert.notNull(deploymentName, "deploymentName must not be null");
        return getRequestHandlerFactory().getServletContainer().getDeployment(deploymentName);
    }

    @Override
    public DeploymentManager getDeploymentManagerByPath(String path) {
        Assert.notNull(path, "path must not be null");
        return getRequestHandlerFactory().getServletContainer().getDeploymentByPath(path);
    }

    @Override
    public SessionHandler getSessionHandler(String deploymentName) {
        DeploymentManager deploymentManager = getDeploymentManager(deploymentName);
        Assert.state(deploymentManager != null, "Deployment named '" + deploymentName + "' not found");
        return getSessionHandler(deploymentManager);
    }

    @Override
    public SessionHandler getSessionHandlerByPath(String path) {
        DeploymentManager deploymentManager = getDeploymentManagerByPath(path);
        Assert.state(deploymentManager != null, "Deployment with path '\" + path + \"' not found");
        return getSessionHandler(deploymentManager);
    }

    @Nullable
    private SessionHandler getSessionHandler(@NonNull DeploymentManager deploymentManager) {
        Deployment deployment = deploymentManager.getDeployment();
        if (deployment != null) {
            SessionManager sessionManager = deployment.getSessionManager();
            if (sessionManager instanceof TowSessionManager towSessionManager) {
                return towSessionManager.getSessionHandler();
            }
        }
        return null;
    }

}
