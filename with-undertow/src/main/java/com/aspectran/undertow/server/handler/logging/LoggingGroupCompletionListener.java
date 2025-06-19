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
