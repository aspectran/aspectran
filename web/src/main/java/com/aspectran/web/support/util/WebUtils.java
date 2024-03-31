/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.web.activity.request.RequestHeaderParser;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.MediaType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Miscellaneous utilities for web applications.
 */
public abstract class WebUtils {

    /**
     * Standard Servlet 2.3+ spec request attribute for error page exception.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.</p>
     */
    public static final String ERROR_EXCEPTION_ATTRIBUTE = "jakarta.servlet.error.exception";

    /**
     * Retrieve the first cookie with the given name. Note that multiple
     * cookies can have the same name but different paths or domains.
     * @param request current servlet request
     * @param name cookie name
     * @return the first cookie with the given name, or {@code null} if none is found
     */
    @Nullable
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Assert.notNull(request, "Request must not be null");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
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
     * @param name cookie name
     * @return the first cookie with the given name, or {@code null} if none is found
     */
    @Nullable
    public static Cookie getCookie(Translet translet, String name) {
        Assert.notNull(translet, "Translet must not be null");
        HttpServletRequest request = translet.getRequestAdapter().getAdaptee();
        return getCookie(request, name);
    }

    /**
     * Returns whether the specified content types are present in the Accept header declared by user agents.
     * @param translet current translet
     * @param contentTypes content types to look for in the Accept header
     * @return true if present in the Accept header, false otherwise
     */
    public static boolean isAcceptContentTypes(Translet translet, MediaType... contentTypes) {
        Assert.notNull(translet, "Translet must not be null");
        Assert.notNull(contentTypes, "contentTypes must not be null");
        try {
            List<MediaType> acceptContentTypes = RequestHeaderParser.resolveAcceptContentTypes(translet.getRequestAdapter());
            for (MediaType mediaType : contentTypes) {
                if (mediaType.isPresentIn(acceptContentTypes)) {
                    return true;
                }
            }
        } catch (HttpMediaTypeNotAcceptableException e) {
            // ignore
        }
        return false;
    }

    public static String getRelativePath(String contextPath, @NonNull String requestUri) {
        if (StringUtils.hasLength(contextPath)) {
            return requestUri.substring(contextPath.length());
        } else {
            return requestUri;
        }

    }

    @Nullable
    public static String getReverseContextPath(@NonNull HttpServletRequest request, String defaultContextPath) {
        String forwardedPath = request.getHeader(HttpHeaders.X_FORWARDED_PATH);
        if (forwardedPath != null) {
            if (forwardedPath.equals(ActivityContext.NAME_SEPARATOR)) {
                return StringUtils.EMPTY;
            } else if (forwardedPath.endsWith(ActivityContext.NAME_SEPARATOR)) {
                return forwardedPath.substring(0, forwardedPath.length() - 1);
            } else {
                return forwardedPath;
            }
        } else {
            return defaultContextPath;
        }
    }

}
