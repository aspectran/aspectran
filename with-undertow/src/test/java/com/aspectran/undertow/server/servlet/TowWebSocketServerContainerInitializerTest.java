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
package com.aspectran.undertow.server.servlet;

import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link TowWebSocketServerContainerInitializer}.
 */
class TowWebSocketServerContainerInitializerTest {

    @Test
    void testDefaults() {
        TowWebSocketServerContainerInitializer initializer = new TowWebSocketServerContainerInitializer();
        assertNull(initializer.getIdleTimeout());
        assertNull(initializer.getAsyncSendTimeout());
        assertNull(initializer.getMaxBinaryMessageSize());
        assertNull(initializer.getMaxTextMessageSize());

        TowServletContext context = new TowServletContext();
        initializer.initialize(context);

        WebSocketDeploymentInfo deploymentInfo = (WebSocketDeploymentInfo)context
                .getServletContextAttributes().get(WebSocketDeploymentInfo.ATTRIBUTE_NAME);
        assertNotNull(deploymentInfo);
        assertNotNull(deploymentInfo.getBuffers());
        assertTrue(deploymentInfo.getBuffers().isDirect(), "directBuffers must default to true");
        assertEquals(16384, deploymentInfo.getBuffers().getBufferSize());
        assertTrue(deploymentInfo.getListeners().isEmpty());
    }

    @Test
    void testCustomProperties() {
        TowWebSocketServerContainerInitializer initializer = new TowWebSocketServerContainerInitializer();
        initializer.setDirectBuffers(false);
        initializer.setBufferSize(8192);
        initializer.setIdleTimeout(60000L);
        initializer.setAsyncSendTimeout(10000L);
        initializer.setMaxBinaryMessageSize(32768);
        initializer.setMaxTextMessageSize(32768);

        assertEquals(60000L, initializer.getIdleTimeout());
        assertEquals(10000L, initializer.getAsyncSendTimeout());
        assertEquals(32768, initializer.getMaxBinaryMessageSize());
        assertEquals(32768, initializer.getMaxTextMessageSize());

        TowServletContext context = new TowServletContext();
        initializer.initialize(context);

        WebSocketDeploymentInfo deploymentInfo = (WebSocketDeploymentInfo)context
                .getServletContextAttributes().get(WebSocketDeploymentInfo.ATTRIBUTE_NAME);
        assertNotNull(deploymentInfo);
        assertFalse(deploymentInfo.getBuffers().isDirect());
        assertEquals(8192, deploymentInfo.getBuffers().getBufferSize());
        assertEquals(1, deploymentInfo.getListeners().size());
    }

}
