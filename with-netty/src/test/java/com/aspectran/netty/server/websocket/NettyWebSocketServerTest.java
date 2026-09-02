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
package com.aspectran.netty.server.websocket;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.netty.server.DefaultNettyServer;
import com.aspectran.netty.server.NettyContext;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for Netty native WebSocket support.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NettyWebSocketServerTest {

    private EmbeddedAspectran aspectran;

    private DefaultNettyServer nettyServer;

    private int port;

    @BeforeAll
    void ready() throws Exception {
        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-multi-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        nettyServer = aspectran.getBean("netty.server");

        // Register WebSocket endpoint on root context
        NettyContext rootContext = nettyServer.getContextRouter().match("/");
        if (rootContext != null) {
            rootContext.addWebSocketEndpoint("/echo-ws", new NettyWebSocketListener() {
                @Override
                public void onMessage(NettyWebSocketSession session, String text) {
                    session.sendText("Echo: " + text);
                }

                @Override
                public void onMessage(NettyWebSocketSession session, byte[] data) {
                    session.sendBinary(data);
                }
            });
        }

        // Register broadcast WebSocket endpoint on /api context
        NettyContext apiContext = nettyServer.getContextRouter().match("/api");
        if (apiContext != null) {
            apiContext.addWebSocketEndpoint("/chat", new AbstractNettyWebSocketEndpoint() {
                @Override
                public void onMessage(NettyWebSocketSession session, String text) {
                    broadcast("Broadcast: " + text);
                }
            });
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
    void testEchoText() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<String> receivedText = new CompletableFuture<>();

        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/echo-ws"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        receivedText.complete(data.toString());
                        return WebSocket.Listener.super.onText(ws, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        webSocket.sendText("Hello Netty WebSocket!", true);
        String response = receivedText.get(5, TimeUnit.SECONDS);
        assertEquals("Echo: Hello Netty WebSocket!", response);

        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done").get(5, TimeUnit.SECONDS);
    }

    @Test
    void testEchoBinary() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<byte[]> receivedBinary = new CompletableFuture<>();

        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/echo-ws"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onBinary(WebSocket ws, ByteBuffer data, boolean last) {
                        byte[] bytes = new byte[data.remaining()];
                        data.get(bytes);
                        receivedBinary.complete(bytes);
                        return WebSocket.Listener.super.onBinary(ws, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        byte[] payload = new byte[] { 10, 20, 30, 40, 50 };
        webSocket.sendBinary(ByteBuffer.wrap(payload), true);
        byte[] response = receivedBinary.get(5, TimeUnit.SECONDS);
        assertArrayEquals(payload, response);

        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done").get(5, TimeUnit.SECONDS);
    }

    @Test
    void testContextScopedBroadcast() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> client1Received = new AtomicReference<>();
        AtomicReference<String> client2Received = new AtomicReference<>();

        WebSocket ws1 = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/api/chat"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        client1Received.set(data.toString());
                        latch.countDown();
                        return WebSocket.Listener.super.onText(ws, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        WebSocket ws2 = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/api/chat"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        client2Received.set(data.toString());
                        latch.countDown();
                        return WebSocket.Listener.super.onText(ws, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        // Client 1 sends message
        ws1.sendText("Hello everyone!", true);

        boolean reached = latch.await(5, TimeUnit.SECONDS);
        assertTrue(reached, "Both clients should receive the broadcast message");
        assertEquals("Broadcast: Hello everyone!", client1Received.get());
        assertEquals("Broadcast: Hello everyone!", client2Received.get());

        ws1.sendClose(WebSocket.NORMAL_CLOSURE, "Bye").get(5, TimeUnit.SECONDS);
        ws2.sendClose(WebSocket.NORMAL_CLOSURE, "Bye").get(5, TimeUnit.SECONDS);
    }

}
