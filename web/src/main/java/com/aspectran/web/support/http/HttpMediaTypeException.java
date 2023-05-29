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
package com.aspectran.web.support.http;

import javax.servlet.ServletException;
import java.util.Collections;
import java.util.List;

/**
 * <p>This class is a clone of org.springframework.web.HttpMediaTypeException</p>
 *
 * Abstract base for exceptions related to media types.
 * Adds a list of supported {@link MediaType MediaTypes}.
 *
 * @author Arjen Poutsma
 */
public abstract class HttpMediaTypeException extends ServletException {

    private final List<MediaType> supportedMediaTypes;

    /**
     * Create a new HttpMediaTypeException.
     *
     * @param message the exception message
     */
    protected HttpMediaTypeException(String message) {
        super(message);
        this.supportedMediaTypes = Collections.emptyList();
    }

    /**
     * Create a new HttpMediaTypeException with a list of supported media types.
     *
     * @param message the exception message
     * @param supportedMediaTypes the list of supported media types
     */
    protected HttpMediaTypeException(String message, List<MediaType> supportedMediaTypes) {
        super(message);
        this.supportedMediaTypes = Collections.unmodifiableList(supportedMediaTypes);
    }


    /**
     * Return the list of supported media types.
     *
     * @return the list of supported media types
     */
    public List<MediaType> getSupportedMediaTypes() {
        return this.supportedMediaTypes;
    }

}
