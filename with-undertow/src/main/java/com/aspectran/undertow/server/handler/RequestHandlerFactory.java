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
package com.aspectran.undertow.server.handler;

import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.ServletContainer;

/**
 * A factory interface for creating the root {@link HttpHandler} for an Undertow server.
 * <p>Implementations of this interface are responsible for building the complete
 * handler chain that processes all incoming HTTP requests. This could range from a simple
 * handler to a full servlet container.</p>
 *
 * <p>Created: 2025-01-08</p>
 */
public interface RequestHandlerFactory {

    /**
     * Creates and returns the root {@link HttpHandler}.
     * This handler is the entry point for all requests to the server.
     * @return the root HTTP handler
     * @throws Exception if an error occurs during handler creation
     */
    HttpHandler createHandler() throws Exception;

    /**
     * Returns the servlet container, if one is used by this factory.
     * This is primarily used for servlet-based deployments.
     * @return the servlet container, or {@code null} if not applicable
     */
    ServletContainer getServletContainer();

    /**
     * Disposes of any resources held by the factory and its created handlers.
     * This is typically called during server shutdown.
     * @throws Exception if an error occurs during disposal
     */
    void dispose() throws Exception;

}
