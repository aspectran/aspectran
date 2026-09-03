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

import com.aspectran.netty.server.DefaultNettyServer;
import com.aspectran.netty.server.NettyContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link NettyWebSocketConfig}.
 */
class NettyWebSocketConfigTest {

    @Test
    void testDefaultValues() {
        NettyWebSocketConfig config = new NettyWebSocketConfig();
        assertEquals(65536, config.getMaxFramePayloadLength());
        assertEquals(65536, config.getMaxMessageSize());
        assertTrue(config.isAllowExtensions());
        assertEquals(0, config.getMaxIdleTimeout());
        assertEquals(65536, config.getMaxTextMessageBufferSize());
        assertEquals(65536, config.getMaxBinaryMessageBufferSize());
    }

    @Test
    void testCustomValues() {
        NettyWebSocketConfig config = new NettyWebSocketConfig();
        config.setMaxFramePayloadLength(131072);
        config.setMaxMessageSize(1048576);
        config.setAllowExtensions(false);
        config.setSubprotocols("chat,echo");
        config.setMaxIdleTimeout(60000);
        config.setMaxTextMessageBufferSize(131072);
        config.setMaxBinaryMessageBufferSize(262144);

        assertEquals(131072, config.getMaxFramePayloadLength());
        assertEquals(1048576, config.getMaxMessageSize());
        assertFalse(config.isAllowExtensions());
        assertEquals("chat,echo", config.getSubprotocols());
        assertEquals(60000, config.getMaxIdleTimeout());
        assertEquals(131072, config.getMaxTextMessageBufferSize());
        assertEquals(262144, config.getMaxBinaryMessageBufferSize());
    }

    @Test
    void testServerIntegration() {
        DefaultNettyServer server = new DefaultNettyServer();
        NettyWebSocketConfig config = new NettyWebSocketConfig();
        config.setMaxMessageSize(1048576);
        server.setWebSocketConfig(config);

        assertNotNull(server.getWebSocketConfig());
        assertEquals(1048576, server.getWebSocketConfig().getMaxMessageSize());
    }

    @Test
    void testContextIntegration() {
        NettyContext context = new NettyContext("/api");
        NettyWebSocketConfig config = new NettyWebSocketConfig();
        config.setMaxFramePayloadLength(131072);
        config.setMaxMessageSize(2097152);
        context.setWebSocketConfig(config);

        assertNotNull(context.getWebSocketConfig());
        assertEquals(131072, context.getWebSocketConfig().getMaxFramePayloadLength());
        assertEquals(2097152, context.getWebSocketConfig().getMaxMessageSize());
    }

}
