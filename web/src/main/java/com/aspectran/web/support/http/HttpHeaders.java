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

/**
 * Represents a collection of standard HTTP header names.
 */
public interface HttpHeaders {

    /**
     * The HTTP {@code Accept} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-12.5.1">Section 12.5.1 of RFC 9110</a>
     */
    String ACCEPT = "Accept";

    /**
     * The HTTP {@code Accept-Charset} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-12.5.2">Section 12.5.2 of RFC 9110</a>
     */
    String ACCEPT_CHARSET = "Accept-Charset";

    /**
     * The HTTP {@code Accept-Encoding} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-12.5.3">Section 12.5.3 of RFC 9110</a>
     */
    String ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * The HTTP {@code Accept-Language} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-12.5.4">Section 12.5.4 of RFC 9110</a>
     */
    String ACCEPT_LANGUAGE = "Accept-Language";

    /**
     * The HTTP {@code Accept-Ranges} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-14.3">Section 14.3 of RFC 9110</a>
     */
    String ACCEPT_RANGES = "Accept-Ranges";

    /**
     * The CORS {@code Access-Control-Allow-Credentials} response header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    /**
     * The CORS {@code Access-Control-Allow-Headers} response header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    /**
     * The CORS {@code Access-Control-Allow-Methods} response header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    /**
     * The CORS {@code Access-Control-Allow-Origin} response header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    /**
     * The CORS {@code Access-Control-Expose-Headers} response header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    /**
     * The CORS {@code Access-Control-Max-Age} response header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    /**
     * The CORS {@code Access-Control-Request-Headers} request header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    /**
     * The CORS {@code Access-Control-Request-Method} request header field name.
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

    /**
     * The HTTP {@code Age} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9111#section-5.1">Section 5.1 of RFC 9111</a>
     */
    String AGE = "Age";

    /**
     * The HTTP {@code Allow} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.2.1">Section 10.2.1 of RFC 9110</a>
     */
    String ALLOW = "Allow";

    /**
     * The HTTP {@code Authorization} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-11.6.2">Section 11.6.2 of RFC 9110</a>
     */
    String AUTHORIZATION = "Authorization";

    /**
     * The HTTP {@code Cache-Control} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9111#section-5.2">Section 5.2 of RFC 9111</a>
     */
    String CACHE_CONTROL = "Cache-Control";

    /**
     * The HTTP {@code Clear-Site-Data} header field name.
     * @see <a href="https://www.w3.org/TR/clear-site-data/">Clear Site Data</a>
     */
    String CLEAR_SITE_DATA = "Clear-Site-Data";

    /**
     * The HTTP {@code Connection} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-7.6.1">Section 7.6.1 of RFC 9110</a>
     */
    String CONNECTION = "Connection";

    /**
     * The HTTP {@code Content-Disposition} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266</a>
     */
    String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * The HTTP {@code Content-Encoding} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.4">Section 8.4 of RFC 9110</a>
     */
    String CONTENT_ENCODING = "Content-Encoding";

    /**
     * The HTTP {@code Content-Language} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.5">Section 8.5 of RFC 9110</a>
     */
    String CONTENT_LANGUAGE = "Content-Language";

    /**
     * The HTTP {@code Content-Length} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.6">Section 8.6 of RFC 9110</a>
     */
    String CONTENT_LENGTH = "Content-Length";

    /**
     * The HTTP {@code Content-Location} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.7">Section 8.7 of RFC 9110</a>
     */
    String CONTENT_LOCATION = "Content-Location";

    /**
     * The HTTP {@code Content-Range} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-14.4">Section 14.4 of RFC 9110</a>
     */
    String CONTENT_RANGE = "Content-Range";

    /**
     * The HTTP {@code Content-Security-Policy} header field name.
     * @see <a href="https://www.w3.org/TR/CSP3/">Content Security Policy Level 3</a>
     */
    String CONTENT_SECURITY_POLICY = "Content-Security-Policy";

    /**
     * The HTTP {@code Content-Security-Policy-Report-Only} header field name.
     * @see <a href="https://www.w3.org/TR/CSP3/">Content Security Policy Level 3</a>
     */
    String CONTENT_SECURITY_POLICY_REPORT_ONLY = "Content-Security-Policy-Report-Only";

