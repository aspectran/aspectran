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
import com.aspectran.core.component.session.redis.lettuce.junit.EnabledIfDockerAvailable;
import io.lettuce.core.api.sync.RedisServerCommands;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link DefaultLettuceSessionStore}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@EnabledIfDockerAvailable
class DefaultLettuceSessionStoreTest {

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private DefaultLettuceSessionStore sessionStore;

    @BeforeAll
    void beforeAll() {
        RedisConnectionPoolConfig poolConfig = new RedisConnectionPoolConfig();
        poolConfig.setUri("redis://" + redis.getHost() + ":" + redis.getFirstMappedPort());

        RedisConnectionPool pool = new RedisConnectionPool(poolConfig);
        sessionStore = new DefaultLettuceSessionStore(pool);
    }

    @BeforeEach
    void beforeEach() throws Exception {
        if (!sessionStore.isInitialized()) {
            sessionStore.initialize();
        }
        // Clean up all keys in Redis before each test
        sessionStore.sync(RedisServerCommands::flushall);
    }

    @AfterAll
    void afterAll() throws Exception {
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
        long expiryTime1 = now + 1000; // Expires in 1 sec
        long expiryTime2 = now + 5000; // Expires in 5 sec

        SessionData session1 = new SessionData("expired-1", now, now, expiryTime1, 1000);
        SessionData session2 = new SessionData("active-1", now, now, expiryTime2, 5000);
        SessionData session3 = new SessionData("immortal-1", now, now, 0, 0);

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
        String[] nonPersistentAttributes = new String[] {"temp-data"};

        sessionStore.setNonPersistentAttributes(nonPersistentAttributes);

        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);
        data.setAttribute("persistent-data", "This should be saved");
        data.setAttribute("temp-data", "This should NOT be saved");

        sessionStore.doSave(id, data);

        SessionData loadedData = sessionStore.load(id);
        assertNotNull(loadedData);
        assertEquals("This should be saved", loadedData.getAttribute("persistent-data"));
        assertNull(loadedData.getAttribute("temp-data"));
    }

}
