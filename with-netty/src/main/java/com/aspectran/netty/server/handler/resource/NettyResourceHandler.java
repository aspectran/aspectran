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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.wildcard.IncludeExcludeParameters;
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
 * Netty inbound handler for serving static files from the filesystem with high performance.
 * <p>Key features include:</p>
 * <ul>
 *   <li><b>Zero-Copy File Transfer</b>: Uses {@link DefaultFileRegion} for cleartext HTTP
 *       to transfer files directly from disk to network buffers via OS kernel copy.</li>
 *   <li><b>Chunked Streaming</b>: Uses {@link ChunkedFile} and {@link HttpChunkedInput}
 *       when SSL/TLS ({@link SslHandler}) or HTTP content compression ({@link HttpContentCompressor})
 *       is active in the channel pipeline.</li>
 *   <li><b>Security &amp; Traversal Prevention</b>: Sanitizes paths to prevent directory traversal
 *       attacks and blocks access to sensitive or protected directories such as {@code /WEB-INF/}
 *       and {@code /META-INF/} by default.</li>
 *   <li><b>HTTP Caching &amp; Conditional Requests</b>: Handles {@code If-Modified-Since} validation
 *       with {@code 304 Not Modified} responses, and sets {@code Cache-Control}, {@code Expires},
 *       and {@code Last-Modified} headers.</li>
 *   <li><b>Path Pattern Filtering</b>: Supports include and exclude wildcard patterns configured
 *       via string arrays, {@link IncludeExcludeParameters}, or APON format.</li>
 *   <li><b>Directory Index Resolution</b>: Automatically serves configured index files (e.g.,
 *       {@code index.html}, {@code index.htm}) when a directory is requested.</li>
 *   <li><b>Application Integration</b>: Implements {@link ApplicationAdapterAware} to automatically
 *       resolve relative base paths against the application root directory.</li>
 * </ul>
 *
 * <p>Created: 2026-09-02</p>
 */
@ChannelHandler.Sharable
public class NettyResourceHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements ApplicationAdapterAware {

    protected static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    private static final int HTTP_CACHE_SECONDS = 60;

    private static final String[] DEFAULT_INDEX_FILES = new String[] { "index.html", "index.htm" };
    private static final String[] PROTECTED_DIRECTORIES = new String[] { "WEB-INF", "META-INF" };

    private volatile ApplicationAdapter applicationAdapter;

    private volatile String contextPath;

    private volatile File baseDir;

    private volatile IncludeExcludeWildcardPatterns pathPatterns;

    private volatile String[] indexFiles = DEFAULT_INDEX_FILES;

    private volatile boolean blockProtectedDirectories = true;

    /**
     * Creates a new instance with auto-release disabled for unhandled requests.
     * <p>Typically used for bean configuration or by subclasses that do not serve
     * directly from a static local filesystem directory.</p>
     */
    public NettyResourceHandler() {
        super(false);
    }

    /**
     * Creates a new resource handler serving static files from the specified base directory.
     * @param baseDir the base directory containing static files
     */
    public NettyResourceHandler(File baseDir) {
        this(baseDir, (String[])null);
    }

    /**
     * Creates a new resource handler serving static files from the specified base directory,
     * restricted to matching include wildcard patterns.
     * @param baseDir the base directory containing static files
     * @param includePatterns wildcard patterns matching request paths to serve
     */
    public NettyResourceHandler(File baseDir, String... includePatterns) {
        super(false);
        this.baseDir = baseDir;
        if (includePatterns != null && includePatterns.length > 0) {
            setPathPatterns(includePatterns, null);
        }
    }

    /**
     * Returns the application adapter.
     * @return the application adapter, or {@code null} if not set
     */
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    /**
     * Returns the context path prefix associated with this handler.
     * @return the context path prefix
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path prefix to strip from incoming request URIs.
     * @param contextPath the context path prefix
     */
    public void setContextPath(String contextPath) {
        this.contextPath = (contextPath != null ? contextPath : "");
    }

