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
package com.aspectran.undertow.server.handler.logging;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import org.jspecify.annotations.NonNull;

/**
 * An {@link ExchangeCompletionListener} that clears the logging group from the current thread.
 * <p>This ensures that the thread-local logging group set by a handler like
 * {@link PathBasedLoggingGroupHandler} does not leak to subsequent requests that might
 * be processed by the same thread.</p>
 *
 * <p>Created: 2024. 12. 11.</p>
 */
public class LoggingGroupCompletionListener implements ExchangeCompletionListener {

    /**
     * Called when the request-response exchange is complete. This implementation
     * clears the current logging group.
     * @param exchange the HTTP server exchange
     * @param nextListener the next listener in the chain
     */
    @Override
    public void exchangeEvent(@NonNull HttpServerExchange exchange, @NonNull NextListener nextListener) {
        try {
            ExchangeLoggingGroupHelper.setFrom(exchange);
        } finally {
            nextListener.proceed();
        }
    }

}
