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
package com.aspectran.web.support.http;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * An enumeration of HTTP status codes.
 *
 * @see <a href="https://www.iana.org/assignments/http-status-codes">HTTP Status Code Registry</a>
 * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_status_codes">List of HTTP status codes - Wikipedia</a>
 */
public enum HttpStatus {

    // 1xx Informational

    /**
     * {@code 100 Continue}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.2.1">Section 15.2.1 of RFC 9110</a>
     */
    CONTINUE(100, "Continue"),
    /**
     * {@code 101 Switching Protocols}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.2.2">Section 15.2.2 of RFC 9110</a>
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    /**
     * {@code 102 Processing}.
     * @see <a href="https://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
     */
    PROCESSING(102, "Processing"),
    /**
     * {@code 103 Early Hints}.
     * @see <a href="https://tools.ietf.org/html/rfc8297">RFC 8297</a>
     */
    EARLY_HINTS(103, "Early Hints"),
    /**
     * {@code 103 Checkpoint}.
     * @see <a href="https://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
     * resumable POST/PUT HTTP requests in HTTP/1.0</a>
     * @deprecated in favor of {@link #EARLY_HINTS}
     */
    @Deprecated
    CHECKPOINT(103, "Checkpoint"),

    // 2xx Success

    /**
     * {@code 200 OK}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.3.1">Section 15.3.1 of RFC 9110</a>
     */
    OK(200, "OK"),
    /**
     * {@code 201 Created}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.3.2">Section 15.3.2 of RFC 9110</a>
     */
    CREATED(201, "Created"),
    /**
     * {@code 202 Accepted}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.3.3">Section 15.3.3 of RFC 9110</a>
     */
    ACCEPTED(202, "Accepted"),
    /**
     * {@code 203 Non-Authoritative Information}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.3.4">Section 15.3.4 of RFC 9110</a>
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    /**
     * {@code 204 No Content}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.3.5">Section 15.3.5 of RFC 9110</a>
     */
    NO_CONTENT(204, "No Content"),
    /**
     * {@code 205 Reset Content}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.3.6">Section 15.3.6 of RFC 9110</a>
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * {@code 206 Partial Content}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.3.7">Section 15.3.7 of RFC 9110</a>
     */
    PARTIAL_CONTENT(206, "Partial Content"),
    /**
     * {@code 207 Multi-Status}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
     */
    MULTI_STATUS(207, "Multi-Status"),
    /**
     * {@code 208 Already Reported}.
     * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
     */
    ALREADY_REPORTED(208, "Already Reported"),
    /**
     * {@code 226 IM Used}.
     * @see <a href="https://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
     */
    IM_USED(226, "IM Used"),

    // 3xx Redirection

    /**
     * {@code 300 Multiple Choices}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.1">Section 15.4.1 of RFC 9110</a>
     */
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    /**
     * {@code 301 Moved Permanently}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.2">Section 15.4.2 of RFC 9110</a>
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * {@code 302 Found}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.3">Section 15.4.3 of RFC 9110</a>
     */
    FOUND(302, "Found"),
    /**
     * {@code 303 See Other}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.4">Section 15.4.4 of RFC 9110</a>
     */
    SEE_OTHER(303, "See Other"),
    /**
     * {@code 304 Not Modified}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.5">Section 15.4.5 of RFC 9110</a>
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * {@code 305 Use Proxy}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.6">Section 15.4.6 of RFC 9110</a>
     * @deprecated due to security concerns regarding in-band configuration of a proxy
     */
    @Deprecated
    USE_PROXY(305, "Use Proxy"),
    /**
     * {@code 307 Temporary Redirect}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.8">Section 15.4.8 of RFC 9110</a>
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /**
     * {@code 308 Permanent Redirect}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.4.9">Section 15.4.9 of RFC 9110</a>
     */
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    // --- 4xx Client Error ---

