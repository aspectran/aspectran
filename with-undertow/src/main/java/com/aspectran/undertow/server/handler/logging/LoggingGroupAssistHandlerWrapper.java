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

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;

/**
 * A {@link HandlerWrapper} that creates a {@link LoggingGroupAssistHandler}.
 * <p>This wrapper is typically used within a servlet context to ensure that the logging
 * group is correctly propagated to the thread handling the servlet request.</p>
 *
 * <p>Created: 2024. 12. 11.</p>
 */
public class LoggingGroupAssistHandlerWrapper implements HandlerWrapper {

    /**
     * Wraps the given handler with a new {@link LoggingGroupAssistHandler}.
     * @param handler the next handler in the chain
     * @return the new {@code LoggingGroupAssistHandler}
     */
    @Override
    public HttpHandler wrap(HttpHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        return new LoggingGroupAssistHandler(handler);
    }

}
