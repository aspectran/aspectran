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
package com.aspectran.web.activity.response;

import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.utils.StringifyContext;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;

/**
 * Represents an HTTP response for a REST resource.
 *
 * <p>Created: 2019-06-16</p>
 */
public interface RestResponse extends CustomTransformer {

    /**
     * Returns the name of the response data.
     * @return the name of the response data
     */
    String getName();

    /**
     * Returns the response data.
     * @return the response data
     */
    Object getData();

    /**
     * Returns whether the response data exists.
     * @return true if the response data exists, false otherwise
     */
    boolean hasData();

    /**
     * Sets the response data.
     * @param data the response data
     * @return this {@code RestResponse} object
     */
    RestResponse setData(Object data);

    /**
     * Sets the response data with a name.
     * @param name the name of the response data
     * @param data the response data
     * @return this {@code RestResponse} object
     */
    RestResponse setData(String name, Object data);

    /**
     * Returns the stringify context.
     * @return the stringify context
     */
    StringifyContext getStringifyContext();

    /**
     * Returns the stringify context, creating it if it does not exist.
     * @return the stringify context
     */
    StringifyContext touchStringifyContext();

    /**
     * Sets the stringify context.
     * @param stringifyContext the stringify context
     */
    void setStringifyContext(StringifyContext stringifyContext);

    /**
     * Sets the stringify context.
     * @param stringifyContext the stringify context
     * @return this {@code RestResponse} object
     */
    RestResponse stringifyContext(StringifyContext stringifyContext);

    /**
     * Sets whether to pretty-print the response data.
     * @param prettyPrint true to pretty-print, false otherwise
     * @return this {@code RestResponse} object
     */
    RestResponse prettyPrint(boolean prettyPrint);

    /**
     * Sets whether to write null values in the response data.
     * @param nullWritable true to write null values, false otherwise
     * @return this {@code RestResponse} object
     */
    RestResponse nullWritable(boolean nullWritable);

    /**
     * Returns whether to favor the path extension when determining the content type.
     * @return true to favor the path extension, false otherwise
     */
    boolean isFavorPathExtension();

    /**
     * Sets whether to favor the path extension when determining the content type.
     * @param favorPathExtension true to favor the path extension, false otherwise
     */
    void setFavorPathExtension(boolean favorPathExtension);

    /**
     * Sets whether to favor the path extension when determining the content type.
     * @param favorPathExtension true to favor the path extension, false otherwise
     * @return this {@code RestResponse} object
     */
    RestResponse favorPathExtension(boolean favorPathExtension);

    /**
     * Returns whether to ignore unknown path extensions.
     * @return true to ignore unknown path extensions, false otherwise
     */
    boolean isIgnoreUnknownPathExtensions();

    /**
     * Sets whether to ignore unknown path extensions.
     * @param ignoreUnknownPathExtensions true to ignore unknown path extensions, false otherwise
     */
    void setIgnoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    /**
     * Sets whether to ignore unknown path extensions.
     * @param ignoreUnknownPathExtensions true to ignore unknown path extensions, false otherwise
     * @return this {@code RestResponse} object
     */
    RestResponse ignoreUnknownPathExtensions(boolean ignoreUnknownPathExtensions);

    /**
     * Returns whether to ignore the Accept header.
     * @return true to ignore the Accept header, false otherwise
     */
    boolean isIgnoreAcceptHeader();

    /**
     * Sets whether to ignore the Accept header.
     * @param ignoreAcceptHeader true to ignore the Accept header, false otherwise
     */
    void setIgnoreAcceptHeader(boolean ignoreAcceptHeader);

    /**
     * Sets whether to ignore the Accept header.
     * @param ignoreAcceptHeader true to ignore the Accept header, false otherwise
     * @return this {@code RestResponse} object
     */
    RestResponse ignoreAcceptHeader(boolean ignoreAcceptHeader);

    /**
     * Returns the default content type.
     * @return the default content type
     */
    MediaType getDefaultContentType();

    /**
     * Sets the default content type.
     * @param defaultContentType the default content type
     */
    void setDefaultContentType(MediaType defaultContentType);

