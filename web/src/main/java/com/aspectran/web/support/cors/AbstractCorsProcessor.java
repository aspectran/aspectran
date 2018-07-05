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

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Process an incoming cross-origin (CORS) requests.
 * Encapsulates the CORS processing logic as specified by the
 * <a href="http://www.w3.org/TR/2013/CR-cors-20130129/">W3C candidate
 * recommendation</a> from 2013-01-29.
 *
 * @since 2.3.0
 */
public abstract class AbstractCorsProcessor implements CorsProcessor {

    /**
     * Wildcard representing <em>all</em> origins, methods, or headers.
     */
    static final String ALL = "*";

    /**
     * Origins that the CORS filter must allow. Requests from origins not
     * included here must be refused with a HTTP 403 "Forbidden" response.
     */
    private Set<String> allowedOrigins;

    /**
     * The supported HTTP methods. Requests for methods not included here
     * must be refused by the CORS filter with a HTTP 405 "Method not
     * allowed" response.
     */
    private Set<String> allowedMethods;

    /**
     * Pre-computed string of the CORS supported methods.
     */
    private String allowedMethodsString;

    /**
     * The names of the supported author request headers.
     */
    private Set<String> allowedHeaders;

    /**
     * Pre-computed string of the CORS supported headers.
     */
    private String allowedHeadersString;

    /**
     * The non-simple response headers that the web browser should expose
     * to the author of the CORS request.
     */
    private Set<String> exposedHeaders;

    /**
     * Pre-computed string of the CORS exposed headers.
     */
    private String exposedHeadersString;

    /**
     * Indicates whether user credentials, such as cookies, HTTP
     * authentication or client-side certificates, are supported.
     */
    private boolean allowCredentials;

    /**
     * Indicates how long the results of a preflight request can be cached
     * by the web client, in seconds. If {@code -1} unspecified.
     */
    private int maxAgeSeconds = -1;

    public String[] getAllowedOrigins() {
        if (allowedOrigins != null) {
            return allowedOrigins.toArray(new String[0]);
        } else {
            return null;
        }
    }

    public void setAllowedOrigins(String allowedOrigins) {
        String[] origins = StringUtils.splitCommaDelimitedString(allowedOrigins);
        setAllowedOrigins(origins);
    }

    public void setAllowedOrigins(String[] allowedOrigins) {
        Set<String> set = new HashSet<>();
        if (allowedOrigins != null) {
            Collections.addAll(set, allowedOrigins);
        }
        setAllowedOrigins(set);
    }

