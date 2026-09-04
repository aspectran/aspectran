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
package com.aspectran.netty.server.handler.resource;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link NettyClassPathResourceHandler}.
 */
class NettyClassPathResourceHandlerTest {

    private EmbeddedChannel channel;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.finishAndReleaseAll();
            channel = null;
        }
    }

    @Test
    void testServeClasspathResource() throws Exception {
        NettyClassPathResourceHandler handler = new NettyClassPathResourceHandler("config/");
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/aspectran-config.apon");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertTrue(handled, "Resource should be handled");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_TYPE));
        assertTrue(response.headers().contains(HttpHeaderNames.LAST_MODIFIED));
    }

    @Test
    void testResourceNotFound() throws Exception {
        NettyClassPathResourceHandler handler = new NettyClassPathResourceHandler("config/");
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/non-existing-file.txt");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertFalse(handled, "Non-existing resource should not be handled");
    }

    @Test
    void testHeadRequest() throws Exception {
        NettyClassPathResourceHandler handler = new NettyClassPathResourceHandler("config/");
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.HEAD, "/aspectran-config.apon");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertTrue(handled, "HEAD request should be handled");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_LENGTH));
    }

    @Test
    void testContextPathStripping() throws Exception {
        NettyClassPathResourceHandler handler = new NettyClassPathResourceHandler("config/");
        handler.setContextPath("/console");
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/console/aspectran-config.apon");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertTrue(handled, "Resource should be handled with contextPath stripped");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
    }

    @Test
    void testExplicitRelativePath() throws Exception {
        NettyClassPathResourceHandler handler = new NettyClassPathResourceHandler("config/");
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/console/aspectran-config.apon");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request, "/aspectran-config.apon");
        assertTrue(handled, "Resource should be handled using explicit relativePath");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
    }

    @Test
    void testDirectoryWithoutIndexFile() throws Exception {
        NettyClassPathResourceHandler handler = new NettyClassPathResourceHandler("config/");
        handler.setContextPath("/console");
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/console/");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request, "/");
        assertFalse(handled, "Directory request without index file should not be handled");
    }

}
