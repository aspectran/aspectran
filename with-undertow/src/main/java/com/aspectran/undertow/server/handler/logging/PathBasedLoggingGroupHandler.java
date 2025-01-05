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
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Map;

/**
 * <p>Created: 2024. 12. 10.</p>
 */
public class PathBasedLoggingGroupHandler implements HttpHandler {

    protected final ExchangeCompletionListener exchangeCompletionListener = new LoggingGroupCompletionListener();

    private final HttpHandler handler;

    private final Map<String, IncludeExcludeWildcardPatterns> pathPatternsByGroupName;

    public PathBasedLoggingGroupHandler(HttpHandler handler, Map<String, IncludeExcludeWildcardPatterns> pathPatternsByGroupName) {
        this.handler = handler;
        this.pathPatternsByGroupName = pathPatternsByGroupName;
    }

    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        String groupName = resolveGroupName(exchange);
        ExchangeLoggingGroupHelper.setTo(exchange, groupName);

        exchange.addExchangeCompleteListener(exchangeCompletionListener);
        handler.handleRequest(exchange);
    }

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
