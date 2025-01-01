/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.adapter.AbstractWebRequestAdapter;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.support.http.MediaType;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
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
public class TowRequestAdapter extends AbstractWebRequestAdapter {

    private boolean headersObtained;

    /**
     * Instantiates a new TowRequestAdapter.
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
            HeaderMap headerMap = getHttpServerExchange().getRequestHeaders();
            if (headerMap.size() > 0) {
                MultiValueMap<String, String> multiValueMap = super.getHeaderMap();
                for (HeaderValues headerValues : headerMap) {
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
    public InputStream getInputStream() throws IOException {
        if (!getHttpServerExchange().isBlocking()) {
            getHttpServerExchange().startBlocking();
        }
        return getHttpServerExchange().getInputStream();
    }

    private HttpServerExchange getHttpServerExchange() {
        return getAdaptee();
    }

    @Override
    public void preparse() {
        HttpServerExchange exchange = getAdaptee();
        for (Map.Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue().toArray(new String[0]);
            getParameterMap().put(name, values);
        }
        String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
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
        String acceptLanguage = exchange.getRequestHeaders().getFirst(Headers.ACCEPT_LANGUAGE);
        List<Locale> locales = LocaleUtils.getLocalesFromHeader(acceptLanguage);
        if (!locales.isEmpty()) {
            setLocale(locales.get(0));
        }
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
