package com.aspectran.undertow.server.handler.logging;

import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2024. 12. 11.</p>
 */
public class LoggingGroupCompletionListener implements ExchangeCompletionListener {

    @Override
    public void exchangeEvent(@NonNull HttpServerExchange exchange, @NonNull NextListener nextListener) {
        try {
            ExchangeLoggingGroupHelper.setFrom(exchange);
        } finally {
            nextListener.proceed();
        }
    }

}