    /**
     * The HTTP {@code Content-Type} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.3">Section 8.3 of RFC 9110</a>
     */
    String CONTENT_TYPE = "Content-Type";

    /**
     * The HTTP {@code Cookie} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6265#section-4.2">Section 4.2 of RFC 6265</a>
     */
    String COOKIE = "Cookie";

    /**
     * The HTTP {@code Cross-Origin-Embedder-Policy} (COEP) header field name.
     * @see <a href="https://html.spec.whatwg.org/multipage/origin.html#coep">COEP</a>
     */
    String CROSS_ORIGIN_EMBEDDER_POLICY = "Cross-Origin-Embedder-Policy";

    /**
     * The HTTP {@code Cross-Origin-Opener-Policy} (COOP) header field name.
     * @see <a href="https://html.spec.whatwg.org/multipage/origin.html#cross-origin-opener-policies">COOP</a>
     */
    String CROSS_ORIGIN_OPENER_POLICY = "Cross-Origin-Opener-Policy";

    /**
     * The HTTP {@code Cross-Origin-Resource-Policy} (CORP) header field name.
     * @see <a href="https://fetch.spec.whatwg.org/#cross-origin-resource-policy-header">CORP</a>
     */
    String CROSS_ORIGIN_RESOURCE_POLICY = "Cross-Origin-Resource-Policy";

    /**
     * The HTTP {@code Date} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-6.6.1">Section 6.6.1 of RFC 9110</a>
     */
    String DATE = "Date";

    /**
     * The HTTP {@code ETag} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.8.3">Section 8.8.3 of RFC 9110</a>
     */
    String ETAG = "ETag";

    /**
     * The HTTP {@code Expect} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.1.1">Section 10.1.1 of RFC 9110</a>
     */
    String EXPECT = "Expect";

    /**
     * The HTTP {@code Expires} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9111#section-5.3">Section 5.3 of RFC 9111</a>
     */
    String EXPIRES = "Expires";

    /**
     * The HTTP {@code Forwarded} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc7239">RFC 7239</a>
     */
    String FORWARDED = "Forwarded";

    /**
     * The HTTP {@code From} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.1.2">Section 10.1.2 of RFC 9110</a>
     */
    String FROM = "From";

    /**
     * The HTTP {@code Host} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-7.2">Section 7.2 of RFC 9110</a>
     */
    String HOST = "Host";

    /**
     * The HTTP {@code If-Match} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-13.1.1">Section 13.1.1 of RFC 9110</a>
     */
    String IF_MATCH = "If-Match";

    /**
     * The HTTP {@code If-Modified-Since} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-13.1.3">Section 13.1.3 of RFC 9110</a>
     */
    String IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * The HTTP {@code If-None-Match} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-13.1.2">Section 13.1.2 of RFC 9110</a>
     */
    String IF_NONE_MATCH = "If-None-Match";

    /**
     * The HTTP {@code If-Range} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-13.1.5">Section 13.1.5 of RFC 9110</a>
     */
    String IF_RANGE = "If-Range";

    /**
     * The HTTP {@code If-Unmodified-Since} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-13.1.4">Section 13.1.4 of RFC 9110</a>
     */
    String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    /**
     * The HTTP {@code Last-Modified} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-8.8.2">Section 8.8.2 of RFC 9110</a>
     */
    String LAST_MODIFIED = "Last-Modified";

    /**
     * The HTTP {@code Link} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc8288">RFC 8288</a>
     */
    String LINK = "Link";

    /**
     * The HTTP {@code Location} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.2.2">Section 10.2.2 of RFC 9110</a>
     */
    String LOCATION = "Location";

    /**
     * The HTTP {@code Max-Forwards} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-7.6.2">Section 7.6.2 of RFC 9110</a>
     */
    String MAX_FORWARDS = "Max-Forwards";

    /**
     * The HTTP {@code Origin} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6454">RFC 6454</a>
     */
    String ORIGIN = "Origin";

    /**
     * The HTTP {@code Permissions-Policy} header field name.
     * @see <a href="https://www.w3.org/TR/permissions-policy-1/">Permissions Policy</a>
     */
    String PERMISSIONS_POLICY = "Permissions-Policy";

