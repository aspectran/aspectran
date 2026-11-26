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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.aspectran.netty.server.handler.accesslog.NettyAccessLogHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link NettyAccessLogHandler}.
 */
class NettyAccessLogHandlerTest {

    private static final String TEST_CATEGORY = "test.netty.accesslog";

    private Logger testLogger;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        testLogger = (Logger) LoggerFactory.getLogger(TEST_CATEGORY);
        testLogger.setLevel(Level.INFO);
        listAppender = new ListAppender<>();
        listAppender.start();
        testLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        if (testLogger != null && listAppender != null) {
            testLogger.detachAppender(listAppender);
            listAppender.stop();
        }
    }

    @Test
    void testDefaultCombinedFormat() {
        NettyAccessLogHandler handler = new NettyAccessLogHandler(TEST_CATEGORY);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/hello", Unpooled.EMPTY_BUFFER);
        request.headers().set(HttpHeaderNames.USER_AGENT, "TestAgent/1.0");
        request.headers().set(HttpHeaderNames.REFERER, "http://localhost/");

        channel.writeInbound(request);

        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(content));
        HttpUtil.setContentLength(response, content.length);

        channel.writeOutbound(response);

        assertFalse(listAppender.list.isEmpty(), "An access log entry should have been recorded");
        String logMessage = listAppender.list.get(0).getFormattedMessage();
        assertTrue(logMessage.contains("\"GET /hello HTTP/1.1\" 200 11 \"http://localhost/\" \"TestAgent/1.0\""),
                "Log should contain combined format elements: " + logMessage);
    }

    @Test
    void testUndertowCompatibleFormat() {
        NettyAccessLogHandler handler = new NettyAccessLogHandler();
        handler.setCategory(TEST_CATEGORY);
        handler.setFormatString("%t %a %{i,X-Forwarded-For} %{c,JSESSIONID} \"%r\" %s %b \"%{i,Referer}\" \"%{i,User-Agent}\"");
        assertEquals(TEST_CATEGORY, handler.getCategory());

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, "/api/test", Unpooled.EMPTY_BUFFER);
        request.headers().set("X-Forwarded-For", "203.0.113.195");
        request.headers().set(HttpHeaderNames.COOKIE, "JSESSIONID=session-xyz-123; other=value");
        request.headers().set(HttpHeaderNames.REFERER, "https://example.com/login");
        request.headers().set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 Edge");

        channel.writeInbound(request);

        byte[] content = "Response Data".getBytes(StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.CREATED, Unpooled.wrappedBuffer(content));
        HttpUtil.setContentLength(response, content.length);

        channel.writeOutbound(response);

        assertFalse(listAppender.list.isEmpty(), "An access log entry should have been recorded");
        String logMessage = listAppender.list.getFirst().getFormattedMessage();
        assertTrue(logMessage.contains("203.0.113.195 session-xyz-123 \"POST /api/test HTTP/1.1\" 201 13 \"https://example.com/login\" \"Mozilla/5.0 Edge\""),
                "Log should match Undertow format output: " + logMessage);
    }

    @Test
    void testMissingHeadersAndCookies() {
        NettyAccessLogHandler handler = new NettyAccessLogHandler();
        handler.setCategory(TEST_CATEGORY);
        handler.setFormatString("%a %{i,X-Forwarded-For} %{c,JSESSIONID} \"%r\" %s %b \"%{i,Referer}\" \"%{i,User-Agent}\"");

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/simple", Unpooled.EMPTY_BUFFER);

        channel.writeInbound(request);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER);

        channel.writeOutbound(response);

        assertFalse(listAppender.list.isEmpty());
        String logMessage = listAppender.list.getFirst().getFormattedMessage();
        assertTrue(logMessage.contains("- - \"GET /simple HTTP/1.1\" 404 - \"-\" \"-\""),
                "Missing values should be replaced with '-': " + logMessage);
    }

    @Test
    void testSessionIdToken() {
        NettyAccessLogHandler handler = new NettyAccessLogHandler();
        handler.setCategory(TEST_CATEGORY);
        handler.setFormatString("%t %a %{i,X-Forwarded-For} %S \"%r\" %s %b \"%{i,Referer}\" \"%{i,User-Agent}\"");

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, "/api/test", Unpooled.EMPTY_BUFFER);
        request.headers().set("X-Forwarded-For", "203.0.113.195");
        request.headers().set(HttpHeaderNames.REFERER, "https://example.com/login");
        request.headers().set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 Edge");

        channel.writeInbound(request);

        // Simulate session ID attached by NettySessionAdapter or context
        com.aspectran.netty.server.session.ChannelSessionHelper.setSessionId(channel, "session-custom-999");

        byte[] content = "Response Data".getBytes(StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.CREATED, Unpooled.wrappedBuffer(content));
        HttpUtil.setContentLength(response, content.length);

        channel.writeOutbound(response);

        assertFalse(listAppender.list.isEmpty(), "An access log entry should have been recorded");
        String logMessage = listAppender.list.getFirst().getFormattedMessage();
        assertTrue(logMessage.contains("203.0.113.195 session-custom-999 \"POST /api/test HTTP/1.1\" 201 13 \"https://example.com/login\" \"Mozilla/5.0 Edge\""),
                "Log should contain session ID from %S: " + logMessage);
    }

    @Test
    void testMissingSessionId() {
        NettyAccessLogHandler handler = new NettyAccessLogHandler();
        handler.setCategory(TEST_CATEGORY);
        handler.setFormatString("%S");

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test", Unpooled.EMPTY_BUFFER);

        channel.writeInbound(request);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);

        channel.writeOutbound(response);

        assertFalse(listAppender.list.isEmpty());
        String logMessage = listAppender.list.getFirst().getFormattedMessage();
        assertEquals("-", logMessage);
    }

    @Test
    void testDurationElementFormatting() {
        NettyAccessLogHandler handler = new NettyAccessLogHandler();
        handler.setCategory(TEST_CATEGORY);
        handler.setFormatString("%D %T");

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test", Unpooled.EMPTY_BUFFER);

        channel.writeInbound(request);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);

        channel.writeOutbound(response);

        assertFalse(listAppender.list.isEmpty());
        String logMessage = listAppender.list.getFirst().getFormattedMessage();
        String[] parts = logMessage.split(" ");
        assertEquals(2, parts.length);
        // %D is millis (e.g. 0, 1, etc.)
        assertTrue(parts[0].matches("\\d+"), "Expected integer millis: " + parts[0]);
        // %T is seconds with 3 decimals (e.g. 0.000, 0.001, etc.)
        assertTrue(parts[1].matches("\\d+\\.\\d{3}"), "Expected seconds with 3 decimals: " + parts[1]);
    }

    @Test
    void testDisabledLoggingBypass() {
        testLogger.setLevel(Level.WARN); // disable INFO

        NettyAccessLogHandler handler = new NettyAccessLogHandler();
        handler.setCategory(TEST_CATEGORY);
        handler.setFormatString("%t %r %s");

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/bypass", Unpooled.EMPTY_BUFFER);

        channel.writeInbound(request);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);

        channel.writeOutbound(response);

        assertTrue(listAppender.list.isEmpty(), "No access log should be recorded when INFO is disabled");
    }

    @Test
    void testIsLoggingEnabled() {
        assertFalse(NettyAccessLogHandler.isLoggingEnabled((io.netty.channel.Channel) null));
        assertFalse(NettyAccessLogHandler.isLoggingEnabled((io.netty.channel.ChannelHandlerContext) null));

        EmbeddedChannel channelWithoutHandler = new EmbeddedChannel();
        assertFalse(NettyAccessLogHandler.isLoggingEnabled(channelWithoutHandler));

        NettyAccessLogHandler handler = new NettyAccessLogHandler(TEST_CATEGORY);
        EmbeddedChannel channelWithHandler = new EmbeddedChannel(handler);
        assertTrue(NettyAccessLogHandler.isLoggingEnabled(channelWithHandler));

        testLogger.setLevel(Level.WARN);
        assertFalse(NettyAccessLogHandler.isLoggingEnabled(channelWithHandler));
    }

}
