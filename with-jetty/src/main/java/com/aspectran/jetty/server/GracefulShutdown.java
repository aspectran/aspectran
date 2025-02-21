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

import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Handles Jetty graceful shutdown.
 *
 * <p>Created: 1/21/24</p>
 */
final class GracefulShutdown {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdown.class);

    private final JettyServer server;

    private volatile boolean shuttingDown = false;

    GracefulShutdown(JettyServer server) {
        this.server = server;
    }

    void shutDownGracefully(@NonNull GracefulShutdownCallback callback) {
        logger.info("Commencing graceful shutdown. Waiting for active requests to complete");
        for (Connector connector : server.getConnectors()) {
            shutdown(connector);
        }
        this.shuttingDown = true;
        new Thread(() -> awaitShutdown(callback), "shutdown").start();
    }

    @SuppressWarnings("unchecked")
    private void shutdown(@NonNull Connector connector) {
        Future<Void> result;
        try {
            result = connector.shutdown();
        } catch (NoSuchMethodError ex) {
            try {
                result = (Future<Void>)MethodUtils.invokeExactMethod(connector, "shutdown");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not access method: shutdown", ExceptionUtils.getRootCause(e));
            }
        }
        try {
            result.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            // Continue
        }
    }

    private void awaitShutdown(GracefulShutdownCallback callback) {
        sleep(300);
        int activeRequests = 0;
        while (this.shuttingDown && (activeRequests = getActiveRequests()) > 0) {
            sleep(100);
        }
        this.shuttingDown = false;
        if (activeRequests == 0) {
            logger.info("Graceful shutdown complete");
            callback.shutdownComplete(GracefulShutdownResult.IDLE);
        } else {
            logger.info("Graceful shutdown aborted with {} request{} still active",
                    activeRequests, (activeRequests == 1 ? "" : "s"));
            callback.shutdownComplete(GracefulShutdownResult.REQUESTS_ACTIVE);
        }
    }

    private int getActiveRequests() {
        StatisticsHandler statisticsHandler = server.getStatisticsHandler();
        return (statisticsHandler != null ? statisticsHandler.getRequestsActive() : 0);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    void abort() {
        this.shuttingDown = false;
    }

}
