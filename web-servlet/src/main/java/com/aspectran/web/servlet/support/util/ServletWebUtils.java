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
package com.aspectran.web.servlet.support.util;

import com.aspectran.core.activity.Translet;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Miscellaneous utility methods for web applications.
 * <p>Provides functionality for cookie handling, header parsing, path manipulation,
 * and building redirect URLs.</p>
 */
public class ServletWebUtils {

    /**
     * Standard Servlet 2.3+ spec request attribute for error page exception.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.</p>
     */
    public static final String ERROR_EXCEPTION_ATTRIBUTE = "jakarta.servlet.error.exception";

    /**
     * This class cannot be instantiated.
     */
    private ServletWebUtils() {
    }

    /**
     * Extracts the remote client IP address from the servlet request.
     * Checks the {@code X-Forwarded-For} header first for proxies/load balancers,
     * falling back to the remote address of the underlying servlet request.
     * @param request the current servlet request
     * @return the remote IP address
     */
    public static String getRemoteAddr(@NonNull HttpServletRequest request) {
        String remoteAddr = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
        if (StringUtils.hasLength(remoteAddr)) {
            if (remoteAddr.contains(",")) {
                remoteAddr = StringUtils.tokenize(remoteAddr, ",", true)[0];
            }
        } else {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }

    /**
     * Extracts the remote client IP address from the translet.
     * Checks the {@code X-Forwarded-For} header first for proxies/load balancers,
     * falling back to the remote address of the underlying servlet request.
     * @param translet the current translet
     * @return the remote IP address
     */
    public static String getRemoteAddr(@NonNull Translet translet) {
        Assert.notNull(translet, "Translet must not be null");
        return getRemoteAddr((HttpServletRequest)translet.getRequestAdaptee());
    }

    /**
     * Retrieve the first cookie with the given name. Note that multiple
     * cookies can have the same name but different paths or domains.
     * @param request current servlet request
     * @param cookieName cookie name
     * @return the first cookie with the given name, or {@code null} if none is found
     */
    @Nullable
    public static Cookie getCookie(HttpServletRequest request, String cookieName) {
        Assert.notNull(request, "Request must not be null");
        Assert.hasLength(cookieName, "Cookie name must not be null or empty");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * Retrieve the first cookie with the given name. Note that multiple
     * cookies can have the same name but different paths or domains.
     * @param translet current translet
     * @param cookieName cookie name
     * @return the first cookie with the given name, or {@code null} if none is found
     */
    @Nullable
    public static Cookie getCookie(Translet translet, String cookieName) {
        Assert.notNull(translet, "Translet must not be null");
        Assert.hasLength(cookieName, "Cookie name must not be null or empty");
        HttpServletRequest request = translet.getRequestAdaptee();
        return getCookie(request, cookieName);
    }

    /**
     * Determines the context path to be used for reverse-proxy scenarios.
     * <p>This method inspects the {@code X-Forwarded-Path} header. If the header
     * is present, it is returned (with any trailing slash removed). This is useful
     * when an application is running behind a reverse proxy that alters the context path.
     * @param request the current servlet request
     * @return the reverse context path from the header, or {@code null} if the header is not found
     * @see HttpHeaders#X_FORWARDED_PATH
     */
    @Nullable
    public static String getReverseContextPath(@NonNull HttpServletRequest request) {
        String forwardedPath = request.getHeader(HttpHeaders.X_FORWARDED_PATH);
        if (forwardedPath != null) {
            if (forwardedPath.equals("/")) {
                return StringUtils.EMPTY;
            } else if (forwardedPath.endsWith("/")) {
                return forwardedPath.substring(0, forwardedPath.length() - 1);
            } else {
                return forwardedPath;
            }
        } else {
            return null;
        }
    }

    /**
     * Determines the context path to be used for reverse-proxy scenarios,
     * falling back to a default context path.
     * @param request the current servlet request
     * @param defaultContextPath the default context path to return if the
     *      {@code X-Forwarded-Path} header is not present
     * @return the reverse context path from the header, or the default context path
     * @see #getReverseContextPath(HttpServletRequest)
     */
    @Nullable
    public static String getReverseContextPath(@NonNull HttpServletRequest request, String defaultContextPath) {
        String reverseContextPath = getReverseContextPath(request);
        if (reverseContextPath != null) {
            return reverseContextPath;
        } else {
            return defaultContextPath;
        }
    }

}
