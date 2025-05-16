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
 * The Class DefaultResponseAdapter.
 *
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
     * Instantiates a new default response adapter.
     * @param adaptee the adaptee object
     */
    public DefaultResponseAdapter(Object adaptee) {
        super(adaptee);
    }

    /**
     * Instantiates a new default response adapter.
     * @param adaptee the adaptee object
     * @param writer the writer to output
     */
    public DefaultResponseAdapter(Object adaptee, Writer writer) {
        super(adaptee);
        setWriter(writer);
    }

    /**
     * Returns the value of the response header with the given name.
     *
     * <p>If a response header with the given name exists and contains
     * multiple values, the value that was added first will be returned.
     * @param name the name of the response header whose value to return
     * @return the value of the response header with the given name,
     *         or {@code null} if no header with the given name has been set
     *         on this response
     */
    @Override
    public String getHeader(String name) {
        return (headers != null ? headers.getFirst(name) : null);
    }

    /**
     * Returns the values of the response header with the given name.
     * @param name the name of the response header whose values to return
     * @return a (possibly empty) {@code Collection} of the values
     *         of the response header with the given name
     */
    @Override
    public List<String> getHeaders(String name) {
        return (headers != null ? headers.get(name) : null);
    }

    /**
     * Returns the names of the headers of this response.
     * @return a (possibly empty) {@code Collection} of the names
     *         of the headers of this response
     */
    @Override
    public Set<String> getHeaderNames() {
        return (headers != null ? headers.keySet() : null);
    }

    /**
     * Returns a boolean indicating whether the named response header
     * has already been set.
     * @param name the header name
     * @return {@code true} if the named response header
     *         has already been set; {@code false} otherwise
     */
    @Override
    public boolean containsHeader(String name) {
        return (headers != null && headers.get(name) != null && !headers.get(name).isEmpty());
    }

    /**
     * Set the given single header value under the given header name.
     * @param name the header name
     * @param value the header value to set
     */
    @Override
    public void setHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        touchHeaders().set(name, value);
    }

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     * @param name the header name
     * @param value the header value to be added
     */
    @Override
    public void addHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name must not be null");
        }
        touchHeaders().add(name, value);
    }

    /**
     * Returns a map of the request headers that can be modified.
     * @return an {@code MultiValueMap} object, may be {@code null}
     */
    public MultiValueMap<String, String> getAllHeaders() {
        return headers;
    }

    /**
     * Returns a map of the response headers that can be modified.
     * If not yet instantiated then create a new one.
     * @return an {@code MultiValueMap} object, may not be {@code null}
     */
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

    @Override
    public OutputStream getOutputStream() {
        if (outputStream == null) {
            throw new IllegalStateException("OutputStream is not available");
        }
        return outputStream;
    }

    protected void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public Writer getWriter() {
        if (writer == null) {
            throw new IllegalStateException("Writer is not available");
        }
        return writer;
    }

    protected void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void commit() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
        }
        if (writer != null) {
            writer.flush();
        }
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("reset");
    }

    @Override
    public void redirect(String location) {
        throw new UnsupportedOperationException("redirect");
    }

    @Override
    public RedirectTarget redirect(RedirectRule redirectRule) {
        throw new UnsupportedOperationException("redirect");
    }

    /**
     * Returns the status code.
     */
    @Override
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status code.
     */
    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String transformPath(String path) {
        return path;
    }

}
