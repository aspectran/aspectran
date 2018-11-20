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

    private volatile boolean headersHeld;

    /**
     * Instantiates a new HttpServletRequestAdapter.
     *
     * @param request the activity request wrapper
     */
    public HttpServletRequestAdapter(HttpServletRequest request) {
        super(request);

        preparse(getHttpServletRequest());
    }

    @Override
    public MultiValueMap<String, String> getHeaderMap() {
        if (!headersHeld) {
            headersHeld = true;
            Enumeration<String> headerNames = getHttpServletRequest().getHeaderNames();
            if (headerNames.hasMoreElements()) {
                MultiValueMap<String, String> headers = super.getHeaderMap();
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    for (Enumeration<String> values = getHttpServletRequest().getHeaders(name); values.hasMoreElements(); ) {
                        String value = values.nextElement();
                        headers.add(name, value);
                    }
                }
            }
        }
        return super.getHeaderMap();
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

    public HttpServletRequest getHttpServletRequest() {
        if (getAdaptee() instanceof ActivityRequestWrapper) {
            return (HttpServletRequest)((ActivityRequestWrapper)getAdaptee()).getRequest();
        } else {
            return (HttpServletRequest) getAdaptee();
        }
    }

    private void preparse(HttpServletRequest request) {
        setRequestMethod(MethodType.resolve(request.getMethod()));
        setAttributeMap(new RequestAttributeMap(request));
        setLocale((request.getLocale()));

        Map<String, String[]> parameters = request.getParameterMap();
        if (!parameters.isEmpty()) {
            getParameterMap().putAll(parameters);
        }
    }

}
