package com.aspectran.jetty;

/**
 * A callback for the result of a graceful shutdown request.
 *
 * <p>Created: 1/21/24</p>
 */
@FunctionalInterface
public interface GracefulShutdownCallback {

    /**
     * Graceful shutdown has completed with the given {@code result}.
     * @param result the result of the shutdown
     */
    void shutdownComplete(GracefulShutdownResult result);

}
