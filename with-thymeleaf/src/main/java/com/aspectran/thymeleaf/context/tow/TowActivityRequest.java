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

import com.aspectran.web.adapter.WebRequestAdapter;
import org.thymeleaf.web.IWebRequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

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

    private final WebRequestAdapter requestAdapter;

    public TowActivityRequest(WebRequestAdapter requestAdapter) {
        this.requestAdapter = requestAdapter;
    }

    @Override
    public String getMethod() {
        return requestAdapter.getRequestMethod().name();
    }

    @Override
    public String getScheme() {
        return requestAdapter.getScheme();
    }

    @Override
    public String getServerName() {
        return requestAdapter.getServerName();
    }

    @Override
    public Integer getServerPort() {
        return requestAdapter.getServerPort();
    }

    @Override
    public String getRequestURI() {
        return requestAdapter.getRequestUri();
    }

    @Override
    public String getContextPath() {
        return requestAdapter.getContextPath();
    }

    @Override
    public String getHeader(String name) {
        return requestAdapter.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(requestAdapter.getHeaderNames());
    }

    @Override
    public List<String> getHeaders(String name) {
        return requestAdapter.getHeaders(name);
    }

    @Override
    public boolean containsParameter(String name) {
        return requestAdapter.hasParameter(name);
    }

    @Override
    public int getParameterCount() {
        return requestAdapter.getParameterNames().size();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(requestAdapter.getParameterNames());
    }

    @Override
    public String getParameterValue(String name) {
        return requestAdapter.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return requestAdapter.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return requestAdapter.getParameterMap();
    }

}
