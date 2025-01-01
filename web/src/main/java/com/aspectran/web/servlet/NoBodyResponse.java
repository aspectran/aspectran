/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * A response that includes no body, for use in (dumb) "HEAD" support.
 * This just swallows that body, counting the bytes in order to set the
 * content length appropriately. All other methods delegate directly to
 * the wrapped HTTP Servlet Response object.
 */
class NoBodyResponse extends HttpServletResponseWrapper {

    private final NoBodyOutputStream noBody;

    private PrintWriter writer;

    private boolean didSetContentLength;

    private boolean usingOutputStream;

    NoBodyResponse(HttpServletResponse response) {
        super(response);
        noBody = new NoBodyOutputStream();
    }

    void setContentLength() {
        if (!didSetContentLength) {
            if (writer != null) {
                writer.flush();
            }
            setContentLength(noBody.getContentLength());
        }
    }

    @Override
    public void setContentLength(int len) {
        super.setContentLength(len);
        didSetContentLength = true;
    }

    @Override
    public void setContentLengthLong(long len) {
        super.setContentLengthLong(len);
        didSetContentLength = true;
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        checkHeader(name);
    }

    private void checkHeader(String name) {
        if ("content-length".equalsIgnoreCase(name)) {
            didSetContentLength = true;
        }
    }

    @Override
    public void reset() {
        super.reset();
        noBody.reset();
        usingOutputStream = false;
        writer = null;
        didSetContentLength = false;
    }

    @Override
    public void resetBuffer() {
        super.resetBuffer();
        if (writer != null) {
            try {
                NoBodyOutputStream.disableFlush.set(Boolean.TRUE);
                writer.flush();
            } finally {
                NoBodyOutputStream.disableFlush.remove();
            }
        }
        noBody.reset();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("Illegal to call getOutputStream() after getWriter() has been called");
        }
        usingOutputStream = true;
        return noBody;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (usingOutputStream) {
            throw new IllegalStateException("Illegal to call getWriter() after getOutputStream() has been called");
        }
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(noBody, getCharacterEncoding()));
        }
        return writer;
    }

    /**
     * Servlet output stream that gobbles up all its data.
     */
    static class NoBodyOutputStream extends ServletOutputStream {

        static ThreadLocal<Boolean> disableFlush = new ThreadLocal<>();

        private int contentLength = 0;

        NoBodyOutputStream() {
        }

        void reset() {
            contentLength = 0;
        }

        int getContentLength() {
            return contentLength;
        }

        @Override
        public void write(int b) {
            contentLength++;
        }

        @Override
        public void write(@NonNull byte[] buf, int offset, int len) throws IOException {
            if (offset < 0 || len < 0 || offset + len > buf.length) {
                throw new IndexOutOfBoundsException("Invalid offset [" + offset + "] and / or length [" +
                    len + "] specified for array of size [" + buf.length + "]");
            }
            contentLength += len;
        }

        @Override
        public void flush() throws IOException {
            if (Boolean.TRUE.equals(disableFlush.get())) {
                super.flush();
            }
        }

        @Override
        public boolean isReady() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }
    }

}
