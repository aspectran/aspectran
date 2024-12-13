package com.aspectran.undertow.server.handler.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.wildcard.WildcardPatterns;
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

    private final Map<String, WildcardPatterns> pathPatternsByGroupName;

    public PathBasedLoggingGroupHandler(HttpHandler handler, Map<String, WildcardPatterns> pathPatternsByGroupName) {
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
            for (Map.Entry<String, WildcardPatterns> entry : pathPatternsByGroupName.entrySet()) {
                WildcardPatterns patterns = entry.getValue();
                if (patterns.matches(requestPath)) {
                    groupName = entry.getKey();
                    break;
                }
            }
        }
        return groupName;
    }

}
