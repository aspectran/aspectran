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
package com.aspectran.jetty.server;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.jetty.server.servlet.JettyWebAppContext;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The Jetty Server managed by Aspectran.
 *
 * <p>Created: 2016. 12. 22.</p>
 */
public class JettyServer extends Server implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);

    private boolean shutdownGracefully = true;

    private GracefulShutdown gracefulShutdown;

    private boolean autoStart = true;

    public JettyServer() {
        super();
    }

    public JettyServer(int port) {
        super(port);
    }

    public JettyServer(ThreadPool pool) {
        super(pool);
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setShutdownGracefully(boolean shutdownGracefully) {
        this.shutdownGracefully = shutdownGracefully;
    }

    public void setMaxConnections(int maxConnections) {
        if (maxConnections > -1) {
            addBean(new ConnectionLimit(maxConnections, this));
        }
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public StatisticsHandler getStatisticsHandler() {
        return findStatisticsHandler(getHandler());
    }

    private StatisticsHandler findStatisticsHandler(Handler handler) {
        if (handler instanceof StatisticsHandler statisticsHandler) {
            return statisticsHandler;
        }
        if (handler instanceof Handler.Wrapper handlerWrapper) {
            return findStatisticsHandler(handlerWrapper.getHandler());
        }
        return null;
    }

    public ContextHandler getContextHandler(String contextPath) {
        return findContextHandler(contextPath, getHandler());
    }

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

    public SessionHandler getSessionHandler(String contextPath) {
        ContextHandler contextHandler = getContextHandler(contextPath);
        if (contextHandler instanceof WebAppContext webAppContext) {
            return webAppContext.getSessionHandler();
        } else {
            return null;
        }
    }

    @Override
    public void initialize() throws Exception {
        setStopAtShutdown(false);
        if (autoStart && !isRunning()) {
            start();
        }
    }

    @Override
    public void destroy() {
        if (!isStopped() && isStopping()) {
            try {
                stop();
            } catch (Exception e) {
                logger.error("Error while stopping jetty server", e);
            }
        }
    }

    @Override
    public void doStart() throws Exception {
        logger.info("Starting embedded Jetty server");
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
        logger.info("Jetty started on port(s) " + getActualPortsDescription()
                + " with context path '" + getContextPath() + "'");
    }

    @Override
    public void doStop() {
        logger.info("Stopping embedded Jetty server");
        if (gracefulShutdown != null) {
            gracefulShutdown.shutDownGracefully(result -> {
                shutdown();
            });
        } else {
            shutdown();
        }
    }

    private void shutdown() {
        try {
            getHandler().stop();
            super.doStop();
            super.destroy();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Unable to stop embedded Jetty server", e);
        } finally {
            for (Handler handler : getHandlers()) {
                handleDeferredDispose(handler);
            }
        }
    }

    private void handleDeferredInitialize(Handler handler) {
        if (handler instanceof JettyWebAppContext jettyWebAppContext) {
            jettyWebAppContext.deferredInitialize(this);
        } else if (handler instanceof Handler.Wrapper handlerWrapper) {
            handleDeferredInitialize(handlerWrapper.getHandler());
        } else if (handler instanceof Handler.Collection handlerCollection) {
            handleDeferredInitialize(handlerCollection.getHandlers());
        }
    }

    private void handleDeferredInitialize(@NonNull List<Handler> handlers) {
        for (Handler handler : handlers) {
            handleDeferredInitialize(handler);
        }
    }

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

    private String getContextPath() {
        Container handlerContainer = (Container)getHandler();
        return handlerContainer.getHandlers().stream()
                .filter(ContextHandler.class::isInstance).map(ContextHandler.class::cast)
                .map(ContextHandler::getContextPath).collect(Collectors.joining("', '"));
    }

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
            ports.append(StringUtils.joinCommaDelimitedList(connector.getProtocols()));
            ports.append(")");
        }
        return ports.toString();
    }

}
