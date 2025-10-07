/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.thymeleaf.context.tow;

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.adapter.WebRequestAdapter;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import org.thymeleaf.web.IWebRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Thymeleaf {@link IWebRequest} implementation for Aspectran's non-servlet
 * web environment.
 *
 * <p>This class adapts Aspectran's generic {@link WebRequestAdapter} to fit
 * Thymeleaf's {@code IWebRequest} interface. It provides access to request
 * details such as parameters, headers, and URI components by delegating
 * calls to the underlying adapter, without any dependency on native
 * server-specific APIs like {@code HttpServerExchange}.</p>
 *
 * <p>Created: 2025-10-07</p>
 */
public class TowActivityRequest implements IWebRequest {

    private final RequestAdapter requestAdapter;

    public TowActivityRequest(RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

       @Override
    public String getMethod() {
        return requestAdapter.getRequestMethod().name();
    }

    @Override
    public String getScheme() {
        return getHttpServerExchange().getRequestScheme();
    }

    @Override
    public String getServerName() {
        return getHttpServerExchange().getHostName();
    }

    @Override
    public Integer getServerPort() {
        return getHttpServerExchange().getHostPort();
    }

    @Override
    public String getApplicationPath() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getPathWithinApplication() {
        String requestURI = getHttpServerExchange().getRequestURI();
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
        return getHttpServerExchange().getQueryString();
    }

    @Override
    public boolean containsHeader(String name) {
        return requestAdapter.containsHeader(name);
    }

    @Override
    public int getHeaderCount() {
        return requestAdapter.getHeaderNames().size();
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
        Iterable<Cookie> cookies = getHttpServerExchange().requestCookies();
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
        Iterable<Cookie> cookies = getHttpServerExchange().requestCookies();
        if (cookies instanceof Collection<?> collection) {
            return collection.size();
        }
        Iterator<Cookie> iterator = cookies.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    @Override
    public Set<String> getAllCookieNames() {
        Iterable<Cookie> cookies = getHttpServerExchange().requestCookies();
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
        Iterable<Cookie> cookies = getHttpServerExchange().requestCookies();
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
        Iterable<Cookie> cookies = getHttpServerExchange().requestCookies();
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

    private HttpServerExchange getHttpServerExchange() {
        return requestAdapter.getAdaptee();
    }

}
