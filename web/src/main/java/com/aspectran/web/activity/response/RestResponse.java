/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.web.activity.response;

import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

/**
 * Base class that represents an HTTP response for a REST resource.
 *
 * @since 6.2.0
 */
public interface RestResponse extends CustomTransformer {

    String getName();

    Object getData();

    /**
     * Specifies response data.
     * @param data the response data
     */
    RestResponse setData(Object data);

    /**
     * Specifies response data with a name.
     * @param name the name of the response data
     * @param data the response data
     */
    RestResponse setData(String name, Object data);

    boolean isPrettyPrint();

    /**
     * Sets whether to apply indentations and line breaks
     * when generating response data.
     * @param prettyPrint true if responding with indentations
     *                    and line breaks; otherwise false
     */
    void setPrettyPrint(boolean prettyPrint);

    RestResponse prettyPrint(boolean prettyPrint);

    boolean isFavorPathExtension();

    void setFavorPathExtension(boolean favorPathExtension);

    RestResponse favorPathExtension(boolean favorPathExtension);

    boolean isIgnoreUnknownPathExtensions();

    void setIgnoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    RestResponse ignoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    boolean isIgnoreAcceptHeader();

    void setIgnoreAcceptHeader(boolean ignoreAcceptHeader);

    RestResponse ignoreAcceptHeader(boolean ignoreAcceptHeader);

    MediaType getDefaultContentType();

    void setDefaultContentType(MediaType defaultContentType);

    void setDefaultContentType(String defaultContentType);

    RestResponse defaultContentType(MediaType defaultContentType);

    RestResponse ok();

    RestResponse created();

    RestResponse created(String location);

    RestResponse accepted();

    RestResponse noContent();

    RestResponse movedPermanently();

    RestResponse seeOther();

    RestResponse notModified();

    RestResponse temporaryRedirect();

    RestResponse badRequest();

    RestResponse unauthorized();

    RestResponse forbidden();

    RestResponse notFound();

    RestResponse methodNotAllowed();

    RestResponse notAcceptable();

    RestResponse conflict();

    RestResponse preconditionFailed();

    RestResponse unsupportedMediaType();

    RestResponse internalServerError();

    int getStatus();

    RestResponse setStatus(int status);

    RestResponse setStatus(HttpStatus status);

    /**
     * Set the given single header value under the given header name.
     * @param name the header name
     * @param value the header value to set
     */
    RestResponse setHeader(String name, String value);

    /**
     * Add the given single header value to the current list of values
     * for the given header.
     * @param name the header name
     * @param value the header value to be added
     */
    RestResponse addHeader(String name, String value);

}
