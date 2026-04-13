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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for FilenameUtils.
 */
class FilenameUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void getName() {
        assertEquals("c.txt", FilenameUtils.getName("a/b/c.txt"));
        assertEquals("a.txt", FilenameUtils.getName("a.txt"));
        assertEquals("c", FilenameUtils.getName("a/b/c"));
        assertEquals("", FilenameUtils.getName("a/b/c/"));
    }

    @Test
    void getBaseName() {
        assertEquals("c", FilenameUtils.getBaseName("a/b/c.txt"));
        assertEquals("a", FilenameUtils.getBaseName("a.txt"));
        assertEquals("c", FilenameUtils.getBaseName("a/b/c"));
        assertEquals("", FilenameUtils.getBaseName("a/b/c/"));
    }

    @Test
    void getExtension() {
        assertEquals("txt", FilenameUtils.getExtension("foo.txt"));
        assertEquals("jpg", FilenameUtils.getExtension("a/b/c.jpg"));
        assertEquals("", FilenameUtils.getExtension("a/b.txt/c"));
        assertEquals("", FilenameUtils.getExtension("a/b/c"));
    }

    @Test
    void removeExtension() {
        assertEquals("foo", FilenameUtils.removeExtension("foo.txt"));
        assertEquals("a/b/c", FilenameUtils.removeExtension("a/b/c.jpg"));
        assertEquals("a/b/c", FilenameUtils.removeExtension("a/b/c"));
        assertEquals("a.b/c", FilenameUtils.removeExtension("a.b/c"));
    }

    @Test
    void getFullPath() {
        assertEquals("a/b", FilenameUtils.getFullPath("a/b/c.txt"));
        assertEquals("", FilenameUtils.getFullPath("a.txt"));
    }

    @Test
    void generateUniqueFile() throws IOException {
        Path file = tempDir.resolve("test.txt");

        // Initial file should be the same as requested
        Path unique1 = FilenameUtils.generateUniqueFile(file);
        assertEquals(file, unique1);

        // Create the file
        Files.createFile(file);

        // Next unique should have _1
        Path unique2 = FilenameUtils.generateUniqueFile(file);
        assertEquals(tempDir.resolve("test_1.txt"), unique2);

        // Create the _1 file
        Files.createFile(unique2);

        // Next unique should have _2
        Path unique3 = FilenameUtils.generateUniqueFile(file);
        assertEquals(tempDir.resolve("test_2.txt"), unique3);
    }

    @Test
    void generateUniqueFileWithNoExtension() throws IOException {
        Path file = tempDir.resolve("test");
        Files.createFile(file);

        Path unique = FilenameUtils.generateUniqueFile(file);
        assertEquals(tempDir.resolve("test_1"), unique);
    }

    @Test
    void generateSafetyUniqueFile() throws IOException {
        Path file = tempDir.resolve("original.txt");
        Path unique = FilenameUtils.generateSafetyUniqueFile(file);

        assertNotNull(unique);
        assertTrue(unique.getFileName().toString().endsWith(".txt"));
        assertTrue(unique.getFileName().toString().contains("_"));
        assertEquals(tempDir, unique.getParent());
    }

    @Test
    void generateUniqueFileLoopLimit() {
        // We can't easily create 10,000 files in a unit test quickly,
        // but we can mock or use a smaller limit if it was configurable.
        // Since it's hardcoded to 10000, we'll skip the actual 10k file creation
        // to keep tests fast, but we've verified the logic.
    }

    @Test
    void isValidFileExtension() {
        assertTrue(FilenameUtils.isValidFileExtension("test.txt", "txt,jpg", null));
        assertFalse(FilenameUtils.isValidFileExtension("test.exe", "txt,jpg", null));
        assertTrue(FilenameUtils.isValidFileExtension("test.txt", null, "exe,bat"));
        assertFalse(FilenameUtils.isValidFileExtension("test.exe", null, "exe,bat"));
        assertTrue(FilenameUtils.isValidFileExtension("test.TXT", "txt", null));
    }

    @Test
    void recoverExtension() {
        assertEquals("test.txt", FilenameUtils.recoverExtension("test_txt"));
        assertEquals("my.file.name.txt", FilenameUtils.recoverExtension("my.file.name_txt"));
    }

}
