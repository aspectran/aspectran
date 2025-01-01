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
package com.aspectran.undertow.service;

import com.aspectran.core.service.CoreService;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;

/**
 * <p>Created: 2019-07-27</p>
 */
public interface TowService extends CoreService {

    /**
     * Executes web activity.
     * @param exchange the HTTP request/response exchange
     * @return true if the activity was handled; false otherwise
     * @throws IOException If an error occurs during Activity execution
     */
    boolean service(HttpServerExchange exchange) throws IOException;

}
