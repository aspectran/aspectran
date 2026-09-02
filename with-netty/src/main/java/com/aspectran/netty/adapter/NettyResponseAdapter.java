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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.response.RedirectTarget;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.netty.activity.NettyActivity;
import com.aspectran.utils.Assert;
import com.aspectran.utils.PathUtils;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;
import com.aspectran.web.support.util.SendRedirectBasedOnXForwardedProtocol;
import com.aspectran.web.support.util.WebUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.aspectran.web.adapter.HttpServletResponseAdapter.PROXY_PROTOCOL_AWARE_SETTING_NAME;

/**
 * An adapter that wraps Netty's response mechanism, exposing it as a
 * {@link com.aspectran.core.adapter.ResponseAdapter} for the Aspectran framework.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyResponseAdapter extends AbstractResponseAdapter {

    private final ChannelHandlerContext ctx;

    private final FullHttpRequest request;

    private final NettyActivity activity;

    private final HttpHeaders headers = new DefaultHttpHeaders();

    private HttpResponseStatus status = HttpResponseStatus.OK;

    private String contentType;

    private String charset;

    private ByteBuf buffer;

    private ByteBufOutputStream outputStream;

    private Writer writer;

    private ResponseState responseState = ResponseState.NONE;

    private String reservedRedirectLocation;

    private boolean committed;

    public NettyResponseAdapter(ChannelHandlerContext ctx, FullHttpRequest request, NettyActivity activity) {
        super(ctx);
        this.ctx = ctx;
        this.request = request;
        this.activity = activity;
    }

    public HttpHeaders getNettyHeaders() {
        return headers;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers.getAll(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.names().stream().map(CharSequence::toString).collect(Collectors.toSet());
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.contains(name);
    }

    @Override
    public void setHeader(String name, String value) {
        Assert.notNull(name, "Header name must not be null");
        if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(name)) {
            setContentType(value);
        } else {
            headers.set(name, value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        Assert.notNull(name, "Header name must not be null");
        if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(name) &&
                !headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
            setContentType(value);
        } else {
            headers.add(name, value);
        }
    }

    @Override
    public String getEncoding() {
        if (charset != null) {
            return charset;
        }
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public void setEncoding(String encoding) {
        this.charset = encoding;
        if (contentType != null) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, getContentType());
        }
    }

    @Override
    public String getContentType() {
        if (contentType != null) {
            if (charset != null) {
                return contentType + "; charset=" + charset;
            } else {
                return contentType;
            }
        }
        return null;
    }

    @Override
    public void setContentType(String contentType) {
        if (contentType == null) {
            return;
        }
        MediaType type = MediaType.parseMediaType(contentType);
        String parsedCharset = type.getParameter(MediaType.PARAM_CHARSET);
        this.contentType = type.getType() + '/' + type.getSubtype();
        if (parsedCharset != null) {
            this.charset = parsedCharset;
        }
        if (this.charset != null) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, this.contentType + "; charset=" + this.charset);
        } else {
            headers.set(HttpHeaderNames.CONTENT_TYPE, this.contentType);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        Assert.state(responseState != ResponseState.WRITER,
                "Cannot call getOutputStream(), getWriter() already called");
        responseState = ResponseState.STREAM;
        if (outputStream == null) {
            ensureBuffer();
            outputStream = new ByteBufOutputStream(buffer);
        }
        return outputStream;
    }

    @Override
    public Writer getWriter() throws IOException {
        if (writer == null) {
            Assert.state(responseState != ResponseState.STREAM,
                    "Cannot call getWriter(), getOutputStream() already called");
            responseState = ResponseState.WRITER;
            ensureBuffer();
            outputStream = new ByteBufOutputStream(buffer);
            writer = new OutputStreamWriter(outputStream, getEncoding());
        }
        return writer;
    }

    private void ensureBuffer() {
        if (buffer == null) {
            buffer = ctx.alloc().buffer();
        }
    }

    @Override
    public void commit() throws IOException {
        if (committed) {
            return;
        }
        committed = true;

        if (reservedRedirectLocation != null) {
            headers.set(HttpHeaderNames.LOCATION, reservedRedirectLocation);
            reservedRedirectLocation = null;
        }

        if (writer != null) {
            writer.flush();
        } else if (outputStream != null) {
            outputStream.flush();
        }

        ByteBuf content = (buffer != null ? buffer : Unpooled.EMPTY_BUFFER);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        response.headers().set(headers);

        if (contentType != null && !response.headers().contains(HttpHeaderNames.CONTENT_TYPE)) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType());
        }

        boolean keepAlive = HttpUtil.isKeepAlive(request) && status.code() < 400;
        HttpUtil.setContentLength(response, content.readableBytes());

        if (keepAlive) {
            HttpUtil.setKeepAlive(response, true);
            ctx.writeAndFlush(response);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ChannelFuture future = ctx.writeAndFlush(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void reset() {
        if (committed) {
            throw new IllegalStateException("Cannot reset response; already committed");
        }
        if (buffer != null) {
            buffer.clear();
        }
        headers.clear();
        status = HttpResponseStatus.OK;
        responseState = ResponseState.NONE;
        writer = null;
        outputStream = null;
    }

    @Override
    public void redirect(String location) {
        setStatus(HttpStatus.FOUND.value());
        if (isAbsoluteUrl(location)) {
            reservedRedirectLocation = location;
        } else {
            boolean proxyProtocolAware = Boolean.parseBoolean(activity.getSetting(PROXY_PROTOCOL_AWARE_SETTING_NAME));
            if (proxyProtocolAware) {
                Translet translet = activity.getTranslet();
                String locationForwarded = SendRedirectBasedOnXForwardedProtocol.getLocationForwarded(translet, location);
                if (locationForwarded != null) {
                    headers.set(HttpHeaderNames.LOCATION, locationForwarded);
                    return;
                }
            }
            String realPath;
            if (location.startsWith("/")) {
                realPath = location;
            } else {
                realPath = PathUtils.cleanPath("/" + location);
            }
            reservedRedirectLocation = realPath;
        }
    }

    @Override
    public RedirectTarget redirect(RedirectRule redirectRule) {
        RedirectTarget redirectTarget = WebUtils.getRedirectTarget(redirectRule, activity);
        String path = redirectTarget.getLocation();
        redirect(path);
        return redirectTarget;
    }

    @Override
    public String transformPath(String path) {
        return path;
    }

    private static boolean isAbsoluteUrl(String location) {
        return (location != null && (location.startsWith("http://") || location.startsWith("https://")));
    }

    public void setReservedRedirectLocation(String reservedRedirectLocation) {
        this.reservedRedirectLocation = reservedRedirectLocation;
    }

    @Override
    public int getStatus() {
        return status.code();
    }

    @Override
    public void setStatus(int status) {
        this.status = HttpResponseStatus.valueOf(status);
    }

    private enum ResponseState {
        NONE,
        STREAM,
        WRITER
    }

}
