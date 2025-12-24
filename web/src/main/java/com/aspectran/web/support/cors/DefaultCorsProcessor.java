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
import com.aspectran.utils.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The default implementation of the {@link CorsProcessor} interface.
 * <p>This class extends {@link AbstractCorsProcessor} and implements the core logic
 * for handling pre-flight and actual CORS requests. It applies the appropriate
 * {@code Access-Control-*} headers to the response based on the configured CORS
 * rules and rejects requests that do not comply with those rules.
 * </p>
 *
 * @since 2.3.0
 */
@NullMarked
public class DefaultCorsProcessor extends AbstractCorsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCorsProcessor.class);

    /**
     * ""CORS.HTTP_STATUS_CODE"" attribute name.
     */
    private static final String CORS_HTTP_STATUS_CODE = "CORS.HTTP_STATUS_CODE";

    /**
     * "CORS.HTTP_STATUS_TEXT" attribute name.
     */
    private static final String CORS_HTTP_STATUS_TEXT = "CORS.HTTP_STATUS_TEXT";

    @Override
    public void processActualRequest(Translet translet) throws CorsException {
        HttpServletRequest req = translet.getRequestAdaptee();
        HttpServletResponse res = translet.getResponseAdaptee();

        if (!isCorsRequest(req)) {
            return;
        }
        if (!checkProcessable(res)) {
            return;
        }
        if (!isAllowedMethod(req.getMethod())) {
            rejectRequest(translet, CorsException.UNSUPPORTED_METHOD);
        }

        String origin = req.getHeader(HttpHeaders.ORIGIN);
        if (!isAllowedOrigin(origin)) {
            rejectRequest(translet, CorsException.ORIGIN_DENIED);
        }

        if (isAllowCredentials()) {
            // Must be exact origin (not '*') in case of credentials
            res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            res.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        } else {
            res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, hasAllowedOrigins() ? origin : ALL);
            res.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        }

        if (getExposedHeadersString() != null) {
            res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, getExposedHeadersString());
        }
    }

    @Override
    public void processPreflightRequest(Translet translet) throws CorsException {
        HttpServletRequest req = translet.getRequestAdaptee();
        HttpServletResponse res = translet.getResponseAdaptee();

        if (!isPreFlightRequest(req)) {
            rejectRequest(translet, CorsException.INVALID_PREFLIGHT_REQUEST);
        }
        if (!checkProcessable(res)) {
            return;
        }

        String requestedMethod = req.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        if (!isAllowedMethod(requestedMethod)) {
            rejectRequest(translet, CorsException.UNSUPPORTED_METHOD);
        }

        String rawRequestHeadersString = req.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        if (rawRequestHeadersString != null && hasAllowedHeaders()) {
            String[] requestHeaders = StringUtils.splitWithComma(rawRequestHeadersString);
            for (String requestHeader : requestHeaders) {
                if (!isAllowedHeader(requestHeader)) {
                    rejectRequest(translet, CorsException.UNSUPPORTED_REQUEST_HEADER);
                }
            }
        }

        String origin = req.getHeader(HttpHeaders.ORIGIN);
        if (origin != null) {
            if (isAllowCredentials()) {
                // Must be exact origin (not '*') in case of credentials
                res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                res.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
            } else {
                res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, hasAllowedOrigins() ? origin : ALL);
                res.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
            }
        }

        if (getAllowedMethodsString() != null) {
            res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, getAllowedMethodsString());
        }
        if (getAllowedHeadersString() != null) {
            res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, getAllowedHeadersString());
        } else if (rawRequestHeadersString != null) {
            res.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, rawRequestHeadersString);
        }
        if (getMaxAgeSeconds() > 0) {
            res.addHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, Integer.toString(getMaxAgeSeconds()));
        }
    }

    @Override
    public void sendError(Translet translet) throws IOException {
        Throwable t = translet.getRootCauseOfRaisedException();
        if (t instanceof CorsException corsException) {
            HttpServletResponse res = translet.getResponseAdaptee();
            res.sendError(corsException.getHttpStatusCode(), corsException.getMessage());
        }
    }

    /**
     * Invoked when one of the CORS checks failed.
     * The default implementation sets the response status to 403.
     * @param translet the Translet instance
     * @param ce the CORS Exception
     * @throws CorsException if the request is denied
     */
    protected void rejectRequest(Translet translet, CorsException ce) throws CorsException {
        HttpServletResponse res = translet.getResponseAdaptee();
        res.setStatus(ce.getHttpStatusCode());

        translet.setAttribute(CORS_HTTP_STATUS_CODE, ce.getHttpStatusCode());
        translet.setAttribute(CORS_HTTP_STATUS_TEXT, ce.getMessage());

        throw ce;
    }

    protected boolean checkProcessable(HttpServletResponse res) {
        if (res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skip CORS processing: response already contains \"Access-Control-Allow-Origin\" header");
            }
            return false;
        } else {
            return true;
        }
    }

}
