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
package com.aspectran.undertow.server.handler;

import com.aspectran.undertow.service.TowService;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;

/**
 * <p>Created: 2019-07-31</p>
 */
public class LightRequestHandler implements HttpHandler {

    private final TowService towService;

    private final SessionManager sessionManager;

    private final SessionConfig sessionConfig;

    private final ExchangeCompletionListener exchangeCompletionListener;

    public LightRequestHandler(TowService towService) {
        this(towService, null);
    }

    public LightRequestHandler(TowService towService, SessionManager sessionManager) {
        this(towService, sessionManager, null);
    }

    public LightRequestHandler(TowService towService, SessionManager sessionManager, SessionConfig sessionConfig) {
        this.towService = towService;
        this.sessionManager = sessionManager;
        if (sessionManager != null) {
            this.sessionConfig = (sessionConfig != null ? sessionConfig : new SessionCookieConfig());
        } else {
            this.sessionConfig = null;
        }
        this.exchangeCompletionListener = new UpdateLastAccessTimeListener(sessionManager, sessionConfig);
    }

    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
        } else {
            if (sessionManager != null) {
                exchange.putAttachment(SessionManager.ATTACHMENT_KEY, sessionManager);
                exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, sessionConfig);
                exchange.addExchangeCompleteListener(exchangeCompletionListener);
            }

            boolean processed = towService.service(exchange);
            if (!processed) {
                ResponseCodeHandler.HANDLE_404.handleRequest(exchange);
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
