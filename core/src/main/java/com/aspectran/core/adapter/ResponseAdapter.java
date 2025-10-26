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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

/**
 * Provides an abstraction for an outgoing response within a specific runtime environment.
 *
 * <p>Implementations of this interface encapsulate a container-specific response object
 * (e.g., {@code HttpServletResponse} in a web environment), exposing a consistent API
 * for managing headers, status codes, content type, and the response body via an
 * {@link OutputStream} or {@link Writer}. This allows response generation logic to
 * remain uniform across different execution contexts.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface ResponseAdapter {

    /**
     * Returns the underlying native response object that this adapter wraps.
     * @param <T> the type of the adaptee object
     * @return the adaptee object
     */
    <T> T getAdaptee();

    /**
     * Returns the value of the specified response header.
     * If the header has multiple values, the first value is returned.
     * @param name the name of the header
     * @return the header value, or {@code null} if the header is not found
     */
    String getHeader(String name);

    /**
     * Returns all values for the specified response header.
     * @param name the name of the header
     * @return a collection of header values, which may be empty
     */
    Collection<String> getHeaders(String name);

    /**
     * Returns a collection of all header names set in this response.
     * @return a collection of header names, which may be empty
     */
    Collection<String> getHeaderNames();

    /**
     * Checks if the specified response header has been set.
     * @param name the name of the header
     * @return true if the header is set, false otherwise
     */
    boolean containsHeader(String name);

    /**
     * Sets a response header, overwriting any existing value.
     * @param name the name of the header
     * @param value the header value
     */
    void setHeader(String name, String value);

    /**
     * Adds a value to the specified response header.
     * @param name the name of the header
     * @param value the header value to add
     */
    void addHeader(String name, String value);

    /**
     * Returns the character encoding for the response body.
     * @return the character encoding name (e.g., "UTF-8")
     */
    String getEncoding();

    /**
     * Sets the character encoding for the response body.
     * @param encoding the character encoding name
     */
    void setEncoding(String encoding);

    /**
     * Returns the content type for the response body.
     * @return the content type string (e.g., "text/html")
     */
    String getContentType();

    /**
     * Sets the content type for the response body.
     * This should be set before the response is committed.
     * @param contentType the MIME type of the content
     */
    void setContentType(String contentType);

    /**
     * Returns an {@link OutputStream} for writing binary data to the response body.
     * Calling this method may prevent {@link #getWriter()} from being used.
     * @return the output stream for binary data
     * @throws IOException if an I/O error occurs
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Returns a {@link Writer} for writing character data to the response body.
     * Calling this method may prevent {@link #getOutputStream()} from being used.
     * @return the writer for character data
     * @throws IOException if an I/O error occurs
     */
    Writer getWriter() throws IOException;

    /**
     * Flushes the response buffer and commits the response, writing any pending data
     * to the client. This method is typically called after all headers and content
     * have been set.
     * @throws IOException if an I/O error occurs
     */
    void commit() throws IOException;

    /**
     * Clears any buffered data, status code, and headers from the response.
     * This method can only be called if the response has not yet been committed.
     */
    void reset();

    /**
     * Sends a redirect response to the client using the specified location URL.
     * @param location the URL to redirect to
     * @throws IOException if an I/O error occurs
     */
    void redirect(String location) throws IOException;

    /**
     * Sends a redirect response based on a configured {@link RedirectRule}.
     * @param redirectRule the rule defining the redirect behavior
     * @return information about the redirect target
     * @throws IOException if an I/O error occurs
     */
    RedirectTarget redirect(RedirectRule redirectRule) throws IOException;

    /**
     * Returns the HTTP status code of the response.
     * @return the status code
     */
    int getStatus();

    /**
     * Sets the HTTP status code of the response.
     * @param status the status code to set
     */
    void setStatus(int status);

    /**
     * Transforms a given path into a URL suitable for the container.
     * For example, in a web environment, this might encode the URL.
     * @param path the path to transform
     * @return the transformed path
     */
    String transformPath(String path);

}
