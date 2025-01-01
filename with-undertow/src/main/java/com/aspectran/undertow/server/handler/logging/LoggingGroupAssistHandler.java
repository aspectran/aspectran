/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
