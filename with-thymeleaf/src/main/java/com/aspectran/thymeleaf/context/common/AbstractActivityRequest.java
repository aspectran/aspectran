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
package com.aspectran.thymeleaf.context.common;

import com.aspectran.core.adapter.RequestAdapter;
import org.thymeleaf.web.IWebRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Thymeleaf {@link IWebRequest} implementation that wraps an Aspectran
 * {@link RequestAdapter}.
 *
 * <p>Created: 2025-10-08</p>
 */
public abstract class AbstractActivityRequest implements IWebRequest {

    private final RequestAdapter requestAdapter;

    /**
     * Instantiates a new Abstract activity request.
     * @param requestAdapter the request adapter
     */
    public AbstractActivityRequest(RequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    /**
     * Returns the request adapter.
     * @return the request adapter
     */
    protected RequestAdapter getRequestAdapter() {
        return requestAdapter;
    }

    @Override
    public boolean containsHeader(String name) {
        return requestAdapter.containsHeader(name);
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

}
