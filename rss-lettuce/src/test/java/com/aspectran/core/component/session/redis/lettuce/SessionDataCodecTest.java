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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.SessionData;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SessionDataCodec}.
 */
class SessionDataCodecTest {

    @Test
    void testEncodeDecodeFullSessionData() {
        SessionDataCodec codec = new SessionDataCodec(null);
        long now = System.currentTimeMillis();
        SessionData original = new SessionData("session-123", now, 1800000L);
        original.setAttribute("user", "testUser");
        original.setAttribute("role", "ADMIN");

        ByteBuffer encoded = codec.encodeValue(original);
        assertNotNull(encoded);

        SessionData decoded = codec.decodeValue(encoded);
        assertNotNull(decoded);
        assertFalse(decoded.isIdOnly());
        assertEquals("session-123", decoded.getId());
        assertEquals(now, decoded.getCreated());
        assertEquals(1800000L, decoded.getInactiveInterval());
        assertEquals("testUser", decoded.getAttribute("user"));
        assertEquals("ADMIN", decoded.getAttribute("role"));
    }

    @Test
    void testEncodeDecodeIdOnlySessionData() {
        SessionDataCodec codec = new SessionDataCodec(null);
        SessionData idOnly = SessionData.of("session-idx-456");
        assertTrue(idOnly.isIdOnly());

        ByteBuffer encoded = codec.encodeValue(idOnly);
        assertNotNull(encoded);

        SessionData decoded = codec.decodeValue(encoded);
        assertNotNull(decoded);
        assertTrue(decoded.isIdOnly());
        assertEquals("session-idx-456", decoded.getId());
    }

    @Test
    void testNonPersistentAttributes() {
        SessionDataCodec codec = new SessionDataCodec(Set.of("transientKey"));
        long now = System.currentTimeMillis();
        SessionData original = new SessionData("session-789", now, 1800000L);
        original.setAttribute("persistentKey", "savedValue");
        original.setAttribute("transientKey", "discardedValue");

        ByteBuffer encoded = codec.encodeValue(original);
        SessionData decoded = codec.decodeValue(encoded);

        assertNotNull(decoded);
        assertEquals("savedValue", decoded.getAttribute("persistentKey"));
        assertNull(decoded.getAttribute("transientKey"));
    }

}
