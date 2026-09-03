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
import com.aspectran.netty.server.handler.accesslog.NettyAccessLogHandler;
import com.aspectran.netty.server.handler.encoding.NettyEncodingHandler;
import com.aspectran.netty.server.handler.logging.PathBasedLoggingGroupHandler;
import com.aspectran.netty.server.handler.resource.NettyResourceHandler;
import com.aspectran.netty.server.websocket.NettyWebSocketConfig;
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

    public static final String SSL_HANDLER_NAME = "ssl";

    public static final String IDLE_STATE_HANDLER_NAME = "idleState";

    public static final String HTTP_CODEC_HANDLER_NAME = "codec";

    public static final String COMPRESSOR_HANDLER_NAME = "compressor";

    public static final String AGGREGATOR_HANDLER_NAME = "aggregator";

    public static final String CHUNKED_WRITER_HANDLER_NAME = "chunkedWriter";

    public static final String LOGGING_GROUP_HANDLER_NAME = "loggingGroup";

    public static final String ACCESS_LOG_HANDLER_NAME = "accessLog";

    public static final String RESOURCE_HANDLER_NAME = "resource";

    public static final String HTTP_HANDLER_NAME = "handler";

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

    @Override
    protected void initChannel(@NonNull SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        if (listenerConfig != null && listenerConfig.isSsl()) {
            SslContext sslContext = listenerConfig.getSslContext();
            if (sslContext == null) {
                sslContext = listenerConfig.buildSslContext();
            }
            p.addLast(SSL_HANDLER_NAME, sslContext.newHandler(ch.alloc()));
        }

        if (idleTimeout > 0) {
            p.addLast(IDLE_STATE_HANDLER_NAME, new IdleStateHandler(idleTimeout, 0, 0, TimeUnit.MILLISECONDS));
        }

        p.addLast(HTTP_CODEC_HANDLER_NAME, new HttpServerCodec());

        if (encodingHandler != null) {
            p.addLast(COMPRESSOR_HANDLER_NAME, encodingHandler.createContentCompressor());
        } else if (contentCompression) {
            p.addLast(COMPRESSOR_HANDLER_NAME, new HttpContentCompressor());
        }

        p.addLast(AGGREGATOR_HANDLER_NAME, new HttpObjectAggregator(maxContentLength));
        p.addLast(CHUNKED_WRITER_HANDLER_NAME, new ChunkedWriteHandler());

        if (loggingGroupHandler != null) {
            p.addLast(LOGGING_GROUP_HANDLER_NAME, loggingGroupHandler);
        }

        if (accessLogHandler != null) {
            if (proxyAddressForwarding) {
                accessLogHandler.setProxyAddressForwarding(true);
            }
            p.addLast(ACCESS_LOG_HANDLER_NAME, accessLogHandler);
        }

        if (resourceHandler != null) {
            p.addLast(RESOURCE_HANDLER_NAME, resourceHandler);
        }

        p.addLast(HTTP_HANDLER_NAME, new NettyHttpHandler(contextRouter, requestExecutor, loggingGroupHandler, webSocketConfig, proxyAddressForwarding));
    }

}
