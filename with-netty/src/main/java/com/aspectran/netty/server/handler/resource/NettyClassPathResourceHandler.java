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
package com.aspectran.netty.server.handler.resource;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedStream;
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A {@link NettyResourceHandler} implementation that serves static resources from the classpath.
 * <p>Supports ETag/If-Modified-Since caching headers, streaming transfer via {@link ChunkedStream},
 * and seamless integration with SSL and HTTP content compression.</p>
 *
 * <p>Created: 2026-09-03</p>
 */
@ChannelHandler.Sharable
public class NettyClassPathResourceHandler extends NettyResourceHandler implements ActivityContextAware {

    private ActivityContext activityContext;

    private ClassLoader classLoader;

    private String prefix = "";

    public NettyClassPathResourceHandler() {
        this("");
    }

    public NettyClassPathResourceHandler(String prefix) {
        setPrefix(prefix);
    }

    public NettyClassPathResourceHandler(ClassLoader classLoader, String prefix) {
        this.classLoader = classLoader;
        setPrefix(prefix);
    }

    public NettyClassPathResourceHandler(String prefix, String... includePatterns) {
        setPrefix(prefix);
        if (includePatterns != null && includePatterns.length > 0) {
            setPathPatterns(includePatterns, null);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            this.prefix = "";
        } else {
            String p = prefix.strip();
            if (p.startsWith("/")) {
                p = p.substring(1);
            }
            if (!p.isEmpty() && !p.endsWith("/")) {
                p = p + "/";
            }
            this.prefix = p;
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext activityContext) {
        this.activityContext = activityContext;
        if (this.classLoader == null) {
            this.classLoader = activityContext.getClassLoader();
        }
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, @NonNull FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            return false;
        }

        HttpMethod method = request.method();
        if (!HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method)) {
            return false;
        }

        String uri = request.uri();
        int queryIndex = uri.indexOf('?');
        String path = (queryIndex != -1 ? uri.substring(0, queryIndex) : uri);
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);

        if (getPathPatterns() != null && !getPathPatterns().matches(path)) {
            return false;
        }

        String sanitizedPath = sanitizePath(path);
        if (sanitizedPath == null) {
            return false;
        }
        if (sanitizedPath.startsWith("/")) {
            sanitizedPath = sanitizedPath.substring(1);
        }

        ClassLoader cl = resolveClassLoader();
        String resourcePath = prefix + sanitizedPath;
        URL resourceUrl = cl.getResource(resourcePath);

        // Try index files if path represents a directory or ends with '/'
        if (resourceUrl == null || resourcePath.endsWith("/")) {
            String[] indexFiles = getIndexFiles();
            if (indexFiles != null) {
                String dirPath = (resourcePath.endsWith("/") ? resourcePath : resourcePath + "/");
                for (String indexFile : indexFiles) {
                    URL indexUrl = cl.getResource(dirPath + indexFile);
                    if (indexUrl != null) {
                        resourceUrl = indexUrl;
                        resourcePath = dirPath + indexFile;
                        break;
                    }
                }
            }
        }

        if (resourceUrl == null) {
            return false;
        }

        URLConnection conn = resourceUrl.openConnection();
        long lastModified = conn.getLastModified();
        long contentLength = conn.getContentLengthLong();

        // Cache Validation (If-Modified-Since)
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (StringUtils.hasLength(ifModifiedSince) && lastModified > 0) {
            try {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
                long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
                long fileLastModifiedSeconds = lastModified / 1000;
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    sendNotModified(ctx, request);
                    return true;
                }
            } catch (Exception ignore) {
                // ignore parse failure
            }
        }

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        if (contentLength >= 0) {
            HttpUtil.setContentLength(response, contentLength);
        }
        setContentTypeHeader(response, resourcePath);
        setDateAndCacheHeaders(response, lastModified);

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            HttpUtil.setKeepAlive(response, true);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        if (HttpMethod.HEAD.equals(method)) {
            ChannelFuture future = ctx.writeAndFlush(response);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
            return true;
        }

        // Write the response headers
        ctx.write(response);

        // Write the body
        InputStream in = conn.getInputStream();
        ChannelFuture sendFileFuture;
        boolean hasCompressor = (ctx.pipeline().get(HttpContentCompressor.class) != null);
        if (hasCompressor || ctx.pipeline().get(SslHandler.class) != null) {
            sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(in)));
        } else {
            ctx.write(new ChunkedStream(in));
            sendFileFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }

        if (!keepAlive) {
            sendFileFuture.addListener(ChannelFutureListener.CLOSE);
        }
        return true;
    }

    @NonNull
    private ClassLoader resolveClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        if (activityContext != null && activityContext.getClassLoader() != null) {
            return activityContext.getClassLoader();
        }
        return ClassUtils.getDefaultClassLoader();
    }

}
