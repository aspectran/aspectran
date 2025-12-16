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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

/**
 * Represents a dynamic response mechanism that allows programmatic control over the output.
 *
 * <p>Unlike predefined response types (e.g., {@link ForwardResponse}, {@link RedirectResponse},
 * or {@link com.aspectran.core.activity.response.transform.TransformResponse}),
 * {@code ResponseTemplate} does not have a fixed response content or format.
 * Instead, it provides direct access to the underlying {@link ResponseAdapter},
 * enabling developers to dynamically generate output by writing directly to the
 * {@code OutputStream} or {@code Writer}.</p>
 *
 * <p>This class is particularly useful for scenarios requiring fine-grained control over the response,
 * such as:</p>
 * <ul>
 *   <li>Directly streaming binary data or large text content.</li>
 *   <li>Setting HTTP status codes and headers programmatically.</li>
 *   <li>Performing server-side redirects or forwards based on complex runtime conditions.</li>
 * </ul>
 *
 * <p>Created: 2020/07/26</p>
 */
public class ResponseTemplate implements Response, ResponseAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseTemplate.class);

    private final ResponseAdapter responseAdapter;

    /**
     * Constructs a new {@code ResponseTemplate} with the specified {@code ResponseAdapter}.
     * @param responseAdapter the underlying {@code ResponseAdapter} to use for generating the response
     */
    public ResponseTemplate(@NonNull ResponseAdapter responseAdapter) {
        this.responseAdapter = responseAdapter;
    }

    /**
     * Generates the response. For {@code ResponseTemplate}, this method primarily logs
     * the response type as the actual output is handled programmatically via the
     * underlying {@code ResponseAdapter}.
     * @param activity the current activity
     * @throws ResponseException if an error occurs during response generation
     */
    @Override
    public void respond(Activity activity) throws ResponseException {
        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.appendForce("type", getResponseType());
            logger.debug(tsb.toString());
        }
    }

    /**
     * Returns the type of this response, which is {@link ResponseType#TEMPLATE}.
     * @return the response type
     */
    @Override
    public ResponseType getResponseType() {
        return ResponseType.TEMPLATE;
    }

    /**
     * Returns the underlying adaptee object from the {@code ResponseAdapter}.
     * @param <T> the type of the adaptee
     * @return the adaptee object
     */
    @Override
    public <T> T getAdaptee() {
        return responseAdapter.getAdaptee();
    }

    /**
     * Returns the value of the response header with the given name.
     * @param name the name of the response header
     * @return the value of the response header, or {@code null} if not set
     */
    @Override
    public String getHeader(String name) {
        return responseAdapter.getHeader(name);
    }

    /**
     * Returns all the values of the response header with the given name.
     * @param name the name of the response header
     * @return a collection of the header values, or an empty collection if not set
     */
    @Override
    public Collection<String> getHeaders(String name) {
        return responseAdapter.getHeaders(name);
    }

    /**
     * Returns the names of all headers set for this response.
     * @return a collection of header names
     */
    @Override
    public Collection<String> getHeaderNames() {
        return responseAdapter.getHeaderNames();
    }

    /**
     * Checks if the response contains a header with the given name.
     * @param name the name of the header
     * @return {@code true} if the header is present, {@code false} otherwise
     */
    @Override
    public boolean containsHeader(String name) {
        return responseAdapter.containsHeader(name);
    }

    /**
     * Sets a single header value for the given header name, replacing any existing values.
     * @param name the name of the header
     * @param value the value to set for the header
     */
    @Override
    public void setHeader(String name, String value) {
        responseAdapter.setHeader(name, value);
    }

    /**
     * Adds a single header value to the current list of values for the given header.
     * @param name the name of the header
     * @param value the value to add for the header
     */
    @Override
    public void addHeader(String name, String value) {
        responseAdapter.addHeader(name, value);
    }

    /**
     * Returns the character encoding of the response.
     * @return the character encoding
     */
    @Override
    public String getEncoding() {
        return responseAdapter.getEncoding();
    }

    /**
     * Sets the character encoding for the response.
     * @param encoding the character encoding to set
     */
    @Override
    public void setEncoding(String encoding) {
        responseAdapter.setEncoding(encoding);
    }

    /**
     * Returns the content type of the response.
     * @return the content type
     */
    @Override
    public String getContentType() {
        return responseAdapter.getContentType();
    }

    /**
     * Sets the content type for the response.
     * @param contentType the content type to set
     */
    public void setContentType(String contentType) {
        responseAdapter.setContentType(contentType);
    }

    /**
     * Returns an {@code OutputStream} for writing binary data to the response.
     * @return the output stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return responseAdapter.getOutputStream();
    }

    /**
     * Returns a {@code Writer} for writing character data to the response.
     * @return the writer
     * @throws IOException if an I/O error occurs
     */
    @Override
    public Writer getWriter() throws IOException {
        return responseAdapter.getWriter();
    }

    /**
     * Commits the response, flushing any buffered output.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void commit() throws IOException {
        responseAdapter.commit();
    }

    /**
     * Resets the response, clearing any buffered content, headers, and status code.
     */
    @Override
    public void reset() {
        responseAdapter.reset();
    }

    /**
     * Sends a redirect to the specified location.
     * @param location the URL to redirect to
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void redirect(String location) throws IOException {
        responseAdapter.redirect(location);
    }

    /**
     * Sends a redirect based on the provided {@code RedirectRule}.
     * @param redirectRule the rule defining the redirect target
     * @return a {@link RedirectTarget} object representing the redirect destination
     * @throws IOException if an I/O error occurs
     */
    @Override
    public RedirectTarget redirect(RedirectRule redirectRule) throws IOException {
        return responseAdapter.redirect(redirectRule);
    }

    /**
     * Returns the HTTP status code of the response.
     * @return the HTTP status code
     */
    @Override
    public int getStatus() {
        return responseAdapter.getStatus();
    }

    /**
     * Sets the HTTP status code for the response.
     * @param status the HTTP status code to set
     */
    @Override
    public void setStatus(int status) {
        responseAdapter.setStatus(status);
    }

    /**
     * Transforms the given path, typically for URL rewriting or context path resolution.
     * @param path the path to transform
     * @return the transformed path
     */
    @Override
    public String transformPath(String path) {
        return responseAdapter.transformPath(path);
    }

    /**
     * This operation is not supported for {@code ResponseTemplate} as it represents
     * a dynamic, programmatic response that cannot be replicated in a meaningful way.
     * @throws UnsupportedOperationException always
     */
    @Override
    public Response replicate() {
        throw new UnsupportedOperationException();
    }

}
