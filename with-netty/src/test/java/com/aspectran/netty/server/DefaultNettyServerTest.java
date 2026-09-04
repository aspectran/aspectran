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
package com.aspectran.netty.server;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.netty.server.handler.resource.NettyResourceHandler;
import com.aspectran.utils.ResourceUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link DefaultNettyServer} and {@link com.aspectran.netty.service.DefaultNettyService}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultNettyServerTest {

    private EmbeddedAspectran aspectran;

    private NettyServer nettyServer;

    private int port;

    @BeforeAll
    void ready() throws Exception {
        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);

        File testDir = new File("target/test-resources");
        File staticDir = new File(testDir, "static");
        staticDir.mkdirs();
        File staticFile = new File(staticDir, "test.txt");
        java.nio.file.Files.writeString(staticFile.toPath(), "Static content from Netty");
        File indexFile = new File(staticDir, "index.html");
        java.nio.file.Files.writeString(indexFile.toPath(), "Welcome to Netty Static");

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        nettyServer = aspectran.getBean("netty.server");
        if (nettyServer instanceof AbstractNettyServer ans) {
            ans.setResourceHandler(new NettyResourceHandler(testDir, "/static/**"));
        }
        nettyServer.start();

        port = nettyServer.getActivePort();
        assertTrue(port > 0, "Active port must be positive");
    }

    @AfterAll
    void finish() throws Exception {
        if (nettyServer != null && nettyServer.isRunning()) {
            nettyServer.stop();
        }
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    @Test
    void testHello() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/hello");
            String response = httpClient.execute(request, res -> {
                assertEquals(200, res.getCode());
                return EntityUtils.toString(res.getEntity()).trim();
            });
            assertTrue(response.contains("Hello Netty World!"), "Response should contain expected greeting");
        }
    }

    @Test
    void testEchoPost() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("http://127.0.0.1:" + port + "/echo");
            request.setEntity(new UrlEncodedFormEntity(List.of(new BasicNameValuePair("msg", "AspectranNetty"))));
            String response = httpClient.execute(request, res -> {
                assertEquals(200, res.getCode());
                return EntityUtils.toString(res.getEntity()).trim();
            });
            assertEquals("Echo: AspectranNetty", response);
        }
    }

    @Test
    void testVirtualThread() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/virtual-thread");
            String response = httpClient.execute(request, res -> {
                assertEquals(200, res.getCode());
                return EntityUtils.toString(res.getEntity()).trim();
            });
            assertTrue(response.contains("\"isVirtual\":true") || response.contains("\"isVirtual\": true"),
                    "Expected isVirtual:true in: " + response);
        }
    }

    @Test
    void testSessionCookie() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request1 = new HttpGet("http://127.0.0.1:" + port + "/session");
            String sessionCookie = httpClient.execute(request1, res -> {
                assertEquals(200, res.getCode());
                Header cookieHeader = res.getFirstHeader("Set-Cookie");
                assertNotNull(cookieHeader, "Set-Cookie header must be present on first session access");
                String cookie = cookieHeader.getValue().split(";")[0].trim();
                assertTrue(cookie.startsWith("JSESSIONID="));
                String body;
                try {
                    body = EntityUtils.toString(res.getEntity()).trim();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                assertTrue(body.contains("\"count\":1") || body.contains("\"count\": 1"),
                        "Expected count:1 in: " + body);
                return cookie;
            });

            // Second request with the same session cookie
            HttpGet request2 = new HttpGet("http://127.0.0.1:" + port + "/session");
            request2.setHeader("Cookie", sessionCookie);
            httpClient.execute(request2, res -> {
                assertEquals(200, res.getCode());
                String body;
                try {
                    body = EntityUtils.toString(res.getEntity()).trim();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                assertTrue(body.contains("\"count\":2") || body.contains("\"count\": 2"),
                        "Expected count:2 in: " + body);
                return null;
            });
        }
    }

    @Test
    void testNotFound() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/not-existing-path");
            httpClient.execute(request, res -> {
                assertEquals(404, res.getCode());
                return null;
            });
        }
    }

    @Test
    void testStaticResource() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/static/test.txt");
            String content = httpClient.execute(request, res -> {
                assertEquals(200, res.getCode());
                try {
                    return EntityUtils.toString(res.getEntity()).trim();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            assertEquals("Static content from Netty", content);
        }
    }

    @Test
    void testDirectoryIndex() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/static/");
            String content = httpClient.execute(request, res -> {
                assertEquals(200, res.getCode());
                try {
                    return EntityUtils.toString(res.getEntity()).trim();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            assertEquals("Welcome to Netty Static", content);
        }
    }

    @Test
    void testNativeTransportProperty() {
        if (nettyServer instanceof AbstractNettyServer ans) {
            assertTrue(ans.isNativeTransport());
            ans.setNativeTransport(false);
            assertFalse(ans.isNativeTransport());
            ans.setNativeTransport(true);
            assertTrue(ans.isNativeTransport());
        }
    }

}
