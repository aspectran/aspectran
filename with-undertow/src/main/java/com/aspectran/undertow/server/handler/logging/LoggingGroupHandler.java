package com.aspectran.undertow.server.handler.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.wildcard.WildcardPattern;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.List;
import java.util.Map;

/**
 * <p>Created: 2024. 12. 10.</p>
 */
public class LoggingGroupHandler implements HttpHandler  {

    private final HttpHandler handler;

    private final Map<String, List<WildcardPattern>> pathPatternsByGroupName;

    public LoggingGroupHandler(HttpHandler handler, Map<String, List<WildcardPattern>> pathPatternsByGroupName) {
        this.handler = handler;
        this.pathPatternsByGroupName = pathPatternsByGroupName;
    }

    @Override
    public void handleRequest(@NonNull HttpServerExchange exchange) throws Exception {
        resolveLoggingGroup(exchange.getRequestPath());
        handler.handleRequest(exchange);
    }

    private void resolveLoggingGroup(String requestPath) {
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
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        } else {
            LoggingGroupHelper.clear();
        }
    }

}
