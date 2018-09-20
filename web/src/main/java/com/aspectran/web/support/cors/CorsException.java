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

import com.aspectran.core.context.AspectranCheckedException;

import javax.servlet.http.HttpServletResponse;

/**
 * Process an incoming cross-origin (CORS) requests.
 * Encapsulates the CORS processing logic as specified by the
 * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/">W3C candidate
 * recommendation</a> from 2013-01-29.
 *
 * @since 2.3.0
 */
public class CorsException extends AspectranCheckedException {

    private static final long serialVersionUID = -4522029461215151946L;

    /**
     * CORS origin denied exception.
     */
    public static final CorsException ORIGIN_DENIED =
            new CorsException("CORS origin denied", HttpServletResponse.SC_FORBIDDEN);

    /**
     * Unsupported HTTP method.
     */
    public static final CorsException UNSUPPORTED_METHOD =
            new CorsException("Unsupported HTTP method", HttpServletResponse.SC_METHOD_NOT_ALLOWED);

    /**
     * Unsupported HTTP request header.
     */
    public static final CorsException UNSUPPORTED_REQUEST_HEADER =
            new CorsException("Unsupported HTTP request header", HttpServletResponse.SC_FORBIDDEN);

    /**
     * Invalid simple / actual request.
     */
    public static final CorsException INVALID_ACTUAL_REQUEST =
            new CorsException("Invalid simple/actual CORS request", HttpServletResponse.SC_BAD_REQUEST);

    /**
     * Invalid preflight request.
     */
    public static final CorsException INVALID_PREFLIGHT_REQUEST =
            new CorsException("Invalid preflight CORS request", HttpServletResponse.SC_BAD_REQUEST);

    /**
     * Missing Access-Control-Request-Method header.
     */
    public static final CorsException MISSING_ACCESS_CONTROL_REQUEST_METHOD_HEADER =
            new CorsException("Invalid preflight CORS request: Missing Access-Control-Request-Method header",
                    HttpServletResponse.SC_BAD_REQUEST);

    /**
     * The HTTP status code, zero if not specified.
     */
    private final int httpStatusCode;

    /**
     * Creates a new CORS exception with the specified message and
     * associated HTTP status code.
     *
     * @param message the message
     * @param httpStatusCode the HTTP status code, zero if not specified
     */
    private CorsException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Returns the associated HTTP status code.
     *
     * @return the HTTP status code, zero if not specified
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

}
