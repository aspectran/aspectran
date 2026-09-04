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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.adapter.DefaultApplicationAdapter;
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
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link NettyResourceHandler}.
 */
class NettyResourceHandlerTest {

    private EmbeddedChannel channel;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.finishAndReleaseAll();
            channel = null;
        }
    }

    @Test
    void testServeFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.html");
        Files.writeString(file, "<html><body>Hello</body></html>");

        NettyResourceHandler handler = new NettyResourceHandler(tempDir.toFile());
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/test.html");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertTrue(handled, "Resource should be handled");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_TYPE));
        assertTrue(response.headers().contains(HttpHeaderNames.LAST_MODIFIED));

        channel.finishAndReleaseAll();
        channel = null;
        Files.delete(file);
        assertFalse(Files.exists(file));
    }

    @Test
    void testHeadRequest(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("test.html"), "<html><body>Hello</body></html>");

        NettyResourceHandler handler = new NettyResourceHandler(tempDir.toFile());
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.HEAD, "/test.html");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertTrue(handled, "HEAD request should be handled");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_LENGTH));

        channel.finishAndReleaseAll();
        channel = null;
    }

    @Test
    void testContextPathStripping(@TempDir Path tempDir) throws Exception {
        Path assetsDir = Files.createDirectories(tempDir.resolve("assets"));
        Files.writeString(assetsDir.resolve("style.css"), "body { color: red; }");

        NettyResourceHandler handler = new NettyResourceHandler(tempDir.toFile());
        handler.setContextPath("/console");
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/console/assets/style.css");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertTrue(handled, "Resource should be handled with contextPath stripped");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());

        channel.finishAndReleaseAll();
        channel = null;
    }

    @Test
    void testExplicitRelativePath(@TempDir Path tempDir) throws Exception {
        Path assetsDir = Files.createDirectories(tempDir.resolve("assets"));
        Files.writeString(assetsDir.resolve("style.css"), "body { color: blue; }");

        NettyResourceHandler handler = new NettyResourceHandler(tempDir.toFile());
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/console/assets/style.css");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request, "/assets/style.css");
        assertTrue(handled, "Resource should be handled using explicit relativePath");

        HttpResponse response = channel.readOutbound();
        assertNotNull(response);
        assertEquals(HttpResponseStatus.OK, response.status());

        channel.finishAndReleaseAll();
        channel = null;
    }

    @Test
    void testFileNotFound(@TempDir Path tempDir) throws Exception {
        NettyResourceHandler handler = new NettyResourceHandler(tempDir.toFile());
        channel = new EmbeddedChannel(handler);

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/non-existing.txt");

        boolean handled = handler.handle(channel.pipeline().firstContext(), request);
        assertFalse(handled, "Non-existing file should not be handled");

        channel.finishAndReleaseAll();
        channel = null;
    }

    @Test
    void testBlockProtectedDirectories(@TempDir Path tempDir) throws Exception {
        Path webInfJsp = Files.createDirectories(tempDir.resolve("WEB-INF/jsp/home"));
        Files.writeString(webInfJsp.resolve("main.jsp"), "<h1>JSP Source</h1>");

        Path metaInf = Files.createDirectories(tempDir.resolve("META-INF"));
        Files.writeString(metaInf.resolve("MANIFEST.MF"), "Manifest-Version: 1.0\n");

        NettyResourceHandler handler = new NettyResourceHandler(tempDir.toFile());
        channel = new EmbeddedChannel(handler);

        FullHttpRequest req1 = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/WEB-INF/jsp/home/main.jsp");
        assertFalse(handler.handle(channel.pipeline().firstContext(), req1),
                "Direct request to /WEB-INF/jsp/home/main.jsp must be blocked");

        FullHttpRequest req2 = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/web-inf/jsp/home/main.jsp");
        assertFalse(handler.handle(channel.pipeline().firstContext(), req2),
                "Case-insensitive /web-inf/ request must be blocked");

        FullHttpRequest req3 = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/META-INF/MANIFEST.MF");
        assertFalse(handler.handle(channel.pipeline().firstContext(), req3),
                "Direct request to /META-INF/MANIFEST.MF must be blocked");

        // When protected directory blocking is disabled, resource can be served
        handler.setBlockProtectedDirectories(false);
        FullHttpRequest req4 = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/WEB-INF/jsp/home/main.jsp");
        assertTrue(handler.handle(channel.pipeline().firstContext(), req4),
                "Request should be handled when blockProtectedDirectories is false");

        channel.finishAndReleaseAll();
        channel = null;
    }

    @Test
    void testSetPathPatternsFromApon(@TempDir Path tempDir) throws Exception {
        Path publicDir = Files.createDirectories(tempDir.resolve("public"));
        Files.writeString(publicDir.resolve("index.html"), "<h1>Public</h1>");

        Path secretDir = Files.createDirectories(tempDir.resolve("secret"));
        Files.writeString(secretDir.resolve("secret.txt"), "classified");

        NettyResourceHandler handler = new NettyResourceHandler(tempDir.toFile());
        handler.setPathPatterns("""
                +: /**
                -: /secret/**
                """);
        channel = new EmbeddedChannel(handler);

        FullHttpRequest req1 = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/public/index.html");
        assertTrue(handler.handle(channel.pipeline().firstContext(), req1),
                "/public/index.html should be included");

        FullHttpRequest req2 = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/secret/secret.txt");
        assertFalse(handler.handle(channel.pipeline().firstContext(), req2),
                "/secret/secret.txt should be excluded");

        channel.finishAndReleaseAll();
        channel = null;
    }

    @Test
    void testSetBasePath(@TempDir Path tempDir) {
        NettyResourceHandler handler = new NettyResourceHandler();
        handler.setBasePath(tempDir.toString());
        assertEquals(tempDir.toFile(), handler.getBaseDir());

        Path subDir = tempDir.resolve("static");
        ApplicationAdapter adapter = new DefaultApplicationAdapter(tempDir.toString());
        handler.setApplicationAdapter(adapter);
        handler.setBasePath("/static");
        assertEquals(subDir.toFile(), handler.getBaseDir());
    }

}
