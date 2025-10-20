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
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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

}
