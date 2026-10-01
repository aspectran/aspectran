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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for scanning files.
 */
class FileScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void testFileScan() {
        FileScanner scanner = new FileScanner("./target/test-classes");
        Map<String, File> map = scanner.scan("**/utils/*Test.class");
        for (Map.Entry<String, File> entry : map.entrySet()) {
            assertTrue(entry.getValue().toString().replace(File.separatorChar, '/').endsWith(entry.getKey()));
        }
    }

    @Test
    void testScanWithTempDir() throws IOException {
        Path subDir = Files.createDirectories(tempDir.resolve("sub/dir"));
        Files.createFile(subDir.resolve("test1.txt"));
        Files.createFile(subDir.resolve("test2.log"));
        Files.createFile(tempDir.resolve("root.txt"));

        String basePath = tempDir.toString().replace(File.separatorChar, '/');
        FileScanner scanner = new FileScanner(basePath);

        Map<String, File> txtFiles = scanner.scan("**/*.txt");
        assertEquals(2, txtFiles.size());
        assertTrue(txtFiles.containsKey("root.txt"));
        assertTrue(txtFiles.containsKey("sub/dir/test1.txt"));
        assertFalse(txtFiles.containsKey("sub/dir/test2.log"));
    }

    @Test
    void testScanWithLeadingSlash() throws IOException {
        Path subDir = Files.createDirectories(tempDir.resolve("logs"));
        Files.createFile(subDir.resolve("app.log"));

        String basePath = tempDir.toString().replace(File.separatorChar, '/');
        FileScanner scanner = new FileScanner();

        Map<String, File> logFiles = scanner.scan(basePath + "/logs/*.log");
        assertEquals(1, logFiles.size());
        assertTrue(logFiles.containsKey(basePath + "/logs/app.log"));
    }

    @Test
    void testValidation() {
        FileScanner scanner = new FileScanner();
        assertThrows(IllegalArgumentException.class, () -> scanner.scan(null));
        assertThrows(IllegalArgumentException.class, () -> scanner.scan(""));
    }

}