    /**
     * The HTTP {@code Pragma} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9111#section-5.4">Section 5.4 of RFC 9111</a>
     */
    String PRAGMA = "Pragma";

    /**
     * The HTTP {@code Proxy-Authenticate} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-11.6.3">Section 11.6.3 of RFC 9110</a>
     */
    String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /**
     * The HTTP {@code Proxy-Authorization} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-11.6.4">Section 11.6.4 of RFC 9110</a>
     */
    String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * The HTTP {@code Range} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-14.2">Section 14.2 of RFC 9110</a>
     */
    String RANGE = "Range";

    /**
     * The HTTP {@code Referer} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.1.3">Section 10.1.3 of RFC 9110</a>
     */
    String REFERER = "Referer";

    /**
     * The HTTP {@code Referrer-Policy} header field name.
     * @see <a href="https://www.w3.org/TR/referrer-policy/">Referrer Policy</a>
     */
    String REFERRER_POLICY = "Referrer-Policy";

    /**
     * The HTTP {@code Retry-After} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.2.3">Section 10.2.3 of RFC 9110</a>
     */
    String RETRY_AFTER = "Retry-After";

    /**
     * The HTTP {@code Sec-Fetch-Dest} header field name.
     * @see <a href="https://www.w3.org/TR/fetch-metadata/#sec-fetch-dest-header">Sec-Fetch-Dest</a>
     */
    String SEC_FETCH_DEST = "Sec-Fetch-Dest";

    /**
     * The HTTP {@code Sec-Fetch-Mode} header field name.
     * @see <a href="https://www.w3.org/TR/fetch-metadata/#sec-fetch-mode-header">Sec-Fetch-Mode</a>
     */
    String SEC_FETCH_MODE = "Sec-Fetch-Mode";

    /**
     * The HTTP {@code Sec-Fetch-Site} header field name.
     * @see <a href="https://www.w3.org/TR/fetch-metadata/#sec-fetch-site-header">Sec-Fetch-Site</a>
     */
    String SEC_FETCH_SITE = "Sec-Fetch-Site";

    /**
     * The HTTP {@code Sec-Fetch-User} header field name.
     * @see <a href="https://www.w3.org/TR/fetch-metadata/#sec-fetch-user-header">Sec-Fetch-User</a>
     */
    String SEC_FETCH_USER = "Sec-Fetch-User";

    /**
     * The HTTP {@code Sec-WebSocket-Accept} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-11.3.3">Section 11.3.3 of RFC 6455</a>
     */
    String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";

    /**
     * The HTTP {@code Sec-WebSocket-Extensions} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-11.3.2">Section 11.3.2 of RFC 6455</a>
     */
    String SEC_WEBSOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";

    /**
     * The HTTP {@code Sec-WebSocket-Key} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-11.3.1">Section 11.3.1 of RFC 6455</a>
     */
    String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";

    /**
     * The HTTP {@code Sec-WebSocket-Protocol} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-11.3.4">Section 11.3.4 of RFC 6455</a>
     */
    String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

    /**
     * The HTTP {@code Sec-WebSocket-Version} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-11.3.5">Section 11.3.5 of RFC 6455</a>
     */
    String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";

    /**
     * The HTTP {@code Server} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.2.4">Section 10.2.4 of RFC 9110</a>
     */
    String SERVER = "Server";

    /**
     * The HTTP {@code Set-Cookie} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6265#section-4.1">Section 4.1 of RFC 6265</a>
     */
    String SET_COOKIE = "Set-Cookie";

    /**
     * The HTTP {@code Set-Cookie2} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
     */
    String SET_COOKIE2 = "Set-Cookie2";

    /**
     * The HTTP {@code Strict-Transport-Security} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc6797">RFC 6797</a>
     */
    String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

    /**
     * The HTTP {@code TE} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.1.4">Section 10.1.4 of RFC 9110</a>
     */
    String TE = "TE";

    /**
     * The HTTP {@code Trailer} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-6.6.2">Section 6.6.2 of RFC 9110</a>
     */
    String TRAILER = "Trailer";

