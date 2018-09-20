/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.web.support.cors;

import com.aspectran.core.activity.Translet;

import java.io.IOException;

/**
 * Process an incoming cross-origin (CORS) requests.
 * Encapsulates the CORS processing logic as specified by the
 * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/">W3C candidate
 * recommendation</a> from 2013-01-29.
 *
 * @since 2.3.0
 * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
 */
public interface CorsProcessor {

    /**
     * Process a simple or actual CORS request.
     *
     * @param translet the translet
     * @throws CorsException if the request is invalid or denied
     * @throws IOException in case of I/O errors
     */
    void processActualRequest(Translet translet) throws CorsException, IOException;

    /**
     * Process a preflight CORS request.
     *
     * <p>CORS specification:
     * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/#resource-preflight-requests">PreflightRequest</a>
     *
     * @param translet the translet
     * @throws CorsException if the request is invalid or denied
     * @throws IOException in case of I/O errors
     */
    void processPreflightRequest(Translet translet) throws CorsException, IOException;

    /**
     * Sends an error response to the client using the specified status.
     *
     * @param translet the translet
     * @throws IOException in case of I/O errors
     */
    void sendError(Translet translet) throws IOException;

}
