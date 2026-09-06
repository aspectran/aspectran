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
package com.aspectran.web.servlet.support.multipart.commons;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.web.servlet.mock.MockHttpServletRequest;
import com.aspectran.web.activity.request.MultipartRequestParseException;
import com.aspectran.web.servlet.adapter.HttpServletRequestAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link CommonsMultipartFormDataParser}.
 */
class CommonsMultipartFormDataParserTest {

    private static final String BOUNDARY = "----WebKitFormBoundary7MA4YWxkTrZu0gW";

    @TempDir
    Path tempDir;

    private byte[] createMultipartBody(String fieldName, String fieldValue,
                                       String fileFieldName, String fileName,
                                       String fileContentType, byte[] fileContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String CRLF = "\r\n";

        if (fieldName != null && fieldValue != null) {
            baos.write(("--" + BOUNDARY + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
            baos.write(fieldValue.getBytes(StandardCharsets.UTF_8));
            baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
        }

        if (fileFieldName != null && fileName != null) {
            baos.write(("--" + BOUNDARY + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + fileName + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(("Content-Type: " + fileContentType + CRLF).getBytes(StandardCharsets.UTF_8));
            baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
            baos.write(fileContent);
            baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
        }

        baos.write(("--" + BOUNDARY + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        return baos.toByteArray();
    }

    @Test
    void testFormFieldsAndFileParsing() throws Exception {
        byte[] fileBytes = "Hello Commons FileUpload!".getBytes(StandardCharsets.UTF_8);
        byte[] body = createMultipartBody("username", "aspectranUser", "profileImage", "profile.png", "image/png", fileBytes);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=" + BOUNDARY);
        request.setHeader("Content-Length", String.valueOf(body.length));
        request.setBody(body);
        request.setQueryString("category=avatar&mode=test");

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.POST, request);
        adapter.preparse();

        // Query parameters should be extracted during preparse
        assertEquals("avatar", adapter.getParameter("category"));
        assertEquals("test", adapter.getParameter("mode"));

        CommonsMultipartFormDataParser parser = new CommonsMultipartFormDataParser();
        parser.setAllowedFileExtensions("png,jpg");
        parser.parse(adapter);

        // Form field extracted by parser
        assertEquals("aspectranUser", adapter.getParameter("username"));

        // File parameter extracted by parser
        FileParameter fileParam = adapter.getFileParameter("profileImage");
        assertNotNull(fileParam);
        assertInstanceOf(CommonsMultipartFileParameter.class, fileParam);
        assertEquals("profile.png", fileParam.getFileName());
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
    }

    @Test
    void testAllowedFileExtensions() throws Exception {
        byte[] fileBytes = "Binary data".getBytes(StandardCharsets.UTF_8);
        byte[] body = createMultipartBody(null, null, "doc", "document.pdf", "application/pdf", fileBytes);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=" + BOUNDARY);
        request.setHeader("Content-Length", String.valueOf(body.length));
        request.setBody(body);

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.POST, request);
        adapter.preparse();

        CommonsMultipartFormDataParser parser = new CommonsMultipartFormDataParser();
        parser.setAllowedFileExtensions("jpg,png");
        parser.parse(adapter);

        // Disallowed extension should be ignored
        assertNull(adapter.getFileParameter("doc"));
    }

    @Test
    void testMaxFileSizeLimit() throws Exception {
        byte[] fileBytes = new byte[2048];
        byte[] body = createMultipartBody(null, null, "bigFile", "big.png", "image/png", fileBytes);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=" + BOUNDARY);
        request.setHeader("Content-Length", String.valueOf(body.length));
        request.setBody(body);

        HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(MethodType.POST, request);
        adapter.preparse();

        CommonsMultipartFormDataParser parser = new CommonsMultipartFormDataParser();
        parser.setMaxFileSize(1024); // 1KB limit
        MultipartRequestParseException ex = assertThrows(MultipartRequestParseException.class, () -> parser.parse(adapter));
        assertInstanceOf(SizeLimitExceededException.class, ex.getCause());
    }

}
