/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.undertow.server.handler.session;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import org.jspecify.annotations.NonNull;

/**
 * An {@link HttpHandler} that attaches the {@link SessionManager} and {@link SessionConfig}
 * to the {@link HttpServerExchange}.
 * <p>This handler is a crucial piece of middleware that makes session functionality
 * available to all subsequent handlers in the chain without requiring them to have
 * direct knowledge of the session management setup.</p>
 */
public class SessionAttachmentHandler implements HttpHandler {

    private final HttpHandler next;

    private final SessionManager sessionManager;

    private final SessionConfig sessionConfig;

    /**
     * Constructs a new SessionAttachmentHandler.
     * @param sessionManager the session manager to attach
     * @param sessionConfig the session configuration to attach
     */
    public SessionAttachmentHandler(SessionManager sessionManager, SessionConfig sessionConfig) {
        this(ResponseCodeHandler.HANDLE_404, sessionManager, sessionConfig);
    }

    /**
     * Constructs a new SessionAttachmentHandler with a next handler.
     * @param next the next handler in the chain
     * @param sessionManager the session manager to attach
     * @param sessionConfig the session configuration to attach
     */
    public SessionAttachmentHandler(HttpHandler next, SessionManager sessionManager, SessionConfig sessionConfig) {
        this.sessionManager = sessionManager;
        this.sessionConfig = sessionConfig;
        this.next = next;
    }

    /**
     * Handles the request by attaching the session manager and config to the exchange,
     * then delegating to the next handler.
     * @param exchange the HTTP server exchange
     * @throws Exception if an error occurs in the next handler
     */
    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        exchange.putAttachment(SessionManager.ATTACHMENT_KEY, sessionManager);
        exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, sessionConfig);
        next.handleRequest(exchange);
    }

}
