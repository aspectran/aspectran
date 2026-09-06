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
package com.aspectran.web.servlet.support.multipart.standard;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.web.servlet.mock.MockHttpServletRequest;
import com.aspectran.web.activity.request.MultipartFormDataParser;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import com.aspectran.web.servlet.adapter.HttpServletRequestAdapter;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link StandardServletMultipartFormDataParser}.
 */
class StandardServletMultipartFormDataParserTest {

    @TempDir
    Path tempDir;

    @Test
    void testFormFieldsAndFileParsing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=testBoundary");

        request.addPart(new MockPart("username", null, null, "aspectranUser".getBytes(StandardCharsets.UTF_8)));

        byte[] fileBytes = "Standard Servlet File Content".getBytes(StandardCharsets.UTF_8);
        MockPart filePart = new MockPart("profileImage", "nested/profile.png", "image/png", fileBytes);
        request.addPart(filePart);

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.POST, request);
        adapter.preparse();

        StandardServletMultipartFormDataParser parser = new StandardServletMultipartFormDataParser();
        parser.setAllowedFileExtensions("png,jpg");
        parser.parse(adapter);

        // Verify form field
        assertEquals("aspectranUser", adapter.getParameter("username"));

        // Verify file parameter
        FileParameter fileParam = adapter.getFileParameter("profileImage");
        assertNotNull(fileParam);
        assertInstanceOf(StandardServletMultipartFileParameter.class, fileParam);
        assertEquals("profile.png", fileParam.getFileName());
        assertEquals("image/png", fileParam.getContentType());
        assertEquals(fileBytes.length, fileParam.getFileSize());
        assertArrayEquals(fileBytes, fileParam.getBytes());

        // Test saveAs with Path
        Path destPath = tempDir.resolve("saved-profile.png");
        Path savedPath = fileParam.saveAs(destPath);
        assertEquals(destPath, savedPath);
        assertTrue(Files.exists(savedPath));
        assertArrayEquals(fileBytes, Files.readAllBytes(savedPath));

        // Test moveTo with Path
        Path moveTarget = tempDir.resolve("moved-profile.png");
        Path movedPath = fileParam.moveTo(moveTarget);
        assertEquals(moveTarget, movedPath);
        assertTrue(Files.exists(moveTarget));
        assertArrayEquals(fileBytes, Files.readAllBytes(moveTarget));

        fileParam.delete();
        assertTrue(filePart.isDeleted());
    }

    @Test
    void testAllowedAndDeniedExtensions() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        request.addPart(new MockPart("file1", "document.pdf", "application/pdf", "pdf data".getBytes()));
        request.addPart(new MockPart("file2", "script.sh", "text/plain", "sh data".getBytes()));

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.POST, request);
        adapter.preparse();

        StandardServletMultipartFormDataParser parser = new StandardServletMultipartFormDataParser();
        parser.setAllowedFileExtensions("pdf");
        parser.setDeniedFileExtensions("sh,exe");
        parser.parse(adapter);

        assertNotNull(adapter.getFileParameter("file1"));
        assertNull(adapter.getFileParameter("file2"));
    }

    @Test
    void testMaxFileSizeExceeded() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");

        byte[] largeData = new byte[1024];
        request.addPart(new MockPart("bigFile", "data.bin", "application/octet-stream", largeData));

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.POST, request);
        adapter.preparse();

        StandardServletMultipartFormDataParser parser = new StandardServletMultipartFormDataParser();
        parser.setMaxFileSize(512); // Limit to 512 bytes

        MultipartRequestParseException ex = assertThrows(MultipartRequestParseException.class, () -> parser.parse(adapter));
        assertInstanceOf(SizeLimitExceededException.class, ex.getCause());
    }

    @Test
    void testMaxRequestSizeExceeded() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data");
        request.setBody(new byte[2048]);

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.POST, request);
        adapter.preparse();

        StandardServletMultipartFormDataParser parser = new StandardServletMultipartFormDataParser();
        parser.setMaxRequestSize(1024);

        MultipartRequestParseException ex = assertThrows(MultipartRequestParseException.class, () -> parser.parse(adapter));
        assertInstanceOf(SizeLimitExceededException.class, ex.getCause());
    }

    @Test
    void testFactoryBean() throws Exception {
        StandardServletMultipartFormDataParserFactoryBean factoryBean =
                new StandardServletMultipartFormDataParserFactoryBean();
        factoryBean.setMaxRequestSize("2MB");
        factoryBean.setMaxFileSize("1MB");
        factoryBean.setMaxInMemorySize("64KB");
        factoryBean.setAllowedFileExtensions("txt,csv");
        factoryBean.initialize();

        MultipartFormDataParser parser = factoryBean.getObject();
        assertNotNull(parser);
        assertInstanceOf(StandardServletMultipartFormDataParser.class, parser);
    }

    private static class MockPart implements Part {
        private final String name;
        private final String submittedFileName;
        private final String contentType;
        private final byte[] content;
        private boolean deleted;

        public MockPart(String name, String submittedFileName, String contentType, byte[] content) {
            this.name = name;
            this.submittedFileName = submittedFileName;
            this.contentType = contentType;
            this.content = (content != null ? content : new byte[0]);
        }

        @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override public String getContentType() { return contentType; }
        @Override public String getName() { return name; }
        @Override public String getSubmittedFileName() { return submittedFileName; }
        @Override public long getSize() { return content.length; }
        @Override
        public void write(String fileName) throws IOException {
            try (OutputStream out = new FileOutputStream(fileName)) {
                out.write(content);
            }
        }
        @Override public void delete() { this.deleted = true; }
        public boolean isDeleted() { return deleted; }
        @Override public String getHeader(String name) { return null; }
        @Override public Collection<String> getHeaders(String name) { return Collections.emptyList(); }
        @Override public Collection<String> getHeaderNames() { return Collections.emptyList(); }
    }

}
