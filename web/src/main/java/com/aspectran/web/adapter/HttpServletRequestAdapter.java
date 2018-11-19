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
import com.aspectran.web.activity.request.ActivityRequestWrapper;
import com.aspectran.web.activity.request.RequestAttributeMap;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
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
     * @param request the activity request wrapper
     */
    public HttpServletRequestAdapter(HttpServletRequest request) {
        super(request);

        if (request instanceof ActivityRequestWrapper) {
            init((HttpServletRequest)((ActivityRequestWrapper)request).getRequest());
        } else {
            init(request);
        }
    }

    private void init(HttpServletRequest request) {
        setRequestMethod(MethodType.resolve(request.getMethod()));
        setAttributeMap(new RequestAttributeMap(request));
        setLocale((request.getLocale()));

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames.hasMoreElements()) {
            MultiValueMap<String, String> headers = getHeaderMap();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements();) {
                    String value = values.nextElement();
                    headers.add(name, value);
                }
            }
        }

        Map<String, String[]> parameters = request.getParameterMap();
        if (!parameters.isEmpty()) {
            getParameterMap().putAll(parameters);
        }
    }

    @Override
    public String getEncoding() {
        return ((HttpServletRequest)getAdaptee()).getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        if (encoding != null) {
            ((HttpServletRequest)getAdaptee()).setCharacterEncoding(encoding);
        }
    }

}
