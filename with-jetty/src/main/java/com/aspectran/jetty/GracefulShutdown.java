package com.aspectran.jetty;

import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Handles Jetty graceful shutdown.
 *
 * <p>Created: 1/21/24</p>
 */
final class GracefulShutdown {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdown.class);

    private final Server server;

    private volatile boolean shuttingDown = false;

    GracefulShutdown(Server server) {
        this.server = server;
    }

    void shutDownGracefully(@NonNull GracefulShutdownCallback callback) {
        logger.info("Commencing graceful shutdown. Waiting for active requests to complete");
        boolean jetty10 = isJetty10();
        for (Connector connector : this.server.getConnectors()) {
            shutdown(connector, !jetty10);
        }
        this.shuttingDown = true;
        new Thread(() -> awaitShutdown(callback), "jetty-shutdown").start();
    }

    @SuppressWarnings("unchecked")
    private void shutdown(@NonNull Connector connector, boolean getResult) {
        Future<Void> result;
        try {
            result = connector.shutdown();
        }
        catch (NoSuchMethodError ex) {
            try {
                result = (Future<Void>)MethodUtils.invokeExactMethod(connector, "shutdown");
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not access method: shutdown",
                    ExceptionUtils.getRootCause(e));
            }
        }
        if (getResult) {
            try {
                result.get();
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException ex) {
                // Continue
            }
        }
    }

    private boolean isJetty10() {
        try {
            return CompletableFuture.class.equals(Connector.class.getMethod("shutdown").getReturnType());
        }
        catch (Exception ex) {
            return false;
        }
    }

    private void awaitShutdown(GracefulShutdownCallback callback) {
        int activeRequests = 0;
        while (this.shuttingDown && (activeRequests = getActiveRequests()) > 0) {
            sleep(100);
        }
        this.shuttingDown = false;
        if (activeRequests == 0) {
            logger.info("Graceful shutdown complete");
            callback.shutdownComplete(GracefulShutdownResult.IDLE);
        } else {
            logger.info("Graceful shutdown aborted with " + activeRequests +
                " request" + (activeRequests == 1 ? "" : "s") + " still active");
            callback.shutdownComplete(GracefulShutdownResult.REQUESTS_ACTIVE);
        }
    }

    private int getActiveRequests() {
        StatisticsHandler statisticsHandler = findStatisticsHandler();
        if (statisticsHandler != null) {
            return statisticsHandler.getRequestsActive();
        } else {
            return 0;
        }
    }

    private StatisticsHandler findStatisticsHandler() {
        return findStatisticsHandler(server.getHandler());
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    void abort() {
        this.shuttingDown = false;
    }

}
