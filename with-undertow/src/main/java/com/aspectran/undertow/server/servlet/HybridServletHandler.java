/*
 * Copyright (c) 2008-2024 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.Assert;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 05/10/2019</p>
 */
public class HybridServletHandler implements HttpHandler {

    private final HttpHandler defaultHandler;

    private final HttpHandler staticResourceHandler;

    public HybridServletHandler(HttpHandler defaultHandler) {
        this(defaultHandler, null);
    }

    public HybridServletHandler(HttpHandler defaultHandler, HttpHandler staticResourceHandler) {
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
