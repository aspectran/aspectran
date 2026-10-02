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
package com.aspectran.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileLockerTest {

    @Test
    void testLockAndRelease(@TempDir Path tempDir) throws IOException {
        File lockFile = tempDir.resolve("test.lock").toFile();
        FileLocker locker = new FileLocker(lockFile);

        assertFalse(locker.isLocked());
        assertEquals(-1L, locker.getLockedPid());
        assertEquals(lockFile, locker.getLockFile());

        assertTrue(locker.lock());
        assertTrue(locker.isLocked());
        assertTrue(lockFile.exists());
        assertEquals(ProcessHandle.current().pid(), locker.getLockedPid());

        locker.release();
        assertFalse(locker.isLocked());
        assertEquals(-1L, locker.getLockedPid());
        assertFalse(lockFile.exists());
    }

    @Test
    void testTryWithResources(@TempDir Path tempDir) throws IOException {
        File lockFile = tempDir.resolve("autoclose.lock").toFile();

        try (FileLocker locker = new FileLocker(lockFile)) {
            assertTrue(locker.lock());
            assertTrue(locker.isLocked());
            assertTrue(lockFile.exists());
        }

        assertFalse(lockFile.exists());
    }

    @Test
    void testConcurrentLockAttempt(@TempDir Path tempDir) throws IOException {
        File lockFile = tempDir.resolve("concurrent.lock").toFile();
        FileLocker locker1 = new FileLocker(lockFile);
        FileLocker locker2 = new FileLocker(lockFile);

        assertTrue(locker1.lock());
        assertTrue(locker1.isLocked());

        // Second locker should fail to acquire lock
        assertFalse(locker2.lock());
        assertFalse(locker2.isLocked());

        locker1.release();
        assertFalse(locker1.isLocked());

        // Now second locker can acquire lock
        assertTrue(locker2.lock());
        assertTrue(locker2.isLocked());

        locker2.release();
        assertFalse(locker2.isLocked());
    }

    @Test
    void testDuplicateLockThrowsException(@TempDir Path tempDir) throws IOException {
        File lockFile = tempDir.resolve("duplicate.lock").toFile();
        FileLocker locker = new FileLocker(lockFile);

        assertTrue(locker.lock());
        assertThrows(IllegalStateException.class, locker::lock);

        locker.release();
    }

    @Test
    void testBasePathConstructor(@TempDir Path tempDir) throws IOException {
        FileLocker locker = new FileLocker(tempDir.toString(), "custom.lock");
        assertTrue(locker.lock());
        assertTrue(locker.isLocked());
        assertTrue(locker.getLockFile().exists());

        locker.release();
        assertFalse(locker.getLockFile().exists());
    }

}
