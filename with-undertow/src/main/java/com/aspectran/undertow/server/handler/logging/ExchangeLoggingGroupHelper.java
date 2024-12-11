package com.aspectran.undertow.server.handler.logging;

import com.aspectran.core.support.logging.LoggingGroupHelper;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

/*
 * Since logs are output asynchronously, the key to identify
 * the logging group from MDC should not be removed immediately.
 * In this case, the key may remain in the existing thread and
 * must be initialized or removed in the next request.
 */
public abstract class ExchangeLoggingGroupHelper {

    private static final AttachmentKey<String> KEY = AttachmentKey.create(String.class);

    static void setTo(@NonNull HttpServerExchange exchange, @Nullable String groupName) {
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
            exchange.putAttachment(KEY, groupName);
        } else {
            LoggingGroupHelper.clear();
        }
    }

    static void setFrom(@NonNull HttpServerExchange exchange) {
        String groupName = exchange.getAttachment(KEY);
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        } else {
            LoggingGroupHelper.clear();
        }
    }

}
