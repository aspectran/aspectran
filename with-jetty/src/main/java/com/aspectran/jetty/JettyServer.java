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
package com.aspectran.jetty;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The Jetty Server managed by Aspectran.
 *
 * <p>Created: 2016. 12. 22.</p>
 */
public class JettyServer extends Server implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);

    private boolean autoStart;

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

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
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
            logger.error("Error while stopping jetty server: " + e.getMessage(), e);
        }
    }

    @Override
    public void doStart() throws Exception {
        logger.info("Starting embedded Jetty server");
        super.doStart();
        logger.info("Jetty started on port(s) " + getActualPortsDescription()
                + " with context path '" + getContextPath() + "'");
    }

    @Override
    public void doStop() {
        logger.info("Stopping embedded Jetty server");
        try {
            super.doStop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Unable to stop embedded Jetty server", e);
        }
    }

    private String getContextPath() {
        HandlerContainer handlerContainer = (HandlerContainer)getHandler();
        return Arrays.stream(handlerContainer.getChildHandlers())
                .filter(ContextHandler.class::isInstance).map(ContextHandler.class::cast)
                .map(ContextHandler::getContextPath).collect(Collectors.joining("', '"));
    }

    @NonNull
    private String getActualPortsDescription() {
        StringBuilder ports = new StringBuilder();
        for (Connector connector : getConnectors()) {
            NetworkConnector connector1 = (NetworkConnector)connector;
            if (ports.length() != 0) {
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
