/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.web.activity.request.ActivityRequestWrapper;
import com.aspectran.web.activity.request.RequestAttributeMap;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.support.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

/**
 * Adapt {@link HttpServletRequest} to Core {@link RequestAdapter}.
 * 
 * @since 2011. 3. 13.
 */
public class HttpServletRequestAdapter extends AbstractRequestAdapter {

    private MediaType mediaType;

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
                    for (Enumeration<String> values = getHttpServletRequest().getHeaders(name);
                         values.hasMoreElements();) {
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
        ((HttpServletRequest)getAdaptee()).setCharacterEncoding(encoding);
    }

    @Override
    public String getBody() {
        if (super.getBody() == null) {
            WebRequestBodyParser.parseBody(this);
        }
        return super.getBody();
    }

    @Override
    public <T extends Parameters> T getBodyAsParameters(Class<T> requiredType) {
        return WebRequestBodyParser.parseBodyAsParameters(this, requiredType);
    }

    /**
     * Gets the media type value included in the Content-Type header.
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    private void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    private HttpServletRequest getHttpServletRequest() {
        if (getAdaptee() instanceof ActivityRequestWrapper) {
            return (HttpServletRequest)((ActivityRequestWrapper)getAdaptee()).getRequest();
        } else {
            return (HttpServletRequest)getAdaptee();
        }
    }

    private void preparse(HttpServletRequest request) {
        setAttributeMap(new RequestAttributeMap(request));
        Map<String, String[]> parameters = request.getParameterMap();
        if (!parameters.isEmpty()) {
            getParameterMap().putAll(parameters);
        }
        setRequestMethod(MethodType.resolve(request.getMethod()));
        if (request.getContentType() != null) {
            setMediaType(MediaType.parseMediaType(request.getContentType()));
        }
        setLocale(request.getLocale());
    }

}
