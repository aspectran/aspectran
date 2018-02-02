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
package com.aspectran.core.activity.response;

import com.aspectran.core.util.LinkedCaseInsensitiveMultiValueMap;
import com.aspectran.core.util.MultiValueMap;

import java.util.Collection;

/**
 * Represents response headers, mapping string header names to a list of string values.
 *
 * <p>This class is generally not thread-safe. It is primarily designed for use in a single thread only.
 *
 * @since 3.0.0
 */
public abstract class AbstractResponse {

    private MultiValueMap<String, String> headers;

    private int status;

    /**
     * Returns a map of the request headers that can be modified.
     * If not yet instantiated then create a new one.
     *
     * @return an {@code MultiValueMap} object, may not be {@code null}
     */
    public MultiValueMap<String, String> touchHeaders() {
        if (headers == null) {
            headers = new LinkedCaseInsensitiveMultiValueMap<String>(6);
        }
        return headers;
    }

    /**
     * Returns a map of the request headers that can be modified.
     *
     * @return an {@code MultiValueMap} object, may be {@code null}
     */
    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

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
    public String getHeader(String name) {
        return (headers != null ? headers.getFirst(name) : null);
    }

    /**
     * Returns the values of the response header with the given name.
     *
     * @param name the name of the response header whose values to return
     * @return a (possibly empty) {@code Collection} of the values
     *         of the response header with the given name
     */
    public Collection<String> getHeaders(String name) {
        return (headers != null ? headers.get(name) : null);
    }

    /**
     * Returns the names of the headers of this response.
     *
     * @return a (possibly empty) {@code Collection} of the names
     *         of the headers of this response
     */
    public Collection<String> getHeaderNames() {
        return (headers != null ? headers.keySet() : null);
    }

    /**
     * Returns a boolean indicating whether the named response header
     * has already been set.
     *
     * @param name the header name
     * @return {@code true} if the named response header
     *         has already been set; {@code false} otherwise
     */
    public boolean containsHeader(String name) {
        return (headers != null && headers.get(name) != null && !headers.get(name).isEmpty());
    }

    /**
     * Set the given single header value under the given header name.
     *
     * @param name the header name
     * @param value the header value to set
     */
    public void setHeader(String name, String value) {
        touchHeaders().set(name, value);
    }

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     *
     * @param name the header name
     * @param value the header value to be added
     */
    public void addHeader(String name, String value) {
        touchHeaders().add(name, value);
    }

    /**
     * Returns the status code.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status code.
     *
     * @param status the status code
     */
    public void setStatus(int status) {
        this.status = status;
    }

}
