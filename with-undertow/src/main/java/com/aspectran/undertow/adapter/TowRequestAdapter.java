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
package com.aspectran.undertow.adapter;

import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.support.http.MediaType;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.LocaleUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The Class TowRequestAdapter.
 *
 * <p>Created: 2019-07-27</p>
 */
public class TowRequestAdapter extends AbstractRequestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TowRequestAdapter.class);

    private boolean headersObtained;

    private boolean encodingObtained;

    private boolean bodyObtained;

    private MediaType mediaType;

    /**
     * Instantiates a new TowRequestAdapter.
     *
     * @param requestMethod the request method
     * @param exchange the adaptee object
     */
    public TowRequestAdapter(MethodType requestMethod, HttpServerExchange exchange) {
        super(requestMethod, exchange);
    }

    @Override
    public MultiValueMap<String, String> getHeaderMap() {
        if (!headersObtained) {
            headersObtained = true;
            if (getHttpServerExchange().getRequestHeaders().size() > 0) {
                MultiValueMap<String, String> multiValueMap = super.getHeaderMap();
                for (HeaderValues headerValues : getHttpServerExchange().getRequestHeaders()) {
                    String name = headerValues.getHeaderName().toString();
                    for (String value : headerValues) {
                        multiValueMap.add(name, value);
                    }
                }
            }
        }
        return super.getHeaderMap();
    }

    @Override
    public String getEncoding() {
        if (!encodingObtained) {
            encodingObtained = true;
            String contentType = getHttpServerExchange().getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
            if (contentType == null) {
                return null;
            }
            return Headers.extractQuotedValueFromHeader(contentType, MediaType.PARAM_CHARSET);
        }
        return super.getEncoding();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getHttpServerExchange().getInputStream();
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

    private HttpServerExchange getHttpServerExchange() {
        return (HttpServerExchange)getAdaptee();
    }

    public void preparse() {
        HttpServerExchange exchange = getAdaptee();
        for (Map.Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue().toArray(new String[0]);
            getParameterMap().put(name, values);
        }
        String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
        if (contentType != null) {
            setMediaType(MediaType.parseMediaType(contentType));
        }
        String acceptLanguage = exchange.getRequestHeaders().getFirst(Headers.ACCEPT_LANGUAGE);
        List<Locale> locales = LocaleUtils.getLocalesFromHeader(acceptLanguage);
        if (!locales.isEmpty()) {
            setLocale(locales.get(0));
        }
    }

    public void preparse(TowRequestAdapter requestAdapter) {
        getParameterMap().putAll(requestAdapter.getParameterMap());
        setAttributeMap(requestAdapter.getAttributeMap());
        setMediaType(requestAdapter.getMediaType());
        setLocale(requestAdapter.getLocale());
    }

}
