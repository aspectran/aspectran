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
package com.aspectran.core.component.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for FileSessionStore.
 */
class FileSessionStoreTest {

    private FileSessionStore sessionStore;

    @BeforeEach
    void beforeEach() throws Exception {
        File storeDir = new File("./target/_sessions/fileSessionStoreTest");
        storeDir.mkdirs();
        try (Stream<Path> stream = Files.walk(storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)) {
            stream.filter(p -> !Files.isDirectory(p))
                    .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        sessionStore = new FileSessionStore(storeDir);
        sessionStore.initialize();
    }

    @AfterEach
    void afterEach() throws Exception {
        if (sessionStore != null && sessionStore.isInitialized()) {
            sessionStore.destroy();
        }
    }

    @Test
    void testSaveAndLoad() throws Exception {
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
    void testExists() throws Exception {
        String id = "test-session-2";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);

        assertFalse(sessionStore.exists(id));
        sessionStore.doSave(id, data);
        assertTrue(sessionStore.exists(id));
    }

    @Test
    void testDelete() throws Exception {
        String id = "test-session-3";
        SessionData data = new SessionData(id, Instant.now().toEpochMilli(), -1L);

        sessionStore.doSave(id, data);
        assertTrue(sessionStore.exists(id));

        assertTrue(sessionStore.delete(id));
        assertFalse(sessionStore.exists(id));
        assertNull(sessionStore.load(id));
    }

    @Test
    void testGetExpired() throws Exception {
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
    void testUpdateExistingSession() throws Exception {
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
    void testSessionExpiry() throws Exception {
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
    void testLoadNonExistentSession() throws Exception {
        String id = "non-existent-session";
        assertNull(sessionStore.load(id));
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
    void testDifferentDataTypes() throws Exception {
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
