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
package com.aspectran.web.support.util;

import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for cookie generation, carrying cookie descriptor settings
 * as bean properties and being able to add and remove cookies to/from a
 * given response by directly manipulating the "Set-Cookie" header.
 */
public class CookieGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CookieGenerator.class);

    /**
     * Default path that cookies will be visible to: "/", i.e. the entire server.
     */
    public static final String DEFAULT_COOKIE_PATH = "/";

    @Nullable
    private String cookieName;

    @Nullable
    private String cookieDomain;

    private String cookiePath = DEFAULT_COOKIE_PATH;

    @Nullable
    private Integer cookieMaxAge;

    private boolean cookieSecure = false;

    private boolean cookieHttpOnly = false;

    @Nullable
    private String sameSite;

    public CookieGenerator() {
    }

    public CookieGenerator(@Nullable String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * Use the given name for cookies created by this generator.
     * @see jakarta.servlet.http.Cookie#getName()
     */
    public void setCookieName(@Nullable String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * Return the given name for cookies created by this generator.
     */
    @Nullable
    public String getCookieName() {
        return cookieName;
    }

    /**
     * Use the given domain for cookies created by this generator.
     * The cookie is only visible to servers in this domain.
     * @see jakarta.servlet.http.Cookie#setDomain
     */
    public void setCookieDomain(@Nullable String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    /**
     * Return the domain for cookies created by this generator, if any.
     */
    @Nullable
    public String getCookieDomain() {
        return cookieDomain;
    }

    /**
     * Use the given path for cookies created by this generator.
     * The cookie is only visible to URLs in this path and below.
     * @see jakarta.servlet.http.Cookie#setPath
     */
    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    /**
     * Return the path for cookies created by this generator.
     */
    public String getCookiePath() {
        return cookiePath;
    }

    /**
     * Use the given maximum age (in seconds) for cookies created by this generator.
     * Useful special value: -1 ... not persistent, deleted when client shuts down.
     * <p>Default is no specific maximum age at all, using the Servlet container's default.</p>
     * @see jakarta.servlet.http.Cookie#setMaxAge
     */
    public void setCookieMaxAge(@Nullable Integer cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    /**
     * Return the maximum age for cookies created by this generator.
     */
    @Nullable
    public Integer getCookieMaxAge() {
        return cookieMaxAge;
    }

    /**
     * Set whether the cookie should only be sent using a secure protocol,
     * such as HTTPS (SSL). This is an indication to the receiving browser,
     * not processed by the HTTP server itself.
     * <p>Default is "false".</p>
     * @see jakarta.servlet.http.Cookie#setSecure
     */
    public void setCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }

    /**
     * Return whether the cookie should only be sent using a secure protocol,
     * such as HTTPS (SSL).
     */
    public boolean isCookieSecure() {
        return cookieSecure;
    }

    /**
     * Set whether the cookie is supposed to be marked with the "HttpOnly" attribute.
     * <p>Default is "false".</p>
     * @see jakarta.servlet.http.Cookie#setHttpOnly
     */
    public void setCookieHttpOnly(boolean cookieHttpOnly) {
        this.cookieHttpOnly = cookieHttpOnly;
    }

    /**
     * Return whether the cookie is supposed to be marked with the "HttpOnly" attribute.
     */
    public boolean isCookieHttpOnly() {
        return cookieHttpOnly;
    }

    /**
     * Sets the `SameSite` attribute to prevent the browser from sending this cookie along with cross-site requests.
     * @param sameSite `Strict`, `Lax`, `None`
     */
    public void setSameSite(@Nullable String sameSite) {
        this.sameSite = sameSite;
    }

    /**
     * Return the `SameSite` attribute.
     */
    @Nullable
    public String getSameSite() {
        return sameSite;
    }

    /**
     * Add a cookie with the given value to the response,
     * using the cookie descriptor settings of this generator.
     * This method constructs and adds the "Set-Cookie" header directly to the response.
     * @param response the HTTP response to add the cookie to
     * @param cookieValue the value of the cookie to add
     * @see #setCookieName
     * @see #setCookieDomain
     * @see #setCookiePath
     * @see #setCookieMaxAge
     * @see #setSameSite
     */
    public void addCookie(HttpServletResponse response, String cookieValue) {
        Assert.notNull(response, "HttpServletResponse must not be null");
        Assert.state(StringUtils.hasText(getCookieName()), "Cookie name must not be null or empty");
        String header = buildCookieHeader(cookieValue, getCookieMaxAge());
        response.addHeader("Set-Cookie", header);
        if (logger.isTraceEnabled()) {
            logger.trace("Added cookie [{}={}]", getCookieName(), cookieValue);
        }
    }

    /**
     * Remove the cookie that this generator describes from the response.
     * Will generate a cookie with empty value and max age 0.
     * This method constructs and adds the "Set-Cookie" header directly to the response.
     * @param response the HTTP response to remove the cookie from
     * @see #setCookieName
     * @see #setCookieDomain
     * @see #setCookiePath
     * @see #setSameSite
     */
    public void removeCookie(HttpServletResponse response) {
        Assert.notNull(response, "HttpServletResponse must not be null");
        Assert.state(StringUtils.hasText(getCookieName()), "Cookie name must not be null or empty");
        String header = buildCookieHeader("", 0);
        response.addHeader("Set-Cookie", header);
        if (logger.isTraceEnabled()) {
            logger.trace("Removed cookie '{}'", getCookieName());
        }
    }

    /**
     * Builds the "Set-Cookie" header string based on the current cookie generator settings.
     * @param cookieValue the value of the cookie
     * @param maxAge the maximum age of the cookie in seconds
     * @return the formatted "Set-Cookie" header string
     */
    @NonNull
    private String buildCookieHeader(String cookieValue, @Nullable Integer maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(getCookieName()).append('=').append(cookieValue);
        if (getCookieDomain() != null) {
            sb.append("; Domain=").append(getCookieDomain());
        }
        if (StringUtils.hasText(getCookiePath())) {
            sb.append("; Path=").append(getCookiePath());
        }
        if (maxAge != null) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (isCookieSecure()) {
            sb.append("; Secure");
        }
        if (isCookieHttpOnly()) {
            sb.append("; HttpOnly");
        }
        if (getSameSite() != null) {
            sb.append("; SameSite=").append(getSameSite());
        }
        return sb.toString();
    }

}
