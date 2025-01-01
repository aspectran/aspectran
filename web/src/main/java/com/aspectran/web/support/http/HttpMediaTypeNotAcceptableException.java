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
package com.aspectran.web.support.http;

import java.io.Serial;
import java.util.List;

/**
 * <p>This class is a clone of org.springframework.web.HttpMediaTypeNotAcceptableException</p>
 *
 * Exception thrown when the request handler cannot generate a response that is acceptable by the client.
 *
 * @author Arjen Poutsma
 */
public class HttpMediaTypeNotAcceptableException extends HttpMediaTypeException {

    @Serial
    private static final long serialVersionUID = -3953155861196360051L;

    /**
     * Create a new HttpMediaTypeNotAcceptableException.
     * @param message the exception message
     */
    public HttpMediaTypeNotAcceptableException(String message) {
        super(message);
    }

    /**
     * Create a new HttpMediaTypeNotSupportedException.
     * @param supportedMediaTypes the list of supported media types
     */
    public HttpMediaTypeNotAcceptableException(List<MediaType> supportedMediaTypes) {
        super("Could not find acceptable representation", supportedMediaTypes);
    }

}
