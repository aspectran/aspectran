package com.aspectran.core.support.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.MDC;

/*
 * Since logs are output asynchronously, the key to identify
 * the logging group from MDC should not be removed immediately.
 * In this case, the key may remain in the existing thread and
 * must be initialized or removed in the next request.
 */
public abstract class LoggingGroupHelper {

    public static final String LOGGING_GROUP = "LOGGING_GROUP";

    public static void set(@NonNull String groupName) {
        MDC.put(LOGGING_GROUP, groupName);
    }

    public static void clear() {
        MDC.remove(LOGGING_GROUP);
    }

}