    /**
     * Returns the base directory where static files reside.
     * @return the base directory, or {@code null} if not set
     */
    public File getBaseDir() {
        return baseDir;
    }

    /**
     * Sets the base directory from which static files are served.
     * @param baseDir the base directory
     */
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Sets the base directory path from which static files are served.
     * <p>If an {@link ApplicationAdapter} is configured, relative paths are resolved
     * against the application root directory via {@link ApplicationAdapter#getRealPath(String)}.
     * Otherwise, the path is resolved directly against the local filesystem.</p>
     * @param basePath the base directory path
     * @throws IllegalArgumentException if {@code basePath} is null or empty
     */
    public void setBasePath(String basePath) {
        if (!StringUtils.hasText(basePath)) {
            throw new IllegalArgumentException("Base path must not be null or empty");
        }
        if (applicationAdapter != null) {
            setBaseDir(applicationAdapter.getRealPath(basePath).toFile());
        } else {
            setBaseDir(new File(basePath));
        }
    }

    /**
     * Configures include and exclude URL wildcard patterns for resource serving.
     * @param includePatterns patterns matching request paths to include
     * @param excludePatterns patterns matching request paths to exclude
     */
    public void setPathPatterns(String[] includePatterns, String[] excludePatterns) {
        this.pathPatterns = IncludeExcludeWildcardPatterns.of(includePatterns, excludePatterns, '/');
    }

    /**
     * Configures include and exclude URL wildcard patterns from an {@link IncludeExcludeParameters} bean.
     * @param includeExcludeParameters the parameter object defining include and exclude patterns
     */
    public void setPathPatterns(IncludeExcludeParameters includeExcludeParameters) {
        if (includeExcludeParameters != null && includeExcludeParameters.hasPatterns()) {
            this.pathPatterns = IncludeExcludeWildcardPatterns.of(includeExcludeParameters, '/');
        } else {
            this.pathPatterns = null;
        }
    }

    /**
     * Configures include and exclude URL wildcard patterns from an APON string.
     * <p>For example:</p>
     * <pre>{@code
     * +: /static/**
     * -: /static/secret/**
     * }</pre>
     * @param apon the APON text containing '+' (include) and '-' (exclude) patterns
     * @throws AponParseException if the APON text cannot be parsed
     */
    public void setPathPatterns(String apon) throws AponParseException {
        if (StringUtils.hasText(apon)) {
            setPathPatterns(new IncludeExcludeParameters(apon));
        } else {
            this.pathPatterns = null;
        }
    }

    /**
     * Sets the pre-compiled include/exclude wildcard patterns.
     * @param pathPatterns the wildcard patterns to evaluate against request paths
     */
    public void setPathPatterns(IncludeExcludeWildcardPatterns pathPatterns) {
        this.pathPatterns = pathPatterns;
    }

    /**
     * Returns the include/exclude wildcard patterns applied to request paths.
     * @return the wildcard patterns, or {@code null} if none are configured
     */
    public IncludeExcludeWildcardPatterns getPathPatterns() {
        return pathPatterns;
    }

    /**
     * Returns whether access to protected directories (e.g. {@code /WEB-INF/}, {@code /META-INF/})
     * is blocked by default.
     * @return {@code true} if protected directories are blocked; {@code false} otherwise
     */
    public boolean isBlockProtectedDirectories() {
        return blockProtectedDirectories;
    }

    /**
     * Sets whether access to protected directories (e.g. {@code /WEB-INF/}, {@code /META-INF/})
     * should be blocked.
     * @param blockProtectedDirectories {@code true} to block protected directories; {@code false} to allow
     */
    public void setBlockProtectedDirectories(boolean blockProtectedDirectories) {
        this.blockProtectedDirectories = blockProtectedDirectories;
    }

    /**
     * Checks if the given path targets a protected directory such as {@code /WEB-INF/} or {@code /META-INF/}.
     * <p>The check is case-insensitive and inspects leading, intermediate, and trailing path segments.</p>
     * @param path the request path to inspect
     * @return {@code true} if the path targets a protected directory and blocking is enabled; {@code false} otherwise
     */
    protected boolean isProtectedPath(String path) {
        if (!blockProtectedDirectories || path == null) {
            return false;
        }
        String normalized = (File.separatorChar != '/' ? path.replace(File.separatorChar, '/') : path);
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        for (String dir : PROTECTED_DIRECTORIES) {
            String dirLower = dir.toLowerCase(Locale.ROOT);
            if (lower.startsWith(dirLower)) {
                if (lower.length() == dirLower.length() || lower.charAt(dirLower.length()) == '/') {
                    return true;
                }
            }
            if (lower.contains("/" + dirLower + "/") || lower.endsWith("/" + dirLower)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index files to serve when a directory is requested.
     * @return an array of index file names
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
            ctx.fireChannelRead(request);
        } else {
            request.release();
        }
    }

    /**
     * Attempts to serve a static resource matching the request URI.
     * @param ctx the channel handler context
     * @param request the full HTTP request
     * @return {@code true} if the request was handled and a resource was served; {@code false} otherwise
     * @throws Exception if an error occurs during resource retrieval or transfer
     */
    public boolean handle(ChannelHandlerContext ctx, @NonNull FullHttpRequest request) throws Exception {
        return handle(ctx, request, null);
    }

    /**
     * Attempts to serve a static resource matching the request URI or a pre-resolved relative path.
     * <p>The handling process follows these steps:</p>
     * <ol>
     *   <li>Verifies successful HTTP decoding and ensures the request method is {@code GET} or {@code HEAD}.</li>
     *   <li>Resolves the relative path and checks against protected directories and configured path patterns.</li>
     *   <li>Sanitizes the path and prevents directory traversal attacks outside the {@code baseDir}.</li>
     *   <li>Resolves directory requests to a matching index file if available.</li>
     *   <li>Validates the {@code If-Modified-Since} header for an HTTP 304 Not Modified cache response.</li>
     *   <li>Transfers the file using zero-copy ({@link DefaultFileRegion}) for cleartext HTTP,
     *       or chunked streaming ({@link ChunkedFile}) when SSL or HTTP compression is present.</li>
     * </ol>
     * @param ctx the channel handler context
     * @param request the full HTTP request
     * @param relativePath the relative path to resolve against the base directory,
     *                     or {@code null} to extract and normalize from the request URI
     * @return {@code true} if the request was handled and a resource was served; {@code false} otherwise
     * @throws Exception if an error occurs during resource retrieval or transfer
     */
    public boolean handle(
            ChannelHandlerContext ctx,
            @NonNull FullHttpRequest request,
            @Nullable String relativePath) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            return false;
        }

        HttpMethod method = request.method();
        if (!HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method)) {
            return false;
        }

