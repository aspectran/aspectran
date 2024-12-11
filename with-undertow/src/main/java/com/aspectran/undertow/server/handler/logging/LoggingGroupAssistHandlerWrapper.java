package com.aspectran.undertow.server.handler.logging;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;

/**
 * <p>Created: 2024. 12. 11.</p>
 */
public class LoggingGroupAssistHandlerWrapper implements HandlerWrapper {

    @Override
    public HttpHandler wrap(HttpHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        return new LoggingGroupAssistHandler(handler);
    }

}
