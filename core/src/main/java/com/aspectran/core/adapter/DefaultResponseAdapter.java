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
package com.aspectran.core.adapter;

import com.aspectran.core.activity.response.RedirectTarget;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.utils.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.utils.MultiValueMap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Set;

/**
 * A default, in-memory implementation of {@link ResponseAdapter}.
 * <p>This class captures response data such as headers, status, and content type
 * in memory. It can be configured with an {@link OutputStream} or {@link Writer}
 * to capture the response body. It is suitable for testing or for non-HTTP
 * environments where a concrete response object is needed.
 * Redirection and reset operations are not supported.
 * </p>
 *
 * @author Juho Jeong
 * @since 2016. 2. 13.
 */
public class DefaultResponseAdapter extends AbstractResponseAdapter {

    private MultiValueMap<String, String> headers;

    private String encoding;

    private String contentType;

    private OutputStream outputStream;

    private Writer writer;

    private int status;

    /**
     * Creates a new {@code DefaultResponseAdapter}.
     * @param adaptee the native response object to adapt, may be {@code null}
     */
    public DefaultResponseAdapter(Object adaptee) {
        super(adaptee);
    }

    /**
     * Creates a new {@code DefaultResponseAdapter} with a pre-configured {@link Writer}.
     * @param adaptee the native response object to adapt, may be {@code null}
     * @param writer the writer to which response content will be written
     */
    public DefaultResponseAdapter(Object adaptee, Writer writer) {
        super(adaptee);
        setWriter(writer);
    }

    @Override
    public String getHeader(String name) {
        return (headers != null ? headers.getFirst(name) : null);
    }

    @Override
    public List<String> getHeaders(String name) {
        return (headers != null ? headers.get(name) : null);
    }

    @Override
    public Set<String> getHeaderNames() {
        return (headers != null ? headers.keySet() : null);
    }

    @Override
    public boolean containsHeader(String name) {
        return (headers != null && headers.containsKey(name));
    }

    @Override
    public void setHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        touchHeaders().set(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        touchHeaders().add(name, value);
    }

    public MultiValueMap<String, String> getAllHeaders() {
        return headers;
    }

    public MultiValueMap<String, String> touchHeaders() {
        if (headers == null) {
            headers = new LinkedCaseInsensitiveMultiValueMap<>();
        }
        return headers;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if an output stream is not configured
     */
    @Override
    public OutputStream getOutputStream() {
        if (outputStream == null) {
            throw new IllegalStateException("OutputStream is not available");
        }
        return outputStream;
    }

    /**
     * Sets the {@link OutputStream} to be returned by {@link #getOutputStream()}.
     * @param outputStream the output stream to use
     */
    protected void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if a writer is not configured
     */
    @Override
    public Writer getWriter() {
        if (writer == null) {
            throw new IllegalStateException("Writer is not available");
        }
        return writer;
    }

    /**
     * Sets the {@link Writer} to be returned by {@link #getWriter()}.
     * @param writer the writer to use
     */
    protected void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation flushes the configured {@link OutputStream} or {@link Writer}.
     */
    @Override
    public void commit() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
        }
        if (writer != null) {
            writer.flush();
        }
    }

    /**
     * {@inheritDoc}
     * <p>This operation is not supported by this adapter.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException("reset");
    }

    /**
     * {@inheritDoc}
     * <p>This operation is not supported by this adapter.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void redirect(String location) {
        throw new UnsupportedOperationException("redirect");
    }

    /**
     * {@inheritDoc}
     * <p>This operation is not supported by this adapter.
     * @throws UnsupportedOperationException always
     */
    @Override
    public RedirectTarget redirect(RedirectRule redirectRule) {
        throw new UnsupportedOperationException("redirect");
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation returns the path unchanged.
     */
    @Override
    public String transformPath(String path) {
        return path;
    }

}
