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
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.support.util.UriUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Miscellaneous utility methods for web applications.
 * <p>Provides functionality for cookie handling, header parsing, path manipulation,
 * and building redirect URLs.</p>
 */
public class ServletWebUtils {

    /**
     * This class cannot be instantiated.
     */
    private ServletWebUtils() {
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
     * Parses the query string from the given {@link HttpServletRequest} into a {@link MultiValueMap}.
     * <p>The character encoding specified on the request is used to decode the query parameters;
     * if no character encoding is specified, UTF-8 is used as default.</p>
     * @param request the current servlet request
     * @return a {@link MultiValueMap} containing the parsed query parameters
     */
    @NonNull
    public static MultiValueMap<String, String> parseQueryParams(@NonNull HttpServletRequest request) {
        Assert.notNull(request, "request must not be null");
        String queryString = request.getQueryString();
        if (!StringUtils.hasText(queryString)) {
            return UriUtils.parseQueryParams(null);
        }
        Charset charset = determineEncoding(request);
        return UriUtils.parseQueryParams(queryString, charset);
    }

    /**
     * Parses the query string from the given {@link Translet} into a {@link MultiValueMap}.
     * @param translet the current translet
     * @return a {@link MultiValueMap} containing the parsed query parameters
     */
    @NonNull
    public static MultiValueMap<String, String> parseQueryParams(@NonNull Translet translet) {
        Assert.notNull(translet, "translet must not be null");
        HttpServletRequest request = translet.getRequestAdaptee();
        return parseQueryParams(request);
    }

    /**
     * Parses the query string from the given {@link HttpServletRequest} into a parameter map
     * compatible with {@link HttpServletRequest#getParameterMap()}.
     * <p>The character encoding specified on the request is used to decode the query parameters;
     * if no character encoding is specified, UTF-8 is used as default.</p>
     * @param request the current servlet request
     * @return a map containing query parameter names as keys and parameter value arrays as values
     */
    @NonNull
    public static Map<String, String[]> parseQueryParameters(@NonNull HttpServletRequest request) {
        MultiValueMap<String, String> queryParams = parseQueryParams(request);
        if (queryParams.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String[]> parameterMap = new LinkedHashMap<>(queryParams.size());
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            parameterMap.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }
        return parameterMap;
    }

    /**
     * Parses the query string from the given {@link Translet} into a parameter map
     * compatible with {@link HttpServletRequest#getParameterMap()}.
     * @param translet the current translet
     * @return a map containing query parameter names as keys and parameter value arrays as values
     */
    @NonNull
    public static Map<String, String[]> parseQueryParameters(@NonNull Translet translet) {
        Assert.notNull(translet, "translet must not be null");
        HttpServletRequest request = translet.getRequestAdaptee();
        return parseQueryParameters(request);
    }

    @NonNull
    private static Charset determineEncoding(@NonNull HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding != null) {
            try {
                return Charset.forName(encoding);
            } catch (Exception ignored) {
            }
        }
        return StandardCharsets.UTF_8;
    }

}
