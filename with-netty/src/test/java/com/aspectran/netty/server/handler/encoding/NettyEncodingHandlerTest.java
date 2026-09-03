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
package com.aspectran.netty.server.handler.encoding;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link NettyEncodingHandler}, {@link ContentEncodingPredicates},
 * and {@link SelectiveHttpContentCompressor}.
 */
class NettyEncodingHandlerTest {

    private NettyEncodingHandler handler;

    @BeforeEach
    void setUp() {
        ContentEncodingPredicates predicate1 = new ContentEncodingPredicates();
        predicate1.setMediaTypes(new String[] { "text/html", "text/css" });

        ContentEncodingPredicates predicate2 = new ContentEncodingPredicates();
        predicate2.setContentSizeLargerThan(32);
        predicate2.setMediaTypes(new String[] { "application/json" });

        handler = new NettyEncodingHandler();
        handler.setEncodingProviders("gzip");
        handler.setEncodingPredicates(predicate1, predicate2);
    }

    private void drainChannel(EmbeddedChannel channel) {
        Object msg;
        while ((msg = channel.readOutbound()) != null) {
            ReferenceCountUtil.release(msg);
        }
    }

    @Test
    void testHtmlShouldCompress() {
        EmbeddedChannel channel = new EmbeddedChannel(handler.createContentCompressor());

        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html");
        req.headers().set(HttpHeaderNames.ACCEPT_ENCODING, "gzip");
        channel.writeInbound(req);

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        channel.writeOutbound(res);
        channel.writeOutbound(new DefaultLastHttpContent(Unpooled.copiedBuffer("<html>Hello World!</html>", StandardCharsets.UTF_8)));

        HttpResponse outRes = channel.readOutbound();
        assertNotNull(outRes);
        assertEquals("gzip", outRes.headers().get(HttpHeaderNames.CONTENT_ENCODING));

        drainChannel(channel);
        channel.finishAndReleaseAll();
    }

    @Test
    void testImageShouldNotCompress() {
        EmbeddedChannel channel = new EmbeddedChannel(handler.createContentCompressor());

        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/logo.png");
        req.headers().set(HttpHeaderNames.ACCEPT_ENCODING, "gzip");
        channel.writeInbound(req);

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/png");
        channel.writeOutbound(res);
        channel.writeOutbound(new DefaultLastHttpContent(Unpooled.copiedBuffer(new byte[100])));

        HttpResponse outRes = channel.readOutbound();
        assertNotNull(outRes);
        assertNull(outRes.headers().get(HttpHeaderNames.CONTENT_ENCODING));

        drainChannel(channel);
        channel.finishAndReleaseAll();
    }

    @Test
    void testSmallJsonShouldNotCompress() {
        EmbeddedChannel channel = new EmbeddedChannel(handler.createContentCompressor());

        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/api/status");
        req.headers().set(HttpHeaderNames.ACCEPT_ENCODING, "gzip");
        channel.writeInbound(req);

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        byte[] smallJson = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, smallJson.length);
        channel.writeOutbound(res);
        channel.writeOutbound(new DefaultLastHttpContent(Unpooled.copiedBuffer(smallJson)));

        HttpResponse outRes = channel.readOutbound();
        assertNotNull(outRes);
        assertNull(outRes.headers().get(HttpHeaderNames.CONTENT_ENCODING));

        drainChannel(channel);
        channel.finishAndReleaseAll();
    }

    @Test
    void testLargeJsonShouldCompress() {
        EmbeddedChannel channel = new EmbeddedChannel(handler.createContentCompressor());

        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/api/data");
        req.headers().set(HttpHeaderNames.ACCEPT_ENCODING, "gzip");
        channel.writeInbound(req);

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        byte[] largeJson = "{\"message\":\"This is a longer json response exceeding thirty-two bytes\"}".getBytes(StandardCharsets.UTF_8);
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, largeJson.length);
        channel.writeOutbound(res);
        channel.writeOutbound(new DefaultLastHttpContent(Unpooled.copiedBuffer(largeJson)));

        HttpResponse outRes = channel.readOutbound();
        assertNotNull(outRes);
        assertEquals("gzip", outRes.headers().get(HttpHeaderNames.CONTENT_ENCODING));

        drainChannel(channel);
        channel.finishAndReleaseAll();
    }

    @Test
    void testNoAcceptEncodingShouldNotCompress() {
        EmbeddedChannel channel = new EmbeddedChannel(handler.createContentCompressor());

        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html");
        channel.writeInbound(req);

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        channel.writeOutbound(res);
        channel.writeOutbound(new DefaultLastHttpContent(Unpooled.copiedBuffer("<html>Hello World!</html>", StandardCharsets.UTF_8)));

        HttpResponse outRes = channel.readOutbound();
        assertNotNull(outRes);
        assertNull(outRes.headers().get(HttpHeaderNames.CONTENT_ENCODING));

        drainChannel(channel);
        channel.finishAndReleaseAll();
    }

    @Test
    void testExcludedUserAgent() {
        ContentEncodingPredicates predicate = new ContentEncodingPredicates();
        predicate.setMediaTypes(new String[] { "text/html" });
        predicate.setExcludedUserAgents(new String[] { ".*BadBot.*" });

        NettyEncodingHandler customHandler = new NettyEncodingHandler();
        customHandler.setEncodingProviders("gzip");
        customHandler.setEncodingPredicates(predicate);

        EmbeddedChannel channel = new EmbeddedChannel(customHandler.createContentCompressor());

        // Request with excluded User-Agent
        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html");
        req.headers().set(HttpHeaderNames.ACCEPT_ENCODING, "gzip");
        req.headers().set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 BadBot/1.0");
        channel.writeInbound(req);

        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        channel.writeOutbound(res);
        channel.writeOutbound(new DefaultLastHttpContent(Unpooled.copiedBuffer("<html>Hello World!</html>", StandardCharsets.UTF_8)));

        HttpResponse outRes = channel.readOutbound();
        assertNotNull(outRes);
        assertNull(outRes.headers().get(HttpHeaderNames.CONTENT_ENCODING));

        drainChannel(channel);
        channel.finishAndReleaseAll();
    }

}
