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
package com.aspectran.web.activity.request;

import com.aspectran.core.adapter.RequestAdapter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.jspecify.annotations.NonNull;

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

    /**
     * Constructs a new request wrapper.
     * @param requestAdapter the request adapter to wrap
     */
    public ActivityRequestWrapper(@NonNull RequestAdapter requestAdapter) {
        super(requestAdapter.getAdaptee());
        this.requestAdapter = requestAdapter;
    }

    /**
     * {@inheritDoc}
     * <p>Also updates the underlying request in the {@link RequestAttributeMap} if it exists.
     */
    @Override
    public void setRequest(@NonNull ServletRequest request) {
        super.setRequest(request);
        Map<String, Object> attributeMap = requestAdapter.getAttributeMap();
        if (attributeMap instanceof RequestAttributeMap requestAttributeMap) {
            requestAttributeMap.setRequest(request);
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the header from the underlying {@link RequestAdapter}.
     */
    @Override
    public String getHeader(String name) {
        return requestAdapter.getHeader(name);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the headers from the underlying {@link RequestAdapter}.
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        Collection<String> values = requestAdapter.getHeaderValues(name);
        if (values != null) {
            return Collections.enumeration(values);
        } else {
            return Collections.emptyEnumeration();
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the header names from the underlying {@link RequestAdapter}.
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        Collection<String> names = requestAdapter.getHeaderNames();
        if (names != null) {
            return Collections.enumeration(names);
        } else {
            return Collections.emptyEnumeration();
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the attribute from the underlying {@link RequestAdapter}.
     */
    @Override
    public Object getAttribute(String name) {
        return requestAdapter.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the attribute names from the underlying {@link RequestAdapter}.
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        Collection<String> names = requestAdapter.getAttributeNames();
        if (names != null) {
            return Collections.enumeration(names);
        } else {
            return Collections.emptyEnumeration();
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation sets the attribute on the underlying {@link RequestAdapter}.
     */
    @Override
    public void setAttribute(String name, Object o) {
        requestAdapter.setAttribute(name, o);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation removes the attribute from the underlying {@link RequestAdapter}.
     */
    @Override
    public void removeAttribute(String name) {
        requestAdapter.removeAttribute(name);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the parameter from the underlying {@link RequestAdapter}.
     */
    @Override
    public String getParameter(String name) {
        return requestAdapter.getParameter(name);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the parameter map from the underlying {@link RequestAdapter}.
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return requestAdapter.getParameterMap();
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the parameter names from the underlying {@link RequestAdapter}.
     */
    @Override
    public Enumeration<String> getParameterNames() {
        Collection<String> names = requestAdapter.getParameterNames();
        if (names != null) {
            return Collections.enumeration(names);
        } else {
            return Collections.emptyEnumeration();
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the parameter values from the underlying {@link RequestAdapter}.
     */
    @Override
    public String[] getParameterValues(String name) {
        return requestAdapter.getParameterValues(name);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the locale from the underlying {@link RequestAdapter},
     * falling back to the default request locale if not present.
     */
    @Override
    public Locale getLocale() {
        Locale locale = requestAdapter.getLocale();
        if (locale != null) {
            return locale;
        } else {
            return super.getLocale();
        }
    }

    /**
     * {@inheritDoc}
     * <p>If a specific locale is set on the underlying {@link RequestAdapter}, this
     * implementation returns an enumeration containing only that locale. Otherwise, it
     * falls back to the default request locales.
     */
    @Override
    public Enumeration<Locale> getLocales() {
        Locale locale = requestAdapter.getLocale();
        if (locale != null) {
            return Collections.enumeration(Collections.singleton(locale));
        } else {
            return super.getLocales();
        }
    }

}
