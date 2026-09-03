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
import com.aspectran.netty.server.NettyListenerConfig;
import com.aspectran.netty.server.handler.encoding.NettyEncodingHandler;
import com.aspectran.netty.server.handler.logging.PathBasedLoggingGroupHandler;
import com.aspectran.netty.server.websocket.NettyWebSocketConfig;
import com.aspectran.netty.service.DefaultNettyService;
import com.aspectran.netty.service.NettyService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Netty {@link ChannelInitializer} implementation for configuring the HTTP pipeline.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyListenerConfig listenerConfig;

    private final NettyContextRouter contextRouter;

    private final ExecutorService requestExecutor;

    private final NettyResourceHandler resourceHandler;

    private final NettyAccessLogHandler accessLogHandler;

    private final PathBasedLoggingGroupHandler loggingGroupHandler;

    private final NettyEncodingHandler encodingHandler;

    private final NettyWebSocketConfig webSocketConfig;

    private final int maxContentLength;

    private final boolean contentCompression;

    private final int idleTimeout;

    private final boolean proxyAddressForwarding;

    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            PathBasedLoggingGroupHandler loggingGroupHandler,
            NettyEncodingHandler encodingHandler,
            NettyWebSocketConfig webSocketConfig,
            int maxContentLength,
            boolean contentCompression,
            int idleTimeout,
            boolean proxyAddressForwarding) {
        this.listenerConfig = listenerConfig;
        this.contextRouter = contextRouter;
        this.requestExecutor = requestExecutor;
        this.resourceHandler = resourceHandler;
        this.accessLogHandler = accessLogHandler;
        this.loggingGroupHandler = loggingGroupHandler;
        this.encodingHandler = encodingHandler;
        this.webSocketConfig = webSocketConfig;
        this.maxContentLength = (maxContentLength > 0 ? maxContentLength : 10 * 1024 * 1024);
        this.contentCompression = contentCompression;
        this.idleTimeout = Math.max(0, idleTimeout);
        this.proxyAddressForwarding = proxyAddressForwarding;
    }

    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            PathBasedLoggingGroupHandler loggingGroupHandler,
            NettyEncodingHandler encodingHandler,
            NettyWebSocketConfig webSocketConfig,
            int maxContentLength,
            boolean contentCompression,
            int idleTimeout) {
        this(listenerConfig, contextRouter, requestExecutor, resourceHandler, accessLogHandler, loggingGroupHandler, encodingHandler, webSocketConfig, maxContentLength, contentCompression, idleTimeout, false);
    }

    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            PathBasedLoggingGroupHandler loggingGroupHandler,
            NettyEncodingHandler encodingHandler,
            NettyWebSocketConfig webSocketConfig,
            int maxContentLength,
            boolean contentCompression) {
        this(listenerConfig, contextRouter, requestExecutor, resourceHandler, accessLogHandler, loggingGroupHandler, encodingHandler, webSocketConfig, maxContentLength, contentCompression, 0);
    }

    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            PathBasedLoggingGroupHandler loggingGroupHandler,
            NettyEncodingHandler encodingHandler,
            int maxContentLength,
            boolean contentCompression) {
        this(listenerConfig, contextRouter, requestExecutor, resourceHandler, accessLogHandler, loggingGroupHandler, encodingHandler, null, maxContentLength, contentCompression);
    }

    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            PathBasedLoggingGroupHandler loggingGroupHandler,
            int maxContentLength,
            boolean contentCompression) {
        this(listenerConfig, contextRouter, requestExecutor, resourceHandler, accessLogHandler, loggingGroupHandler, null, maxContentLength, contentCompression);
    }

    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            int maxContentLength,
            boolean contentCompression) {
        this(listenerConfig, contextRouter, requestExecutor, resourceHandler, accessLogHandler, null, maxContentLength, contentCompression);
    }

    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyService nettyService,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            int maxContentLength,
            boolean contentCompression) {
        this(listenerConfig, createContextRouter(nettyService), requestExecutor, resourceHandler, accessLogHandler, null, maxContentLength, contentCompression);
    }

    @NonNull
    private static NettyContextRouter createContextRouter(NettyService nettyService) {
        NettyContextRouter router = new NettyContextRouter();
        if (nettyService instanceof DefaultNettyService defaultNettyService) {
            router.addContext(new NettyContext(nettyService.getContextPath(), defaultNettyService));
        }
        return router;
    }

    @Override
    protected void initChannel(@NonNull SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        if (listenerConfig != null && listenerConfig.isSsl()) {
            SslContext sslContext = listenerConfig.getSslContext();
            if (sslContext == null) {
                sslContext = listenerConfig.buildSslContext();
            }
            p.addLast("ssl", sslContext.newHandler(ch.alloc()));
        }

        if (idleTimeout > 0) {
            p.addLast("idleState", new IdleStateHandler(idleTimeout, 0, 0, TimeUnit.MILLISECONDS));
        }

        p.addLast("codec", new HttpServerCodec());

        if (encodingHandler != null) {
            p.addLast("compressor", encodingHandler.createContentCompressor());
        } else if (contentCompression) {
            p.addLast("compressor", new HttpContentCompressor());
        }

        p.addLast("aggregator", new HttpObjectAggregator(maxContentLength));
        p.addLast("chunkedWriter", new ChunkedWriteHandler());

        if (loggingGroupHandler != null) {
            p.addLast("loggingGroup", loggingGroupHandler);
        }

        if (accessLogHandler != null) {
            if (proxyAddressForwarding) {
                accessLogHandler.setProxyAddressForwarding(true);
            }
            p.addLast("accessLog", accessLogHandler);
        }

        if (resourceHandler != null) {
            p.addLast("resource", resourceHandler);
        }

        p.addLast("handler", new NettyHttpHandler(contextRouter, requestExecutor, loggingGroupHandler, webSocketConfig, proxyAddressForwarding));
    }

}
