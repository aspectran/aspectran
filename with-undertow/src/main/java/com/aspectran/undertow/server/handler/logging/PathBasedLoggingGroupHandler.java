package com.aspectran.undertow.server.handler.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.wildcard.WildcardPattern;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

import java.util.List;
import java.util.Map;

/**
 * <p>Created: 2024. 12. 10.</p>
 */
public class PathBasedLoggingGroupHandler implements HttpHandler  {

    static final AttachmentKey<String> LOGGING_GROUP = AttachmentKey.create(String.class);

    private final ExchangeCompletionListener exchangeCompletionListener = new PathBasedLoggingGroupCompletionListener();

    private final HttpHandler handler;

    private final Map<String, List<WildcardPattern>> pathPatternsByGroupName;

    public PathBasedLoggingGroupHandler(HttpHandler handler, Map<String, List<WildcardPattern>> pathPatternsByGroupName) {
        this.handler = handler;
        this.pathPatternsByGroupName = pathPatternsByGroupName;
    }

    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        String groupName = resolveGroupName(exchange.getRequestPath());
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
            exchange.putAttachment(LOGGING_GROUP, groupName);
        } else {
            LoggingGroupHelper.clear();
        }

        exchange.addExchangeCompleteListener(exchangeCompletionListener);
        handler.handleRequest(exchange);
    }

    private String resolveGroupName(String requestPath) {
        String groupName = null;
        if (pathPatternsByGroupName != null && !pathPatternsByGroupName.isEmpty()) {
            for (Map.Entry<String, List<WildcardPattern>> entry : pathPatternsByGroupName.entrySet()) {
                for (WildcardPattern pattern : entry.getValue()) {
                    if (pattern.matches(requestPath)) {
                        groupName = entry.getKey();
                        break;
                    }
                }
            }
        }
        return groupName;
    }

    private static class PathBasedLoggingGroupCompletionListener implements ExchangeCompletionListener {

        @Override
        public void exchangeEvent(@NonNull HttpServerExchange exchange, NextListener nextListener) {
            try {
                String groupName = exchange.getAttachment(LOGGING_GROUP);
                if (groupName != null) {
                    LoggingGroupHelper.set(groupName);
                } else {
                    LoggingGroupHelper.clear();
                }
            } finally {
                nextListener.proceed();
            }
        }

    }

}
