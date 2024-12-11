package com.aspectran.undertow.server.handler.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2024. 12. 11.</p>
 */
public class LoggingGroupAssistHandler implements HttpHandler  {

    private final HttpHandler handler;

    public LoggingGroupAssistHandler(HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        ExchangeLoggingGroupHelper.setFrom(exchange);
        handler.handleRequest(exchange);
    }

}
