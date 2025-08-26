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

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Map;

/**
 * An Undertow {@link HttpHandler} that sets a logging group for the current request
 * based on matching the request path against a configured set of patterns.
 * <p>This handler ensures that the logging group is cleared at the end of the exchange
 * by using a {@link LoggingGroupCompletionListener}.</p>
 *
 * <p>Created: 2024. 12. 10.</p>
 */
public class PathBasedLoggingGroupHandler implements HttpHandler {

    protected final ExchangeCompletionListener exchangeCompletionListener = new LoggingGroupCompletionListener();

    private final HttpHandler handler;

    private final Map<String, IncludeExcludeWildcardPatterns> pathPatternsByGroupName;

    /**
     * Constructs a new PathBasedLoggingGroupHandler.
     * @param handler the next handler in the chain
     * @param pathPatternsByGroupName a map where keys are logging group names and values are the path patterns
     */
    public PathBasedLoggingGroupHandler(HttpHandler handler, Map<String, IncludeExcludeWildcardPatterns> pathPatternsByGroupName) {
        this.handler = handler;
        this.pathPatternsByGroupName = pathPatternsByGroupName;
    }

    /**
     * Handles the request by resolving and setting the logging group, then delegating to the next handler.
     * @param exchange the HTTP server exchange
     * @throws Exception if an error occurs during request processing
     */
    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        String groupName = resolveGroupName(exchange);
        ExchangeLoggingGroupHelper.setTo(exchange, groupName);

        exchange.addExchangeCompleteListener(exchangeCompletionListener);
        handler.handleRequest(exchange);
    }

    /**
     * Resolves the appropriate logging group name for the given request by matching its path
     * against the configured patterns.
     * @param exchange the current HTTP server exchange
     * @return the matched logging group name, or {@code null} if no pattern matches
     */
    protected String resolveGroupName(@NonNull HttpServerExchange exchange) {
        String groupName = null;
        if (pathPatternsByGroupName != null && !pathPatternsByGroupName.isEmpty()) {
            String requestPath = exchange.getRequestPath();
            for (Map.Entry<String, IncludeExcludeWildcardPatterns> entry : pathPatternsByGroupName.entrySet()) {
                IncludeExcludeWildcardPatterns patterns = entry.getValue();
                if (patterns.matches(requestPath)) {
                    groupName = entry.getKey();
                    break;
                }
            }
        }
        return groupName;
    }

}
