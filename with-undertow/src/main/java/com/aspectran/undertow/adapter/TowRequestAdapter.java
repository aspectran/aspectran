package com.aspectran.undertow.adapter;

import com.aspectran.core.adapter.AbstractRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.apon.Parameters;
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
 * <p>Created: 2019-07-27</p>
 */
public class TowRequestAdapter extends AbstractRequestAdapter {

    private boolean headersObtained;

    private boolean encodingObtained;

    private boolean bodyObtained;

    private MediaType mediaType;

    /**
     * Instantiates a new UndertowRequestAdapter.
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
                MultiValueMap<String, String> headers = super.getHeaderMap();
                for (HeaderValues headerValues : getHttpServerExchange().getRequestHeaders()) {
                    String name = headerValues.getHeaderName().toString();
                    for (String value : headerValues) {
                        headers.add(name, value);
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
            return Headers.extractQuotedValueFromHeader(contentType, "charset");
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
            } catch (IOException e) {
                setBody(null);
            }
        }
        return super.getBody();
    }

    @Override
    public <T extends Parameters> T getBodyAsParameters(Class<T> requiredType) {
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
        setAttributeMap(requestAdapter.getAttributeMap());
        getParameterMap().putAll(requestAdapter.getParameterMap());
        setMediaType(requestAdapter.getMediaType());
        setLocale(requestAdapter.getLocale());
    }

}
