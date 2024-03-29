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
package com.aspectran.undertow.server;

import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import org.xnio.Option;
import org.xnio.OptionMap;

import java.io.IOException;

/**
 * <p>Created: 11/25/23</p>
 */
public abstract class AbstractTowServer extends AbstractLifeCycle implements TowServer {

    private final Undertow.Builder builder = Undertow.builder();

    private ServletContainer servletContainer;

    private HttpHandler handler;

    private boolean autoStart;

    private int shutdownTimeoutSecs;

    public Undertow.Builder getBuilder() {
        return builder;
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

    public HttpHandler getHandler() {
        return handler;
    }

    public void setHandler(HttpHandler handler) {
        this.handler = handler;
        builder.setHandler(handler);
    }

    public ServletContainer getServletContainer() {
        return servletContainer;
    }

    public void setServletContainer(ServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }

    /**
     * Returns whether the server starts automatically.
     * @return true if the server should be started
     */
    @Override
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

    public int getShutdownTimeoutSecs() {
        return shutdownTimeoutSecs;
    }

    public void setShutdownTimeoutSecs(int shutdownTimeoutSecs) {
        this.shutdownTimeoutSecs = shutdownTimeoutSecs;
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    @Override
    public DeploymentManager getDeploymentManager(String deploymentName) {
        if (getServletContainer() == null) {
            throw new IllegalStateException("servletContainer is not set");
        }
        if (deploymentName == null) {
            throw new IllegalArgumentException("deploymentName must not be null");
        }
        return getServletContainer().getDeployment(deploymentName);
    }

    @Override
    public DeploymentManager getDeploymentManagerByPath(String path) {
        if (getServletContainer() == null) {
            throw new IllegalStateException("servletContainer is not set");
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        return getServletContainer().getDeploymentByPath(path);
    }

    @Override
    public SessionHandler getSessionHandler(String deploymentName) {
        DeploymentManager deploymentManager = getDeploymentManager(deploymentName);
        if (deploymentManager == null) {
            throw new IllegalStateException("Deployment named '" + deploymentName + "' not found");
        }
        SessionHandler sessionHandler = getSessionHandler(deploymentManager);
        if (sessionHandler == null) {
            throw new IllegalStateException("No SessionHandler for the deployment named '" + deploymentName + "'");
        }
        return sessionHandler;
    }

    @Override
    public SessionHandler getSessionHandlerByPath(String path) {
        DeploymentManager deploymentManager = getDeploymentManagerByPath(path);
        if (deploymentManager == null) {
            throw new IllegalStateException("Deployment with path '" + path + "' not found");
        }
        SessionHandler sessionHandler = getSessionHandler(deploymentManager);
        if (sessionHandler == null) {
            throw new IllegalStateException("No SessionHandler for the deployment with path " + path + "'");
        }
        return sessionHandler;
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
