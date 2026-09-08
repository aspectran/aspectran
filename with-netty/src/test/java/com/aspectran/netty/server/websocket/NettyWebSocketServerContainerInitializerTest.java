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

import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.NonPersistentValue;
import com.aspectran.core.component.session.Session;
import com.aspectran.netty.server.NettyContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link NettyWebSocketServerContainerInitializer}.
 */
class NettyWebSocketServerContainerInitializerTest {

    @Test
    void testDefaultValues() {
        NettyWebSocketServerContainerInitializer initializer = new NettyWebSocketServerContainerInitializer();
        assertEquals(65536, initializer.getMaxFramePayloadLength());
        assertEquals(65536, initializer.getMaxMessageSize());
        assertEquals(0L, initializer.getIdleTimeout());
        assertEquals(65536, initializer.getMaxTextMessageBufferSize());
        assertEquals(65536, initializer.getMaxBinaryMessageBufferSize());
    }

    @Test
    void testAliases() {
        NettyWebSocketServerContainerInitializer initializer = new NettyWebSocketServerContainerInitializer();
        initializer.setIdleTimeout(60000L);
        assertEquals(60000L, initializer.getIdleTimeout());
        assertEquals(60000L, initializer.getMaxIdleTimeout());

        initializer.setBufferSize(16384);
        assertEquals(16384, initializer.getMaxTextMessageBufferSize());
        assertEquals(16384, initializer.getMaxBinaryMessageBufferSize());
    }

    @Test
    void testInitializeNettyContext() {
        NettyContext context = new NettyContext("/test");
        assertNull(context.getWebSocketConfig());

        NettyWebSocketServerContainerInitializer initializer = new NettyWebSocketServerContainerInitializer();
        initializer.setIdleTimeout(30000L);
        initializer.initialize(context);

        assertNotNull(context.getWebSocketConfig());
        assertSame(initializer, context.getWebSocketConfig());
        assertEquals(30000L, context.getWebSocketConfig().getMaxIdleTimeout());
    }

    @Test
    void testNettyContextGetterSetter() {
        NettyContext context = new NettyContext("/test");
        assertNull(context.getWebSocketServerContainerInitializer());

        NettyWebSocketServerContainerInitializer initializer = new NettyWebSocketServerContainerInitializer();
        context.setWebSocketServerContainerInitializer(initializer);

        assertSame(initializer, context.getWebSocketServerContainerInitializer());
    }

