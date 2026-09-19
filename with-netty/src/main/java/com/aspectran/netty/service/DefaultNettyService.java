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

    /**
     * Returns the Netty context bound to this service.
     * @return the Netty context
     */
    public NettyContext getNettyContext() {
        return nettyContext;
    }

    /**
     * Sets the Netty context bound to this service.
     * @param nettyContext the Netty context
     */
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
                String location;
                if (StringUtils.hasLength(activity.getReverseContextPath())) {
                    location = activity.getReverseContextPath() + requestNameWithTrailingSlash;
                } else {
                    location = requestNameWithTrailingSlash;
                }
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

    private void sendError(
            @NonNull ChannelHandlerContext ctx,
            @Nullable FullHttpRequest request,
            @NonNull HttpResponseStatus status,
            @Nullable String msg) {
        sendError(ctx, request, status, msg, null);
    }

    private void sendError(
            @NonNull ChannelHandlerContext ctx,
            @Nullable FullHttpRequest request,
            @NonNull HttpResponseStatus status,
            @Nullable String msg,
            @Nullable String retryAfter) {
        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.append("code", status.code());
            tsb.append("message", msg);
            if (retryAfter != null) {
                tsb.append("retryAfter", retryAfter);
            }
            logger.debug(tsb.toString());
        }
        ByteBuf content = (msg != null ? Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8) : Unpooled.EMPTY_BUFFER);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        if (msg != null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        }
        if (retryAfter != null) {
            response.headers().set(HttpHeaderNames.RETRY_AFTER, retryAfter);
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
        if (isProxyAddressForwarding()) {
            return WebUtils.getReverseContextPath(request.headers().get(HttpHeaders.X_FORWARDED_PATH), defaultContextPath);
        }
        return defaultContextPath;
    }

    @NonNull
    private String getRemoteAddr(@NonNull ChannelHandlerContext ctx, @NonNull FullHttpRequest request) {
        if (isProxyAddressForwarding()) {
            String forwardedFor = request.headers().get(HttpHeaders.X_FORWARDED_FOR);
            String remoteAddr = WebUtils.parseForwardedFor(forwardedFor);
            if (remoteAddr != null) {
                return remoteAddr;
            }
        }
        String fallbackRemoteAddr = null;
        SocketAddress address = ctx.channel().remoteAddress();
        if (address instanceof InetSocketAddress inetSocketAddress) {
            fallbackRemoteAddr = inetSocketAddress.getAddress().getHostAddress();
        } else if (address != null) {
            fallbackRemoteAddr = address.toString();
        }
        return (fallbackRemoteAddr != null ? fallbackRemoteAddr : "127.0.0.1");
    }

    /**
     * Checks if the service is currently paused and, if so, sends a 503 Service Unavailable response.
     * @param ctx the channel handler context
     * @param request the current HTTP request
     * @return true if the service is paused, false otherwise
     */
    private boolean checkPaused(@NonNull ChannelHandlerContext ctx, @NonNull FullHttpRequest request) {
        // A value of 0L means the service is active.
        // A value of -1L means the service is paused indefinitely.
        // A value of -2L means the service is not yet started.
        // Any other positive value is the time in milliseconds until the service is paused.
        if (pauseTimeout != 0L) {
            // If the service is not yet started, wait for it to start.
            // This is necessary because a request can come in before the service is fully initialized.
            if (pauseTimeout == -2L) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is not yet started, waiting for it to start...", getServiceName());
                }
                while (pauseTimeout == -2L) {
                    try {
                        // Poll every 100ms to see if the state has changed.
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Interrupted while waiting for service to start", e);
                        sendError(ctx, request, HttpResponseStatus.SERVICE_UNAVAILABLE,
                                "Service is starting. Please try again in a moment.");
                        return true;
                    }
                }
                // If the service has started successfully, pauseTimeout will be 0L.
                // In this case, we can proceed with the request.
                if (pauseTimeout == 0L) {
                    return false;
                }
                // If the service state changes to paused (-1L) during startup,
                // fall through to the next check.
            }

            // Check if the service is paused (indefinitely or temporarily).
            // This check is separate from the one above to handle the race condition where
            // the service is paused while it is starting up.
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is paused, so did not respond to requests", getServiceName());
                }
                String msg = "Service is temporarily unavailable. Please try again later.";
                String retryAfter = null;
                if (pauseTimeout > 0L) {
                    long remainingMillis = pauseTimeout - System.currentTimeMillis();
                    if (remainingMillis > 0) {
                        long remainingSeconds = remainingMillis / 1000L;
                        if (remainingSeconds > 0) {
                            retryAfter = String.valueOf(remainingSeconds);
                        }
                        msg = "Service is temporarily unavailable. Please try again in " +
                                DurationUtils.toHumanReadableMillis(remainingMillis) + ".";
                    }
                }
                sendError(ctx, request, HttpResponseStatus.SERVICE_UNAVAILABLE, msg, retryAfter);
                return true;
            } else {
                // If a temporary pause has expired, reset the timeout and allow requests.
                pauseTimeout = 0L;
            }
        }
        return false;
    }

}
