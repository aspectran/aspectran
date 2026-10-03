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
package com.aspectran.core.activity;

import com.aspectran.core.activity.support.SessionFlashMapManager;
import com.aspectran.core.adapter.AbstractSessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.TransletRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link FlashMap} and {@link SessionFlashMapManager}.
 *
 * <p>Created: 2026. 10. 3.</p>
 */
class FlashMapTest {

    @Test
    void testFlashMapExpirationAndComparison() {
        FlashMap map1 = new FlashMap();
        map1.put("msg", "hello");
        map1.setTargetRequestName("/target");
        map1.startExpirationPeriod(1);

        assertFalse(map1.isExpired());
        assertEquals("/target", map1.getTargetRequestName());

        FlashMap map2 = new FlashMap();
        map2.put("msg", "world");

        // map1 has targetRequestName so it should precede map2 in compareTo (return negative)
        assertTrue(map1.compareTo(map2) < 0);
        assertTrue(map2.compareTo(map1) > 0);
        assertEquals(0, map1.compareTo(map1));

        FlashMap map3 = new FlashMap();
        map3.put("msg", "hello");
        map3.setTargetRequestName("/target");
        assertEquals(map1, map3);
        assertEquals(map1.hashCode(), map3.hashCode());
        assertNotNull(map1.toString());
    }

    @Test
    void testSessionFlashMapManagerLifecycle() throws ActivityContextBuilderException, ActivityPerformException {
        HybridActivityContextBuilder builder = new HybridActivityContextBuilder();
        ActivityContext context = builder.build();

        SessionFlashMapManager manager = new SessionFlashMapManager();
        manager.setFlashMapTimeout(60);
        assertEquals(60, manager.getFlashMapTimeout());

        MockSessionAdapter sessionAdapter = new MockSessionAdapter();

        InstantActivity senderActivity = new InstantActivity(context);
        senderActivity.setSessionAdapter(sessionAdapter);

        // Save flash attribute in sender
        senderActivity.perform(() -> {
            TransletRule transletRule = new TransletRule();
            transletRule.setName("/sender");
            Translet translet = new CoreTranslet(transletRule, senderActivity);
            translet.getOutputFlashMap().put("greeting", "Hello FlashMap");
            translet.getOutputFlashMap().setTargetRequestName("/welcome");
            manager.saveFlashMap(translet);
            return null;
        });

        // Receiver activity matching targetRequestName
        InstantActivity receiverActivity = new InstantActivity(context);
        receiverActivity.setSessionAdapter(sessionAdapter);

        receiverActivity.perform(() -> {
            TransletRule mismatchRule = new TransletRule();
            CoreTranslet mismatchTranslet = new CoreTranslet(mismatchRule, receiverActivity);
            mismatchTranslet.setRequestName("/other");

            // Test non-matching request
            FlashMap mismatch = manager.retrieveAndUpdate(mismatchTranslet);
            assertNull(mismatch);

            // Matching target request name
            TransletRule matchRule = new TransletRule();
            CoreTranslet matchTranslet = new CoreTranslet(matchRule, receiverActivity);
            matchTranslet.setRequestName("/welcome");

            FlashMap directMatch = manager.retrieveAndUpdate(matchTranslet);
            assertNotNull(directMatch);
            assertEquals("Hello FlashMap", directMatch.get("greeting"));

            // Second retrieve should return null (already consumed/removed)
            FlashMap consumed = manager.retrieveAndUpdate(matchTranslet);
            assertNull(consumed);
            return null;
        });

        builder.destroy();
    }

    private static class MockSessionAdapter extends AbstractSessionAdapter {
        private final Map<String, Object> attributes = new HashMap<>();

        public MockSessionAdapter() {
            super(new Object());
        }

        @Override
        public String getId() {
            return "mock-session-id";
        }

        @Override
        public boolean isNew() {
            return false;
        }

        @Override
        public long getCreationTime() {
            return 0;
        }

        @Override
        public long getLastAccessedTime() {
            return 0;
        }

        @Override
        public int getMaxInactiveInterval() {
            return 0;
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(attributes.keySet());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String name) {
            return (T)attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            if (value != null) {
                attributes.put(name, value);
            } else {
                attributes.remove(name);
            }
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        @Override
        public void invalidate() {
            attributes.clear();
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

}
