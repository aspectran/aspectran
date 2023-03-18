/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.activity.request.RequestAttributeMap;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.support.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

/**
 * Adapt {@link HttpServletRequest} to Core {@link RequestAdapter}.
 * 
 * @since 2011. 3. 13.
 */
public class HttpServletRequestAdapter extends AbstractRequestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestAdapter.class);

    private boolean headersObtained;

    private boolean bodyObtained;

    private MediaType mediaType;

    /**
     * Instantiates a new HttpServletRequestAdapter.
     *
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
                    for (Enumeration<String> values = request.getHeaders(name);
                            values.hasMoreElements();) {
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
        return ((HttpServletRequest)getAdaptee()).getCharacterEncoding();
    }

    @Override
    public void setEncoding(String encoding) throws UnsupportedEncodingException {
        ((HttpServletRequest)getAdaptee()).setCharacterEncoding(encoding);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return ((HttpServletRequest)getAdaptee()).getInputStream();
    }

    @Override
    public String getBody() {
        if (!bodyObtained) {
            bodyObtained = true;
            try {
                String body = WebRequestBodyParser.parseBody(getInputStream(), getEncoding(), getMaxRequestSize());
                setBody(body);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to parse request body", e);
                }
                setBody(null);
            }
        }
        return super.getBody();
    }

    @Override
    public <T extends Parameters> T getBodyAsParameters(Class<T> requiredType) throws RequestParseException {
        if (getMediaType() != null) {
            return WebRequestBodyParser.parseBodyAsParameters(this, getMediaType(), requiredType);
        } else {
            return null;
        }
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

    public void preparse() {
        HttpServletRequest request = getAdaptee();
        Map<String, String[]> parameters = request.getParameterMap();
        if (!parameters.isEmpty()) {
            getParameterMap().putAll(parameters);
        }
        setAttributeMap(new RequestAttributeMap(request));
        if (request.getContentType() != null) {
            setMediaType(MediaType.parseMediaType(request.getContentType()));
        }
        setLocale(request.getLocale());
    }

    public void preparse(HttpServletRequestAdapter requestAdapter) {
        getParameterMap().putAll(requestAdapter.getParameterMap());
        setAttributeMap(requestAdapter.getAttributeMap());
        setMediaType(requestAdapter.getMediaType());
        setLocale(requestAdapter.getLocale());
    }

}
