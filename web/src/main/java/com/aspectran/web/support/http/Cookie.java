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

import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents an HTTP Cookie, independent of any servlet container.
 * <p>This class encapsulates cookie attributes as specified in RFC 6265,
 * providing a unified cookie abstraction across different web environments
 * such as Servlet and Netty.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class Cookie implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;

    private String value;

    private String domain;

    private String path;

    private Integer maxAge;

    private boolean secure;

    private boolean httpOnly;

    private String sameSite;

    /**
     * Constructs a cookie with a specified name and an empty value.
     * @param name the name of the cookie
     */
    public Cookie(@NonNull String name) {
        this(name, null);
    }

    /**
     * Constructs a cookie with a specified name and value.
     * @param name the name of the cookie
     * @param value the value of the cookie
     */
    public Cookie(@NonNull String name, @Nullable String value) {
        Assert.notNull(name, "Cookie name must not be null");
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name of the cookie.
     * @return the cookie name
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the cookie.
     * @return the cookie value
     */
    @Nullable
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the cookie.
     * @param value the new value
     */
    public void setValue(@Nullable String value) {
        this.value = value;
    }

    /**
     * Returns the domain name set for this cookie.
     * @return the domain name
     */
    @Nullable
    public String getDomain() {
        return domain;
    }

    /**
     * Specifies the domain within which this cookie should be presented.
     * @param domain the domain name
     */
    public void setDomain(@Nullable String domain) {
        this.domain = domain;
    }

    /**
     * Returns the path on the server to which the browser returns this cookie.
     * @return the path
     */
    @Nullable
    public String getPath() {
        return path;
    }

    /**
     * Specifies a path for the cookie to which the client should return the cookie.
     * @param path the path
     */
    public void setPath(@Nullable String path) {
        this.path = path;
    }

    /**
     * Returns the maximum age of the cookie, specified in seconds.
     * @return an integer specifying the maximum age of the cookie in seconds
     */
    @Nullable
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the maximum age of the cookie in seconds.
     * @param maxAge an integer specifying the maximum age in seconds
     */
    public void setMaxAge(@Nullable Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Indicates whether the cookie should only be sent using a secure protocol,
     * such as HTTPS.
     * @return {@code true} if the cookie can be sent over other protocols; {@code false} otherwise
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Indicates whether the cookie should only be sent using a secure protocol,
     * such as HTTPS.
     * @param secure if {@code true}, the cookie can only be sent over a secure protocol
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Checks whether this Cookie has been marked as <i>HttpOnly</i>.
     * @return {@code true} if this Cookie has been marked as <i>HttpOnly</i>, {@code false} otherwise
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * Marks or unmarks this Cookie as <i>HttpOnly</i>.
     * @param httpOnly {@code true} if this Cookie is to be marked as <i>HttpOnly</i>, {@code false} otherwise
     */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * Returns the value of the {@code SameSite} attribute for this cookie.
     * @return the {@code SameSite} attribute value
     */
    @Nullable
    public String getSameSite() {
        return sameSite;
    }

    /**
     * Sets the value of the {@code SameSite} attribute for this cookie.
     * @param sameSite the {@code SameSite} attribute value (e.g. "Strict", "Lax", "None")
     */
    public void setSameSite(@Nullable String sameSite) {
        this.sameSite = sameSite;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Cookie otherCookie)) {
            return false;
        }
        return name.equals(otherCookie.name) &&
                ObjectUtils.nullSafeEquals(path, otherCookie.path) &&
                ObjectUtils.nullSafeEquals(domain, otherCookie.domain);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + ObjectUtils.nullSafeHashCode(path);
        result = 31 * result + ObjectUtils.nullSafeHashCode(domain);
        return result;
    }

    @Override
    public String toString() {
        return name + "=" + (value != null ? value : "");
    }

}
