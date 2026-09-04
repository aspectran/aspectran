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
package com.aspectran.netty.server.session;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Configuration options for HTTP session cookies in a Netty environment.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettySessionConfig {

    public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";

    private String cookieName = DEFAULT_SESSION_COOKIE_NAME;

    private String cookiePath = "/";

    private String cookieDomain;

    private int maxAge = -1;

    private boolean secure;

    private boolean httpOnly = true;

    private CookieHeaderNames.SameSite sameSite = CookieHeaderNames.SameSite.Lax;

    /**
     * Returns the name of the session cookie.
     * @return the cookie name
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * Sets the name of the session cookie.
     * @param cookieName the cookie name, or {@code null} to use the default {@value #DEFAULT_SESSION_COOKIE_NAME}
     */
    public void setCookieName(String cookieName) {
        this.cookieName = (cookieName != null ? cookieName : DEFAULT_SESSION_COOKIE_NAME);
    }

    /**
     * Returns the path attribute of the session cookie.
     * @return the cookie path
     */
    public String getCookiePath() {
        return cookiePath;
    }

    /**
     * Sets the path attribute of the session cookie.
     * @param cookiePath the cookie path
     */
    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    /**
     * Returns the domain attribute of the session cookie.
     * @return the cookie domain, or {@code null} if not specified
     */
    public String getCookieDomain() {
        return cookieDomain;
    }

    /**
     * Sets the domain attribute of the session cookie.
     * @param cookieDomain the cookie domain
     */
    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    /**
     * Returns the maximum age in seconds for the session cookie.
     * @return the maximum age in seconds, or -1 if the cookie expires when the browser closes
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the maximum age in seconds for the session cookie.
     * @param maxAge the maximum age in seconds (-1 for a session-scoped cookie)
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Returns whether the session cookie should only be transmitted over secure HTTPS connections.
     * @return {@code true} if secure; {@code false} otherwise
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets whether the session cookie should only be transmitted over secure HTTPS connections.
     * @param secure {@code true} to require HTTPS; {@code false} otherwise
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Returns whether the session cookie is marked as HttpOnly to prevent client-side script access.
     * @return {@code true} if HttpOnly; {@code false} otherwise
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * Sets whether the session cookie is marked as HttpOnly.
     * @param httpOnly {@code true} to mark as HttpOnly; {@code false} otherwise
     */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * Returns the SameSite attribute value configured for the session cookie.
     * @return the SameSite attribute, or {@code null} if not set
     */
    public CookieHeaderNames.SameSite getSameSite() {
        return sameSite;
    }

    /**
     * Sets the SameSite attribute value for the session cookie.
     * @param sameSite the SameSite policy (e.g., "Strict", "Lax", "None"), or {@code null} to disable
     */
    public void setSameSite(String sameSite) {
        if (sameSite != null) {
            try {
                this.sameSite = CookieHeaderNames.SameSite.valueOf(sameSite);
            } catch (IllegalArgumentException e) {
                this.sameSite = CookieHeaderNames.SameSite.Lax;
            }
        } else {
            this.sameSite = null;
        }
    }

    /**
     * Extracts the session ID from the incoming HTTP request cookie header.
     * @param request the HTTP request
     * @return the extracted session ID, or null if not found
     */
    public String findSessionId(@NonNull FullHttpRequest request) {
        String cookieHeader = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals(cookieName)) {
                    return cookie.value();
                }
            }
        }
        return null;
    }

    /**
     * Encodes a Set-Cookie header string for the given session ID.
     * @param sessionId the session ID
     * @return the encoded Set-Cookie header value
     */
    public String encodeCookie(String sessionId) {
        DefaultCookie cookie = new DefaultCookie(cookieName, sessionId);
        if (cookiePath != null) {
            cookie.setPath(cookiePath);
        }
        if (cookieDomain != null) {
            cookie.setDomain(cookieDomain);
        }
        if (maxAge >= 0) {
            cookie.setMaxAge(maxAge);
        } else {
            cookie.setMaxAge(Cookie.UNDEFINED_MAX_AGE);
        }
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        if (sameSite != null) {
            cookie.setSameSite(sameSite);
        }
        return ServerCookieEncoder.STRICT.encode(cookie);
    }

    /**
     * Encodes an expired Set-Cookie header string to invalidate the cookie on the client.
     * @return the encoded Set-Cookie header value
     */
    public String encodeExpiredCookie() {
        DefaultCookie cookie = new DefaultCookie(cookieName, "");
        if (cookiePath != null) {
            cookie.setPath(cookiePath);
        }
        if (cookieDomain != null) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setMaxAge(0);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        return ServerCookieEncoder.STRICT.encode(cookie);
    }

}