    /**
     * Sets the default content type.
     * @param defaultContentType the default content type
     */
    void setDefaultContentType(String defaultContentType);

    /**
     * Sets the default content type.
     * @param defaultContentType the default content type
     * @return this {@code RestResponse} object
     */
    RestResponse defaultContentType(MediaType defaultContentType);

    /**
     * Sets the HTTP status to 200 (OK).
     * @return this {@code RestResponse} object
     */
    RestResponse ok();

    /**
     * Sets the HTTP status to 201 (Created).
     * @return this {@code RestResponse} object
     */
    RestResponse created();

    /**
     * Sets the HTTP status to 201 (Created) and adds a Location header.
     * @param location the location
     * @return this {@code RestResponse} object
     */
    RestResponse created(String location);

    /**
     * Sets the HTTP status to 202 (Accepted).
     * @return this {@code RestResponse} object
     */
    RestResponse accepted();

    /**
     * Sets the HTTP status to 204 (No Content).
     * @return this {@code RestResponse} object
     */
    RestResponse noContent();

    /**
     * Sets the HTTP status to 301 (Moved Permanently).
     * @return this {@code RestResponse} object
     */
    RestResponse movedPermanently();

    /**
     * Sets the HTTP status to 303 (See Other).
     * @return this {@code RestResponse} object
     */
    RestResponse seeOther();

    /**
     * Sets the HTTP status to 304 (Not Modified).
     * @return this {@code RestResponse} object
     */
    RestResponse notModified();

    /**
     * Sets the HTTP status to 307 (Temporary Redirect).
     * @return this {@code RestResponse} object
     */
    RestResponse temporaryRedirect();

    /**
     * Sets the HTTP status to 400 (Bad Request).
     * @return this {@code RestResponse} object
     */
    RestResponse badRequest();

    /**
     * Sets the HTTP status to 401 (Unauthorized).
     * @return this {@code RestResponse} object
     */
    RestResponse unauthorized();

    /**
     * Sets the HTTP status to 403 (Forbidden).
     * @return this {@code RestResponse} object
     */
    RestResponse forbidden();

    /**
     * Sets the HTTP status to 404 (Not Found).
     * @return this {@code RestResponse} object
     */
    RestResponse notFound();

    /**
     * Sets the HTTP status to 405 (Method Not Allowed).
     * @return this {@code RestResponse} object
     */
    RestResponse methodNotAllowed();

    /**
     * Sets the HTTP status to 406 (Not Acceptable).
     * @return this {@code RestResponse} object
     */
    RestResponse notAcceptable();

    /**
     * Sets the HTTP status to 409 (Conflict).
     * @return this {@code RestResponse} object
     */
    RestResponse conflict();

    /**
     * Sets the HTTP status to 412 (Precondition Failed).
     * @return this {@code RestResponse} object
     */
    RestResponse preconditionFailed();

    /**
     * Sets the HTTP status to 415 (Unsupported Media Type).
     * @return this {@code RestResponse} object
     */
    RestResponse unsupportedMediaType();

    /**
     * Sets the HTTP status to 500 (Internal Server Error).
     * @return this {@code RestResponse} object
     */
    RestResponse internalServerError();

    /**
     * Returns the HTTP status code.
     * @return the HTTP status code
     */
    int getStatus();

    /**
     * Sets the HTTP status code.
     * @param status the HTTP status code
     * @return this {@code RestResponse} object
     */
    RestResponse setStatus(int status);

    /**
     * Sets the HTTP status.
     * @param status the HTTP status
     * @return this {@code RestResponse} object
     */
    RestResponse setStatus(HttpStatus status);

    /**
     * Sets the given single header value under the given header name.
     * @param name the header name
     * @param value the header value to set
     * @return this {@code RestResponse} object
     */
    RestResponse setHeader(String name, String value);

    /**
     * Adds the given single header value to the current list of values for the given header.
     * @param name the header name
     * @param value the header value to be added
     * @return this {@code RestResponse} object
     */
    RestResponse addHeader(String name, String value);

}