        String path = resolvePath(request, relativePath);
        if (isProtectedPath(path)) {
            return false;
        }
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

        try {
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
            if (ctx.pipeline().get(SslHandler.class) != null ||
                    ctx.pipeline().get(HttpContentCompressor.class) != null) {
                // Cannot use zero-copy with SSL or HTTP content compression
                sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)), ctx.newProgressivePromise());
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
        } catch (Throwable t) {
            try {
                raf.close();
            } catch (Exception ignore) {
                // ignore
            }
            throw t;
        }
    }

    /**
     * Resolves the request path to a normalized relative path against the configured context path.
     * <p>Strips any query string, removes the matching context path prefix, decodes URL-encoded
     * characters in UTF-8, and guarantees a leading slash.</p>
     * @param request the HTTP request
     * @param relativePath the pre-calculated relative path, or {@code null}
     * @return the resolved and normalized relative path
     */
    @NonNull
    protected String resolvePath(@NonNull FullHttpRequest request, @Nullable String relativePath) {
        String path;
        if (relativePath != null) {
            path = relativePath;
        } else {
            String uri = request.uri();
            int queryIndex = uri.indexOf('?');
            path = (queryIndex != -1 ? uri.substring(0, queryIndex) : uri);
            if (StringUtils.hasLength(contextPath)) {
                if (path.equals(contextPath) || path.startsWith(contextPath + "/")) {
                    path = path.substring(contextPath.length());
                }
            }
        }
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    /**
     * Sanitizes a requested path to prevent directory traversal attacks and system path exploits.
     * <p>Replaces slash separators with the local file separator, strips leading separators,
     * and rejects any relative navigation containing {@code ".."} or hidden dot segments.</p>
     * @param path the raw normalized request path
     * @return the sanitized filesystem-safe path, or {@code null} if the path is invalid or attempts traversal
     */
    @Nullable
    protected static String sanitizePath(String path) {
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

    /**
     * Searches for a valid index file within the specified directory matching the configured index file list.
     * @param dir the directory to inspect
     * @return the first existing, non-hidden index file found, or {@code null} if none match
     */
    @Nullable
    protected File findIndexFile(File dir) {
        if (indexFiles != null) {
            for (String indexFileName : indexFiles) {
                File indexFile = new File(dir, indexFileName);
                if (indexFile.isFile() && !indexFile.isHidden()) {
                    return indexFile;
                }
            }
        }
        return null;
    }

    /**
     * Sends an HTTP 304 Not Modified response indicating that the cached resource remains valid.
     * @param ctx the channel handler context
     * @param request the incoming HTTP request
     */
    protected static void sendNotModified(ChannelHandlerContext ctx, FullHttpRequest request) {
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

    /**
     * Sets the {@code Date} HTTP header on the response using the standard HTTP-date GMT format.
     * @param response the HTTP response
     */
    protected static void setDateHeader(@NonNull FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets standard HTTP caching headers ({@code Date}, {@code Expires}, {@code Cache-Control},
     * and {@code Last-Modified}) on the response based on the file's last modified timestamp.
     * @param response the HTTP response
     * @param fileToCache the file being served
     */
    protected static void setDateAndCacheHeaders(@NonNull HttpResponse response, @NonNull File fileToCache) {
        setDateAndCacheHeaders(response, fileToCache.lastModified());
    }

    /**
     * Sets standard HTTP caching headers ({@code Date}, {@code Expires}, {@code Cache-Control},
     * and {@code Last-Modified}) on the response.
     * @param response the HTTP response
     * @param lastModified the last modified timestamp in milliseconds, or 0 if unknown
     */
    protected static void setDateAndCacheHeaders(@NonNull HttpResponse response, long lastModified) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        if (lastModified > 0) {
            response.headers().set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(lastModified)));
        }
    }

    /**
     * Determines and sets the {@code Content-Type} header on the response for the specified file.
     * <p>First attempts to probe the MIME type using {@link java.nio.file.Files#probeContentType(java.nio.file.Path)};
     * if undetermined, falls back to matching the file extension.</p>
     * @param response the HTTP response
     * @param file the file to inspect
     */
    protected static void setContentTypeHeader(HttpResponse response, @NonNull File file) {
        String mimeType = null;
        try {
            mimeType = java.nio.file.Files.probeContentType(file.toPath());
        } catch (IOException ignore) {
            // ignore
        }
        if (mimeType != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType);
        } else {
            setContentTypeHeader(response, file.getName());
        }
    }

    /**
     * Sets the {@code Content-Type} header on the response based on the file extension of the specified filename.
     * @param response the HTTP response
     * @param filename the file name whose extension is used to determine MIME type
     */
    protected static void setContentTypeHeader(HttpResponse response, @NonNull String filename) {
        String mimeType;
        String ext = FilenameUtils.getExtension(filename);
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
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeType);
    }

}
