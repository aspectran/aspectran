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

    public ActivityRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void setWebActivity(WebActivity activity) {
        this.activity = activity;
    }

    @Override
    public String getHeader(String name) {
        if (getRequestAdapter() == null) {
            return super.getHeader(name);
        }
        return getRequestAdapter().getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (getRequestAdapter() == null) {
            return super.getHeaders(name);
        }
        return Collections.enumeration(getRequestAdapter().getHeaderValues(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        if (getRequestAdapter() == null) {
            return super.getHeaderNames();
        }
        return Collections.enumeration(getRequestAdapter().getHeaderNames());
    }

    @Override
    public String getParameter(String name) {
        if (getRequestAdapter() == null) {
            return super.getParameter(name);
        }
        return getRequestAdapter().getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (getRequestAdapter() == null) {
            return super.getParameterMap();
        }
        return getRequestAdapter().getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (getRequestAdapter() == null) {
            return super.getParameterNames();
        }
        return Collections.enumeration(getRequestAdapter().getParameterNames());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (getRequestAdapter() == null) {
            return super.getParameterValues(name);
        }
        return getRequestAdapter().getParameterValues(name);
    }

    @Override
    public Object getAttribute(String name) {
        if (getRequestAdapter() == null) {
            return super.getAttribute(name);
        }
        Object value = getRequestAdapter().getAttribute(name);
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
        if (getRequestAdapter() == null) {
            return super.getAttributeNames();
        }
        return Collections.enumeration(getRequestAdapter().getAttributeNames());
    }

    @Override
    public void setAttribute(String name, Object o) {
        if (getRequestAdapter() == null) {
            super.setAttribute(name, o);
            return;
        }
        getRequestAdapter().setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        if (getRequestAdapter() == null) {
            super.removeAttribute(name);
            return;
        }
        getRequestAdapter().removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        if (getRequestAdapter() == null) {
            return super.getLocale();
        }
        return getRequestAdapter().getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        if (getRequestAdapter() == null || getRequestAdapter().getLocale() == null) {
            return super.getLocales();
        }
        List<Locale> list = new ArrayList<>();
        list.add(getRequestAdapter().getLocale());
        Enumeration<Locale> locales = super.getLocales();
        while (locales.hasMoreElements()) {
            list.add(locales.nextElement());
        }
        return Collections.enumeration(list);
    }

    private RequestAdapter getRequestAdapter() {
        return (activity != null ? activity.getRequestAdapter() : null);
    }

}
