/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.undertow.server.http;

import com.aspectran.undertow.server.resource.StaticResourceHandler;
import com.aspectran.undertow.service.TowService;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.ResourceSupplier;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

/**
 * <p>Created: 2019-07-31</p>
 */
public class HttpHybridHandler extends ResourceHandler {

    private volatile StaticResourceHandler staticResourceHandler;

    private volatile SessionManager sessionManager;

    private volatile SessionConfig sessionConfig;

    private volatile TowService towService;

    public HttpHybridHandler(ResourceManager resourceManager) {
        super(resourceManager);
    }

    public HttpHybridHandler(ResourceManager resourceManager, HttpHandler next) {
        super(resourceManager, next);
    }

    public HttpHybridHandler(ResourceSupplier resourceSupplier) {
        super(resourceSupplier);
    }

    public HttpHybridHandler(ResourceSupplier resourceSupplier, HttpHandler next) {
        super(resourceSupplier, next);
    }

    public StaticResourceHandler getStaticResourceHandler() {
        return staticResourceHandler;
    }

    public void setStaticResourceHandler(StaticResourceHandler staticResourceHandler) {
        if (staticResourceHandler != null && staticResourceHandler.hasPatterns()) {
            this.staticResourceHandler = staticResourceHandler;
        } else {
            this.staticResourceHandler = null;
        }
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public void setTowService(TowService towService) {
        this.towService = towService;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
        } else {
            if (staticResourceHandler != null) {
                staticResourceHandler.handleRequest(exchange);
                if (exchange.isDispatched() || exchange.isComplete()) {
                    return;
                }
            }

            if (sessionManager != null) {
                exchange.putAttachment(SessionManager.ATTACHMENT_KEY, sessionManager);
                exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, sessionConfig);
                UpdateLastAccessTimeListener listener = new UpdateLastAccessTimeListener(sessionManager, sessionConfig);
                exchange.addExchangeCompleteListener(listener);
            }

            boolean processed = towService.service(exchange);
            if (!processed) {
                super.handleRequest(exchange);
            }
        }
    }

    private static class UpdateLastAccessTimeListener implements ExchangeCompletionListener {

        private final SessionManager sessionManager;
        private final SessionConfig sessionConfig;

        private UpdateLastAccessTimeListener(SessionManager sessionManager, SessionConfig sessionConfig) {
            this.sessionManager = sessionManager;
            this.sessionConfig = sessionConfig;
        }

        @Override
        public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
            try {
                Session session = sessionManager.getSession(exchange, sessionConfig);
                if (session != null) {
                    session.requestDone(exchange);
                }
            } finally {
                next.proceed();
            }
        }

    }

}
