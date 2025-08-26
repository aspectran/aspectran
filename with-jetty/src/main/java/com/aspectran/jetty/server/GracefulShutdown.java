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
 * A helper class that handles the graceful shutdown of a Jetty server.
 * <p>This class works by first stopping all connectors from accepting new connections,
 * then waiting for a period for all active requests to complete. Once all requests
 * are idle, it triggers a final shutdown callback.</p>
 *
 * <p>Created: 1/21/24</p>
 */
final class GracefulShutdown {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdown.class);

    private final JettyServer server;

    private volatile boolean shuttingDown = false;

    /**
     * Constructs a new GracefulShutdown handler for the given server.
     * @param server the Jetty server instance to manage
     */
    GracefulShutdown(JettyServer server) {
        this.server = server;
    }

    /**
     * Initiates the graceful shutdown process.
     * <p>This method stops all connectors and starts a background thread to monitor
     * active requests. When all requests are complete, the provided callback is invoked.</p>
     * @param callback the callback to be invoked upon completion of the shutdown
     */
    void shutDownGracefully(@NonNull GracefulShutdownCallback callback) {
        logger.info("Commencing graceful shutdown. Waiting for active requests to complete");
        for (Connector connector : server.getConnectors()) {
            shutdown(connector);
        }
        this.shuttingDown = true;
        new Thread(() -> awaitShutdown(callback), "shutdown").start();
    }

    /**
     * Shuts down a single connector, preventing it from accepting new connections.
     * @param connector the connector to shut down
     */
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

    /**
     * Runs in a background thread, periodically checking for active requests.
     * Once all requests are complete, it invokes the shutdown callback.
     * @param callback the callback to invoke when shutdown is complete
     */
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

    /**
     * Gets the number of currently active requests from the server's {@link StatisticsHandler}.
     * @return the number of active requests
     */
    private int getActiveRequests() {
        StatisticsHandler statisticsHandler = server.getStatisticsHandler();
        return (statisticsHandler != null ? statisticsHandler.getRequestsActive() : 0);
    }

    /**
     * Pauses the current thread for a specified duration.
     * @param millis the number of milliseconds to sleep
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Aborts the graceful shutdown process if it is in progress.
     */
    void abort() {
        this.shuttingDown = false;
    }

}
