package com.aspectran.undertow.server.servlet;

import com.aspectran.core.util.Assert;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 05/10/2019</p>
 */
public class HybridHandler implements HttpHandler {

    private final HttpHandler defaultHandler;

    private final HttpHandler staticResourceHandler;

    public HybridHandler(HttpHandler defaultHandler) {
        this(defaultHandler, null);
    }

    public HybridHandler(HttpHandler defaultHandler, HttpHandler staticResourceHandler) {
        Assert.notNull(defaultHandler, "defaultHandler must not be null");
        this.defaultHandler = defaultHandler;
        this.staticResourceHandler = staticResourceHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (staticResourceHandler != null) {
            staticResourceHandler.handleRequest(exchange);
            if (!exchange.isDispatched() && !exchange.isComplete()) {
                defaultHandler.handleRequest(exchange);
            }
        } else {
            defaultHandler.handleRequest(exchange);
        }
    }

}
