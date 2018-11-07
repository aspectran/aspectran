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

public class ActivityRequestWrapper extends HttpServletRequestWrapper {

    private final WebActivity activity;

    private final HttpServletRequestAdapter requestAdapter;

    public ActivityRequestWrapper(WebActivity activity, HttpServletRequest request) {
        super(request);

        this.activity = activity;
        this.requestAdapter = (HttpServletRequestAdapter)activity.getRequestAdapter();
    }

    @Override
    public String getHeader(String name) {
        return requestAdapter.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(requestAdapter.getHeaders(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(requestAdapter.getHeaderNames());
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
        return Collections.enumeration(requestAdapter.getParameterNames());
    }

    @Override
    public String[] getParameterValues(String name) {
        return requestAdapter.getParameterValues(name);
    }

    @Override
    public Object getAttribute(String name) {
        Object value = requestAdapter.getAttribute(name);
        if (value != null) {
            return value;
        } else {
            return activity.getTranslet().getProcessResult(name);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(requestAdapter.getAttributeNames());
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
    public Locale getLocale() {
        return requestAdapter.getLocale();
    }

}
