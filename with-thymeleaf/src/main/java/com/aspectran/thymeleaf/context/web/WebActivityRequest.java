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
package com.aspectran.thymeleaf.context.web;

import com.aspectran.thymeleaf.context.common.AbstractActivityRequest;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.support.http.Cookie;
import com.aspectran.web.support.util.WebUtils;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Thymeleaf {@link org.thymeleaf.web.IWebRequest} implementation for Aspectran's
 * web environment.
 *
 * <p>This class extends {@link AbstractActivityRequest} and wraps a {@link WebRequestAdapter}.</p>
 *
 * <p>Created: 2024-11-27</p>
 */
public class WebActivityRequest extends AbstractActivityRequest {

    private final WebRequestAdapter webRequestAdapter;

    private final String contextPath;

    /**
     * Instantiates a new WebActivityRequest.
     * @param requestAdapter the web request adapter
     */
    public WebActivityRequest(@NonNull WebRequestAdapter requestAdapter) {
        super(requestAdapter);
        this.webRequestAdapter = requestAdapter;
        this.contextPath = WebUtils.getReverseContextPath(requestAdapter, requestAdapter.getContextPath());
    }

    @Override
    public String getMethod() {
        return webRequestAdapter.getRequestMethod().name();
    }

    @Override
    public String getScheme() {
        return webRequestAdapter.getScheme();
    }

    @Override
    public String getServerName() {
        return webRequestAdapter.getServerName();
    }

    @Override
    public Integer getServerPort() {
        return webRequestAdapter.getServerPort();
    }

    @Override
    public String getApplicationPath() {
        // This protects against a redirection behaviour in Jetty
        if (contextPath != null && contextPath.length() == 1 && contextPath.charAt(0) == '/') {
            return StringUtils.EMPTY;
        } else {
            return (contextPath != null ? contextPath : StringUtils.EMPTY);
        }
    }

    @Override
    public String getPathWithinApplication() {
        String requestURI = webRequestAdapter.getRequestURI();
        if (requestURI == null) {
            return null;
        }
        String applicationPath = getApplicationPath();
        if (StringUtils.isEmpty(applicationPath)) {
            return requestURI;
        }
        return requestURI.substring(applicationPath.length());
    }

    @Override
    public String getQueryString() {
        return webRequestAdapter.getQueryString();
    }

    @Override
    public boolean containsCookie(String name) {
        Assert.notNull(name, "name cannot be null");
        Cookie[] cookies = WebUtils.getCookies(webRequestAdapter);
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getCookieCount() {
        Cookie[] cookies = WebUtils.getCookies(webRequestAdapter);
        return (cookies == null ? 0 : cookies.length);
    }

    @Override
    public Set<String> getAllCookieNames() {
        Cookie[] cookies = WebUtils.getCookies(webRequestAdapter);
        if (cookies == null) {
            return Collections.emptySet();
        }
        Set<String> cookieNames = new LinkedHashSet<>(3);
        for (Cookie cookie : cookies) {
            cookieNames.add(cookie.getName());
        }
        return Collections.unmodifiableSet(cookieNames);
    }

    @Override
    public Map<String, String[]> getCookieMap() {
        Cookie[] cookies = WebUtils.getCookies(webRequestAdapter);
        if (cookies == null) {
            return Collections.emptyMap();
        }
        Map<String, String[]> cookieMap = new LinkedHashMap<>(3);
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName();
            String cookieValue = cookie.getValue();
            if (cookieMap.containsKey(cookieName)) {
                String[] currentCookieValues = cookieMap.get(cookieName);
                String[] newCookieValues = Arrays.copyOf(currentCookieValues, currentCookieValues.length + 1);
                newCookieValues[currentCookieValues.length] = cookieValue;
                cookieMap.put(cookieName, newCookieValues);
            } else {
                cookieMap.put(cookieName, new String[] {cookieValue});
            }
        }
        return Collections.unmodifiableMap(cookieMap);
    }

    @Override
    public String[] getCookieValues(String name) {
        Assert.notNull(name, "Name cannot be null");
        Cookie[] cookies = WebUtils.getCookies(webRequestAdapter);
        if (cookies == null) {
            return null;
        }
        String[] cookieValues = null;
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName();
            if (name.equals(cookieName)) {
                String cookieValue = cookie.getValue();
                if (cookieValues != null) {
                    String[] newCookieValues = Arrays.copyOf(cookieValues, cookieValues.length + 1);
                    newCookieValues[cookieValues.length] = cookieValue;
                    cookieValues = newCookieValues;
                } else {
                    cookieValues = new String[] {cookieValue};
                }
            }
        }
        return cookieValues;
    }

}
