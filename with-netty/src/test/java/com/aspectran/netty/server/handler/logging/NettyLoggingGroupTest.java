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
package com.aspectran.netty.server.handler.logging;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.netty.server.DefaultNettyServer;
import com.aspectran.netty.server.NettyContext;
import com.aspectran.netty.server.websocket.AbstractNettyWebSocketEndpoint;
import com.aspectran.netty.server.websocket.NettyWebSocketSession;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.logging.LoggingGroupHelper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.MDC;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the hybrid logging group resolution strategy
 * (context-level automatic inheritance + fine-grained path pattern overrides).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NettyLoggingGroupTest {

    private EmbeddedAspectran aspectran;

    private DefaultNettyServer nettyServer;

    private int port;

    @BeforeAll
    void ready() throws Exception {
        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-multi-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        nettyServer = aspectran.getBean("netty.server");

        // 1. Configure fine-grained path pattern override:
        // /api/special/** should be assigned to "special" group
        Map<String, String> patterns = new HashMap<>();
        patterns.put("special", "+: /api/special/**");
        nettyServer.setPathPatternsByGroupName(patterns);

        // 2. Register WebSocket endpoints under the /api context:
        // - /chat: normal endpoint (should inherit "api" group)
        // - /special/chat: special endpoint (should be overridden to "special" group)
        NettyContext apiContext = nettyServer.getContextRouter().match("/api");
        if (apiContext != null) {
            apiContext.addWebSocketEndpoint("/chat", new AbstractNettyWebSocketEndpoint() {
                @Override
                public void onMessage(NettyWebSocketSession session, String text) {
                    String group = MDC.get(LoggingGroupHelper.LOGGING_GROUP);
                    session.sendText("group:" + group);
                }
            });
            apiContext.addWebSocketEndpoint("/special/chat", new AbstractNettyWebSocketEndpoint() {
                @Override
                public void onMessage(NettyWebSocketSession session, String text) {
                    String group = MDC.get(LoggingGroupHelper.LOGGING_GROUP);
                    session.sendText("group:" + group);
                }
            });
        }

        nettyServer.start();
        port = nettyServer.getActivePort();
        assertTrue(port > 0, "Server port must be greater than 0");
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
    void testContextAutomaticInheritanceForApi() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://127.0.0.1:" + port + "/api/logging-group");
            try (CloseableHttpResponse response = client.execute(get)) {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertEquals("api", body, "Default logging group should match context name 'api'");
            }
        }
    }

    @Test
    void testContextAutomaticInheritanceForAdmin() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://127.0.0.1:" + port + "/admin/logging-group");
            try (CloseableHttpResponse response = client.execute(get)) {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertEquals("admin", body, "Default logging group should match context name 'admin'");
            }
        }
    }

    @Test
    void testContextAutomaticInheritanceForRoot() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://127.0.0.1:" + port + "/logging-group");
            try (CloseableHttpResponse response = client.execute(get)) {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertEquals("root", body, "Default logging group should match root context name 'root'");
            }
        }
    }

    @Test
    void testPathBasedPatternOverride() throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://127.0.0.1:" + port + "/api/special/logging-group");
            try (CloseableHttpResponse response = client.execute(get)) {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertEquals("special", body, "Pattern override should take precedence over context name");
            }
        }
    }

    @Test
    void testWebSocketContextLoggingGroup() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<String> receivedText = new CompletableFuture<>();

        WebSocket ws = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/api/chat"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        receivedText.complete(data.toString());
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        ws.sendText("check", true);
        String response = receivedText.get(5, TimeUnit.SECONDS);
        assertEquals("group:api", response, "WebSocket messages should inherit context logging group 'api'");

        ws.sendClose(WebSocket.NORMAL_CLOSURE, "Done").get(5, TimeUnit.SECONDS);
    }

    @Test
    void testWebSocketPatternOverrideLoggingGroup() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<String> receivedText = new CompletableFuture<>();

        WebSocket ws = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/api/special/chat"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        receivedText.complete(data.toString());
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        ws.sendText("check", true);
        String response = receivedText.get(5, TimeUnit.SECONDS);
        assertEquals("group:special", response, "WebSocket messages should inherit pattern override 'special'");

        ws.sendClose(WebSocket.NORMAL_CLOSURE, "Done").get(5, TimeUnit.SECONDS);
    }

    @Test
    void testMdcNotLeakedToCurrentThread() {
        assertNull(MDC.get(LoggingGroupHelper.LOGGING_GROUP), "MDC should not have leaked to calling thread");
    }

}
