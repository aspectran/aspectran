/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.web.activity.WebActivity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Request Wrapper to access activity data.
 *
 * @since 5.7.1
 */
public class ActivityRequestWrapper extends HttpServletRequestWrapper {

    private WebActivity activity;

    private RequestAdapter requestAdapter;

    public ActivityRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void setWebActivity(WebActivity activity) {
        this.activity = activity;
        this.requestAdapter = activity.getRequestAdapter();
    }

    @Override
    public String getHeader(String name) {
        if (requestAdapter == null) {
            return super.getHeader(name);
        }
        return requestAdapter.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (requestAdapter == null) {
            return super.getHeaders(name);
        }
        return Collections.enumeration(requestAdapter.getHeaderValues(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        if (requestAdapter == null) {
            return super.getHeaderNames();
        }
        return Collections.enumeration(requestAdapter.getHeaderNames());
    }

    @Override
    public String getParameter(String name) {
        if (requestAdapter == null) {
            return super.getParameter(name);
        }
        return requestAdapter.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (requestAdapter == null) {
            return super.getParameterMap();
        }
        return requestAdapter.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (requestAdapter == null) {
            return super.getParameterNames();
        }
        return Collections.enumeration(requestAdapter.getParameterNames());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (requestAdapter == null) {
            return super.getParameterValues(name);
        }
        return requestAdapter.getParameterValues(name);
    }

    @Override
    public Object getAttribute(String name) {
        if (requestAdapter == null) {
            return super.getAttribute(name);
        }
        Object value = requestAdapter.getAttribute(name);
        if (value != null) {
            return value;
        }
        if (activity != null && activity.getTranslet() != null) {
            return activity.getTranslet().getProcessResult(name);
        }
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        if (activity == null) {
            return super.getAttributeNames();
        }
        return Collections.enumeration(requestAdapter.getAttributeNames());
    }

    @Override
    public void setAttribute(String name, Object o) {
        if (activity == null) {
            super.setAttribute(name, o);
            return;
        }
        requestAdapter.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        if (activity == null) {
            super.removeAttribute(name);
            return;
        }
        requestAdapter.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        if (activity == null) {
            return super.getLocale();
        }
        return requestAdapter.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        if (activity == null || requestAdapter.getLocale() == null) {
            return super.getLocales();
        }
        List<Locale> list = new ArrayList<>();
        list.add(requestAdapter.getLocale());
        Enumeration<Locale> locales = super.getLocales();
        while (locales.hasMoreElements()) {
            list.add(locales.nextElement());
        }
        return Collections.enumeration(list);
    }

}
