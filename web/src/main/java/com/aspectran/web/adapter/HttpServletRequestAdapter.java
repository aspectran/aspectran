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
import com.aspectran.web.activity.request.ActivityRequestWrapper;
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

    private final HttpServletRequest request;

    private boolean headersHeld;

    private boolean parametersHeld;

    private boolean attributesHeld;

    /**
     * Instantiates a new HttpServletRequestAdapter.
     *
     * @param requestWrapper the activity request wrapper
     */
    public HttpServletRequestAdapter(ActivityRequestWrapper requestWrapper) {
        super(requestWrapper);
        this.request = requestWrapper.getRequest();
        setRequestMethod(MethodType.resolve(this.request.getMethod()));
    }

    @Override
    public MultiValueMap<String, String> getHeaderMap() {
        if (!headersHeld) {
            headersHeld = true;
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames.hasMoreElements()) {
                MultiValueMap<String, String> headers = super.getHeaderMap();
                for (Enumeration<String> names = headerNames; names.hasMoreElements(); ) {
                    String name = names.nextElement();
                    for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements(); ) {
                        String value = values.nextElement();
                        headers.add(name, value);
                    }
                }
                return headers;
            }
        }
        return super.getHeaderMap();
    }

    @Override
    public ParameterMap getParameterMap() {
        if (!parametersHeld) {
            parametersHeld = true;
            Map<String, String[]> parameters = request.getParameterMap();
            if (!parameters.isEmpty()) {
                super.getParameterMap().putAll(parameters);
            }
        }
        return super.getParameterMap();
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        if (!attributesHeld) {
            attributesHeld = true;
            setAttributeMap(new RequestAttributeMap(request));
        }
        return super.getAttributeMap();
    }

    @Override
    public String getEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        if (encoding != null) {
            super.setEncoding(encoding);
            request.setCharacterEncoding(encoding);
        }
    }

    @Override
    public Locale getLocale() {
        if (super.getLocale() != null) {
            return super.getLocale();
        }
        return request.getLocale();
    }

}
