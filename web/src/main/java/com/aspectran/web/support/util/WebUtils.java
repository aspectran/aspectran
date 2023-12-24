/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>This class is a clone of org.springframework.web.util.WebUtils</p>
 *
 * Miscellaneous utilities for web applications.
 * Used by various framework classes.
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

}
