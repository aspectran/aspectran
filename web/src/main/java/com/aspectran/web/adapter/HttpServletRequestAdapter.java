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

import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.web.activity.request.RequestAttributeMap;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
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
        super(request, true);
        setRequestMethod(MethodType.resolve(request.getMethod()));
    }

    @Override
    public MultiValueMap<String, String> touchHeaders() {
        MultiValueMap<String, String> headers = getHeaders();
        if (headers == null) {
            headers = super.touchHeaders();
            HttpServletRequest request = ((HttpServletRequest)adaptee);
            for (Enumeration<String> names = request.getHeaderNames(); names.hasMoreElements(); ) {
                String name = names.nextElement();
                for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements(); ) {
                    String value = values.nextElement();
                    headers.add(name, value);
                }
            }
        }
        return headers;
    }

    @Override
    public ParameterMap touchParameterMap() {
        ParameterMap parameterMap = getParameterMap();
        if (parameterMap == null) {
            parameterMap = super.touchParameterMap();
            parameterMap.putAll(((HttpServletRequest)getAdaptee()).getParameterMap());
        }
        return parameterMap;
    }

    @Override
    public Map<String, Object> touchAttributes() {
        Map<String, Object> attributeMap = getAttributeMap();
        if (attributeMap == null) {
            attributeMap = new RequestAttributeMap((HttpServletRequest)adaptee);
            setAttributeMap(attributeMap);
        }
        return attributeMap;
    }

    @Override
    public String getEncoding() {
        return ((HttpServletRequest)adaptee).getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        if (encoding != null) {
            super.setEncoding(encoding);
            ((HttpServletRequest)adaptee).setCharacterEncoding(encoding);
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
