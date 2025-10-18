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
package com.aspectran.jetty.server;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.jetty.server.servlet.JettyWebAppContext;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnectionLimit;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an embedded Jetty server instance that is managed by the Aspectran framework.
 * <p>This class extends the standard Jetty {@link Server} and integrates with Aspectran's
 * bean lifecycle by implementing {@link InitializableBean} and {@link DisposableBean}.
 * This allows the server to be started and stopped automatically along with the
 * Aspectran application context.</p>
 *
 * <p>Created: 2016. 12. 22.</p>
 */
public class JettyServer extends Server implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);

    private boolean shutdownGracefully = true;

    private GracefulShutdown gracefulShutdown;

    private boolean autoStart = true;

    /**
     * Constructs a new JettyServer.
     */
    public JettyServer() {
        super();
    }

    /**
     * Constructs a new JettyServer to listen on the specified port.
     * @param port the port to listen on
     */
    public JettyServer(int port) {
        super(port);
    }

    /**
     * Constructs a new JettyServer with a specific thread pool.
     * @param pool the thread pool for the server to use
     */
    public JettyServer(ThreadPool pool) {
        super(pool);
    }

    /**
     * Returns whether the server should start automatically when the Aspectran context is initialized.
     * @return true if auto-start is enabled, false otherwise
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Sets whether the server should start automatically.
     * @param autoStart true to enable auto-start; false otherwise
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /**
     * Sets whether the server should attempt a graceful shutdown.
     * @param shutdownGracefully true to enable graceful shutdown; false otherwise
     */
    public void setShutdownGracefully(boolean shutdownGracefully) {
        this.shutdownGracefully = shutdownGracefully;
    }

    /**
     * Sets the maximum number of concurrent network connections.
     * @param maxConnections the maximum number of connections
     */
    public void setMaxConnections(int maxConnections) {
        if (maxConnections > -1) {
            addBean(new NetworkConnectionLimit(maxConnections, this));
        }
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
     * Finds and returns the {@link StatisticsHandler} if it has been configured.
     * This is required for graceful shutdown to work correctly.
     * @return the statistics handler, or {@code null} if not found
     */
    public StatisticsHandler getStatisticsHandler() {
        return findStatisticsHandler(getHandler());
    }

    /**
     * Recursively searches for a {@link StatisticsHandler} within the handler hierarchy.
     * @param handler the handler to search within
     * @return the found handler, or {@code null}
     */
    private StatisticsHandler findStatisticsHandler(Handler handler) {
        if (handler instanceof StatisticsHandler statisticsHandler) {
            return statisticsHandler;
        }
        if (handler instanceof Handler.Wrapper handlerWrapper) {
            return findStatisticsHandler(handlerWrapper.getHandler());
        }
        return null;
    }

    /**
     * Finds and returns the {@link ContextHandler} for a specific context path.
     * @param contextPath the context path to search for
     * @return the context handler, or {@code null} if not found
     */
    public ContextHandler getContextHandler(String contextPath) {
        return findContextHandler(contextPath, getHandler());
    }

    /**
     * Recursively searches for a {@link ContextHandler} with a matching context path.
     * @param contextPath the context path to match
     * @param handler the handler to search within
     * @return the found handler, or {@code null}
     */
    private ContextHandler findContextHandler(String contextPath, Handler handler) {
        if (handler instanceof ContextHandler contextHandler) {
            if (Objects.equals(contextPath, contextHandler.getContextPath())) {
                return contextHandler;
            }
        }
        if (handler instanceof Handler.Wrapper handlerWrapper) {
            return findContextHandler(contextPath, handlerWrapper.getHandler());
        }
        if (handler instanceof Handler.Sequence handlerSequence) {
            for (Handler child : handlerSequence.getHandlers()) {
                return findContextHandler(contextPath, child);
            }
        }
        return null;
    }

    /**
     * Finds and returns the {@link SessionHandler} for a specific context path.
     * @param contextPath the context path of the web application
     * @return the session handler, or {@code null} if not found
     */
    public SessionHandler getSessionHandler(String contextPath) {
        ContextHandler contextHandler = getContextHandler(contextPath);
        if (contextHandler instanceof WebAppContext webAppContext) {
            return webAppContext.getSessionHandler();
        }
        return null;
    }

    /**
     * Initializes the server as part of the Aspectran bean lifecycle.
     * If {@code autoStart} is enabled, this method will start the Jetty server.
     * @throws Exception if the server fails to start
     */
    @Override
    public void initialize() throws Exception {
        setStopAtShutdown(false);
        if (autoStart && !isRunning()) {
            start();
        }
    }

    /**
     * Destroys the server as part of the Aspectran bean lifecycle.
     * This method ensures that the Jetty server is stopped.
     */
    @Override
    public void destroy() {
        if (!isStopped() && !isStopping()) {
            try {
                stop();
            } catch (Exception e) {
                logger.error("Error stopping Jetty server", e);
            }
        }
    }

    /**
     * Overrides the default start behavior to add logging and graceful shutdown setup.
     * @throws Exception if an error occurs during startup
     */
    @Override
    public void doStart() throws Exception {
        logger.info("Starting Jetty server");
        if (shutdownGracefully && getStatisticsHandler() != null) {
            gracefulShutdown = new GracefulShutdown(this);
        } else if (gracefulShutdown != null) {
            gracefulShutdown.abort();
            gracefulShutdown = null;
        }
        for (Handler handler : getHandlers()) {
            handleDeferredInitialize(handler);
        }
        super.doStart();
        logger.info("Jetty server started on port(s) {} with context path '{}'",
                getActualPortsDescription(), getContextPath());
    }

    /**
     * Overrides the default stop behavior to initiate a graceful or immediate shutdown.
     */
    @Override
    public void doStop() {
        logger.info("Stopping Jetty server");
        if (gracefulShutdown != null) {
            gracefulShutdown.shutDownGracefully(result -> shutdown());
        } else {
            shutdown();
        }
    }

    /**
     * Performs the actual server shutdown, stopping handlers and destroying the server instance.
     */
    private void shutdown() {
        try {
            getHandler().stop();
            super.doStop();
            super.destroy();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Failed to shutdown Jetty server", e);
        } finally {
            for (Handler handler : getHandlers()) {
                handleDeferredDispose(handler);
            }
        }
    }

    /**
     * Recursively finds and initializes handlers that support deferred initialization.
     * @param handler the handler to process
     */
    private void handleDeferredInitialize(Handler handler) {
        if (handler instanceof JettyWebAppContext webAppContext) {
            disableLogbackInitializerIfAvailable(webAppContext);
            webAppContext.deferredInitialize(this);
        } else if (handler instanceof Handler.Wrapper handlerWrapper) {
            handleDeferredInitialize(handlerWrapper.getHandler());
        } else if (handler instanceof Handler.Collection handlerCollection) {
            handleDeferredInitialize(handlerCollection.getHandlers());
        }
    }

    private void disableLogbackInitializerIfAvailable(@NonNull JettyWebAppContext webAppContext) {
        try {
            // To prevent Logback's ServletContextListener from shutting down the logging context,
            // set a context parameter to disable the initializer if Logback is the logging implementation.
            ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
            if (iLoggerFactory != null && iLoggerFactory.getClass().getName().equals("ch.qos.logback.classic.LoggerContext")) {
                webAppContext.setInitParameter("logbackDisableServletContainerInitializer", "true");
                if (logger.isDebugEnabled()) {
                    logger.debug("Disabled Logback ServletContainerInitializer for {}", webAppContext.getContextPath());
                }
            }
        } catch (Exception e) {
            // Ignore exceptions, as this is an optional enhancement
        }
    }

    private void handleDeferredInitialize(@NonNull List<Handler> handlers) {
        for (Handler handler : handlers) {
            handleDeferredInitialize(handler);
        }
    }

    /**
     * Recursively finds and disposes of handlers that support deferred disposal.
     * @param handler the handler to process
     */
    private void handleDeferredDispose(Handler handler) {
        if (handler instanceof JettyWebAppContext jettyWebAppContext) {
            jettyWebAppContext.deferredDispose();
        } else if (handler instanceof Handler.Wrapper handlerWrapper) {
            handleDeferredDispose(handlerWrapper.getHandler());
        } else if (handler instanceof Handler.Collection handlerCollection) {
            handleDeferredDispose(handlerCollection.getHandlers());
        }
    }

    private void handleDeferredDispose(@NonNull List<Handler> handlers) {
        for (Handler handler : handlers) {
            handleDeferredDispose(handler);
        }
    }

    /**
     * Gathers the context paths of all configured context handlers.
     * @return a string containing the context paths
     */
    private String getContextPath() {
        Container handlerContainer = (Container)getHandler();
        return handlerContainer.getHandlers().stream()
                .filter(ContextHandler.class::isInstance).map(ContextHandler.class::cast)
                .map(ContextHandler::getContextPath).collect(Collectors.joining("', '"));
    }

    /**
     * Gathers the port and protocol information from all configured connectors.
     * @return a descriptive string of active ports and protocols
     */
    @NonNull
    private String getActualPortsDescription() {
        StringBuilder ports = new StringBuilder();
        for (Connector connector : getConnectors()) {
            NetworkConnector connector1 = (NetworkConnector)connector;
            if (!ports.isEmpty()) {
                ports.append(", ");
            }
            ports.append(connector1.getLocalPort());
            ports.append(" (");
            ports.append(StringUtils.joinWithCommas(connector.getProtocols()));
            ports.append(")");
        }
        return ports.toString();
    }

}
