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

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Abstract base class for {@link CorsProcessor} implementations.
 * <p>This class provides the configuration properties and common logic for processing
 * Cross-Origin Resource Sharing (CORS) requests, based on the W3C recommendation.
 * It encapsulates settings for allowed origins, methods, headers, and other CORS
 * attributes, and provides helper methods to check requests against this configuration.
 * </p>
 *
 * @since 2.3.0
 * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommendation</a>
 */
@NullMarked
public abstract class AbstractCorsProcessor implements CorsProcessor {

    /**
     * Wildcard representing <em>all</em> origins, methods, or headers.
     */
    static final String ALL = "*";

    /**
     * The default set of allowed HTTP methods.
     */
    private static final Set<String> DEFAULT_ALLOWED_METHODS = Set.of(MethodType.GET.name(), MethodType.HEAD.name());

    /**
     * Origins that the CORS filter must allow. Requests from origins not
     * included here must be refused with an HTTP 403 "Forbidden" response.
     * A {@code null} value means a wildcard {@code *} is configured.
     */
    @Nullable
    private Set<String> allowedOrigins;

    /**
     * The supported HTTP methods. Requests for methods not included here
     * must be refused by the CORS filter with an HTTP 405 "Method not
     * allowed" response.
     * A {@code null} value means a wildcard {@code *} is configured.
     */
    @Nullable
    private Set<String> allowedMethods;

    /**
     * Pre-computed string of the CORS supported methods.
     */
    @Nullable
    private String allowedMethodsString;

    /**
     * The names of the supported author request headers.
     */
    @Nullable
    private Set<String> allowedHeaders;

    /**
     * Pre-computed string of the CORS supported headers.
     */
    @Nullable
    private String allowedHeadersString;

    /**
     * The non-simple response headers that the web browser should expose
     * to the author of the CORS request.
     */
    @Nullable
    private Set<String> exposedHeaders;

    /**
     * Pre-computed string of the CORS exposed headers.
     */
    @Nullable
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

    /**
     * Gets the allowed origins for CORS requests.
     * @return the array of allowed origins, or {@code null} if all origins are allowed
     */
    public String @Nullable [] getAllowedOrigins() {
        if (allowedOrigins != null) {
            return allowedOrigins.toArray(new String[0]);
        } else {
            return null;
        }
    }

    /**
     * Sets the allowed origins from a comma-separated string.
     * @param allowedOrigins a comma-separated string of allowed origins
     */
    public void setAllowedOrigins(@Nullable String allowedOrigins) {
        String[] origins = StringUtils.splitWithComma(allowedOrigins);
        setAllowedOrigins(origins);
    }

    /**
     * Sets the allowed origins from a string array.
     * @param allowedOrigins an array of allowed origins
     */
    public void setAllowedOrigins(String @Nullable [] allowedOrigins) {
        Set<String> set = new LinkedHashSet<>();
        if (allowedOrigins != null) {
            Collections.addAll(set, allowedOrigins);
        }
        setAllowedOrigins(set);
    }

