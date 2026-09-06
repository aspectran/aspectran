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

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
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
 * Test case for {@link NettyMultipartFormDataParser}.
 */
class NettyMultipartFormDataParserTest {

    @TempDir
    Path tempDir;

    @FunctionalInterface
    private interface EncoderConsumer {
        void accept(HttpPostRequestEncoder encoder) throws Exception;
    }

    private FullHttpRequest createMultipartRequest(EncoderConsumer consumer) throws Exception {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, "/upload");
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(factory, request, true);
        consumer.accept(encoder);
        HttpRequest finalizedRequest = encoder.finalizeRequest();

        if (encoder.isChunked()) {
            ByteBuf buffer = Unpooled.buffer();
            while (!encoder.isEndOfInput()) {
                HttpContent chunk = encoder.readChunk(ByteBufAllocator.DEFAULT);
                if (chunk != null) {
                    buffer.writeBytes(chunk.content());
                    chunk.release();
                }
            }
            DefaultFullHttpRequest fullRequest = new DefaultFullHttpRequest(
                    finalizedRequest.protocolVersion(),
                    finalizedRequest.method(),
                    finalizedRequest.uri(),
                    buffer,
                    finalizedRequest.headers(),
                    EmptyHttpHeaders.INSTANCE
            );
            HttpUtil.setTransferEncodingChunked(fullRequest, false);
            HttpUtil.setContentLength(fullRequest, buffer.readableBytes());
            return fullRequest;
        } else {
            return (FullHttpRequest) finalizedRequest;
        }
    }

    @Test
    void testBasicMultipartForm() throws Exception {
        File uploadFile = tempDir.resolve("sample.txt").toFile();
        Files.writeString(uploadFile.toPath(), "Hello from Aspectran multipart!");

        FullHttpRequest request = createMultipartRequest(encoder -> {
            encoder.addBodyAttribute("title", "Aspectran Netty");
            encoder.addBodyAttribute("author", "Juho Jeong");
            encoder.addBodyFileUpload("file", uploadFile, "text/plain", false);
        });

        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.POST, request);
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        parser.parse(requestAdapter);

        assertEquals("Aspectran Netty", requestAdapter.getParameter("title"));
        assertEquals("Juho Jeong", requestAdapter.getParameter("author"));

        FileParameter fileParameter = requestAdapter.getFileParameter("file");
        assertNotNull(fileParameter);
        assertEquals("sample.txt", fileParameter.getFileName());
        assertEquals(31, fileParameter.getFileSize());
        assertEquals("Hello from Aspectran multipart!", new String(fileParameter.getBytes(), StandardCharsets.UTF_8));
        try (InputStream in = fileParameter.getInputStream()) {
            assertEquals("Hello from Aspectran multipart!", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testMultipleFilesSameParameterName() throws Exception {
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        Files.writeString(file1.toPath(), "Content 1");
        Files.writeString(file2.toPath(), "Content 2");

        FullHttpRequest request = createMultipartRequest(encoder -> {
            encoder.addBodyFileUpload("files", file1, "text/plain", false);
            encoder.addBodyFileUpload("files", file2, "text/plain", false);
        });

        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.POST, request);
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        parser.parse(requestAdapter);

        FileParameter[] fileParameters = requestAdapter.getFileParameterValues("files");
        assertNotNull(fileParameters);
        assertEquals(2, fileParameters.length);
        assertEquals("file1.txt", fileParameters[0].getFileName());
        assertEquals("file2.txt", fileParameters[1].getFileName());
        assertArrayEquals("Content 1".getBytes(StandardCharsets.UTF_8), fileParameters[0].getBytes());
        assertArrayEquals("Content 2".getBytes(StandardCharsets.UTF_8), fileParameters[1].getBytes());
    }

    @Test
    void testMaxRequestSizeExceeded() throws Exception {
        File uploadFile = tempDir.resolve("payload.txt").toFile();
        Files.writeString(uploadFile.toPath(), "This payload is certainly larger than 10 bytes");

        FullHttpRequest request = createMultipartRequest(encoder -> {
            encoder.addBodyFileUpload("file", uploadFile, "text/plain", false);
        });

        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.POST, request);
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        parser.setMaxRequestSize(10L);

        assertThrows(MultipartRequestParseException.class, () -> parser.parse(requestAdapter));
    }

    @Test
    void testMaxFileSizeExceeded() throws Exception {
        File uploadFile = tempDir.resolve("large-file.txt").toFile();
        Files.writeString(uploadFile.toPath(), "01234567890123456789");

        FullHttpRequest request = createMultipartRequest(encoder -> {
            encoder.addBodyFileUpload("file", uploadFile, "text/plain", false);
        });

        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.POST, request);
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        parser.setMaxFileSize(5L);

        assertThrows(MultipartRequestParseException.class, () -> parser.parse(requestAdapter));
    }

    @Test
    void testAllowedFileExtensions() throws Exception {
        File allowedFile = tempDir.resolve("photo.png").toFile();
        File deniedFile = tempDir.resolve("script.sh").toFile();
        Files.writeString(allowedFile.toPath(), "fake png");
        Files.writeString(deniedFile.toPath(), "echo hacked");

        FullHttpRequest request = createMultipartRequest(encoder -> {
            encoder.addBodyFileUpload("allowed", allowedFile, "image/png", false);
            encoder.addBodyFileUpload("denied", deniedFile, "text/x-shellscript", false);
        });

        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.POST, request);
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        parser.setAllowedFileExtensions("png,jpg");
        parser.parse(requestAdapter);

        assertNotNull(requestAdapter.getFileParameter("allowed"));
        assertEquals("photo.png", requestAdapter.getFileParameter("allowed").getFileName());
        assertNull(requestAdapter.getFileParameter("denied"));
    }

    @Test
    void testDeniedFileExtensions() throws Exception {
        File allowedFile = tempDir.resolve("readme.txt").toFile();
        File deniedFile = tempDir.resolve("malware.exe").toFile();
        Files.writeString(allowedFile.toPath(), "safe text");
        Files.writeString(deniedFile.toPath(), "binary payload");

        FullHttpRequest request = createMultipartRequest(encoder -> {
            encoder.addBodyFileUpload("readme", allowedFile, "text/plain", false);
            encoder.addBodyFileUpload("malware", deniedFile, "application/octet-stream", false);
        });

        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.POST, request);
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        parser.setDeniedFileExtensions("exe,bat");
        parser.parse(requestAdapter);

        assertNotNull(requestAdapter.getFileParameter("readme"));
        assertEquals("readme.txt", requestAdapter.getFileParameter("readme").getFileName());
        assertNull(requestAdapter.getFileParameter("malware"));
    }

    @Test
    void testTempFileDir() throws IOException {
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        Path customDir = tempDir.resolve("custom_upload_tmp");
        parser.setTempFileDir(customDir.toString());

        assertEquals(customDir.toString(), parser.getTempFileDir());
        assertTrue(Files.isDirectory(customDir));

        // When given a path that exists but is a file (not a directory)
        Path regularFile = tempDir.resolve("regular.txt");
        Files.createFile(regularFile);
        assertThrows(IOException.class, () -> parser.setTempFileDir(regularFile.toString()));
    }

    @Test
    void testInvalidAdaptee() {
        NettyMultipartFormDataParser parser = new NettyMultipartFormDataParser();
        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.POST, "NotAFullHttpRequest");

        assertThrows(MultipartRequestParseException.class, () -> parser.parse(requestAdapter));
    }

}
