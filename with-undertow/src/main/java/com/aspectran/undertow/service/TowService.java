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
package com.aspectran.undertow.service;

import com.aspectran.core.service.CoreService;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;

/**
 * The main interface for the Aspectran Undertow service.
 * <p>This service extends the {@link CoreService} to provide web functionality using an
 * embedded Undertow server. It defines the contract for handling web requests directly
 * using Undertow's native {@link io.undertow.server.HttpServerExchange} objects,
 * enabling a high-performance, servlet-less web environment.</p>
 */
public interface TowService extends CoreService {

    /**
     * Returns whether session adaptation is enabled for this Undertow service.
     * @return {@code true} if session adaptation is enabled, {@code false} otherwise
     */
    boolean isSessionAdaptable();

    /**
     * Processes an incoming HTTP request using the Undertow {@link HttpServerExchange}.
     * This is the main entry point for handling web requests in this service.
     * @param exchange the HTTP request/response exchange
     * @return {@code true} if the request was handled by an Aspectran activity; {@code false} otherwise
     * @throws IOException if an input or output error occurs during activity execution
     */
    boolean service(HttpServerExchange exchange) throws IOException;

}
