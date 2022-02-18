/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.web.support.http;

import com.aspectran.core.lang.Nullable;

import java.util.List;

/**
 * <p>This class is a clone of org.springframework.web.HttpMediaTypeNotSupportedException</p>
 *
 * Exception thrown when a client POSTs, PUTs, or PATCHes content of a type
 * not supported by request handler.
 *
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class HttpMediaTypeNotSupportedException extends HttpMediaTypeException {

    @Nullable
    private final MediaType contentType;

    /**
     * Create a new HttpMediaTypeNotSupportedException.
     *
     * @param message the exception message
     */
    public HttpMediaTypeNotSupportedException(String message) {
        super(message);
        this.contentType = null;
    }

    /**
     * Create a new HttpMediaTypeNotSupportedException.
     *
     * @param contentType         the unsupported content type
     * @param supportedMediaTypes the list of supported media types
     */
    public HttpMediaTypeNotSupportedException(@Nullable MediaType contentType, List<MediaType> supportedMediaTypes) {
        this(contentType, supportedMediaTypes, "Content type '" +
            (contentType != null ? contentType : "") + "' not supported");
    }

    /**
     * Create a new HttpMediaTypeNotSupportedException.
     *
     * @param contentType         the unsupported content type
     * @param supportedMediaTypes the list of supported media types
     * @param msg                 the detail message
     */
    public HttpMediaTypeNotSupportedException(@Nullable MediaType contentType,
                                              List<MediaType> supportedMediaTypes, String msg) {
        super(msg, supportedMediaTypes);
        this.contentType = contentType;
    }

    /**
     * Return the HTTP request content type method that caused the failure.
     *
     * @return the media type
     */
    @Nullable
    public MediaType getContentType() {
        return this.contentType;
    }

}
