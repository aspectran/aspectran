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
import io.lettuce.core.api.sync.RedisServerCommands;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link DefaultLettuceSessionStore}.
 */
@Testcontainers(disabledWithoutDocker = true)
class DefaultLettuceSessionStoreTest {

    private static GenericContainer<?> redis;

    private DefaultLettuceSessionStore sessionStore;

    @BeforeAll
    static void startContainer() {
        redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
        redis.start();
    }

    @AfterAll
    static void stopContainer() {
        if (redis != null) {
            redis.stop();
        }
    }

    @BeforeEach
    void beforeEach() throws Exception {
        RedisConnectionPoolConfig poolConfig = new RedisConnectionPoolConfig();
        poolConfig.setUri("redis://" + redis.getHost() + ":" + redis.getFirstMappedPort());

        RedisConnectionPool pool = new RedisConnectionPool(poolConfig);
        sessionStore = new DefaultLettuceSessionStore(pool);

        // Set non-persistent attributes before initialization
        sessionStore.setNonPersistentAttributes(new String[]{"temp-data"});

        sessionStore.initialize();

        // Clean up all keys in Redis before each test
        sessionStore.sync(RedisServerCommands::flushall);
    }

    @AfterEach
    void afterEach() throws Exception {
        if (sessionStore != null && sessionStore.isInitialized()) {
            sessionStore.destroy();
        }
    }

    @Test
    void testSaveAndLoad() {
        String id = "test-session-1";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);
        data.setAttribute("name", "John Doe");
        data.setAttribute("age", 40);

        sessionStore.doSave(id, data);

        SessionData loadedData = sessionStore.load(id);
        assertNotNull(loadedData);
        assertEquals(id, loadedData.getId());
        assertEquals("John Doe", loadedData.getAttribute("name"));
        assertEquals(40, (Integer)loadedData.getAttribute("age"));
    }

    @Test
    void testExists() {
        String id = "test-session-2";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);

        assertFalse(sessionStore.exists(id));
        sessionStore.doSave(id, data);
        assertTrue(sessionStore.exists(id));
    }

    @Test
    void testDelete() {
        String id = "test-session-3";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);

        sessionStore.doSave(id, data);
        assertTrue(sessionStore.exists(id));

        assertTrue(sessionStore.delete(id));
        assertFalse(sessionStore.exists(id));
        assertNull(sessionStore.load(id));
    }

    @Test
    void testGetExpired() throws InterruptedException {
        long now = System.currentTimeMillis();

        SessionData session1 = new SessionData("expired-1", now, 1000);
        SessionData session2 = new SessionData("active-1", now, 5000);
        SessionData session3 = new SessionData("immortal-1", now, 0);

        sessionStore.doSave(session1.getId(), session1);
        sessionStore.doSave(session2.getId(), session2);
        sessionStore.doSave(session3.getId(), session3);

        // Wait for session1 to expire
        Thread.sleep(1500);

        Set<String> expiredIds = sessionStore.doGetExpired(System.currentTimeMillis());

        assertEquals(1, expiredIds.size());
        assertTrue(expiredIds.contains("expired-1"));
        assertFalse(expiredIds.contains("active-1"));
        assertFalse(expiredIds.contains("immortal-1"));
    }

    @Test
    void testNonPersistentAttributes() {
        String id = "test-session-4";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);
        data.setAttribute("persistent-data", "This should be saved");
        data.setAttribute("temp-data", "This should NOT be saved");

        sessionStore.doSave(id, data);

        SessionData loadedData = sessionStore.load(id);
        assertNotNull(loadedData);
        assertEquals("This should be saved", loadedData.getAttribute("persistent-data"));
        assertNull(loadedData.getAttribute("temp-data"));
    }

    @Test
    void testUpdateExistingSession() {
        String id = "test-session-update";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);
        data.setAttribute("status", "initial");
        sessionStore.doSave(id, data);

        SessionData loadedData1 = sessionStore.load(id);
        assertNotNull(loadedData1);
        assertEquals("initial", loadedData1.getAttribute("status"));

        // Update the session
        data.setAttribute("status", "updated");
        sessionStore.doSave(id, data);

        SessionData loadedData2 = sessionStore.load(id);
        assertNotNull(loadedData2);
        assertEquals("updated", loadedData2.getAttribute("status"));
    }

    @Test
    void testSessionExpiry() throws InterruptedException {
        String id = "test-session-expiry";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), 1000); // 1 second expiry
        sessionStore.doSave(id, data);

        assertTrue(sessionStore.exists(id));

        // Wait for the session to expire
        Thread.sleep(1500);

        // Check for logical expiry
        assertFalse(sessionStore.exists(id));

        // Simulate scavenger: get expired session IDs and delete them
        Set<String> expiredIds = sessionStore.doGetExpired(System.currentTimeMillis());
        for (String expiredId : expiredIds) {
            sessionStore.delete(expiredId);
        }

        // Now that it's physically deleted, load should return null
        assertNull(sessionStore.load(id));
    }

    @Test
    void testLoadNonExistentSession() {
        String id = "non-existent-session";
        assertNull(sessionStore.load(id));
    }

    @Test
    void testScan() {
        SessionData session1 = new SessionData("scan-1", System.currentTimeMillis(), 0);
        SessionData session2 = new SessionData("scan-2", System.currentTimeMillis(), 0);
        SessionData session3 = new SessionData("scan-3", System.currentTimeMillis(), 0);

        sessionStore.doSave(session1.getId(), session1);
        sessionStore.doSave(session2.getId(), session2);
        sessionStore.doSave(session3.getId(), session3);

        Set<String> scannedIds = new HashSet<>();
        sessionStore.scan(d -> {
            if (d != null && d.getId().startsWith("scan-")) {
                scannedIds.add(d.getId());
            }
        });

        assertEquals(3, scannedIds.size());
        assertTrue(scannedIds.contains("scan-1"));
        assertTrue(scannedIds.contains("scan-2"));
        assertTrue(scannedIds.contains("scan-3"));
    }

    private static class CustomObject implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private final String value;

        public CustomObject(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CustomObject that = (CustomObject) obj;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    @Test
    void testDifferentDataTypes() {
        String id = "test-session-types";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);

        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 100);
        map.put("key2", 200);

        CustomObject customObject = new CustomObject("custom-data");

        data.setAttribute("list-data", list);
        data.setAttribute("map-data", map);
        data.setAttribute("custom-object-data", customObject);

        sessionStore.doSave(id, data);

        SessionData loadedData = sessionStore.load(id);
        assertNotNull(loadedData);
        assertEquals(list, loadedData.getAttribute("list-data"));
        assertEquals(map, loadedData.getAttribute("map-data"));
        assertEquals(customObject, loadedData.getAttribute("custom-object-data"));
    }

}
