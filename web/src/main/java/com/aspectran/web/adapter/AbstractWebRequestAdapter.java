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

import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.web.activity.request.WebRequestBodyParser;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link WebRequestAdapter} implementations.
 * <p>This class extends {@link com.aspectran.core.adapter.AbstractRequestAdapter}
 * and provides common functionality for web-based request adapters, such as handling
 * the request's {@link MediaType} and parsing the request body on demand.
 * </p>
 *
 * @since 6.3.0
 */
public abstract class AbstractWebRequestAdapter extends AbstractRequestAdapter implements WebRequestAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWebRequestAdapter.class);

    private MediaType mediaType;

    private boolean bodyObtained;

    private String requestURI;

    private String queryString;

    private String contextPath;

    /**
     * Creates a new {@code AbstractWebRequestAdapter}.
     * @param requestMethod the request method
     * @param adaptee the native request object to adapt
     */
    public AbstractWebRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod, adaptee);
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * Sets the {@link MediaType} of the request body.
     * @param mediaType the media type
     */
    protected void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation parses the request body using {@link WebRequestBodyParser}
     * on the first call and caches the result.</p>
     */
    @Override
    public String getBody() {
        if (!bodyObtained) {
            bodyObtained = true;
            try {
                String body = WebRequestBodyParser.parseBody(this);
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

    /**
     * {@inheritDoc}
     * <p>This implementation parses the request body as parameters using
     * {@link WebRequestBodyParser}.</p>
     */
    @Override
    public <T extends Parameters> T getBodyAsParameters(Class<T> requiredType) throws RequestParseException {
        if (getMediaType() != null) {
            return WebRequestBodyParser.parseBodyAsParameters(this, requiredType);
        } else {
            return null;
        }
    }

    @Override
    public String getScheme() {
        String scheme = getHeader(HttpHeaders.X_FORWARDED_PROTO);
        if (StringUtils.hasLength(scheme)) {
            return scheme;
        }
        return "http";
    }

    @Override
    public String getServerName() {
        String host = getHeader(HttpHeaders.HOST);
        if (StringUtils.hasLength(host)) {
            int idx = host.indexOf(':');
            return (idx > -1 ? host.substring(0, idx) : host);
        }
        return "localhost";
    }

    @Override
    public int getServerPort() {
        String forwardedPort = getHeader(HttpHeaders.X_FORWARDED_PORT);
        if (StringUtils.hasLength(forwardedPort)) {
            try {
                return Integer.parseInt(forwardedPort);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        String host = getHeader(HttpHeaders.HOST);
        if (StringUtils.hasLength(host)) {
            int idx = host.indexOf(':');
            if (idx > -1) {
                try {
                    return Integer.parseInt(host.substring(idx + 1));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return ("https".equalsIgnoreCase(getScheme()) ? 443 : 80);
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getContextPath() {
        return (contextPath != null ? contextPath : StringUtils.EMPTY);
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
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
        setRequestURI(requestAdapter.getRequestURI());
        setQueryString(requestAdapter.getQueryString());
        setContextPath(requestAdapter.getContextPath());
    }

}

