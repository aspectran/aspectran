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
package com.aspectran.netty.support.multipart;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.MemoryFileUpload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link NettyFileParameter}.
 */
class NettyFileParameterTest {

    @TempDir
    Path tempDir;

    @Test
    void testInMemoryFileUpload() throws IOException {
        byte[] content = "Hello, Netty FileParameter!".getBytes(StandardCharsets.UTF_8);
        MemoryFileUpload fileUpload = new MemoryFileUpload(
                "testParam", "sample.txt", "text/plain", null, StandardCharsets.UTF_8, content.length);
        fileUpload.setContent(Unpooled.wrappedBuffer(content));

        NettyFileParameter parameter = new NettyFileParameter(fileUpload);

        assertEquals("sample.txt", parameter.getFileName());
        assertEquals("text/plain", parameter.getContentType());
        assertEquals(content.length, parameter.getFileSize());
        assertNull(parameter.getFile());
        assertArrayEquals(content, parameter.getBytes());

        try (InputStream in = parameter.getInputStream()) {
            assertArrayEquals(content, in.readAllBytes());
        }

        File destFile = tempDir.resolve("saved-sample.txt").toFile();
        parameter.saveAs(destFile, false);
        assertTrue(destFile.exists());
        assertEquals("Hello, Netty FileParameter!", Files.readString(destFile.toPath()));

        // Overwrite false should throw IOException
        assertThrows(IOException.class, () -> parameter.saveAs(destFile, false));

        // Overwrite true should succeed
        parameter.saveAs(destFile, true);
        assertTrue(destFile.exists());

        parameter.delete();
    }

    @Test
    void testDiskFileUpload() throws IOException {
        DiskFileUpload.baseDirectory = tempDir.toString();
        byte[] content = new byte[] { 0x10, 0x20, 0x30, 0x40 };

        DiskFileUpload fileUpload = new DiskFileUpload(
                "diskParam", "nested/data.bin", "application/octet-stream",
                null, StandardCharsets.UTF_8, content.length);
        fileUpload.setContent(Unpooled.wrappedBuffer(content));

        NettyFileParameter parameter = new NettyFileParameter(fileUpload);

        // FilenameUtils.getName should extract only "data.bin"
        assertEquals("data.bin", parameter.getFileName());
        assertEquals("application/octet-stream", parameter.getContentType());
        assertEquals(content.length, parameter.getFileSize());
        assertNotNull(parameter.getFile());
        assertTrue(parameter.getFile().exists());
        assertArrayEquals(content, parameter.getBytes());

        try (InputStream in = parameter.getInputStream()) {
            assertArrayEquals(content, in.readAllBytes());
        }

        File destFile = tempDir.resolve("sub/dir/saved-data.bin").toFile();
        parameter.saveAs(destFile, false);
        assertTrue(destFile.exists());
        assertArrayEquals(content, Files.readAllBytes(destFile.toPath()));

        parameter.delete();
        assertTrue(destFile.delete());
    }

}
