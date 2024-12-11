package com.aspectran.undertow.server.handler.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2024. 12. 11.</p>
 */
public class PathBasedLoggingGroupAssistHandler implements HttpHandler  {

    private final HttpHandler handler;

    public PathBasedLoggingGroupAssistHandler(HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        String groupName = exchange.getAttachment(PathBasedLoggingGroupHandler.LOGGING_GROUP);
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        } else {
            LoggingGroupHelper.clear();
        }

        handler.handleRequest(exchange);
    }

}