    /**
     * {@code 400 Bad Request}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.1">Section 15.5.1 of RFC 9110</a>
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * {@code 401 Unauthorized}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.2">Section 15.5.2 of RFC 9110</a>
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * {@code 402 Payment Required}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.3">Section 15.5.3 of RFC 9110</a>
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * {@code 403 Forbidden}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.4">Section 15.5.4 of RFC 9110</a>
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * {@code 404 Not Found}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.5">Section 15.5.5 of RFC 9110</a>
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * {@code 405 Method Not Allowed}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.6">Section 15.5.6 of RFC 9110</a>
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * {@code 406 Not Acceptable}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.7">Section 15.5.7 of RFC 9110</a>
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * {@code 407 Proxy Authentication Required}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.8">Section 15.5.8 of RFC 9110</a>
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /**
     * {@code 408 Request Timeout}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.9">Section 15.5.9 of RFC 9110</a>
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /**
     * {@code 409 Conflict}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.10">Section 15.5.10 of RFC 9110</a>
     */
    CONFLICT(409, "Conflict"),
    /**
     * {@code 410 Gone}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.11">Section 15.5.11 of RFC 9110</a>
     */
    GONE(410, "Gone"),
    /**
     * {@code 411 Length Required}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.12">Section 15.5.12 of RFC 9110</a>
     */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * {@code 412 Precondition failed}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.13">Section 15.5.13 of RFC 9110</a>
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * {@code 413 Payload Too Large}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.14">Section 15.5.14 of RFC 9110</a>
     */
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    /**
     * {@code 414 URI Too Long}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.15">Section 15.5.15 of RFC 9110</a>
     */
    URI_TOO_LONG(414, "URI Too Long"),
    /**
     * {@code 415 Unsupported Media Type}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.16">Section 15.5.16 of RFC 9110</a>
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * {@code 416 Requested Range Not Satisfiable}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.17">Section 15.5.17 of RFC 9110</a>
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested range not satisfiable"),
    /**
     * {@code 417 Expectation Failed}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.18">Section 15.5.18 of RFC 9110</a>
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /**
     * {@code 418 I'm a teapot}.
     * @see <a href="https://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
     */
    I_AM_A_TEAPOT(418, "I'm a teapot"),
    /**
     * {@code 422 Unprocessable Entity}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.21">Section 15.5.21 of RFC 9110</a>
     */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    /**
     * {@code 423 Locked}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
     */
    LOCKED(423, "Locked"),
    /**
     * {@code 424 Failed Dependency}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
     */
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    /**
     * {@code 425 Too Early}.
     * @see <a href="https://tools.ietf.org/html/rfc8470">RFC 8470</a>
     */
    TOO_EARLY(425, "Too Early"),
    /**
     * {@code 426 Upgrade Required}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.5.22">Section 15.5.22 of RFC 9110</a>
     */
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    /**
     * {@code 428 Precondition Required}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
     */
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    /**
     * {@code 429 Too Many Requests}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    /**
     * {@code 431 Request Header Fields Too Large}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    /**
     * {@code 451 Unavailable For Legal Reasons}.
     * @see <a href="https://tools.ietf.org/html/rfc7725">RFC 7725</a>
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    // --- 5xx Server Error ---

    /**
     * {@code 500 Internal Server Error}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.6.1">Section 15.6.1 of RFC 9110</a>
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * {@code 501 Not Implemented}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.6.2">Section 15.6.2 of RFC 9110</a>
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * {@code 502 Bad Gateway}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.6.3">Section 15.6.3 of RFC 9110</a>
     */
    BAD_GATEWAY(502, "Bad Gateway"),
    /**
     * {@code 503 Service Unavailable}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.6.4">Section 15.6.4 of RFC 9110</a>
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /**
     * {@code 504 Gateway Timeout}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.6.5">Section 15.6.5 of RFC 9110</a>
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /**
     * {@code 505 HTTP Version Not Supported}.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-15.6.6">Section 15.6.6 of RFC 9110</a>
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version not supported"),
    /**
     * {@code 506 Variant Also Negotiates}
     * @see <a href="https://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
     */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    /**
     * {@code 507 Insufficient Storage}
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
     */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    /**
     * {@code 508 Loop Detected}
     * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
     */
    LOOP_DETECTED(508, "Loop Detected"),
    /**
     * {@code 509 Bandwidth Limit Exceeded}
     */
    BANDWIDTH_LIMIT_EXCEEDED(509, "Bandwidth Limit Exceeded"),
    /**
     * {@code 510 Not Extended}
     * @see <a href="https://tools.ietf.org/html/rfc2774#section-7">HTTP Extension Framework</a>
     */
    NOT_EXTENDED(510, "Not Extended"),
    /**
     * {@code 511 Network Authentication Required}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    private static final HttpStatus[] VALUES = values();

    private final int value;

    private final String reasonPhrase;

    HttpStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Return the integer value of this status code.
     * @return the integer value of this status code
     */
    public int value() {
        return this.value;
    }

    /**
     * Return the reason phrase of this status code.
     * @return the reason phrase of this status code
     */
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    /**
     * Whether this status code is in the HTTP series
     * {@link Series#INFORMATIONAL}.
     * This is a shortcut for {@code Series.forStatus(this.value) == Series.INFORMATIONAL}.
     * @return whether this status code is in the INFORMATIONAL series
     * @see #getSeries()
     */
    public boolean is1xxInformational() {
        return (getSeries() == Series.INFORMATIONAL);
    }

