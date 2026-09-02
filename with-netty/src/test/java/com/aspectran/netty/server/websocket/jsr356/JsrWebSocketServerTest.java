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
package com.aspectran.netty.server.websocket.jsr356;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.netty.server.DefaultNettyServer;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.web.websocket.jsr356.SimplifiedEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for JSR-356 (@ServerEndpoint) compatibility adapter on Netty.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsrWebSocketServerTest {

    private EmbeddedAspectran aspectran;

    private DefaultNettyServer nettyServer;

    private int port;

    @BeforeAll
    void ready() throws Exception {
        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-multi-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        nettyServer = aspectran.getBean("netty.server");

        // Manually export JSR endpoints on the server
        NettyServerEndpointExporter exporter = new NettyServerEndpointExporter(nettyServer);
        exporter.registerEndpoint(EchoServerEndpoint.class);
        exporter.registerEndpoint(ChatServerEndpoint.class);

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
    void testJsrEchoEndpoint() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<String> receivedText = new CompletableFuture<>();

        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/jsr-echo"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        receivedText.complete(data.toString());
                        return WebSocket.Listener.super.onText(ws, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        webSocket.sendText("Hello JSR-356 on Netty!", true);
        String response = receivedText.get(5, TimeUnit.SECONDS);
        assertEquals("JSR-Echo: Hello JSR-356 on Netty!", response);

        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done").get(5, TimeUnit.SECONDS);
    }

    @Test
    void testJsrSimplifiedEndpointBroadcast() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> client1Received = new AtomicReference<>();
        AtomicReference<String> client2Received = new AtomicReference<>();

        WebSocket ws1 = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/jsr-chat"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        client1Received.set(data.toString());
                        latch.countDown();
                        return WebSocket.Listener.super.onText(ws, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        WebSocket ws2 = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://127.0.0.1:" + port + "/jsr-chat"), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        client2Received.set(data.toString());
                        latch.countDown();
                        return WebSocket.Listener.super.onText(ws, data, last);
                    }
                })
                .get(5, TimeUnit.SECONDS);

        // Allow registration to complete
        Thread.sleep(100);

        // Client 1 sends message
        ws1.sendText("Broadcast from Client 1", true);

        boolean reached = latch.await(5, TimeUnit.SECONDS);
        assertTrue(reached, "Both clients should receive the broadcast message");
        assertEquals("JSR-Broadcast: Broadcast from Client 1", client1Received.get());
        assertEquals("JSR-Broadcast: Broadcast from Client 1", client2Received.get());

        ws1.sendClose(WebSocket.NORMAL_CLOSURE, "Done").get(5, TimeUnit.SECONDS);
        ws2.sendClose(WebSocket.NORMAL_CLOSURE, "Done").get(5, TimeUnit.SECONDS);
    }

    @ServerEndpoint("/jsr-echo")
    public static class EchoServerEndpoint {

        @OnOpen
        public void onOpen(Session session) {
        }

        @OnMessage
        public String onMessage(Session session, String message) {
            return "JSR-Echo: " + message;
        }

        @OnClose
        public void onClose(Session session, CloseReason reason) {
        }

    }

    @ServerEndpoint("/jsr-chat")
    public static class ChatServerEndpoint extends SimplifiedEndpoint {

        @Override
        protected void registerMessageHandlers(Session session) {
            session.addMessageHandler(String.class, msg -> {
                broadcast("JSR-Broadcast: " + msg);
            });
            addSession(session);
        }

        @Override
        protected void onSessionRemoved(Session session) {
        }

    }

}