    /**
     * Sets the allowed origins from a set. If the set contains the wildcard "*",
     * all origins will be allowed.
     * @param allowedOrigins a set of allowed origins
     */
    public void setAllowedOrigins(@Nullable Set<String> allowedOrigins) {
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            boolean allowAnyOrigin = allowedOrigins.contains(ALL);
            this.allowedOrigins = allowAnyOrigin ? null : Collections.unmodifiableSet(new LinkedHashSet<>(allowedOrigins));
        } else {
            this.allowedOrigins = null;
        }
    }

    /**
     * Gets the allowed HTTP methods for CORS requests.
     * @return the array of allowed methods, or {@code null} if all methods are allowed
     */
    public String @Nullable [] getAllowedMethods() {
        if (allowedMethods != null) {
            return allowedMethods.toArray(new String[0]);
        } else {
            return null;
        }
    }

    /**
     * Returns whether the specified method is in the list of allowed methods.
     * @param method the HTTP method
     * @return {@code true} if the method is allowed, {@code false} otherwise
     */
    public boolean containsMethod(@Nullable String method) {
        return (method != null && isAllowedMethod(method));
    }

    /**
     * Gets the pre-computed string of allowed HTTP methods for the
     * {@code Access-Control-Allow-Methods} header.
     * @return the comma-separated string of allowed methods
     */
    @Nullable
    public String getAllowedMethodsString() {
        return allowedMethodsString;
    }

    /**
     * Sets the allowed HTTP methods from a comma-separated string.
     * @param allowedMethods a comma-separated string of allowed methods
     */
    public void setAllowedMethods(@Nullable String allowedMethods) {
        String[] methods = StringUtils.splitWithComma(allowedMethods);
        setAllowedMethods(methods);
    }

    /**
     * Sets the allowed HTTP methods from a string array.
     * @param allowedMethods an array of allowed methods
     */
    public void setAllowedMethods(String @Nullable [] allowedMethods) {
        Set<String> set = new LinkedHashSet<>();
        if (allowedMethods != null) {
            Collections.addAll(set, allowedMethods);
        }
        setAllowedMethods(set);
    }

    /**
     * Sets the allowed HTTP methods from a set. If the set is null or empty, it defaults
     * to GET and HEAD. If the set contains the wildcard "*", all methods are allowed.
     * @param allowedMethods a set of allowed methods
     */
    public void setAllowedMethods(@Nullable Set<String> allowedMethods) {
        if (allowedMethods == null || allowedMethods.isEmpty()) {
            this.allowedMethods = DEFAULT_ALLOWED_METHODS;
        } else if (allowedMethods.contains(ALL)) {
            this.allowedMethods = null; // Allow all
        } else {
            Set<String> set = new LinkedHashSet<>();
            for (String method : allowedMethods) {
                if (StringUtils.hasText(method)) {
                    set.add(method.trim().toUpperCase(Locale.ROOT));
                }
            }
            this.allowedMethods = Collections.unmodifiableSet(set);
        }
        if (this.allowedMethods != null) {
            this.allowedMethodsString = StringUtils.joinWithCommas(this.allowedMethods);
        } else {
            this.allowedMethodsString = null;
        }
    }

    /**
     * Gets the allowed headers for CORS requests.
     * @return the array of allowed headers, or {@code null} if all headers are allowed
     */
    public String @Nullable [] getAllowedHeaders() {
        if (allowedHeaders != null) {
            return allowedHeaders.toArray(new String[0]);
        } else {
            return null;
        }
    }

    /**
     * Gets the pre-computed string of allowed headers for the
     * {@code Access-Control-Allow-Headers} header.
     * @return the comma-separated string of allowed headers
     */
    @Nullable
    public String getAllowedHeadersString() {
        return allowedHeadersString;
    }

    /**
     * Sets the allowed headers from a comma-separated string.
     * @param allowedHeaders a comma-separated string of allowed headers
     */
    public void setAllowedHeaders(@Nullable String allowedHeaders) {
        String[] headers = StringUtils.splitWithComma(allowedHeaders);
        setAllowedHeaders(headers);
    }

    /**
     * Sets the allowed headers from a string array.
     * @param allowedHeaders an array of allowed headers
     */
    public void setAllowedHeaders(String @Nullable [] allowedHeaders) {
        Set<String> set = new LinkedHashSet<>();
        if (allowedHeaders != null) {
            Collections.addAll(set, allowedHeaders);
        }
        setAllowedHeaders(set);
    }

    /**
     * Sets the allowed headers from a set. If the set contains the wildcard "*",
     * all headers will be allowed.
     * @param allowedHeaders a set of allowed headers
     */
    public void setAllowedHeaders(@Nullable Set<String> allowedHeaders) {
        if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            boolean allowAnyHeader = allowedHeaders.contains(ALL);
            if (allowAnyHeader) {
                this.allowedHeaders = null;
                this.allowedHeadersString = null;
            } else {
                Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                set.addAll(allowedHeaders);
                this.allowedHeaders = Collections.unmodifiableSet(set);
                this.allowedHeadersString = StringUtils.joinWithCommas(allowedHeaders);
            }
        } else {
            this.allowedHeaders = null;
            this.allowedHeadersString = null;
        }
    }

    /**
     * Gets the headers to expose to the client.
     * @return the array of exposed headers
     */
    public String @Nullable [] getExposedHeaders() {
        if (exposedHeaders != null) {
            return exposedHeaders.toArray(new String[0]);
        } else {
            return null;
        }
    }

    /**
     * Gets the pre-computed string of exposed headers for the
     * {@code Access-Control-Expose-Headers} header.
     * @return the comma-separated string of exposed headers
     */
    @Nullable
    public String getExposedHeadersString() {
        return exposedHeadersString;
    }

    /**
     * Sets the exposed headers from a comma-separated string.
     * @param exposedHeaders a comma-separated string of exposed headers
     */
    public void setExposedHeaders(@Nullable String exposedHeaders) {
        String[] headers = StringUtils.splitWithComma(exposedHeaders);
        setExposedHeaders(headers);
    }

    /**
     * Sets the exposed headers from a string array.
     * @param exposedHeaders an array of exposed headers
     */
    public void setExposedHeaders(String @Nullable [] exposedHeaders) {
        Set<String> set = new LinkedHashSet<>();
        if (exposedHeaders != null) {
            Collections.addAll(set, exposedHeaders);
        }
        setExposedHeaders(set);
    }

    /**
     * Sets the exposed headers from a set.
     * @param exposedHeaders a set of exposed headers
     */
    public void setExposedHeaders(@Nullable Set<String> exposedHeaders) {
        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            boolean allowAnyHeader = exposedHeaders.contains(ALL);
            if (allowAnyHeader) {
                this.exposedHeaders = null;
                this.exposedHeadersString = null;
            } else {
                Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                set.addAll(exposedHeaders);
                this.exposedHeaders = Collections.unmodifiableSet(set);
                this.exposedHeadersString = StringUtils.joinWithCommas(exposedHeaders);
            }
        } else {
            this.exposedHeaders = null;
            this.exposedHeadersString = null;
        }
    }

    /**
     * Returns whether user credentials are to be supported.
     * @return {@code true} if credentials are to be supported, {@code false} otherwise
     */
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    /**
     * Sets whether user credentials, such as cookies, HTTP authentication or
     * client-side certificates, are to be supported.
     * @param allowCredentials {@code true} to support credentials, {@code false} otherwise
     */
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    /**
     * Gets the cache duration for pre-flight requests.
     * @return the cache duration in seconds, or -1 if not specified
     */
    public int getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    /**
     * Sets how long the results of a pre-flight request can be cached by the
     * web client, in seconds.
     * @param maxAgeSeconds the cache duration in seconds, or -1 to disable
     */
    public void setMaxAgeSeconds(int maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    /**
     * Returns whether any origins are configured.
     * @return {@code true} if origins are configured, {@code false} otherwise
     */
    protected boolean hasAllowedOrigins() {
        return (allowedOrigins != null);
    }

    /**
     * Returns whether any methods are configured.
     * @return {@code true} if methods are configured, {@code false} otherwise
     */
    protected boolean hasAllowedMethods() {
        return (allowedMethods != null);
    }

    /**
     * Returns whether any headers are configured.
     * @return {@code true} if headers are configured, {@code false} otherwise
     */
    protected boolean hasAllowedHeaders() {
        return (allowedHeaders != null);
    }

    /**
     * Returns whether any exposed headers are configured.
     * @return {@code true} if exposed headers are configured, {@code false} otherwise
     */
    protected boolean hasExposedHeaders() {
        return (exposedHeaders != null);
    }

    /**
     * Checks whether requests from the specified origin must be allowed.
     * @param origin The origin as reported by the web client (browser), {@code null} if unknown.
     * @return {@code true} if the origin is allowed, else {@code false}.
     */
    protected boolean isAllowedOrigin(@Nullable String origin) {
        return (allowedOrigins == null || (origin != null && allowedOrigins.contains(origin)));
    }

    /**
     * Checks whether the specified HTTP method is supported. If a wildcard ({@code *})
     * was configured, all methods are allowed. If no methods were configured, it
     * defaults to GET and HEAD.
     * @param method The HTTP method.
     * @return {@code true} if the method is supported, else {@code false}.
     */
    protected boolean isAllowedMethod(@Nullable String method) {
        if (allowedMethods == null) {
            return true; // Wildcard '*' was configured, allow all
        }
        if (method == null) {
            return false;
        }
        return allowedMethods.contains(method.toUpperCase(Locale.ROOT));
    }

    /**
     * Checks whether the specified HTTP header is supported.
     * @param header the HTTP header
     * @return {@code true} if the header is supported, else {@code false}.
     */
    protected boolean isAllowedHeader(@Nullable String header) {
        return (allowedHeaders == null || (header != null && allowedHeaders.contains(header)));
    }

    @Override
    public boolean isCorsRequest(RequestAdapter request) {
        return (request.getHeader(HttpHeaders.ORIGIN) != null);
    }

    @Override
    public boolean isPreflightRequest(RequestAdapter request) {
        return (isCorsRequest(request)
                && MethodType.OPTIONS.equals(request.getRequestMethod())
                && request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null);
    }

}
