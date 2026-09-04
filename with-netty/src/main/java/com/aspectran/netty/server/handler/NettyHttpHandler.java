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

import com.aspectran.netty.server.NettyContext;
import com.aspectran.netty.server.NettyContextRouter;
import com.aspectran.netty.server.handler.logging.ChannelLoggingGroupHelper;
import com.aspectran.netty.server.handler.logging.PathBasedLoggingGroupHandler;
import com.aspectran.netty.server.websocket.DefaultNettyWebSocketSession;
import com.aspectran.netty.server.websocket.NettyWebSocketConfig;
import com.aspectran.netty.server.websocket.NettyWebSocketHandler;
import com.aspectran.netty.server.websocket.NettyWebSocketListener;
import com.aspectran.netty.server.websocket.WebSocketEndpointMatch;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.logging.LoggingGroupHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_HOST;
import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_PROTO;
import static com.aspectran.web.support.http.HttpHeaders.X_FORWARDED_SSL;

/**
 * Netty inbound handler that dispatches incoming {@link FullHttpRequest}s
 * to the appropriate {@link NettyContext} using {@link NettyContextRouter}
 * and an executor service (such as Java 21 Virtual Threads).
 *
 * <p>Created: 2026-09-02</p>
 */
@ChannelHandler.Sharable
public class NettyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpHandler.class);

    public static final String WS_IDLE_STATE_HANDLER_NAME = "wsIdleState";

    public static final String WS_FRAME_AGGREGATOR_HANDLER_NAME = "wsFrameAggregator";

    public static final String WS_HANDLER_NAME = "wsHandler";

    private final NettyContextRouter contextRouter;

    private final ExecutorService requestExecutor;

    private final PathBasedLoggingGroupHandler loggingGroupHandler;

    private final boolean proxyAddressForwarding;

    /**
     * Constructs a new {@code NettyHttpHandler}.
     * @param contextRouter the context router for dispatching requests
     * @param requestExecutor the executor service for asynchronous request dispatching
     */
    public NettyHttpHandler(@NonNull NettyContextRouter contextRouter, ExecutorService requestExecutor) {
        this(contextRouter, requestExecutor, null, false);
    }

    /**
     * Constructs a new {@code NettyHttpHandler} with a logging group handler.
     * @param contextRouter the context router for dispatching requests
     * @param requestExecutor the executor service for asynchronous request dispatching
     * @param loggingGroupHandler the logging group handler
     */
    public NettyHttpHandler(
            @NonNull NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            PathBasedLoggingGroupHandler loggingGroupHandler) {
        this(contextRouter, requestExecutor, loggingGroupHandler, false);
    }

    /**
     * Constructs a new {@code NettyHttpHandler} with full configuration.
     * @param contextRouter the context router for dispatching requests
     * @param requestExecutor the executor service for asynchronous request dispatching
     * @param loggingGroupHandler the logging group handler
     * @param proxyAddressForwarding whether forwarded headers (X-Forwarded-*) are honored
     */
    public NettyHttpHandler(
            @NonNull NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            PathBasedLoggingGroupHandler loggingGroupHandler,
            boolean proxyAddressForwarding) {
        super(false);
        this.contextRouter = contextRouter;
        this.requestExecutor = requestExecutor;
        this.loggingGroupHandler = loggingGroupHandler;
        this.proxyAddressForwarding = proxyAddressForwarding;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NonNull FullHttpRequest request) {
        if (requestExecutor != null) {
            requestExecutor.execute(() -> processRequest(ctx, request));
        } else {
            processRequest(ctx, request);
        }
    }

    private void processRequest(@NonNull ChannelHandlerContext ctx, @NonNull FullHttpRequest request) {
        String uri = request.uri();
        int queryIndex = uri.indexOf('?');
        String path = (queryIndex != -1 ? uri.substring(0, queryIndex) : uri);
        NettyContext context = contextRouter.match(path);

        String groupName = resolveLoggingGroup(path, context);
        ChannelLoggingGroupHelper.setTo(ctx.channel(), groupName);

        try {
            if (context == null) {
                sendNotFound(ctx, request);
                return;
            }

            String contextPath = context.getContextPath();
            String relativePath = path;
            if (StringUtils.hasLength(contextPath)) {
                if (path.equals(contextPath) || path.startsWith(contextPath + "/")) {
                    relativePath = path.substring(contextPath.length());
                }
            }
            if (relativePath.isEmpty()) {
                relativePath = "/";
            }

            if (isWebSocketUpgrade(request)) {
                WebSocketEndpointMatch match = context.matchWebSocketEndpoint(relativePath);
                if (match != null) {
                    handleWebSocketHandshake(ctx, request, relativePath, match, context);
                    return;
                }
            }

            if (context.getNettyService() == null) {
                sendNotFound(ctx, request);
                return;
            }

            if (context.getResourceHandler() != null && context.getResourceHandler().handle(ctx, request, relativePath)) {
                return;
            }

            boolean handled = context.getNettyService().service(ctx, request);
            if (!handled && ctx.channel().isActive()) {
                sendNotFound(ctx, request);
            }
        } catch (Exception e) {
            logger.error("Unexpected error during request processing", e);
            if (ctx.channel().isActive()) {
                sendInternalServerError(ctx, request);
            }
        } finally {
            LoggingGroupHelper.clear();
            request.release();
        }
    }

    private String resolveLoggingGroup(String path, NettyContext context) {
        String groupName = null;
        if (loggingGroupHandler != null) {
            groupName = loggingGroupHandler.resolveGroupName(path);
        }
        if (groupName == null && context != null) {
            groupName = context.getLoggingGroup();
        }
        return groupName;
    }

    private boolean isWebSocketUpgrade(@NonNull FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        return headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)
                && headers.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true);
    }

    private void handleWebSocketHandshake(
            ChannelHandlerContext ctx,
            FullHttpRequest request,
            String path,
            @NonNull WebSocketEndpointMatch match,
            @Nullable NettyContext context) {
        NettyWebSocketListener listener = match.getListener();
        String wsLocation = getWebSocketLocation(request, ctx);
        NettyWebSocketConfig webSocketConfig = (context != null ? context.getWebSocketConfig() : null);
        int maxFramePayloadLength = (webSocketConfig != null ? webSocketConfig.getMaxFramePayloadLength() : 65536);
        int maxMessageSize = (webSocketConfig != null ? webSocketConfig.getMaxMessageSize() : 65536);
        boolean allowExtensions = (webSocketConfig == null || webSocketConfig.isAllowExtensions());
        String subprotocols = (webSocketConfig != null ? webSocketConfig.getSubprotocols() : null);

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                wsLocation, subprotocols, allowExtensions, maxFramePayloadLength);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }

        handshaker.handshake(ctx.channel(), request).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().eventLoop().execute(() -> {
                    DefaultNettyWebSocketSession session = new DefaultNettyWebSocketSession(
                            ctx.channel(), request.uri(), path, request.headers(), handshaker,
                            match.getPathParameters(), webSocketConfig);

                    String wsGroup = ChannelLoggingGroupHelper.get(ctx.channel());
                    if (wsGroup != null) {
                        LoggingGroupHelper.set(wsGroup);
                    }
                    try {
                        listener.onOpen(session);
                    } catch (Exception e) {
                        logger.error("Error in WebSocket onOpen", e);
                        session.close(1011, "Internal error");
                        return;
                    } finally {
                        if (wsGroup != null) {
                            LoggingGroupHelper.clear();
                        }
                    }

                    ChannelPipeline pipeline = ctx.pipeline();
                    if (pipeline.get(WS_HANDLER_NAME) == null) {
                        if (pipeline.get(NettyChannelInitializer.IDLE_STATE_HANDLER_NAME) != null) {
                            pipeline.remove(NettyChannelInitializer.IDLE_STATE_HANDLER_NAME);
                        }
                        if (webSocketConfig != null && webSocketConfig.getMaxIdleTimeout() > 0) {
                            int idleSeconds = (int)Math.max(1, webSocketConfig.getMaxIdleTimeout() / 1000);
                            pipeline.addBefore(ctx.name(), WS_IDLE_STATE_HANDLER_NAME, new IdleStateHandler(0, 0, idleSeconds));
                        }
                        pipeline.addBefore(ctx.name(), WS_FRAME_AGGREGATOR_HANDLER_NAME, new WebSocketFrameAggregator(maxMessageSize));
                        pipeline.addBefore(ctx.name(), WS_HANDLER_NAME,
                                new NettyWebSocketHandler(session, listener, requestExecutor, handshaker));
                    }
                });
            } else {
                logger.error("WebSocket handshake failed", future.cause());
                ctx.close();
            }
        });
    }

    @NonNull
    private String getWebSocketLocation(@NonNull FullHttpRequest req, ChannelHandlerContext ctx) {
        String host = null;
        String scheme = "ws";
        if (proxyAddressForwarding) {
            String forwardedHost = req.headers().get(X_FORWARDED_HOST);
            if (StringUtils.hasText(forwardedHost)) {
                int idx = forwardedHost.indexOf(',');
                host = (idx != -1 ? forwardedHost.substring(0, idx).trim() : forwardedHost.trim());
            }
            String proto = req.headers().get(X_FORWARDED_PROTO);
            if ("https".equalsIgnoreCase(proto) || "wss".equalsIgnoreCase(proto) ||
                    "on".equalsIgnoreCase(req.headers().get(X_FORWARDED_SSL))) {
                scheme = "wss";
            }
        }
        if (host == null) {
            host = req.headers().get(HttpHeaderNames.HOST);
        }
        if (scheme.equals("ws") && ctx.pipeline().get(SslHandler.class) != null) {
            scheme = "wss";
        }
        return scheme + "://" + (host != null ? host : "localhost") + req.uri();
    }

    private void sendNotFound(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        HttpUtil.setContentLength(response, 0);
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            HttpUtil.setKeepAlive(response, true);
            ctx.writeAndFlush(response);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void sendInternalServerError(@NonNull ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        HttpUtil.setContentLength(response, 0);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void userEventTriggered(@NonNull ChannelHandlerContext ctx, @NonNull Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            if (idleStateEvent.state() == IdleState.READER_IDLE || idleStateEvent.state() == IdleState.ALL_IDLE) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Closing idle HTTP connection: {}", ctx.channel());
                }
                ctx.close();
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(@NonNull ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Netty pipeline exception caught", cause);
        ctx.close();
    }

}
