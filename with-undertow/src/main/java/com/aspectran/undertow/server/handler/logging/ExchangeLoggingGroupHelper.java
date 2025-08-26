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
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.LoggingGroupHelper;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

/**
 * A helper class for managing the logging group within the scope of an {@link HttpServerExchange}.
 * <p>Since Undertow can process requests asynchronously across different threads, this class
 * uses an {@link AttachmentKey} to store the logging group name on the exchange itself.
 * This ensures that the correct logging context can be restored when a task is resumed
 * on a different thread and is properly cleared at the end of the exchange.</p>
 */
public abstract class ExchangeLoggingGroupHelper {

    private static final AttachmentKey<String> KEY = AttachmentKey.create(String.class);

    /**
     * Sets the logging group on the current thread and attaches the group name to the exchange.
     * @param exchange the HTTP server exchange
     * @param groupName the logging group name to set, or {@code null} to clear it
     */
    static void setTo(@NonNull HttpServerExchange exchange, @Nullable String groupName) {
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
            exchange.putAttachment(KEY, groupName);
        } else {
            LoggingGroupHelper.clear();
        }
    }

    /**
     * Retrieves the logging group name from the exchange attachment and sets it on the current thread.
     * @param exchange the HTTP server exchange
     */
    static void setFrom(@NonNull HttpServerExchange exchange) {
        String groupName = exchange.getAttachment(KEY);
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        } else {
            LoggingGroupHelper.clear();
        }
    }

}
