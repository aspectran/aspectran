/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.thymeleaf.web.IWebRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebActivityRequest implements IWebRequest {

    private final RequestAdapter requestAdapter;

    WebActivityRequest(@NonNull RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    @Override
    public String getMethod() {
        return getHttpServletRequest().getMethod();
    }

    @Override
    public String getScheme() {
        return getHttpServletRequest().getScheme();
    }

    @Override
    public String getServerName() {
        return getHttpServletRequest().getServerName();
    }

    @Override
    public Integer getServerPort() {
        return getHttpServletRequest().getServerPort();
    }

    @Override
    public String getApplicationPath() {
        String contextPath = getHttpServletRequest().getContextPath();
        // This protects against a redirection behaviour in Jetty
        if (contextPath != null && contextPath.length() == 1 && contextPath.charAt(0) == '/') {
            contextPath = "";
        }
        return contextPath;
    }

    @Override
    public String getPathWithinApplication() {
        String requestURI = getHttpServletRequest().getRequestURI();
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
        return getHttpServletRequest().getQueryString();
    }

    @Override
    public boolean containsHeader(String name) {
        return (getHttpServletRequest().getHeader(name) != null);
    }

    @Override
    public int getHeaderCount() {
        return requestAdapter.getHeaderMap().size();
    }

    @Override
    public Set<String> getAllHeaderNames() {
        return requestAdapter.getHeaderNames();
    }

    @Override
    public Map<String, String[]> getHeaderMap() {
        Map<String, String[]> headerMap = new LinkedHashMap<>(10);
        requestAdapter.getHeaderMap().forEach((key, value)
            -> headerMap.put(key, value.toArray(new String[0])));
        return Collections.unmodifiableMap(headerMap);
    }

    @Override
    public String[] getHeaderValues(String name) {
        List<String> values = requestAdapter.getHeaderValues(name);
        return (values != null && !values.isEmpty() ? values.toArray(new String[0]) : null);
    }

    @Override
    public boolean containsParameter(String name) {
        return requestAdapter.hasParameter(name);
    }

    @Override
    public int getParameterCount() {
        return requestAdapter.getParameterMap().size();
    }

    @Override
    public Set<String> getAllParameterNames() {
        Collection<String> names = requestAdapter.getParameterNames();
        if (names != null) {
            return Set.copyOf(requestAdapter.getParameterNames());
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return requestAdapter.getParameterMap();
    }

    @Override
    public String[] getParameterValues(String name) {
        return requestAdapter.getParameterValues(name);
    }

    @Override
    public boolean containsCookie(String name) {
        Assert.notNull(name, "name cannot be null");
        Cookie[] cookies = getHttpServletRequest().getCookies();
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
        Cookie[] cookies = getHttpServletRequest().getCookies();
        return (cookies == null ? 0 : cookies.length);
    }

    @Override
    public Set<String> getAllCookieNames() {
        Cookie[] cookies = getHttpServletRequest().getCookies();
        if (cookies == null) {
            return Collections.emptySet();
        }
        Set<String> cookieNames = new LinkedHashSet<String>(3);
        for (Cookie cookie : cookies) {
            cookieNames.add(cookie.getName());
        }
        return Collections.unmodifiableSet(cookieNames);
    }

    @Override
    public Map<String, String[]> getCookieMap() {
        Cookie[] cookies = getHttpServletRequest().getCookies();
        if (cookies == null) {
            return Collections.emptyMap();
        }
        Map<String,String[]> cookieMap = new LinkedHashMap<>(3);
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
        Cookie[] cookies = getHttpServletRequest().getCookies();
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

    private HttpServletRequest getHttpServletRequest() {
        return requestAdapter.getAdaptee();
    }

}
