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
import com.aspectran.netty.server.websocket.NettyWebSocketHandler;
import com.aspectran.netty.server.websocket.NettyWebSocketListener;
import com.aspectran.netty.service.DefaultNettyService;
import com.aspectran.netty.service.NettyService;
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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

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

    private final NettyContextRouter contextRouter;

    private final ExecutorService requestExecutor;

    private final PathBasedLoggingGroupHandler loggingGroupHandler;

    public NettyHttpHandler(@NonNull NettyService nettyService, ExecutorService requestExecutor) {
        this(nettyService, requestExecutor, null);
    }

    public NettyHttpHandler(
            @NonNull NettyService nettyService,
            ExecutorService requestExecutor,
            PathBasedLoggingGroupHandler loggingGroupHandler) {
        super(false);
        this.contextRouter = new NettyContextRouter();
        if (nettyService instanceof DefaultNettyService defaultNettyService) {
            this.contextRouter.addContext(new NettyContext(nettyService.getContextPath(), defaultNettyService));
        }
        this.requestExecutor = requestExecutor;
        this.loggingGroupHandler = loggingGroupHandler;
    }

    public NettyHttpHandler(@NonNull NettyContextRouter contextRouter, ExecutorService requestExecutor) {
        this(contextRouter, requestExecutor, null);
    }

    public NettyHttpHandler(
            @NonNull NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            PathBasedLoggingGroupHandler loggingGroupHandler) {
        super(false);
        this.contextRouter = contextRouter;
        this.requestExecutor = requestExecutor;
        this.loggingGroupHandler = loggingGroupHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NonNull FullHttpRequest request) {
        request.retain();
        if (requestExecutor != null) {
            requestExecutor.execute(() -> processRequest(ctx, request));
        } else {
            processRequest(ctx, request);
        }
    }

    private void processRequest(ChannelHandlerContext ctx, @NonNull FullHttpRequest request) {
        String uri = request.uri();
        int queryIndex = uri.indexOf('?');
        String path = (queryIndex != -1 ? uri.substring(0, queryIndex) : uri);
        NettyContext context = contextRouter.match(path);

        String groupName = resolveLoggingGroup(path, context);
        if (groupName != null) {
            ChannelLoggingGroupHelper.setTo(ctx.channel(), groupName);
        } else {
            ChannelLoggingGroupHelper.setTo(ctx.channel(), null);
        }

        try {
            if (context == null) {
                sendNotFound(ctx, request);
                return;
            }

            if (isWebSocketUpgrade(request)) {
                String contextPath = context.getContextPath();
                String relativePath = path;
                if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
                    relativePath = path.substring(contextPath.length());
                }
                if (relativePath.isEmpty()) {
                    relativePath = "/";
                }
                NettyWebSocketListener endpoint = context.getWebSocketEndpoint(relativePath);
                if (endpoint != null) {
                    handleWebSocketHandshake(ctx, request, relativePath, endpoint);
                    return;
                }
            }

            if (context.getNettyService() == null) {
                sendNotFound(ctx, request);
                return;
            }

            if (context.getResourceHandler() != null && context.getResourceHandler().handle(ctx, request)) {
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
        if (groupName == null && contextRouter.getRootContext() != null) {
            groupName = contextRouter.getRootContext().getLoggingGroup();
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
            NettyWebSocketListener listener) {
        String wsLocation = getWebSocketLocation(request);
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                wsLocation, null, true, 65536);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }

        handshaker.handshake(ctx.channel(), request).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().eventLoop().execute(() -> {
                    DefaultNettyWebSocketSession session = new DefaultNettyWebSocketSession(
                            ctx.channel(), request.uri(), path, request.headers(), handshaker);

                    ChannelPipeline pipeline = ctx.pipeline();
                    if (pipeline.get("wsHandler") == null) {
                        pipeline.addBefore(ctx.name(), "wsFrameAggregator", new WebSocketFrameAggregator(65536));
                        pipeline.addBefore(ctx.name(), "wsHandler",
                                new NettyWebSocketHandler(session, listener, requestExecutor, handshaker));
                    }

                    String wsGroup = ChannelLoggingGroupHelper.get(ctx.channel());
                    Runnable openTask = () -> {
                        if (wsGroup != null) {
                            LoggingGroupHelper.set(wsGroup);
                        }
                        try {
                            listener.onOpen(session);
                        } catch (Exception e) {
                            logger.error("Error in WebSocket onOpen", e);
                            session.close(1011, "Internal error");
                        } finally {
                            if (wsGroup != null) {
                                LoggingGroupHelper.clear();
                            }
                        }
                    };
                    if (requestExecutor != null) {
                        requestExecutor.execute(openTask);
                    } else {
                        openTask.run();
                    }
                });
            } else {
                logger.warn("WebSocket handshake failed", future.cause());
            }
        });
    }

    @NonNull
    private String getWebSocketLocation(@NonNull FullHttpRequest req) {
        String host = req.headers().get(HttpHeaderNames.HOST);
        return "ws://" + (host != null ? host : "localhost") + req.uri();
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
    public void exceptionCaught(@NonNull ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Netty pipeline exception caught", cause);
        ctx.close();
    }

}
