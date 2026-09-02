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
package com.aspectran.web.adapter;

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.web.support.http.MediaType;

/**
 * A specialization of {@link com.aspectran.core.adapter.RequestAdapter} for web-based environments.
 * <p>This interface extends the base request adapter with methods for handling
 * web-specific concepts, such as the request body's {@link MediaType} and
 * a pre-parsing mechanism for initializing the adapter from a native request.</p>
 *
 * @since 6.3.0
 */
public interface WebRequestAdapter extends RequestAdapter {

    /**
     * Returns the name of the scheme used to make this request,
     * for example, {@code http} or {@code https}.
     * @return a string specifying the name of the scheme
     */
    String getScheme();

    /**
     * Returns the host name of the server to which the request was sent.
     * @return a string containing the name of the server
     */
    String getServerName();

    /**
     * Returns the port number to which the request was sent.
     * @return an integer specifying the port number
     */
    int getServerPort();

    /**
     * Returns the portion of the request URI that indicates the context of the request.
     * @return a string specifying the context path, or an empty string if in default context
     */
    String getContextPath();

    /**
     * Returns the part of this request's URL from the protocol
     * name up to the query string in the first line of the HTTP request.
     * @return a string specifying the request URI
     */
    String getRequestURI();

    /**
     * Returns the query string that is contained in the request URL after the path.
     * @return a string containing the query string, or {@code null} if the URL contains no query string
     */
    String getQueryString();

    /**
     * Returns the {@link MediaType} of the request body.
     * @return the media type, or {@code null} if not specified
     */
    MediaType getMediaType();

    /**
     * Pre-parses the native request to initialize this adapter.
     * <p>This typically involves extracting parameters, attributes, and headers.</p>
     */
    void preparse();

    /**
     * Pre-parses this adapter using data from another {@code WebRequestAdapter}.
     * <p>This is useful for replicating request data.</p>
     * @param requestAdapter the source request adapter
     */
    void preparse(WebRequestAdapter requestAdapter);

}
