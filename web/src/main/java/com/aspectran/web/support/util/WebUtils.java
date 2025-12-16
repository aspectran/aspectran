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
import com.aspectran.web.activity.request.RequestHeaderParser;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.MediaType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Miscellaneous utility methods for web applications.
 * <p>Provides functionality for cookie handling, header parsing, path manipulation,
 * and building redirect URLs.</p>
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
     * Checks if any of the specified content types is acceptable according to
     * the {@code Accept} header sent by the client.
     * @param translet the current translet, which provides access to the request
     * @param contentTypes an array of media types to check
     * @return {@code true} if one of the specified media types is acceptable,
     *      {@code false} otherwise
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

    /**
     * Returns the path within the application for the given request URI.
     * <p>Detects and strips the context path from the beginning of the request URI.</p>
     * @param contextPath the context path of the application
     * @param requestUri the full request URI
     * @return the request URI relative to the context path
     */
    public static String getRelativePath(String contextPath, @NonNull String requestUri) {
        if (StringUtils.hasLength(contextPath)) {
            return requestUri.substring(contextPath.length());
        } else {
            return requestUri;
        }
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

    /**
     * Builds a {@link RedirectTarget} object from a {@link RedirectRule}.
     * <p>This method constructs the final redirect URL by combining the path from the rule,
     * prepending the reverse context path if necessary, and appending any parameters
     * defined in the rule. Parameters are evaluated and URL-encoded (using UTF-8
     * by default).</p>
     * @param redirectRule the rule that defines the redirect path and parameters
     * @param activity the current activity, used to evaluate parameters and get context
     * @return a {@link RedirectTarget} containing the final redirect URL
     */
    @NonNull
    public static RedirectTarget getRedirectTarget(RedirectRule redirectRule, Activity activity) {
        Assert.notNull(redirectRule, "redirectRule must not be null");
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
                            encoding = StandardCharsets.UTF_8.name();
                        }
                        stringValue = UriUtils.encodeQueryParam(stringValue, encoding);
                        sb.append(stringValue);
                    }
                }
            }
        }
        String requestName = (questionPos != -1 ? path.substring(0, questionPos) : path);
        return RedirectTarget.of(requestName, sb.toString());
    }

}
