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
package com.aspectran.web.adapter;

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.activity.request.RequestAttributeMap;
import com.aspectran.web.support.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;

/**
 * Adapt {@link HttpServletRequest} to Core {@link RequestAdapter}.
 *
 * @since 2011. 3. 13.
 */
public class HttpServletRequestAdapter extends AbstractWebRequestAdapter {

    private boolean headersObtained;

    /**
     * Instantiates a new HttpServletRequestAdapter.
     * @param requestMethod the request method
     * @param request the activity request wrapper
     */
    public HttpServletRequestAdapter(MethodType requestMethod, HttpServletRequest request) {
        super(requestMethod, request);
    }

    @Override
    public MultiValueMap<String, String> getHeaderMap() {
        if (!headersObtained) {
            headersObtained = true;
            HttpServletRequest request = getAdaptee();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames.hasMoreElements()) {
                MultiValueMap<String, String> multiValueMap = super.getHeaderMap();
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements();) {
                        String value = values.nextElement();
                        multiValueMap.add(name, value);
                    }
                }
            }
        }
        return super.getHeaderMap();
    }

    @Override
    public String getEncoding() {
        return getHttpServletRequest().getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        getHttpServletRequest().setCharacterEncoding(encoding);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getHttpServletRequest().getInputStream();
    }

    @Override
    public Principal getPrincipal() {
        return getHttpServletRequest().getUserPrincipal();
    }

    private HttpServletRequest getHttpServletRequest() {
        return getAdaptee();
    }

    @Override
    public void preparse() {
        HttpServletRequest request = getAdaptee();

        RequestAttributeMap requestAttributeMap = new RequestAttributeMap();
        requestAttributeMap.setRequest(request);
        setAttributeMap(requestAttributeMap);

        Map<String, String[]> parameters = request.getParameterMap();
        if (!parameters.isEmpty()) {
            getParameterMap().putAll(parameters);
        }

        String contentType = request.getContentType();
        if (StringUtils.hasLength(contentType)) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                setMediaType(mediaType);
                if (mediaType.getCharset() != null) {
                    setEncoding(mediaType.getCharset().name());
                }
            } catch (Exception e) {
                // ignored
            }
        }

        setLocale(request.getLocale());
    }

    @Override
    public void preparse(WebRequestAdapter requestAdapter) {
        if (requestAdapter == this) {
            throw new IllegalStateException("Unable To Replicate");
        }
        setAttributeMap(requestAdapter.getAttributeMap());
        getParameterMap().putAll(requestAdapter.getParameterMap());
        setMediaType(requestAdapter.getMediaType());
        setLocale(requestAdapter.getLocale());
    }

}
