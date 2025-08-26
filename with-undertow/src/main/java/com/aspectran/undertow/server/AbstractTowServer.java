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
package com.aspectran.undertow.server;

import com.aspectran.core.component.session.SessionManager;
import com.aspectran.undertow.server.handler.RequestHandlerFactory;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Option;
import org.xnio.OptionMap;

import java.io.IOException;

/**
 * Abstract base class for {@link TowServer} implementations.
 * <p>This class provides the fundamental infrastructure for an embedded Undertow server,
 * managing its lifecycle and configuration via an {@link Undertow.Builder}. It offers
 * setters for common server options like listeners, threads, and buffers, which can be
 * configured in a bean-style manner.</p>
 *
 * <p>Created: 11/25/23</p>
 */
public abstract class AbstractTowServer extends AbstractLifeCycle implements TowServer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTowServer.class);

    private final Undertow.Builder builder = Undertow.builder();

    private boolean autoStart = true;

    private boolean shutdownGracefully = true;

    private int shutdownTimeoutSecs;

    private RequestHandlerFactory requestHandlerFactory;

    private HttpHandler handler;

    /**
     * Returns whether the server should start automatically when the context is initialized.
     * @return true if auto-start is enabled
     */
    protected boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Sets whether the server should start automatically.
     * @param autoStart true to enable auto-start
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /**
     * Returns whether to attempt a graceful shutdown.
     * @return true if graceful shutdown is enabled
     */
    protected boolean isShutdownGracefully() {
        return shutdownGracefully;
    }

    /**
     * Sets whether to attempt a graceful shutdown.
     * @param shutdownGracefully true to enable graceful shutdown
     */
    public void setShutdownGracefully(boolean shutdownGracefully) {
        this.shutdownGracefully = shutdownGracefully;
    }

    /**
     * Returns the timeout in seconds for a graceful shutdown.
     * @return the shutdown timeout in seconds
     */
    protected int getShutdownTimeoutSecs() {
        return shutdownTimeoutSecs;
    }

    /**
     * Sets the timeout in seconds for a graceful shutdown.
     * @param shutdownTimeoutSecs the shutdown timeout in seconds
     */
    public void setShutdownTimeoutSecs(int shutdownTimeoutSecs) {
        this.shutdownTimeoutSecs = shutdownTimeoutSecs;
    }

    /**
     * A utility method to set a Java system property.
     * @param key the property key
     * @param value the property value
     */
    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    /**
     * Adds HTTP listeners to the server.
     * @param httpListenerConfigs an array of HTTP listener configurations
     */
    public void setHttpListeners(HttpListenerConfig... httpListenerConfigs) {
        Assert.notNull(httpListenerConfigs, "httpListenerConfigs must not be null");
        for (HttpListenerConfig listenerConfig : httpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    /**
     * Adds HTTPS listeners to the server.
     * @param httpsListenerConfigs an array of HTTPS listener configurations
     * @throws IOException if an SSL context cannot be created
     */
    public void setHttpsListeners(HttpsListenerConfig... httpsListenerConfigs) throws IOException {
        Assert.notNull(httpsListenerConfigs, "httpsListenerConfigs must not be null");
        for (HttpsListenerConfig listenerConfig : httpsListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    /**
     * Adds AJP listeners to the server.
     * @param ajpListenerConfigs an array of AJP listener configurations
     */
    public void setAjpListeners(AjpListenerConfig... ajpListenerConfigs) {
        Assert.notNull(ajpListenerConfigs, "ajpListenerConfigs must not be null");
        for (AjpListenerConfig listenerConfig : ajpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    /**
     * Sets the buffer size for the server.
     * @param bufferSize the buffer size in bytes
     */
    public void setBufferSize(int bufferSize) {
        builder.setBufferSize(bufferSize);
    }

    /**
     * Sets the number of I/O threads.
     * @param ioThreads the number of I/O threads
     */
    public void setIoThreads(int ioThreads) {
        builder.setIoThreads(ioThreads);
    }

    /**
     * Sets the number of worker threads.
     * @param workerThreads the number of worker threads
     */
    public void setWorkerThreads(int workerThreads) {
        builder.setWorkerThreads(workerThreads);
    }

    /**
     * Sets whether to use direct buffers.
     * @param directBuffers true to use direct buffers
     */
    public void setDirectBuffers(final boolean directBuffers) {
        builder.setDirectBuffers(directBuffers);
    }

    /**
     * Sets a low-level server option.
     * @param option the XNIO option
     * @param value the option value
     * @param <T> the type of the option value
     */
    public <T> void setServerOption(final Option<T> option, final T value) {
        builder.setServerOption(option, value);
    }

    /**
     * Sets a low-level socket option.
     * @param option the XNIO option
     * @param value the option value
     * @param <T> the type of the option value
     */
    public <T> void setSocketOption(final Option<T> option, final T value) {
        builder.setSocketOption(option, value);
    }

    /**
     * Sets a low-level worker option.
     * @param option the XNIO option
     * @param value the option value
     * @param <T> the type of the option value
     */
    public <T> void setWorkerOption(final Option<T> option, final T value) {
        builder.setWorkerOption(option, value);
    }

    /**
     * Sets multiple server options from a {@link TowOptions} object.
     * @param options the server options to set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setServerOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setServerOption(option, optionMap.get(option));
            }
        }
    }

    /**
     * Sets multiple socket options from a {@link TowOptions} object.
     * @param options the socket options to set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setSocketOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setSocketOption(option, optionMap.get(option));
            }
        }
    }

    /**
     * Sets multiple worker options from a {@link TowOptions} object.
     * @param options the worker options to set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setWorkerOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setWorkerOption(option, optionMap.get(option));
            }
        }
    }

    /**
     * Returns the factory used to create the root HTTP handler.
     * @return the request handler factory
     */
    protected RequestHandlerFactory getRequestHandlerFactory() {
        Assert.state(requestHandlerFactory != null, "requestHandlerFactory is not set");
        return requestHandlerFactory;
    }

    /**
     * Sets the factory used to create the root HTTP handler.
     * @param requestHandlerFactory the request handler factory
     */
    public void setRequestHandlerFactory(RequestHandlerFactory requestHandlerFactory) {
        this.requestHandlerFactory = requestHandlerFactory;
    }

    /**
     * Builds the final {@link Undertow} server instance.
     * <p>This method creates the root handler, wraps it in a {@link GracefulShutdownHandler}
     * if enabled, and constructs the server.</p>
     * @return the configured Undertow server instance
     * @throws Exception if an error occurs during handler creation
     */
    protected Undertow buildServer() throws Exception {
        HttpHandler handler = getRequestHandlerFactory().createHandler();
        if (isShutdownGracefully()) {
            handler = new GracefulShutdownHandler(handler);
        }

        this.handler = handler;

        builder.setHandler(handler);
        return builder.build();
    }

    /**
     * Returns the root {@link HttpHandler} for the server.
     * @return the root HTTP handler
     */
    protected HttpHandler getHandler() {
        Assert.state(handler != null, "handler is not set");
        return handler;
    }

    /**
     * Initiates the server shutdown process.
     * <p>If graceful shutdown is enabled, it will wait for active requests to complete.
     * Otherwise, it disposes of resources immediately.</p>
     */
    protected void shutdown() {
        if (getHandler() instanceof GracefulShutdownHandler shutdownHandler) {
            shutdownHandler.shutdown();
            shutdownHandler.addShutdownListener(shutdownSuccessful -> {
                try {
                    getRequestHandlerFactory().dispose();
                } catch (Exception e) {
                    logger.error("TowServer shutdown failed", e);
                }
            });
            try {
                if (getShutdownTimeoutSecs() > 0) {
                    boolean result = shutdownHandler.awaitShutdown(getShutdownTimeoutSecs() * 1000L);
                    if (!result) {
                        logger.warn("Undertow server did not shut down gracefully within {} seconds. " +
                                "Proceeding with forceful shutdown", getShutdownTimeoutSecs());
                    }
                } else {
                    shutdownHandler.awaitShutdown();
                }
            } catch (Exception ex) {
                logger.error("Unable to gracefully stop Undertow server");
            }
        } else {
            try {
                getRequestHandlerFactory().dispose();
            } catch (Exception e) {
                logger.error("TowServer shutdown failed", e);
            }
        }
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
    public SessionManager getSessionManager(String deploymentName) {
        DeploymentManager deploymentManager = getDeploymentManager(deploymentName);
        Assert.state(deploymentManager != null, "Deployment named '" + deploymentName + "' not found");
        return getSessionManager(deploymentManager);
    }

    @Override
    public SessionManager getSessionManagerByPath(String path) {
        DeploymentManager deploymentManager = getDeploymentManagerByPath(path);
        Assert.state(deploymentManager != null, "Deployment with path '\" + path + \"' not found");
        return getSessionManager(deploymentManager);
    }

    /**
     * Retrieves the Aspectran {@link SessionManager} from an Undertow {@link DeploymentManager}.
     * @param deploymentManager the deployment manager for a specific web application
     * @return the Aspectran session manager, or {@code null} if not applicable
     */
    @Nullable
    private SessionManager getSessionManager(@NonNull DeploymentManager deploymentManager) {
        Deployment deployment = deploymentManager.getDeployment();
        if (deployment != null) {
            io.undertow.server.session.SessionManager sessionManager = deployment.getSessionManager();
            if (sessionManager instanceof TowSessionManager towSessionManager) {
                return towSessionManager.getSessionManager();
            }
        }
        return null;
    }

}
