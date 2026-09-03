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
package com.aspectran.netty.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.component.session.MaxSessionsExceededException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreService;
import com.aspectran.netty.activity.NettyActivity;
import com.aspectran.netty.server.NettyContext;
import com.aspectran.utils.DurationUtils;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.thread.ThreadContextHelper;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.util.WebUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.aspectran.core.component.session.MaxSessionsExceededException.MAX_SESSIONS_EXCEEDED;

/**
 * Default implementation of the {@link NettyService} interface.
 * <p>Handles incoming {@link FullHttpRequest}s, executes {@link NettyActivity} instances,
 * and manages errors, trailing slash redirects, and paused server state.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class DefaultNettyService extends AbstractNettyService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNettyService.class);

    protected volatile long pauseTimeout = -2L;

    private NettyContext nettyContext;

    DefaultNettyService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    public NettyContext getNettyContext() {
        return nettyContext;
    }

    public void setNettyContext(NettyContext nettyContext) {
        this.nettyContext = nettyContext;
    }

    @Override
    public boolean service(@NonNull ChannelHandlerContext ctx, @NonNull FullHttpRequest request) throws IOException {
        if (checkPaused(ctx, request)) {
            return false;
        }

        String uri = request.uri();
        int queryIndex = uri.indexOf('?');
        String path = (queryIndex != -1 ? uri.substring(0, queryIndex) : uri);

        final String decodedPath;
        if (getUriDecoding() != null) {
            decodedPath = URLDecoder.decode(path, getUriDecoding());
        } else {
            decodedPath = path;
        }

        final String requestName;
        String contextPath = getContextPath();
        if (StringUtils.hasLength(contextPath)) {
            if (decodedPath.startsWith(contextPath)) {
                String relPath = decodedPath.substring(contextPath.length());
                requestName = (!relPath.startsWith("/") ? "/" + relPath : relPath);
            } else {
                requestName = decodedPath;
            }
        } else {
            requestName = decodedPath;
        }
        final MethodType requestMethod = MethodType.resolve(request.method().name(), MethodType.GET);
        final String reverseContextPath = getReverseContextPath(request, contextPath);

        if (logger.isDebugEnabled()) {
            logger.debug(getRequestInfo(ctx, request, reverseContextPath, requestName, requestMethod));
        }

        if (!isRequestAcceptable(requestName)) {
            sendError(ctx, request, HttpResponseStatus.NOT_FOUND, null);
            return false;
        }

        NettyActivity activity = new NettyActivity(this, ctx, request, reverseContextPath);
        activity.setRequestName(requestName);
        activity.setRequestMethod(requestMethod);
        try {
            activity.prepare();
        } catch (TransletNotFoundException e) {
            transletNotFound(activity);
            return false;
        } catch (Exception e) {
            sendError(activity, e);
            return false;
        }

        perform(activity);
        return true;
    }

    private void perform(NettyActivity activity) {
        ClassLoader origClassLoader = ThreadContextHelper.overrideClassLoader(getServiceClassLoader());
        try {
            activity.perform();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: {}", e.getMessage());
            }
        } catch (Exception e) {
            sendError(activity, e);
        } finally {
            ThreadContextHelper.restoreClassLoader(origClassLoader);
        }
    }

    private void transletNotFound(NettyActivity activity) {
        if (isTrailingSlashRedirect() &&
                activity.getRequestMethod() == MethodType.GET &&
                StringUtils.startsWith(activity.getRequestName(), ActivityContext.NAME_SEPARATOR_CHAR) &&
                !StringUtils.endsWith(activity.getRequestName(), ActivityContext.NAME_SEPARATOR_CHAR)) {
            String requestNameWithTrailingSlash = activity.getRequestName() + ActivityContext.NAME_SEPARATOR_CHAR;
            if (getActivityContext().getTransletRuleRegistry().contains(requestNameWithTrailingSlash, activity.getRequestMethod())) {
                String location = getContextPath() + requestNameWithTrailingSlash;
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY);
                response.headers().set(HttpHeaderNames.LOCATION, location);
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                HttpUtil.setContentLength(response, 0);
                activity.getChannelHandlerContext().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("No translet mapped for {}", activity.getFullRequestName());
        }
        sendError(activity.getChannelHandlerContext(), activity.getRequest(), HttpResponseStatus.NOT_FOUND, null);
    }

    private void sendError(@NonNull NettyActivity activity, Exception e) {
        Throwable t;
        if (activity.isExceptionRaised()) {
            t = activity.getRaisedException();
        } else {
            t = e;
        }
        Throwable cause = ExceptionUtils.getRootCause(t);
        logger.error("Error occurred while processing request: {}", activity.getFullRequestName(), t);
        if (activity.getChannelHandlerContext().channel().isActive()) {
            if (cause instanceof RequestMethodNotAllowedException) {
                sendError(activity.getChannelHandlerContext(), activity.getRequest(), HttpResponseStatus.METHOD_NOT_ALLOWED, null);
            } else if (cause instanceof SizeLimitExceededException) {
                sendError(activity.getChannelHandlerContext(), activity.getRequest(), HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, null);
            } else if (cause instanceof MaxSessionsExceededException) {
                sendError(activity.getChannelHandlerContext(), activity.getRequest(), HttpResponseStatus.SERVICE_UNAVAILABLE, MAX_SESSIONS_EXCEEDED);
            } else {
                sendError(activity.getChannelHandlerContext(), activity.getRequest(), HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
            }
        }
    }

    private void sendError(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status, String msg) {
        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.append("code", status.code());
            tsb.append("message", msg);
            logger.debug(tsb.toString());
        }
        ByteBuf content = (msg != null ? Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8) : Unpooled.EMPTY_BUFFER);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (msg != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        }
        HttpUtil.setContentLength(response, content.readableBytes());
        boolean keepAlive = request != null && HttpUtil.isKeepAlive(request) && status.code() < 400;
        if (keepAlive) {
            HttpUtil.setKeepAlive(response, true);
            ctx.writeAndFlush(response);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private boolean checkPaused(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L) {
                logger.warn("NettyService is not yet started");
                sendError(ctx, request, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
                return true;
            } else if (pauseTimeout == -2L) {
                logger.warn("NettyService is not available");
                sendError(ctx, request, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
                return true;
            } else if (pauseTimeout > 0L) {
                if (pauseTimeout >= System.currentTimeMillis()) {
                    logger.warn("NettyService is paused; Service will resume after {}",
                            DurationUtils.toHumanReadableMillis(pauseTimeout - System.currentTimeMillis()));
                    sendError(ctx, request, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
                    return true;
                } else {
                    pauseTimeout = 0L;
                }
            }
        }
        return false;
    }

    @NonNull
    private String getRequestInfo(
            @NonNull ChannelHandlerContext ctx,
            @NonNull FullHttpRequest request,
            String reverseContextPath,
            String requestName,
            MethodType requestMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestMethod).append(" ");
        if (StringUtils.hasLength(reverseContextPath)) {
            sb.append(reverseContextPath);
        }
        sb.append(requestName).append(" ");
        sb.append(request.protocolVersion().text()).append(" ");
        sb.append(getRemoteAddr(ctx, request));
        return sb.toString();
    }

    @Nullable
    private String getReverseContextPath(@NonNull FullHttpRequest request, String defaultContextPath) {
        return WebUtils.getReverseContextPath(request.headers().get(HttpHeaders.X_FORWARDED_PATH), defaultContextPath);
    }

    @NonNull
    private String getRemoteAddr(@NonNull ChannelHandlerContext ctx, @NonNull FullHttpRequest request) {
        String fallbackRemoteAddr = null;
        SocketAddress address = ctx.channel().remoteAddress();
        if (address instanceof InetSocketAddress inetSocketAddress) {
            fallbackRemoteAddr = inetSocketAddress.getAddress().getHostAddress();
        } else if (address != null) {
            fallbackRemoteAddr = address.toString();
        }
        String forwardedFor = (isProxyAddressForwarding() ? request.headers().get(HttpHeaders.X_FORWARDED_FOR) : null);
        String remoteAddr = WebUtils.getRemoteAddr(forwardedFor, fallbackRemoteAddr);
        return (remoteAddr != null ? remoteAddr : "127.0.0.1");
    }

}