    /**
     * The HTTP {@code Transfer-Encoding} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9112#section-6.1">Section 6.1 of RFC 9112</a>
     */
    String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * The HTTP {@code Upgrade} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-7.8">Section 7.8 of RFC 9110</a>
     */
    String UPGRADE = "Upgrade";

    /**
     * The HTTP {@code User-Agent} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-10.1.5">Section 10.1.5 of RFC 9110</a>
     */
    String USER_AGENT = "User-Agent";

    /**
     * The HTTP {@code Vary} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-12.5.5">Section 12.5.5 of RFC 9110</a>
     */
    String VARY = "Vary";

    /**
     * The HTTP {@code Via} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-7.6.3">Section 7.6.3 of RFC 9110</a>
     */
    String VIA = "Via";

    /**
     * The HTTP {@code Warning} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.5">Section 5.5 of RFC 7234</a>
     */
    String WARNING = "Warning";

    /**
     * The HTTP {@code WWW-Authenticate} header field name.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9110#section-11.6.1">Section 11.6.1 of RFC 9110</a>
     */
    String WWW_AUTHENTICATE = "WWW-Authenticate";

    /**
     * The X-Authorization header is used to carry authorization information for HTTP requests.
     */
    String X_AUTHORIZATION = "X-Authorization";

    /**
     * The HTTP {@code X-Content-Type-Options} header field name.
     * @see <a href="https://fetch.spec.whatwg.org/#x-content-type-options-header">X-Content-Type-Options</a>
     */
    String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    /**
     * The HTTP {@code X-Frame-Options} header field name.
     * @see <a href="https://tools.ietf.org/html/rfc7034">RFC 7034</a>
     */
    String X_FRAME_OPTIONS = "X-Frame-Options";

    /**
     * Some HTTP proxies do not support arbitrary HTTP methods or
     * newer HTTP methods (such as PATCH).
     * In that case it’s possible to “proxy” HTTP methods through
     * another HTTP method in total violation of the protocol.
     * The way this works is by letting the client do an HTTP POST request and
     * set the X-HTTP-Method-Override header and set the value to
     * the intended HTTP method (such as PATCH).
     * <p>
     * Web infrastructure and solutions providers have proposed to use customized HTTP header fields:</p>
     * <pre>
     *   X-HTTP-Method-Override (Google/GData)
     *   X-HTTP-Method (Microsoft)
     *   X-METHOD-OVERRIDE (IBM)
     *   X-Method-Override (Aspectran)
     * </pre>
     */
    String X_METHOD_OVERRIDE = "X-Method-Override";

    /**
     * The X-Forwarded-For (XFF) request header is a de-facto standard header for identifying
     * the originating IP address of a client connecting to a web server through a proxy server.
     */
    String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * The X-Forwarded-Host (XFH) header is a de-facto standard header for identifying
     * the original host requested by the client in the Host HTTP request header.
     */
    String X_FORWARDED_HOST = "X-Forwarded-Host";

    /**
     * The X-Forwarded-Proto (XFP) header is a de-facto standard header for identifying
     * the protocol (HTTP or HTTPS) that a client used to connect to your proxy or load balancer.
     */
    String X_FORWARDED_PROTO = "X-Forwarded-Proto";

    /**
     * Contains the original path that the client requested.
     */
    String X_FORWARDED_PATH = "X-Forwarded-Path";

    /**
     * The X-Forwarded-Port header is a de-facto standard header for identifying
     * the port that a client used to connect to your proxy or load balancer.
     */
    String X_FORWARDED_PORT = "X-Forwarded-Port";

    /**
     * The X-Forwarded-Ssl header is used to determine if SSL was used when connecting to the proxy.
     */
    String X_FORWARDED_SSL = "X-Forwarded-Ssl";

    /**
     * The X-Real-IP header is a de-facto standard header for identifying the client IP address.
     */
    String X_REAL_IP = "X-Real-IP";

    /**
     * The X-Requested-With header is a de-facto standard header for identifying Ajax/XMLHttpRequest requests.
     */
    String X_REQUESTED_WITH = "X-Requested-With";

    /**
     * The HTTP {@code X-XSS-Protection} header field name.
     */
    String X_XSS_PROTECTION = "X-XSS-Protection";

}
