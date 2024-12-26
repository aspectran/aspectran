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
package com.aspectran.undertow.server.handler.session;

import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

/**
 * Handler that attaches the session to the request.
 * This handler is also the place where session cookie configuration properties are configured.
 */
public class SessionAttachmentHandler implements HttpHandler {

    private final HttpHandler next;

    private final SessionManager sessionManager;

    private final SessionConfig sessionConfig;

    public SessionAttachmentHandler(SessionManager sessionManager, SessionConfig sessionConfig) {
        this(ResponseCodeHandler.HANDLE_404, sessionManager, sessionConfig);
    }

    public SessionAttachmentHandler(HttpHandler next, SessionManager sessionManager, SessionConfig sessionConfig) {
        this.sessionManager = sessionManager;
        this.sessionConfig = sessionConfig;
        this.next = next;
    }

    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        exchange.putAttachment(SessionManager.ATTACHMENT_KEY, sessionManager);
        exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, sessionConfig);
        next.handleRequest(exchange);
    }

}
