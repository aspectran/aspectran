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
package com.aspectran.web.adapter;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The Class HttpServletRequestAdapter.
 * 
 * @since 2011. 3. 13.
 */
public class HttpServletRequestAdapter extends AbstractRequestAdapter {

    /**
     * Instantiates a new HttpServletRequestAdapter.
     *
     * @param request the HTTP request
     */
    public HttpServletRequestAdapter(HttpServletRequest request) {
        super(request, request.getParameterMap());
        setRequestMethod(MethodType.resolve(request.getMethod()));
    }

    @Override
    public String getEncoding() {
        return ((HttpServletRequest)adaptee).getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        if (encoding != null) {
            ((HttpServletRequest)adaptee).setCharacterEncoding(encoding);
        }
    }

    @Override
    protected MultiValueMap<String, String> touchHeaders() {
        boolean headersInstantiated = isHeadersInstantiated();
        MultiValueMap<String, String> headers = super.touchHeaders();

        if (!headersInstantiated) {
            HttpServletRequest request = ((HttpServletRequest)adaptee);

            for (Enumeration<String> names = request.getHeaderNames(); names.hasMoreElements();) {
                String name = names.nextElement();
                for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements();) {
                    String value = values.nextElement();
                    headers.add(name, value);
                }
            }
        }

        return headers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)((HttpServletRequest)adaptee).getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        ((HttpServletRequest)adaptee).setAttribute(name, o);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return ((HttpServletRequest)adaptee).getAttributeNames();
    }

    @Override
    public void removeAttribute(String name) {
        ((HttpServletRequest)adaptee).removeAttribute(name);
    }

    @Override
    public Map<String, Object> getAllAttributes() {
        Map<String, Object> attrs = new LinkedHashMap<>();
        fillAllAttributes(attrs);
        return attrs;
    }

    @Override
    public void putAllAttributes(Map<String, Object> attributes) {
        if (attributes != null) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void fillAllAttributes(Map<String, Object> targetAttributes) {
        if (targetAttributes == null) {
            throw new IllegalArgumentException("Argument 'targetAttributes' must not be null");
        }
        Enumeration<String> names = getAttributeNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            targetAttributes.put(name, getAttribute(name));
        }
    }

    @Override
    public Locale getLocale() {
        if (super.getLocale() != null) {
            return super.getLocale();
        }
        return ((HttpServletRequest)adaptee).getLocale();
    }

}
