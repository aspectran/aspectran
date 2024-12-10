package com.aspectran.undertow.server.handler.logging;

import org.slf4j.MDC;

/*
 * Since logs are output asynchronously, the key to identify
 * the logging group from MDC should not be removed immediately.
 * In this case, the key may remain in the existing thread and
 * must be initialized or removed in the next request.
 */
public abstract class LoggingGroupHelper {

    public static final String LOGGING_GROUP_KEY = "LOGGING_GROUP";

    public static void set(String groupName) {
        MDC.put(LOGGING_GROUP_KEY, groupName);
    }

    public static void clear() {
        MDC.remove(LOGGING_GROUP_KEY);
    }

}
