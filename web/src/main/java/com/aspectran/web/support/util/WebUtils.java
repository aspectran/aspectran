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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.response.RedirectTarget;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.asel.item.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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

    private static final char QUESTION_CHAR = '?';

    private static final char AMPERSAND_CHAR = '&';

    private static final char EQUAL_CHAR = '=';

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
        HttpServletRequest request = translet.getRequestAdaptee();
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
    public static String getReverseContextPath(@NonNull HttpServletRequest request) {
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
            return null;
        }
    }

    @Nullable
    public static String getReverseContextPath(@NonNull HttpServletRequest request, String defaultContextPath) {
        String reverseContextPath = getReverseContextPath(request);
        if (reverseContextPath != null) {
            return reverseContextPath;
        } else {
            return defaultContextPath;
        }
    }

    @NonNull
    public static RedirectTarget getRedirectTarget(RedirectRule redirectRule, Activity activity) throws IOException {
        if (redirectRule == null) {
            throw new IllegalArgumentException("redirectRule must not be null");
        }
        String path = redirectRule.getPath(activity);
        int questionPos = -1;
        StringBuilder sb = new StringBuilder(256);
        if (path != null) {
            if (path.startsWith("/")) {
                String contextPath = activity.getReverseContextPath();
                if (StringUtils.hasLength(contextPath)) {
                    sb.append(contextPath);
                }
            }
            sb.append(path);
            questionPos = path.indexOf(QUESTION_CHAR);
        }
        ItemRuleMap parameterItemRuleMap = redirectRule.getParameterItemRuleMap();
        if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
            ItemEvaluator evaluator = activity.getItemEvaluator();
            Map<String, Object> valueMap = evaluator.evaluate(parameterItemRuleMap);
            if (valueMap != null && !valueMap.isEmpty()) {
                if (questionPos == -1) {
                    sb.append(QUESTION_CHAR);
                }
                String name = null;
                Object value;
                for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                    if (name != null) {
                        sb.append(AMPERSAND_CHAR);
                    }
                    name = entry.getKey();
                    value = entry.getValue();
                    String stringValue = (value != null ? value.toString() : null);
                    if (redirectRule.isExcludeEmptyParameters() &&
                        stringValue != null && !stringValue.isEmpty()) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else if (redirectRule.isExcludeNullParameters() && stringValue != null) {
                        sb.append(name).append(EQUAL_CHAR);
                    } else {
                        sb.append(name).append(EQUAL_CHAR);
                    }
                    if (stringValue != null) {
                        String encoding = redirectRule.getEncoding();
                        if (encoding == null) {
                            encoding = StandardCharsets.ISO_8859_1.name();
                        }
                        stringValue = URLEncoder.encode(stringValue, encoding);
                        sb.append(stringValue);
                    }
                }
            }
        }
        String requestName = (questionPos != -1 ? path.substring(0, questionPos) : path);
        return RedirectTarget.of(requestName, sb.toString());
    }

}
