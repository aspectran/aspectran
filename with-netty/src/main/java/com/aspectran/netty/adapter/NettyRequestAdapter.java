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
package com.aspectran.netty.adapter;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.adapter.AbstractWebRequestAdapter;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.support.http.MediaType;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An adapter that wraps a Netty {@link FullHttpRequest}, exposing it as a
 * {@link WebRequestAdapter} for the Aspectran framework.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyRequestAdapter extends AbstractWebRequestAdapter {

    private final ChannelHandlerContext ctx;

    private final String contextPath;

    private boolean headersObtained;

    public NettyRequestAdapter(
            MethodType requestMethod,
            FullHttpRequest request,
            ChannelHandlerContext ctx,
            String contextPath) {
        super(requestMethod, request);
        this.ctx = ctx;
        this.contextPath = (contextPath != null ? contextPath : StringUtils.EMPTY);
    }

    public FullHttpRequest getHttpRequest() {
        return getAdaptee();
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return ctx;
    }

    @Override
    public MultiValueMap<String, String> getHeaderMap() {
        if (!headersObtained) {
            headersObtained = true;
            HttpHeaders nettyHeaders = getHttpRequest().headers();
            if (!nettyHeaders.isEmpty()) {
                MultiValueMap<String, String> multiValueMap = super.getHeaderMap();
                for (Map.Entry<String, String> entry : nettyHeaders) {
                    multiValueMap.add(entry.getKey(), entry.getValue());
                }
            }
        }
        return super.getHeaderMap();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteBufInputStream(getHttpRequest().content().duplicate());
    }

    @Override
    public String getScheme() {
        if (ctx != null && ctx.pipeline().get(SslHandler.class) != null) {
            return "https";
        }
        return super.getScheme();
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getRequestURI() {
        FullHttpRequest request = getHttpRequest();
        if (request != null) {
            return new QueryStringDecoder(request.uri()).rawPath();
        }
        return null;
    }

    @Override
    public String getQueryString() {
        FullHttpRequest request = getHttpRequest();
        if (request != null) {
            return new QueryStringDecoder(request.uri()).rawQuery();
        }
        return null;
    }

    public String getRemoteAddr() {
        if (ctx != null && ctx.channel() != null) {
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            if (remoteAddress instanceof InetSocketAddress inetAddress) {
                return inetAddress.getAddress().getHostAddress();
            }
        }
        return null;
    }

    @Override
    public void preparse() {
        FullHttpRequest request = getHttpRequest();
        Charset charset = (getEncoding() != null ? Charset.forName(getEncoding()) : StandardCharsets.UTF_8);
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri(), charset);
        for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
            getParameterMap().put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }

        String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (StringUtils.hasLength(contentType)) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                setMediaType(mediaType);
                if (mediaType.getCharset() != null) {
                    setEncoding(mediaType.getCharset().name());
                }
            } catch (Exception e) {
                // ignore
            }
        }

        String acceptLanguage = request.headers().get(HttpHeaderNames.ACCEPT_LANGUAGE);
        if (StringUtils.hasLength(acceptLanguage)) {
            try {
                String firstLang = acceptLanguage.split(",")[0].trim().split(";")[0].trim();
                setLocale(Locale.forLanguageTag(firstLang));
            } catch (Exception e) {
                // ignore
            }
        }
    }

}
