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
package com.aspectran.web.activity.request;

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Request Wrapper to access activity data.
 *
 * @since 5.7.1
 */
public class ActivityRequestWrapper extends HttpServletRequestWrapper {

    private final RequestAdapter requestAdapter;

    public ActivityRequestWrapper(@NonNull RequestAdapter requestAdapter) {
        super(requestAdapter.getAdaptee());
        this.requestAdapter = requestAdapter;
    }

    @Override
    public void setRequest(@NonNull ServletRequest request) {
        super.setRequest(request);
        Map<String, Object> requestAttributeMap = requestAdapter.getAttributeMap();
        if (requestAttributeMap instanceof RequestAttributeMap) {
            ((RequestAttributeMap)requestAttributeMap).setRequest(request);
        }
    }

    @Override
    public String getHeader(String name) {
        return requestAdapter.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Collection<String> values = requestAdapter.getHeaderValues(name);
        if (values != null) {
            return Collections.enumeration(requestAdapter.getHeaderValues(name));
        } else {
            return Collections.emptyEnumeration();
        }
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Collection<String> names = requestAdapter.getHeaderNames();
        if (names != null) {
            return Collections.enumeration(requestAdapter.getHeaderNames());
        } else {
            return Collections.emptyEnumeration();
        }
    }

    @Override
    public Object getAttribute(String name) {
        return requestAdapter.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Collection<String> names = requestAdapter.getAttributeNames();
        if (names != null) {
            return Collections.enumeration(requestAdapter.getAttributeNames());
        } else {
            return Collections.emptyEnumeration();
        }
    }

    @Override
    public void setAttribute(String name, Object o) {
        requestAdapter.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        requestAdapter.removeAttribute(name);
    }

    @Override
    public String getParameter(String name) {
        return requestAdapter.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return requestAdapter.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Collection<String> names = requestAdapter.getParameterNames();
        if (names != null) {
            return Collections.enumeration(requestAdapter.getParameterNames());
        } else {
            return Collections.emptyEnumeration();
        }
    }

    @Override
    public String[] getParameterValues(String name) {
        return requestAdapter.getParameterValues(name);
    }

    @Override
    public Locale getLocale() {
        if (requestAdapter.getLocale() == null) {
            return super.getLocale();
        }
        return requestAdapter.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        if (requestAdapter.getLocale() == null) {
            return super.getLocales();
        }
        return Collections.enumeration(Collections.singleton(requestAdapter.getLocale()));
    }

}
