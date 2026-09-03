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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebSocketEndpointTemplateTest {

    private final NettyWebSocketListener dummyListener = new NettyWebSocketListener() {};

    @Test
    void testExactMatch() {
        WebSocketEndpointTemplate template = new WebSocketEndpointTemplate("/chat", dummyListener);
        assertFalse(template.isTemplate());

        Map<String, String> match = template.match("/chat");
        assertNotNull(match);
        assertTrue(match.isEmpty());

        assertNull(template.match("/other"));
        assertNull(template.match("/chat/extra"));
    }

    @Test
    void testTemplateMatchWithParameters() {
        WebSocketEndpointTemplate template = new WebSocketEndpointTemplate(
                "/nodes/{nodeId}/appmon/websocket/{token}", dummyListener);
        assertTrue(template.isTemplate());

        Map<String, String> match = template.match("/nodes/node1/appmon/websocket/secretToken123");
        assertNotNull(match);
        assertEquals(2, match.size());
        assertEquals("node1", match.get("nodeId"));
        assertEquals("secretToken123", match.get("token"));
    }

    @Test
    void testTemplateMismatch() {
        WebSocketEndpointTemplate template = new WebSocketEndpointTemplate(
                "/nodes/{nodeId}/appmon/websocket/{token}", dummyListener);

        assertNull(template.match("/nodes/node1/other/websocket/secretToken123"));
        assertNull(template.match("/nodes/node1/appmon/websocket"));
        assertNull(template.match("/nodes/node1/appmon/websocket/token/extra"));
    }

    @Test
    void testQueryStringIgnoredInMatch() {
        WebSocketEndpointTemplate template = new WebSocketEndpointTemplate(
                "/nodes/{nodeId}/appmon/websocket/{token}", dummyListener);

        Map<String, String> match = template.match("/nodes/node1/appmon/websocket/token123?query=val&foo=bar");
        assertNotNull(match);
        assertEquals("node1", match.get("nodeId"));
        assertEquals("token123", match.get("token"));
    }

    @Test
    void testPrecedence() {
        WebSocketEndpointTemplate specific = new WebSocketEndpointTemplate(
                "/nodes/{nodeId}/appmon/websocket/special", dummyListener);
        WebSocketEndpointTemplate generic = new WebSocketEndpointTemplate(
                "/nodes/{nodeId}/appmon/websocket/{token}", dummyListener);

        // specific has 4 literals, generic has 3 literals
        assertTrue(specific.compareTo(generic) < 0, "Specific template should have higher precedence");
    }

}
