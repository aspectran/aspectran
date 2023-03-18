/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.Activity;

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

    private final Activity activity;

    public ActivityRequestWrapper(Activity activity) {
        super(activity.getRequestAdapter().getAdaptee());
        this.activity = activity;
    }

    @Override
    public String getHeader(String name) {
        return activity.getRequestAdapter().getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(activity.getRequestAdapter().getHeaderValues(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(activity.getRequestAdapter().getHeaderNames());
    }

    @Override
    public String getParameter(String name) {
        return activity.getRequestAdapter().getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return activity.getRequestAdapter().getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(activity.getRequestAdapter().getParameterNames());
    }

    @Override
    public String[] getParameterValues(String name) {
        return activity.getRequestAdapter().getParameterValues(name);
    }

    @Override
    public Object getAttribute(String name) {
        return activity.getRequestAdapter().getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(activity.getRequestAdapter().getAttributeNames());
    }

    @Override
    public void setAttribute(String name, Object o) {
        activity.getRequestAdapter().setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        activity.getRequestAdapter().removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        if (activity.getRequestAdapter().getLocale() == null) {
            return super.getLocale();
        }
        return activity.getRequestAdapter().getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        if (activity.getRequestAdapter().getLocale() == null) {
            return super.getLocales();
        }
        return Collections.enumeration(Collections.singleton(activity.getRequestAdapter().getLocale()));
    }

}
