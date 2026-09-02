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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Netty handler that logs HTTP access events in NCSA Combined Log Format.
 *
 * <p>Created: 2026-09-02</p>
 */
@ChannelHandler.Sharable
public class NettyAccessLogHandler extends ChannelDuplexHandler {

    private static final Logger defaultLogger = LoggerFactory.getLogger(NettyAccessLogHandler.class);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);

    private static final AttributeKey<Long> START_TIME_KEY = AttributeKey.valueOf("netty.access.start_time");

    private static final AttributeKey<String> CLIENT_IP_KEY = AttributeKey.valueOf("netty.access.client_ip");

    private static final AttributeKey<String> REQUEST_LINE_KEY = AttributeKey.valueOf("netty.access.request_line");

    private static final AttributeKey<String> REFERER_KEY = AttributeKey.valueOf("netty.access.referer");

    private static final AttributeKey<String> USER_AGENT_KEY = AttributeKey.valueOf("netty.access.user_agent");

    private final Logger logger;

    public NettyAccessLogHandler() {
        this(defaultLogger);
    }

    public NettyAccessLogHandler(Logger logger) {
        this.logger = (logger != null ? logger : defaultLogger);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest request) {
            ctx.channel().attr(START_TIME_KEY).set(System.currentTimeMillis());

            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            String clientIp = (remoteAddress instanceof InetSocketAddress inetAddress)
                    ? inetAddress.getAddress().getHostAddress() : "127.0.0.1";
            ctx.channel().attr(CLIENT_IP_KEY).set(clientIp);

            String requestLine = request.method().name() + " " + request.uri() + " " + request.protocolVersion().text();
            ctx.channel().attr(REQUEST_LINE_KEY).set(requestLine);

            String referer = request.headers().get(HttpHeaderNames.REFERER);
            ctx.channel().attr(REFERER_KEY).set(referer != null ? referer : "-");

            String userAgent = request.headers().get(HttpHeaderNames.USER_AGENT);
            ctx.channel().attr(USER_AGENT_KEY).set(userAgent != null ? userAgent : "-");
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse response) {
            Long startTime = ctx.channel().attr(START_TIME_KEY).getAndSet(null);
            if (startTime != null && logger.isInfoEnabled()) {
                long duration = System.currentTimeMillis() - startTime;
                String clientIp = ctx.channel().attr(CLIENT_IP_KEY).get();
                String requestLine = ctx.channel().attr(REQUEST_LINE_KEY).get();
                String referer = ctx.channel().attr(REFERER_KEY).get();
                String userAgent = ctx.channel().attr(USER_AGENT_KEY).get();

                int statusCode = response.status().code();
                long contentLength = HttpUtil.getContentLength(response, -1L);
                String contentLengthStr = (contentLength >= 0 ? String.valueOf(contentLength) : "-");
                String timestamp = ZonedDateTime.now().format(DATE_FORMATTER);

                String logEntry = String.format("%s - - [%s] \"%s\" %d %s \"%s\" \"%s\" %dms",
                        clientIp, timestamp, requestLine, statusCode, contentLengthStr, referer, userAgent, duration);
                logger.info(logEntry);
            }
        }
        super.write(ctx, msg, promise);
    }

}
