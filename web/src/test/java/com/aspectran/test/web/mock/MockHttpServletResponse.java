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
package com.aspectran.test.web.mock;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Mock implementation of {@link HttpServletResponse}.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
public class MockHttpServletResponse implements HttpServletResponse {

    private int status = 200;
    private String characterEncoding = "UTF-8";
    private String contentType;
    private final StringWriter writer = new StringWriter();

    public String getContentAsString() { return writer.toString(); }

    @Override public void addCookie(Cookie cookie) {}
    @Override public boolean containsHeader(String name) { return false; }
    @Override public String encodeURL(String url) { return url; }
    @Override public String encodeRedirectURL(String url) { return url; }
    @Override public void sendError(int sc, String msg) throws IOException { this.status = sc; }
    @Override public void sendError(int sc) throws IOException { this.status = sc; }
    @Override public void sendRedirect(String location) throws IOException { this.status = 302; }
    @Override public void setDateHeader(String name, long date) {}
    @Override public void addDateHeader(String name, long date) {}
    @Override public void setHeader(String name, String value) {}
    @Override public void addHeader(String name, String value) {}
    @Override public void setIntHeader(String name, int value) {}
    @Override public void addIntHeader(String name, int value) {}
    @Override public void setStatus(int sc) { this.status = sc; }
    @Override public int getStatus() { return status; }
    @Override public String getHeader(String name) { return null; }
    @Override public Collection<String> getHeaders(String name) { return Collections.emptyList(); }
    @Override public Collection<String> getHeaderNames() { return Collections.emptyList(); }
    @Override public String getCharacterEncoding() { return characterEncoding; }
    @Override public String getContentType() { return contentType; }
    @Override public ServletOutputStream getOutputStream() throws IOException { return null; }
    @Override public PrintWriter getWriter() throws IOException { return new PrintWriter(writer); }
    @Override public void setCharacterEncoding(String charset) { this.characterEncoding = charset; }
    @Override public void setContentLength(int len) {}
    @Override public void setContentLengthLong(long len) {}
    @Override public void setContentType(String type) { this.contentType = type; }
    @Override public void setBufferSize(int size) {}
    @Override public int getBufferSize() { return 0; }
    @Override public void flushBuffer() throws IOException { writer.flush(); }
    @Override public void resetBuffer() {}
    @Override public boolean isCommitted() { return false; }
    @Override public void reset() {}
    @Override public void setLocale(Locale loc) {}
    @Override public Locale getLocale() { return Locale.getDefault(); }

}
