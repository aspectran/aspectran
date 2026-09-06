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
package com.aspectran.core.activity.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link FileParameter}.
 */
class FileParameterTest {

    @TempDir
    Path tempDir;

    @Test
    void testBasicPropertiesAndContent() throws IOException {
        Path sourcePath = tempDir.resolve("sample.txt");
        byte[] content = "Aspectran FileParameter Test Content".getBytes(StandardCharsets.UTF_8);
        Files.write(sourcePath, content);

        FileParameter parameter = new FileParameter(sourcePath.toFile(), "text/plain");
        assertEquals("sample.txt", parameter.getFileName());
        assertEquals("text/plain", parameter.getContentType());
        assertEquals(content.length, parameter.getFileSize());
        assertNotNull(parameter.getFile());
        assertArrayEquals(content, parameter.getBytes());

        try (InputStream in = parameter.getInputStream()) {
            assertArrayEquals(content, in.readAllBytes());
        }

        assertFalse(parameter.isRefused());
        parameter.setRefused(true);
        assertTrue(parameter.isRefused());
    }

    @Test
    void testSaveAsWithPath() throws IOException {
        Path sourcePath = tempDir.resolve("original.txt");
        byte[] content = "SaveAs Test".getBytes(StandardCharsets.UTF_8);
        Files.write(sourcePath, content);

        FileParameter parameter = new FileParameter(sourcePath.toFile());
        Path destPath = tempDir.resolve("saved-copy.txt");

        Path result = parameter.saveAs(destPath);
        assertEquals(destPath, result);
        assertTrue(Files.exists(destPath));
        assertArrayEquals(content, Files.readAllBytes(destPath));
        assertEquals(destPath, parameter.getSavedPath());
        assertEquals(destPath.toFile(), parameter.getSavedFile());

        // Overwrite test
        parameter.saveAs(destPath, true);
        assertTrue(Files.exists(destPath));

        parameter.deleteSavedFile();
        assertFalse(Files.exists(destPath));
    }

    @Test
    void testMoveToWithPath() throws IOException {
        Path sourcePath = tempDir.resolve("to-move.txt");
        byte[] content = "MoveTo Test".getBytes(StandardCharsets.UTF_8);
        Files.write(sourcePath, content);

        FileParameter parameter = new FileParameter(sourcePath.toFile());
        Path destPath = tempDir.resolve("moved.txt");

        Path result = parameter.moveTo(destPath);
        assertEquals(destPath, result);
        assertTrue(Files.exists(destPath));
        assertFalse(Files.exists(sourcePath));
        assertArrayEquals(content, Files.readAllBytes(destPath));
        assertEquals(destPath, parameter.getSavedPath());
    }

    @Test
    void testDeleteAndRelease() throws IOException {
        Path sourcePath = tempDir.resolve("to-delete.txt");
        Files.writeString(sourcePath, "Delete Test");

        FileParameter parameter = new FileParameter(sourcePath.toFile());
        assertTrue(Files.exists(sourcePath));

        parameter.delete();
        assertFalse(Files.exists(sourcePath));

        parameter.release();
        assertNull(parameter.getSavedPath());
    }

    @Test
    void testCleanupFileParameters() throws IOException {
        Path tempFile1 = tempDir.resolve("auto-clean-1.txt");
        Path tempFile2 = tempDir.resolve("auto-clean-2.txt");
        Files.writeString(tempFile1, "clean1");
        Files.writeString(tempFile2, "clean2");

        com.aspectran.core.adapter.DefaultRequestAdapter adapter =
                new com.aspectran.core.adapter.DefaultRequestAdapter(com.aspectran.core.context.rule.type.MethodType.POST);
        adapter.setFileParameter("f1", new FileParameter(tempFile1.toFile()));
        adapter.setFileParameter("f2", new FileParameter(tempFile2.toFile()));

        assertTrue(Files.exists(tempFile1));
        assertTrue(Files.exists(tempFile2));

        adapter.cleanupFileParameters();

        assertFalse(Files.exists(tempFile1));
        assertFalse(Files.exists(tempFile2));
    }

}
