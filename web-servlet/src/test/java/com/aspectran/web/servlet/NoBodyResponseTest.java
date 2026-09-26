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
package com.aspectran.web.servlet;

import com.aspectran.test.web.servlet.mock.MockHttpServletResponse;
import com.aspectran.web.support.http.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test case for {@link NoBodyResponse}.
 */
class NoBodyResponseTest {

    @Test
    void testNormalOutputCalculatesContentLength() throws IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        NoBodyResponse response = new NoBodyResponse(mockResponse);

        PrintWriter writer = response.getWriter();
        writer.write("Hello World");
        response.setContentLength();

        assertEquals("11", mockResponse.getHeader(HttpHeaders.CONTENT_LENGTH));
        assertEquals("", mockResponse.getContentAsString(), "Body must be swallowed");
    }

    @Test
    void testSendErrorDoesNotSetContentLengthZero() throws IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        NoBodyResponse response = new NoBodyResponse(mockResponse);

        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service Paused");
        response.setContentLength();

        assertEquals(503, mockResponse.getStatus());
        assertNull(mockResponse.getHeader(HttpHeaders.CONTENT_LENGTH), "Content-Length should not be set on sendError");
    }

    @Test
    void testSendRedirectDoesNotSetContentLengthZero() throws IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        NoBodyResponse response = new NoBodyResponse(mockResponse);

        response.sendRedirect("/redirect-target");
        response.setContentLength();

        assertEquals(302, mockResponse.getStatus());
        assertNull(mockResponse.getHeader(HttpHeaders.CONTENT_LENGTH), "Content-Length should not be set on sendRedirect");
    }

    @Test
    void testNoContentStatusDoesNotSetContentLength() {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        NoBodyResponse response = new NoBodyResponse(mockResponse);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.setContentLength();

        assertNull(mockResponse.getHeader(HttpHeaders.CONTENT_LENGTH), "Content-Length should not be set on 204 NO_CONTENT");
    }

    @Test
    void testNotModifiedStatusDoesNotSetContentLength() {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        NoBodyResponse response = new NoBodyResponse(mockResponse);

        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        response.setContentLength();

        assertNull(mockResponse.getHeader(HttpHeaders.CONTENT_LENGTH), "Content-Length should not be set on 304 NOT_MODIFIED");
    }

    @Test
    void testUnusedStreamDoesNotSetContentLengthZero() {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        NoBodyResponse response = new NoBodyResponse(mockResponse);

        // Neither getWriter() nor getOutputStream() called
        response.setContentLength();

        assertNull(mockResponse.getHeader(HttpHeaders.CONTENT_LENGTH), "Content-Length should not be set when stream was unused");
    }

    @Test
    void testExplicitContentLengthTakesPrecedence() throws IOException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        NoBodyResponse response = new NoBodyResponse(mockResponse);

        response.setContentLength(100);
        PrintWriter writer = response.getWriter();
        writer.write("Hello World");
        response.setContentLength();

        assertEquals("100", mockResponse.getHeader(HttpHeaders.CONTENT_LENGTH));
    }

}
