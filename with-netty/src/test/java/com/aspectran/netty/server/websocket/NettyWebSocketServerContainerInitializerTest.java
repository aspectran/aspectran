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

import com.aspectran.netty.server.NettyContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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

}
