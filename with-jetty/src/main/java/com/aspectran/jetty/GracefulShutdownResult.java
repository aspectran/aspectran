package com.aspectran.jetty;

/**
 * The result of a graceful shutdown request.
 *
 * <p>Created: 1/21/24</p>
 */
public enum GracefulShutdownResult {

    /**
     * Requests remained active at the end of the grace period.
     */
    REQUESTS_ACTIVE,

    /**
     * The server was idle with no active requests at the end of the grace period.
     */
    IDLE,

    /**
     * The server was shutdown immediately, ignoring any active requests.
     */
    IMMEDIATE;

}
