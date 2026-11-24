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
package com.aspectran.core.adapter;

import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link SessionAdapter} and {@link DefaultSessionAdapter}.
 *
 * <p>Created: 2026-11-24</p>
 */
class DefaultSessionAdapterTest {

    private DefaultSessionManager sessionManager;

    @BeforeEach
    void setUp() throws Exception {
        sessionManager = new DefaultSessionManager();
        sessionManager.initialize();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (sessionManager != null) {
            sessionManager.destroy();
        }
    }

    @Test
    void testStatelessSessionGetter() {
        SessionAgent agent = new SessionAgent(sessionManager);
        SessionAdapter adapter = new DefaultSessionAdapter(agent);

        // Before session creation
        assertNull(adapter.getId());
        assertNull(adapter.getId(false));
        assertFalse(adapter.isValid());
        assertEquals(0L, adapter.getCreationTime());
        assertEquals(0L, adapter.getLastAccessedTime());
        assertEquals(0, adapter.getMaxInactiveInterval());
        assertNull(adapter.getAttribute("any"));

        // Still no session created in sessionManager
        assertEquals(0, sessionManager.getStatistics().getNumberOfActives());

        // Create session via getId(true)
        String createdId = adapter.getId(true);
        assertNotNull(createdId);
        assertTrue(adapter.isValid());
        assertEquals(createdId, adapter.getId());
        assertEquals(createdId, adapter.getId(false));
        assertTrue(adapter.getCreationTime() > 0L);
        assertTrue(adapter.getLastAccessedTime() > 0L);
    }

}