    /**
     * Whether this status code is in the HTTP series
     * {@link Series#SUCCESSFUL}.
     * This is a shortcut for {@code Series.forStatus(this.value) == Series.SUCCESSFUL}.
     * @return whether this status code is in the SUCCESSFUL series
     * @see #getSeries()
     */
    public boolean is2xxSuccessful() {
        return (getSeries() == Series.SUCCESSFUL);
    }

    /**
     * Whether this status code is in the HTTP series
     * {@link Series#REDIRECTION}.
     * This is a shortcut for {@code Series.forStatus(this.value) == Series.REDIRECTION}.
     * @return whether this status code is in the REDIRECTION series
     * @see #getSeries()
     */
    public boolean is3xxRedirection() {
        return (getSeries() == Series.REDIRECTION);
    }

    /**
     * Whether this status code is in the HTTP series
     * {@link Series#CLIENT_ERROR}.
     * This is a shortcut for {@code Series.forStatus(this.value) == Series.CLIENT_ERROR}.
     * @return whether this status code is in the CLIENT_ERROR series
     * @see #getSeries()
     */
    public boolean is4xxClientError() {
        return (getSeries() == Series.CLIENT_ERROR);
    }

    /**
     * Whether this status code is in the HTTP series
     * {@link Series#SERVER_ERROR}.
     * This is a shortcut for {@code Series.forStatus(this.value) == Series.SERVER_ERROR}.
     * @return whether this status code is in the SERVER_ERROR series
     * @see #getSeries()
     */
    public boolean is5xxServerError() {
        return (getSeries() == Series.SERVER_ERROR);
    }

    /**
     * Whether this status code is in the HTTP series
     * {@link Series#CLIENT_ERROR} or {@link Series#SERVER_ERROR}.
     * @return whether this status code is in the CLIENT_ERROR or SERVER_ERROR series
     * @see #is4xxClientError()
     * @see #is5xxServerError()
     */
    public boolean isError() {
        return (is4xxClientError() || is5xxServerError());
    }

    /**
     * Returns the HTTP status series of this status code.
     * @return the HTTP status series
     * @see Series
     */
    @NonNull
    public Series getSeries() {
        return Series.forStatus(this.value);
    }

    /**
     * Return a string representation of this status code.
     * @return a string representation of this status code
     */
    @Override
    @NonNull
    public String toString() {
        return this.value + " " + name();
    }

    /**
     * Return the enum constant of this type with the specified numeric value.
     * @param statusCode the numeric value of the enum to be returned
     * @return the enum constant with the specified numeric value
     * @throws IllegalArgumentException if this enum has no constant for the specified numeric value
     */
    @NonNull
    public static HttpStatus valueOf(int statusCode) {
        HttpStatus status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        }
        return status;
    }

    /**
     * Resolve the given status code to an {@code HttpStatus}, if possible.
     * @param statusCode the HTTP status code (potentially non-standard)
     * @return the corresponding {@code HttpStatus}, or {@code null} if not found
     */
    @Nullable
    public static HttpStatus resolve(int statusCode) {
        for (HttpStatus status : VALUES) {
            if (status.value == statusCode) {
                return status;
            }
        }
        return null;
    }

    /**
     * An enumeration of HTTP status series.
     * @see <a href="https://www.iana.org/assignments/http-status-codes">HTTP Status Code Registry</a>
     */
    public enum Series {

        INFORMATIONAL(1), // 1xx
        SUCCESSFUL(2),    // 2xx
        REDIRECTION(3),   // 3xx
        CLIENT_ERROR(4),  // 4xx
        SERVER_ERROR(5);  // 5xx

        private final int value;

        Series(int value) {
            this.value = value;
        }

        /**
         * Return the integer value of this status series.
         * @return the integer value of this status series
         */
        public int value() {
            return this.value;
        }

        /**
         * Return the enum constant of this type with the specified status code.
         * @param statusCode the HTTP status code
         * @return the enum constant for the series of the specified status code
         * @throws IllegalArgumentException if this enum has no corresponding constant
         */
        @NonNull
        public static Series forStatus(int statusCode) {
            Series series = resolve(statusCode);
            if (series == null) {
                throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
            }
            return series;
        }

        /**
         * Resolve the given status code to an {@code HttpStatus.Series}, if possible.
         * @param statusCode the HTTP status code
         * @return the corresponding {@code Series}, or {@code null} if not found
         */
        @Nullable
        public static Series resolve(int statusCode) {
            int seriesCode = statusCode / 100;
            return switch (seriesCode) {
                case 1 -> INFORMATIONAL;
                case 2 -> SUCCESSFUL;
                case 3 -> REDIRECTION;
                case 4 -> CLIENT_ERROR;
                case 5 -> SERVER_ERROR;
                default -> null;
            };
        }
    }

}
