/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.context.rule.RedirectResponseRule;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;

/**
 * The Interface ResponseAdapter.
 *
 * @since 2011. 3. 13.
 */
public interface ResponseAdapter {

    /**
     * Returns the adaptee object to provide response information.
     *
     * @param <T> the type of the adaptee object
     * @return the adaptee object
     */
    <T> T getAdaptee();

    /**
     * Returns the value of the response header with the given name.
     *
     * <p>If a response header with the given name exists and contains
     * multiple values, the value that was added first will be returned.
     *
     * @param name the name of the response header whose value to return
     * @return the value of the response header with the given name,
     *         or {@code null} if no header with the given name has been set
     *         on this response
     */
    String getHeader(String name);

    /**
     * Returns the values of the response header with the given name.
     *
     * @param name the name of the response header whose values to return
     * @return a (possibly empty) {@code Collection} of the values
     *         of the response header with the given name
     */
    Collection<String> getHeaders(String name);

    /**
     * Returns the names of the headers of this response.
     *
     * @return a (possibly empty) {@code Collection} of the names
     *         of the headers of this response
     */
    Collection<String> getHeaderNames();

    /**
     * Returns a boolean indicating whether the named response header
     * has already been set.
     *
     * @param name the header name
     * @return {@code true} if the named response header
     *         has already been set; {@code false} otherwise
     */
    boolean containsHeader(String name);

    /**
     * Set the given single header value under the given header name.
     * If the header had already been set, the new value overwrites the previous one.
     *
     * @param name the header name
     * @param value the header value to set
     */
    void setHeader(String name, String value);

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     *
     * @param name the header name
     * @param value the header value to be added
     */
    void addHeader(String name, String value);

    /**
     * Returns the name of the character encoding (MIME charset) used for the body
     * sent in this response.
     *
     * @return a {@code String} specifying the name of the character encoding,
     *         for example, UTF-8
     */
    String getEncoding();

    /**
     * Sets the character encoding of the response being sent to the client.
     *
     * @param encoding a {@code String} specifying only the character set
     *         defined by IANA Character Sets (http://www.iana.org/assignments/character-sets)
     * @throws UnsupportedEncodingException if character encoding is not supported
     */
    void setEncoding(String encoding) throws UnsupportedEncodingException;

    /**
     * Returns the content type used for the MIME body sent in this response.
     *
     * @return a {@code String} specifying the content type,
     *         for example, {@code text/html}, or null
     */
    String getContentType();

    /**
     * Sets the content type of the response being sent to the client,
     * if the response has not been committed yet.
     *
     * @param contentType a {@code String} specifying the MIME type of the content
     */
    void setContentType(String contentType);

    /**
     * Returns a {@code OutputStream} suitable for writing binary data in the response.
     *
     * @return a {@code OutputStream} for writing binary data
     * @throws IOException if an input or output exception occurs
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Returns a {@code Writer} object that can send character text to the client.
     *
     * @return a {@code Writer} object that can return character data to the client
     * @throws IOException if an input or output exception occurs
     */
    Writer getWriter() throws IOException;

    /**
     * Forces any content in the buffer to be written to the client.
     *
     * @throws IOException if an input or output exception occurs
     */
    void flush() throws IOException;

    /**
     * Redirects a client to a new URL.
     *
     * @param path the redirect path
     * @throws IOException if an input or output exception occurs
     */
    void redirect(String path) throws IOException;

    /**
     * Redirects a client to a new URL.
     *
     * @param redirectResponseRule the redirect response rule
     * @return the redirect path
     * @throws IOException if an input or output exception occurs
     */
    String redirect(RedirectResponseRule redirectResponseRule) throws IOException;

    /**
     * Returns the status code.
     *
     * @return the status
     */
    int getStatus();

    /**
     * Sets the status code.
     *
     * @param status the status code
     */
    void setStatus(int status);

}
