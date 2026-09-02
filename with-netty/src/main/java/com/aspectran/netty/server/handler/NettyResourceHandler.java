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
package com.aspectran.netty.server.handler;

import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import com.aspectran.web.support.http.MediaType;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Netty inbound handler for serving static files with high performance.
 * <p>Supports zero-copy file transfer via {@link DefaultFileRegion} when cleartext HTTP
 * is used, and chunked transfer via {@link ChunkedFile} when SSL/TLS is enabled.
 * Also handles HTTP 304 Not Modified caching headers and include/exclude wildcard patterns.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
@ChannelHandler.Sharable
public class NettyResourceHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    private static final int HTTP_CACHE_SECONDS = 60;

    private static final String[] DEFAULT_INDEX_FILES = new String[] { "index.html", "index.htm" };

    private final File baseDir;

    private volatile IncludeExcludeWildcardPatterns pathPatterns;

    private String[] indexFiles = DEFAULT_INDEX_FILES;

    public NettyResourceHandler(File baseDir) {
        this(baseDir, (String[])null);
    }

    public NettyResourceHandler(String basePath) {
        this(new File(basePath));
    }

    public NettyResourceHandler(File baseDir, String... includePatterns) {
        super(false);
        this.baseDir = baseDir;
        if (includePatterns != null && includePatterns.length > 0) {
            setPathPatterns(includePatterns, null);
        }
    }

    public NettyResourceHandler(String basePath, String... includePatterns) {
        this(new File(basePath), includePatterns);
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setPathPatterns(String[] includePatterns, String[] excludePatterns) {
        this.pathPatterns = IncludeExcludeWildcardPatterns.of(includePatterns, excludePatterns, '/');
    }

    public void setPathPatterns(IncludeExcludeWildcardPatterns pathPatterns) {
        this.pathPatterns = pathPatterns;
    }

    /**
     * Returns the index files to serve when a directory is requested.
     * @return the array of index file names
     */
    public String[] getIndexFiles() {
        return indexFiles;
    }

    /**
     * Sets the index files to serve when a directory is requested.
     * @param indexFiles the index file names, or {@code null} to disable directory index resolution
     */
    public void setIndexFiles(String... indexFiles) {
        this.indexFiles = indexFiles;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!handle(ctx, request)) {
            ctx.fireChannelRead(request.retain());
        }
    }

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

        if (pathPatterns != null && !pathPatterns.matches(path)) {
            return false;
        }

        if (baseDir == null || !baseDir.exists() || !baseDir.isDirectory()) {
            return false;
        }

        // Sanitize path and prevent directory traversal
        String sanitizedPath = sanitizePath(path);
        if (sanitizedPath == null) {
            return false;
        }

        File file = new File(baseDir, sanitizedPath);
        try {
            if (!file.getCanonicalPath().startsWith(baseDir.getCanonicalPath())) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        if (file.isHidden() || !file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File indexFile = findIndexFile(file);
            if (indexFile != null) {
                file = indexFile;
            } else {
                return false;
            }
        }

        if (!file.isFile()) {
            return false;
        }

        // Cache Validation (If-Modified-Since)
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (StringUtils.hasLength(ifModifiedSince)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx, request);
                return true;
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            return false;
        }

        long fileLength = raf.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpUtil.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            HttpUtil.setKeepAlive(response, true);
        }

        // Write the initial line and the header
        ctx.write(response);

        // If HEAD request, do not write content
        if (HttpMethod.HEAD.equals(method)) {
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            raf.close();
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
            return true;
        }

        // Write the content
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) != null) {
            // Cannot use zero-copy with SSL
            sendFileFuture = ctx.write(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)), ctx.newProgressivePromise());
            lastContentFuture = sendFileFuture;
        } else {
            // Zero-copy file transfer
            sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }

        if (!keepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
        return true;
    }

    @Nullable
    private static String sanitizePath(String path) {
        path = path.replace('/', File.separatorChar);
        while (path.startsWith(File.separator)) {
            path = path.substring(1);
        }
        if (path.contains(File.separator + '.') ||
                path.contains('.' + File.separator) ||
                path.startsWith(".") || path.endsWith(".")) {
            return null;
        }
        return path;
    }

    @Nullable
    private File findIndexFile(File dir) {
        if (indexFiles != null && indexFiles.length > 0) {
            for (String indexFileName : indexFiles) {
                File indexFile = new File(dir, indexFileName);
                if (indexFile.isFile() && !indexFile.isHidden()) {
                    return indexFile;
                }
            }
        }
        return null;
    }

    private static void sendNotModified(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
        setDateHeader(response);
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            HttpUtil.setKeepAlive(response, true);
            ctx.writeAndFlush(response);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void setDateHeader(@NonNull FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }

    private static void setDateAndCacheHeaders(@NonNull HttpResponse response, @NonNull File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private static void setContentTypeHeader(HttpResponse response, @NonNull File file) {
        String mimeType = null;
        try {
            mimeType = java.nio.file.Files.probeContentType(file.toPath());
        } catch (IOException ignore) {
            // ignore
        }
        if (mimeType == null) {
            String ext = FilenameUtils.getExtension(file.getName());
            if (ext != null) {
                mimeType = switch (ext.toLowerCase()) {
                    case "html", "htm" -> "text/html; charset=UTF-8";
                    case "css" -> "text/css; charset=UTF-8";
                    case "js" -> "application/javascript; charset=UTF-8";
                    case "json" -> "application/json; charset=UTF-8";
                    case "png" -> "image/png";
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "gif" -> "image/gif";
                    case "svg" -> "image/svg+xml";
                    case "ico" -> "image/x-icon";
                    case "txt" -> "text/plain; charset=UTF-8";
                    case "xml" -> "application/xml; charset=UTF-8";
                    default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
                };
            } else {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType);
    }

}
