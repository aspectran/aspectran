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

public interface HttpHeaders {

    /**
     * The HTTP {@code Accept} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.3.2">Section 5.3.2 of RFC 7231</a>
     */
    String ACCEPT = "Accept";

    /**
     * The HTTP {@code Accept-Charset} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3 of RFC 7231</a>
     */
    String ACCEPT_CHARSET = "Accept-Charset";

    /**
     * The HTTP {@code Accept-Encoding} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4 of RFC 7231</a>
     */
    String ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * The HTTP {@code Accept-Language} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5 of RFC 7231</a>
     */
    String ACCEPT_LANGUAGE = "Accept-Language";

    /**
     * The HTTP {@code Accept-Ranges} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7233#section-2.3">Section 5.3.5 of RFC 7233</a>
     */
    String ACCEPT_RANGES = "Accept-Ranges";

    /**
     * The CORS {@code Access-Control-Allow-Credentials} response header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    /**
     * The CORS {@code Access-Control-Allow-Headers} response header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    /**
     * The CORS {@code Access-Control-Allow-Methods} response header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    /**
     * The CORS {@code Access-Control-Allow-Origin} response header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    /**
     * The CORS {@code Access-Control-Expose-Headers} response header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    /**
     * The CORS {@code Access-Control-Max-Age} response header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    /**
     * The CORS {@code Access-Control-Request-Headers} request header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    /**
     * The CORS {@code Access-Control-Request-Method} request header field name.
     * @see <a href="http://www.w3.org/TR/cors/">CORS W3C recommandation</a>
     */
    String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

    /**
     * The HTTP {@code Age} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7234#section-5.1">Section 5.1 of RFC 7234</a>
     */
    String AGE = "Age";

    /**
     * The HTTP {@code Allow} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-7.4.1">Section 7.4.1 of RFC 7231</a>
     */
    String ALLOW = "Allow";

    /**
     * The HTTP {@code Authorization} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7235#section-4.2">Section 4.2 of RFC 7235</a>
     */
    String AUTHORIZATION = "Authorization";

    /**
     * The HTTP {@code Cache-Control} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7234#section-5.2">Section 5.2 of RFC 7234</a>
     */
    String CACHE_CONTROL = "Cache-Control";

    /**
     * The HTTP {@code Connection} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-6.1">Section 6.1 of RFC 7230</a>
     */
    String CONNECTION = "Connection";

    /**
     * The HTTP {@code Content-Encoding} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-3.1.2.2">Section 3.1.2.2 of RFC 7231</a>
     */
    String CONTENT_ENCODING = "Content-Encoding";

    /**
     * The HTTP {@code Content-Disposition} header field name
     * @see <a href="http://tools.ietf.org/html/rfc6266">RFC 6266</a>
     */
    String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * The HTTP {@code Content-Language} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2 of RFC 7231</a>
     */
    String CONTENT_LANGUAGE = "Content-Language";

    /**
     * The HTTP {@code Content-Length} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.3.2">Section 3.3.2 of RFC 7230</a>
     */
    String CONTENT_LENGTH = "Content-Length";

    /**
     * The HTTP {@code Content-Location} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-3.1.4.2">Section 3.1.4.2 of RFC 7231</a>
     */
    String CONTENT_LOCATION = "Content-Location";

    /**
     * The HTTP {@code Content-Range} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7233#section-4.2">Section 4.2 of RFC 7233</a>
     */
    String CONTENT_RANGE = "Content-Range";

    /**
     * The HTTP {@code Content-Type} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5 of RFC 7231</a>
     */
    String CONTENT_TYPE = "Content-Type";

    /**
     * The HTTP {@code Cookie} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc2109#section-4.3.4">Section 4.3.4 of RFC 2109</a>
     */
    String COOKIE = "Cookie";

    /**
     * The HTTP {@code Date} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-7.1.1.2">Section 7.1.1.2 of RFC 7231</a>
     */
    String DATE = "Date";

    /**
     * The HTTP {@code ETag} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
     */
    String ETAG = "ETag";

    /**
     * The HTTP {@code Expect} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.1.1">Section 5.1.1 of RFC 7231</a>
     */
    String EXPECT = "Expect";

    /**
     * The HTTP {@code Expires} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7234#section-5.3">Section 5.3 of RFC 7234</a>
     */
    String EXPIRES = "Expires";

    /**
     * The HTTP {@code From} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.5.1">Section 5.5.1 of RFC 7231</a>
     */
    String FROM = "From";

    /**
     * The HTTP {@code Host} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-5.4">Section 5.4 of RFC 7230</a>
     */
    String HOST = "Host";

