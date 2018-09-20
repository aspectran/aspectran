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
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.support.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Process an incoming cross-origin (CORS) requests.
 * Encapsulates the CORS processing logic as specified by the
 * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/">W3C candidate
 * recommendation</a> from 2013-01-29.
 *
 * @since 2.3.0
 */
public class DefaultCorsProcessor extends AbstractCorsProcessor {

    private static final Log log = LogFactory.getLog(DefaultCorsProcessor.class);

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
        if (rawRequestHeadersString != null) {
            String[] requestHeaders = StringUtils.splitCommaDelimitedString(rawRequestHeadersString);
            if (hasAllowedHeaders() && requestHeaders.length > 0) {
                for (String requestHeader : requestHeaders) {
                    if (!isAllowedHeader(requestHeader)) {
                        rejectRequest(translet, CorsException.UNSUPPORTED_REQUEST_HEADER);
                    }
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
        if (t instanceof CorsException) {
            CorsException corsException = (CorsException)t;
            HttpServletResponse res = translet.getResponseAdaptee();
            res.sendError(corsException.getHttpStatusCode(), corsException.getMessage());
        }
    }

    /**
     * Invoked when one of the CORS checks failed.
     * The default implementation sets the response status to 403.
     *
     * @param translet the translet
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
            log.debug("Skip CORS processing: response already contains \"Access-Control-Allow-Origin\" header");
            return false;
        } else {
            return true;
        }
    }

}
