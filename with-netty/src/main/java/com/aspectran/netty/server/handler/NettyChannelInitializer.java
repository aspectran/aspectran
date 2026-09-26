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

import com.aspectran.netty.server.NettyContextRouter;
import com.aspectran.netty.server.NettyListenerConfig;
import com.aspectran.netty.server.handler.accesslog.NettyAccessLogHandler;
import com.aspectran.netty.server.handler.encoding.NettyEncodingHandler;
import com.aspectran.netty.server.handler.logging.PathBasedLoggingGroupHandler;
import com.aspectran.netty.server.handler.resource.NettyResourceHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AsciiString;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Netty {@link ChannelInitializer} implementation for configuring the HTTP/1.1 and HTTP/2 pipelines.
 *
 * <p>Created: 2026-09-02</p>
 */
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final String SSL_HANDLER_NAME = "ssl";

    public static final String ALPN_HANDLER_NAME = "alpn";

    public static final String IDLE_STATE_HANDLER_NAME = "idleState";

    public static final String HTTP2_UPGRADE_HANDLER_NAME = "h2cUpgrade";

    public static final String HTTP2_CODEC_HANDLER_NAME = "http2Codec";

    public static final String HTTP2_MULTIPLEX_HANDLER_NAME = "http2Multiplex";

    public static final String HTTP2_STREAM_CODEC_HANDLER_NAME = "http2StreamCodec";

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

    private final int maxContentLength;

    private final boolean contentCompression;

    private final int idleTimeout;

    private final boolean proxyAddressForwarding;

    /**
     * Constructs a new {@code NettyChannelInitializer} with full configuration options.
     * @param listenerConfig the listener configuration
     * @param contextRouter the context router for dispatching requests to NettyContexts
     * @param requestExecutor the executor service for asynchronous request dispatching
     * @param resourceHandler the static resource handler
     * @param accessLogHandler the access log handler
     * @param loggingGroupHandler the path-based logging group handler
     * @param encodingHandler the character encoding handler
     * @param maxContentLength the maximum allowable HTTP request body size in bytes
     * @param contentCompression whether HTTP response content compression (gzip/deflate) is enabled
     * @param idleTimeout the HTTP connection idle timeout in milliseconds
     * @param proxyAddressForwarding whether forwarded headers (X-Forwarded-*) should be honored
     */
    public NettyChannelInitializer(
            NettyListenerConfig listenerConfig,
            NettyContextRouter contextRouter,
            ExecutorService requestExecutor,
            NettyResourceHandler resourceHandler,
            NettyAccessLogHandler accessLogHandler,
            PathBasedLoggingGroupHandler loggingGroupHandler,
            NettyEncodingHandler encodingHandler,
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
            int maxContentLength,
            boolean contentCompression,
            int idleTimeout) {
        this(listenerConfig, contextRouter, requestExecutor, resourceHandler, accessLogHandler, loggingGroupHandler, encodingHandler, maxContentLength, contentCompression, idleTimeout, false);
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
        this(listenerConfig, contextRouter, requestExecutor, resourceHandler, accessLogHandler, loggingGroupHandler, encodingHandler, maxContentLength, contentCompression, 0);
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

        boolean isSsl = (listenerConfig != null && listenerConfig.isSsl());
        boolean isHttp2 = (listenerConfig != null && listenerConfig.isHttp2());

        if (isSsl) {
            SslContext sslContext = listenerConfig.getSslContext();
            if (sslContext == null) {
                sslContext = listenerConfig.buildSslContext();
            }
            p.addLast(SSL_HANDLER_NAME, sslContext.newHandler(ch.alloc()));

            if (idleTimeout > 0) {
                p.addLast(IDLE_STATE_HANDLER_NAME, new IdleStateHandler(idleTimeout, 0, 0, TimeUnit.MILLISECONDS));
            }

            if (isHttp2) {
                p.addLast(ALPN_HANDLER_NAME, new Http2OrHttp11NegotiationHandler());
            } else {
                p.addLast(HTTP_CODEC_HANDLER_NAME, createHttpServerCodec());
                configureHttpPipeline(p);
            }
        } else {
            if (idleTimeout > 0) {
                p.addLast(IDLE_STATE_HANDLER_NAME, new IdleStateHandler(idleTimeout, 0, 0, TimeUnit.MILLISECONDS));
            }

            if (isHttp2) {
                HttpServerCodec sourceCodec = createHttpServerCodec();
                Http2FrameCodec frameCodec = createHttp2FrameCodec();
                Http2MultiplexHandler multiplexHandler = createHttp2MultiplexHandler();

                HttpServerUpgradeHandler.UpgradeCodecFactory upgradeCodecFactory = protocol -> {
                    if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                        return new Http2ServerUpgradeCodec(frameCodec, multiplexHandler);
                    }
                    return null;
                };
                HttpServerUpgradeHandler upgradeHandler = new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory);
                CleartextHttp2ServerUpgradeHandler cleartextHandler = new CleartextHttp2ServerUpgradeHandler(
                        sourceCodec, upgradeHandler, new PriorKnowledgeHandler(frameCodec, multiplexHandler));

                p.addLast(HTTP2_UPGRADE_HANDLER_NAME, cleartextHandler);
                configureHttpPipeline(p);
            } else {
                p.addLast(HTTP_CODEC_HANDLER_NAME, createHttpServerCodec());
                configureHttpPipeline(p);
            }
        }
    }

    @NonNull
    private HttpServerCodec createHttpServerCodec() {
        if (listenerConfig != null) {
            return new HttpServerCodec(
                    listenerConfig.getMaxInitialLineLength(),
                    listenerConfig.getMaxHeaderSize(),
                    listenerConfig.getMaxChunkSize());
        }
        return new HttpServerCodec();
    }

    private Http2FrameCodec createHttp2FrameCodec() {
        Http2FrameCodecBuilder builder = Http2FrameCodecBuilder.forServer();
        if (listenerConfig != null) {
            Http2Settings settings = listenerConfig.getHttp2Settings();
            if (settings != null) {
                builder.initialSettings(settings);
            }
            if (listenerConfig.getHttp2GracefulShutdownTimeoutMillis() > 0) {
                builder.gracefulShutdownTimeoutMillis(listenerConfig.getHttp2GracefulShutdownTimeoutMillis());
            }
        }
        return builder.build();
    }

    private void configureHttpPipeline(@NonNull ChannelPipeline p) {
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

        p.addLast(HTTP_HANDLER_NAME, new NettyHttpHandler(contextRouter, requestExecutor, loggingGroupHandler, proxyAddressForwarding));
    }

    @NonNull
    private Http2MultiplexHandler createHttp2MultiplexHandler() {
        return new Http2MultiplexHandler(new Http2StreamChannelInitializer());
    }

    private final class Http2OrHttp11NegotiationHandler extends ApplicationProtocolNegotiationHandler {

        Http2OrHttp11NegotiationHandler() {
            super(ApplicationProtocolNames.HTTP_1_1);
        }

        @Override
        protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
            if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                ctx.pipeline().addLast(HTTP2_CODEC_HANDLER_NAME, createHttp2FrameCodec());
                ctx.pipeline().addLast(HTTP2_MULTIPLEX_HANDLER_NAME, createHttp2MultiplexHandler());
            } else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                ctx.pipeline().addLast(HTTP_CODEC_HANDLER_NAME, createHttpServerCodec());
                configureHttpPipeline(ctx.pipeline());
            } else {
                throw new IllegalStateException("Unsupported protocol: " + protocol);
            }
        }
    }

    private final class Http2StreamChannelInitializer extends ChannelInitializer<Http2StreamChannel> {

        @Override
        protected void initChannel(@NonNull Http2StreamChannel ch) {
            ChannelPipeline p = ch.pipeline();
            p.addLast(HTTP2_STREAM_CODEC_HANDLER_NAME, new Http2StreamFrameToHttpObjectCodec(true));
            configureHttpPipeline(p);
        }
    }

    private static final class PriorKnowledgeHandler extends ChannelInboundHandlerAdapter {

        private final Http2FrameCodec frameCodec;
        private final Http2MultiplexHandler multiplexHandler;

        PriorKnowledgeHandler(Http2FrameCodec frameCodec, Http2MultiplexHandler multiplexHandler) {
            this.frameCodec = frameCodec;
            this.multiplexHandler = multiplexHandler;
        }

        @Override
        public void handlerAdded(@NonNull ChannelHandlerContext ctx) {
            ctx.pipeline()
                    .addBefore(ctx.name(), HTTP2_CODEC_HANDLER_NAME, frameCodec)
                    .addBefore(ctx.name(), HTTP2_MULTIPLEX_HANDLER_NAME, multiplexHandler)
                    .remove(this);
        }
    }

}
