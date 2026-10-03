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
package com.aspectran.web.support.cors;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.RequestAdapter;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

/**
 * Defines the contract for processing Cross-Origin Resource Sharing (CORS) requests.
 * <p>This interface encapsulates the core logic for handling both simple/actual and
 * pre-flight CORS requests, as specified by the W3C recommendation.
 * Implementations of this interface are responsible for checking the validity of
 * a CORS request and applying the appropriate CORS headers to the response.
 * </p>
 *
 * @since 2.3.0
 * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommendation</a>
 */
@NullMarked
public interface CorsProcessor {

    /**
     * Process a simple or actual CORS request.
     * @param translet the Translet instance
     * @throws CorsException if the request is invalid or denied
     * @throws IOException in case of I/O errors
     */
    void processActualRequest(Translet translet) throws CorsException, IOException;

    /**
     * Process a preflight CORS request.
     *
     * <p>CORS specification:
     * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/#resource-preflight-requests">PreflightRequest</a>
     * @param translet the Translet instance
     * @throws CorsException if the request is invalid or denied
     * @throws IOException in case of I/O errors
     */
    void processPreflightRequest(Translet translet) throws CorsException, IOException;

    /**
     * Returns whether the specified request is a CORS request.
     * @param request the request adapter
     * @return {@code true} if the request is a CORS request, else {@code false}
     */
    boolean isCorsRequest(RequestAdapter request);

    /**
     * Returns whether the specified request is a CORS preflight request.
     * @param request the request adapter
     * @return {@code true} if the request is a CORS preflight request, else {@code false}
     */
    boolean isPreflightRequest(RequestAdapter request);

    /**
     * Sends an error response to the client using the specified status.
     * @param translet the Translet instance
     * @throws IOException in case of I/O errors
     */
    void sendError(Translet translet) throws IOException;

}