    @Test
    void testBindSessionAndChannelClose() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.initialize();
        try {
            Session httpSession = sessionManager.createSession(sessionManager.createSessionId());
            TestWebSocketSession wsSession = new TestWebSocketSession();

            NettyWebSocketServerContainerInitializer.bindSession(httpSession, wsSession);

            NettyWebSocketServerContainerInitializer.WebSocketSessions sessions =
                    httpSession.getAttribute(NettyWebSocketServerContainerInitializer.WEBSOCKET_SESSIONS_ATTRIBUTE);
            assertNotNull(sessions);
            assertEquals(1, sessions.size());
            assertTrue(sessions.contains(wsSession));

            // Closing the channel should automatically remove wsSession from sessions
            wsSession.getChannel().close().sync();
            assertEquals(0, sessions.size());
        } finally {
            sessionManager.destroy();
        }
    }

    @Test
    void testGracefulCloseOnSessionDestroyed() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.initialize();
        sessionManager.addSessionListener(new NettyWebSocketServerContainerInitializer.WebSocketGracefulCloseListener());
        try {
            Session httpSession = sessionManager.createSession(sessionManager.createSessionId());
            TestWebSocketSession wsSession1 = new TestWebSocketSession();
            TestWebSocketSession wsSession2 = new TestWebSocketSession();

            NettyWebSocketServerContainerInitializer.bindSession(httpSession, wsSession1);
            NettyWebSocketServerContainerInitializer.bindSession(httpSession, wsSession2);

            assertTrue(wsSession1.isOpen());
            assertTrue(wsSession2.isOpen());

            // Invalidate session -> triggers graceful close
            httpSession.invalidate();

            assertFalse(wsSession1.isOpen());
            assertFalse(wsSession2.isOpen());
            assertTrue(wsSession1.closed.get());
            assertTrue(wsSession2.closed.get());
            assertEquals(NettyWebSocketSession.POLICY_VIOLATION, wsSession1.closeCode.get());
            assertEquals(NettyWebSocketSession.POLICY_VIOLATION, wsSession2.closeCode.get());
        } finally {
            sessionManager.destroy();
        }
    }

    @Test
    void testGracefulCloseOnAttributeRemoved() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.initialize();
        sessionManager.addSessionListener(new NettyWebSocketServerContainerInitializer.WebSocketGracefulCloseListener());
        try {
            Session httpSession = sessionManager.createSession(sessionManager.createSessionId());
            TestWebSocketSession wsSession = new TestWebSocketSession();

            NettyWebSocketServerContainerInitializer.bindSession(httpSession, wsSession);
            assertTrue(wsSession.isOpen());

            // Manually removing attribute triggers attributeRemoved on listener
            httpSession.removeAttribute(NettyWebSocketServerContainerInitializer.WEBSOCKET_SESSIONS_ATTRIBUTE);

            assertFalse(wsSession.isOpen());
            assertTrue(wsSession.closed.get());
            assertEquals(NettyWebSocketSession.POLICY_VIOLATION, wsSession.closeCode.get());
        } finally {
            sessionManager.destroy();
        }
    }

    @Test
    void testGracefulCloseOnAttributeUpdated() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.initialize();
        sessionManager.addSessionListener(new NettyWebSocketServerContainerInitializer.WebSocketGracefulCloseListener());
        try {
            Session httpSession = sessionManager.createSession(sessionManager.createSessionId());
            TestWebSocketSession wsSession = new TestWebSocketSession();

            NettyWebSocketServerContainerInitializer.bindSession(httpSession, wsSession);
            assertTrue(wsSession.isOpen());

            // Overwriting attribute triggers attributeUpdated on listener
            httpSession.setAttribute(NettyWebSocketServerContainerInitializer.WEBSOCKET_SESSIONS_ATTRIBUTE,
                    NonPersistentValue.wrap(new NettyWebSocketServerContainerInitializer.WebSocketSessions()));

            assertFalse(wsSession.isOpen());
            assertTrue(wsSession.closed.get());
            assertEquals(NettyWebSocketSession.POLICY_VIOLATION, wsSession.closeCode.get());
        } finally {
            sessionManager.destroy();
        }
    }

    private static class TestWebSocketSession implements NettyWebSocketSession {
        private final Channel channel = new EmbeddedChannel();
        final AtomicBoolean closed = new AtomicBoolean(false);
        final AtomicInteger closeCode = new AtomicInteger();
        private volatile Session httpSession;

        @Override public String getId() { return "test-ws-id"; }
        @Override public Channel getChannel() { return channel; }
        @Override public SocketAddress getLocalAddress() { return channel.localAddress(); }
        @Override public SocketAddress getRemoteAddress() { return channel.remoteAddress(); }
        @Override public String getUri() { return "/ws"; }
        @Override public String getPath() { return "/ws"; }
        @Override public Map<String, String> getPathParameters() { return Collections.emptyMap(); }
        @Override public HttpHeaders getHandshakeHeaders() { return EmptyHttpHeaders.INSTANCE; }
        @Override public Map<String, Object> getAttributes() { return new HashMap<>(); }
        @Override public <T> T getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public Object removeAttribute(String name) { return null; }
        @Override public Session getHttpSession() { return httpSession; }
        public void setHttpSession(Session httpSession) { this.httpSession = httpSession; }
        @Override public boolean isOpen() { return channel.isActive() && !closed.get(); }
        @Override public ChannelFuture sendText(String text) { return channel.newSucceededFuture(); }
        @Override public ChannelFuture sendBinary(byte[] data) { return channel.newSucceededFuture(); }
        @Override public ChannelFuture sendBinary(io.netty.buffer.ByteBuf data) { return channel.newSucceededFuture(); }
        @Override public ChannelFuture sendPing() { return channel.newSucceededFuture(); }
        @Override public ChannelFuture sendPong() { return channel.newSucceededFuture(); }
        @Override public ChannelFuture close() { return close(1000, null); }
        @Override public ChannelFuture close(int statusCode, String reasonText) {
            closed.set(true);
            closeCode.set(statusCode);
            return channel.close();
        }
        @Override public NettyWebSocketConfig getWebSocketConfig() { return null; }
        @Override public long getMaxIdleTimeout() { return 0; }
        @Override public void setMaxIdleTimeout(long milliseconds) {}
        @Override public int getMaxTextMessageBufferSize() { return 0; }
        @Override public void setMaxTextMessageBufferSize(int length) {}
        @Override public int getMaxBinaryMessageBufferSize() { return 0; }
        @Override public void setMaxBinaryMessageBufferSize(int length) {}
    }

}
