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
import com.aspectran.web.support.util.WebUtils;
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

    private boolean proxyAddressForwarding;

    private MediaType mediaType;

    private boolean bodyObtained;

    /**
     * Creates a new {@code AbstractWebRequestAdapter}.
     * @param requestMethod the request method
     * @param adaptee the native request object to adapt
     */
    public AbstractWebRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod, adaptee);
    }

    /**
     * Returns whether proxy address forwarding headers (X-Forwarded-*) are trusted.
     * @return true if proxy address forwarding is enabled; false otherwise
     */
    public boolean isProxyAddressForwarding() {
        return proxyAddressForwarding;
    }

    /**
     * Sets whether proxy address forwarding headers (X-Forwarded-*) are trusted.
     * @param proxyAddressForwarding true to enable proxy address forwarding; false otherwise
     */
    public void setProxyAddressForwarding(boolean proxyAddressForwarding) {
        this.proxyAddressForwarding = proxyAddressForwarding;
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
        if (proxyAddressForwarding) {
            String scheme = getHeader(HttpHeaders.X_FORWARDED_PROTO);
            if (StringUtils.hasLength(scheme)) {
                int idx = scheme.indexOf(',');
                return (idx != -1 ? scheme.substring(0, idx).trim() : scheme.trim()).toLowerCase();
            }
        }
        return "http";
    }

    @Override
    public String getServerName() {
        if (proxyAddressForwarding) {
            String forwardedHost = getHeader(HttpHeaders.X_FORWARDED_HOST);
            if (StringUtils.hasLength(forwardedHost)) {
                int idx = forwardedHost.indexOf(',');
                String host = (idx != -1 ? forwardedHost.substring(0, idx).trim() : forwardedHost.trim());
                int colonIdx = host.indexOf(':');
                return (colonIdx != -1 ? host.substring(0, colonIdx) : host);
            }
        }
        String host = getHeader(HttpHeaders.HOST);
        if (StringUtils.hasLength(host)) {
            int idx = host.indexOf(':');
            return (idx > -1 ? host.substring(0, idx) : host);
        }
        return "localhost";
    }

    @Override
    public int getServerPort() {
        if (proxyAddressForwarding) {
            String forwardedPort = getHeader(HttpHeaders.X_FORWARDED_PORT);
            if (StringUtils.hasLength(forwardedPort)) {
                try {
                    int idx = forwardedPort.indexOf(',');
                    String portStr = (idx != -1 ? forwardedPort.substring(0, idx).trim() : forwardedPort.trim());
                    return Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    // ignore
                }
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
    public String getRemoteAddr() {
        if (proxyAddressForwarding) {
            String forwardedFor = getHeader(HttpHeaders.X_FORWARDED_FOR);
            String remoteAddr = WebUtils.parseForwardedFor(forwardedFor);
            if (remoteAddr != null) {
                return remoteAddr;
            }
        }
        return null;
    }

    @Override
    public abstract String getContextPath();

    @Override
    public abstract String getRequestURI();

    @Override
    public abstract String getQueryString();

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