    public void setAllowedOrigins(Set<String> allowedOrigins) {
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            boolean allowAnyOrigin = allowedOrigins.contains("*");
            this.allowedOrigins = allowAnyOrigin ? null : allowedOrigins;
        } else {
            this.allowedOrigins = null;
        }
    }

    public String[] getAllowedMethods() {
        if (allowedMethods != null) {
            return allowedMethods.toArray(new String[0]);
        } else {
            return null;
        }
    }

    public boolean containsMethod(String method) {
        return (allowedMethods != null && allowedMethods.contains(method));
    }

    public String getAllowedMethodsString() {
        return allowedMethodsString;
    }

    public void setAllowedMethods(String allowedMethods) {
        String[] methods = StringUtils.splitCommaDelimitedString(allowedMethods);
        setAllowedMethods(methods);
    }

    public void setAllowedMethods(String[] allowedMethods) {
        Set<String> set = new HashSet<>();
        if (allowedMethods != null) {
            Collections.addAll(set, allowedMethods);
        }
        setAllowedMethods(set);
    }

    public void setAllowedMethods(Set<String> allowedMethods) {
        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            boolean allowAnyMethod = allowedMethods.contains(ALL);
            if (allowAnyMethod) {
                this.allowedMethods = null;
                this.allowedMethodsString = null;
            } else {
                this.allowedMethods = allowedMethods;
                this.allowedMethodsString = StringUtils.joinCommaDelimitedList(allowedMethods);
            }
        } else {
            this.allowedMethods = null;
            this.allowedMethodsString = null;
        }
    }

    public String[] getAllowedHeaders() {
        if (allowedHeaders != null) {
            return allowedHeaders.toArray(new String[0]);
        } else {
            return null;
        }
    }

    public String getAllowedHeadersString() {
        return allowedHeadersString;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        String[] headers = StringUtils.splitCommaDelimitedString(allowedHeaders);
        setAllowedHeaders(headers);
    }

    public void setAllowedHeaders(String[] allowedHeaders) {
        Set<String> set = new HashSet<>();
        if (allowedHeaders != null) {
            Collections.addAll(set, allowedHeaders);
        }
        setAllowedHeaders(set);
    }

    public void setAllowedHeaders(Set<String> allowedHeaders) {
        if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            boolean allowAnyHeader = allowedHeaders.contains(ALL);
            if (allowAnyHeader) {
                this.allowedHeaders = null;
                this.allowedHeadersString = null;
            } else {
                this.allowedHeaders = allowedHeaders;
                this.allowedHeadersString = StringUtils.joinCommaDelimitedList(allowedHeaders);
            }
        } else {
            this.allowedHeaders = null;
            this.allowedHeadersString = null;
        }
    }

    public String[] getExposedHeaders() {
        if (exposedHeaders != null) {
            return exposedHeaders.toArray(new String[0]);
        } else {
            return null;
        }
    }

    public String getExposedHeadersString() {
        return exposedHeadersString;
    }

    public void setExposedHeaders(String exposedHeaders) {
        String[] headers = StringUtils.splitCommaDelimitedString(exposedHeaders);
        setExposedHeaders(headers);
    }

    public void setExposedHeaders(String[] exposedHeaders) {
        Set<String> set = new HashSet<>();
        if (exposedHeaders != null) {
            Collections.addAll(set, exposedHeaders);
        }
        setExposedHeaders(set);
    }

    public void setExposedHeaders(Set<String> exposedHeaders) {
        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            boolean allowAnyHeader = exposedHeaders.contains(ALL);
            if (allowAnyHeader) {
                this.exposedHeaders = null;
                this.exposedHeadersString = null;
            } else {
                this.exposedHeaders = exposedHeaders;
                this.exposedHeadersString = StringUtils.joinCommaDelimitedList(exposedHeaders);
            }
        } else {
            this.exposedHeaders = null;
            this.exposedHeadersString = null;
        }
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public int getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(int maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    protected boolean hasAllowedOrigins() {
        return (allowedOrigins != null);
    }

    protected boolean hasAllowedMethods() {
        return (allowedMethods != null);
    }

    protected boolean hasAllowedHeaders() {
        return (allowedHeaders != null);
    }

    protected boolean hasExposedHeaders() {
        return (exposedHeaders != null);
    }

    /**
     * Helper method to check whether requests from the specified origin must be allowed.
     *
     * @param origin The origin as reported by the web client (browser), {@code null} if unknown.
     * @return {@code true} if the origin is allowed, else {@code false}.
     */
    protected boolean isAllowedOrigin(String origin) {
        return (allowedOrigins == null || allowedOrigins.contains(origin));
    }

    /**
     * Helper method to check whether the specified HTTP method is
     * supported. This is done by looking up {@link #allowedMethods}.
     * GET and HEAD, must never be disabled and should not return 405 error code.
     *
     * @param method The HTTP method.
     * @return {@code true} if the method is supported, else {@code false}.
     */
    protected boolean isAllowedMethod(String method) {
        if (allowedMethods == null) {
            return ("GET".equals(method) || "HEAD".equals(method));
        } else {
            return allowedMethods.contains(method);
        }
    }

    /**
     * Helper method to check whether the specified HTTP header is supported.
     *
     * @param header the HTTP header
     * @return {@code true} if the header is supported, else {@code false}.
     */
    protected boolean isAllowedHeader(String header) {
        return (allowedHeaders == null || allowedHeaders.contains(header));
    }

    /**
     * Returns {@code true} if the request is a valid CORS one.
     *
     * @param request the http request
     * @return {@code true} if the request is a valid CORS one, else {@code false}
     */
    protected boolean isCorsRequest(HttpServletRequest request) {
        return (request.getHeader(HttpHeaders.ORIGIN) != null);
    }

    /**
     * Returns {@code true} if the request is a valid CORS pre-flight one.
     *
     * @param request the http request
     * @return {@code true} if the request is a valid CORS pre-flight one, else {@code false}
     */
    protected boolean isPreFlightRequest(HttpServletRequest request) {
        return (isCorsRequest(request)
                && MethodType.OPTIONS.name().equals(request.getMethod())
                && request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null);
    }

}