    /**
     * The HTTP {@code If-Match} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7232#section-3.1">Section 3.1 of RFC 7232</a>
     */
    String IF_MATCH = "If-Match";

    /**
     * The HTTP {@code If-Modified-Since} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7232#section-3.3">Section 3.3 of RFC 7232</a>
     */
    String IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * The HTTP {@code If-None-Match} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7232#section-3.2">Section 3.2 of RFC 7232</a>
     */
    String IF_NONE_MATCH = "If-None-Match";

    /**
     * The HTTP {@code If-Range} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7233#section-3.2">Section 3.2 of RFC 7233</a>
     */
    String IF_RANGE = "If-Range";

    /**
     * The HTTP {@code If-Unmodified-Since} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7232#section-3.4">Section 3.4 of RFC 7232</a>
     */
    String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    /**
     * The HTTP {@code Last-Modified} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7232#section-2.2">Section 2.2 of RFC 7232</a>
     */
    String LAST_MODIFIED = "Last-Modified";

    /**
     * The HTTP {@code Link} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc5988">RFC 5988</a>
     */
    String LINK = "Link";

    /**
     * The HTTP {@code Location} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-7.1.2">Section 7.1.2 of RFC 7231</a>
     */
    String LOCATION = "Location";

    /**
     * The HTTP {@code Max-Forwards} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.1.2">Section 5.1.2 of RFC 7231</a>
     */
    String MAX_FORWARDS = "Max-Forwards";

    /**
     * The HTTP {@code Origin} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc6454">RFC 6454</a>
     */
    String ORIGIN = "Origin";

    /**
     * The HTTP {@code Pragma} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7234#section-5.4">Section 5.4 of RFC 7234</a>
     */
    String PRAGMA = "Pragma";

    /**
     * The HTTP {@code Proxy-Authenticate} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7235#section-4.3">Section 4.3 of RFC 7235</a>
     */
    String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /**
     * The HTTP {@code Proxy-Authorization} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7235#section-4.4">Section 4.4 of RFC 7235</a>
     */
    String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * The HTTP {@code Range} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7233#section-3.1">Section 3.1 of RFC 7233</a>
     */
    String RANGE = "Range";

    /**
     * The HTTP {@code Referer} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.5.2">Section 5.5.2 of RFC 7231</a>
     */
    String REFERER = "Referer";

    /**
     * The HTTP {@code Retry-After} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-7.1.3">Section 7.1.3 of RFC 7231</a>
     */
    String RETRY_AFTER = "Retry-After";

    /**
     * The HTTP {@code Server} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-7.4.2">Section 7.4.2 of RFC 7231</a>
     */
    String SERVER = "Server";

    /**
     * The HTTP {@code Set-Cookie} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc2109#section-4.2.2">Section 4.2.2 of RFC 2109</a>
     */
    String SET_COOKIE = "Set-Cookie";

    /**
     * The HTTP {@code Set-Cookie2} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc2965">RFC 2965</a>
     */
    String SET_COOKIE2 = "Set-Cookie2";

    /**
     * The HTTP {@code TE} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-4.3">Section 4.3 of RFC 7230</a>
     */
    String TE = "TE";

    /**
     * The HTTP {@code Trailer} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-4.4">Section 4.4 of RFC 7230</a>
     */
    String TRAILER = "Trailer";

    /**
     * The HTTP {@code Transfer-Encoding} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-3.3.1">Section 3.3.1 of RFC 7230</a>
     */
    String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * The HTTP {@code Upgrade} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-6.7">Section 6.7 of RFC 7230</a>
     */
    String UPGRADE = "Upgrade";

    /**
     * The HTTP {@code User-Agent} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-5.5.3">Section 5.5.3 of RFC 7231</a>
     */
    String USER_AGENT = "User-Agent";

    /**
     * The HTTP {@code Vary} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4 of RFC 7231</a>
     */
    String VARY = "Vary";

    /**
     * The HTTP {@code Via} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7230#section-5.7.1">Section 5.7.1 of RFC 7230</a>
     */
    String VIA = "Via";

    /**
     * The HTTP {@code Warning} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7234#section-5.5">Section 5.5 of RFC 7234</a>
     */
    String WARNING = "Warning";

    /**
     * The HTTP {@code WWW-Authenticate} header field name.
     * @see <a href="http://tools.ietf.org/html/rfc7235#section-4.1">Section 4.1 of RFC 7235</a>
     */
    String WWW_AUTHENTICATE = "WWW-Authenticate";

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

}
