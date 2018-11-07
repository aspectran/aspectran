/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.adapter.HttpServletRequestAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
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

    private final WebActivity activity;

    public ActivityRequestWrapper(WebActivity activity, HttpServletRequest request) {
        super(request);
        this.activity = activity;
    }

    @Override
    public HttpServletRequest getRequest() {
        return (HttpServletRequest)super.getRequest();
    }

    @Override
    public String getHeader(String name) {
        return getRequestAdapter().getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(getRequestAdapter().getHeaders(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(getRequestAdapter().getHeaderNames());
    }

    @Override
    public String getParameter(String name) {
        return getRequestAdapter().getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return getRequestAdapter().getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getRequestAdapter().getParameterNames());
    }

    @Override
    public String[] getParameterValues(String name) {
        return getRequestAdapter().getParameterValues(name);
    }

    @Override
    public Object getAttribute(String name) {
        Object value = getRequestAdapter().getAttribute(name);
        if (value != null) {
            return value;
        } else {
            return activity.getTranslet().getProcessResult(name);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(getRequestAdapter().getAttributeNames());
    }

    @Override
    public void setAttribute(String name, Object o) {
        getRequestAdapter().setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        getRequestAdapter().removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        return getRequestAdapter().getLocale();
    }

    private HttpServletRequestAdapter getRequestAdapter() {
        return (HttpServletRequestAdapter)activity.getRequestAdapter();
    }

}
