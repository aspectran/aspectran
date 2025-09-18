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
package com.aspectran.core.component.template;

import java.io.Serial;

/**
 * The base exception that gets thrown from the template package.
 * This exception is thrown when a general error occurs during template processing.
 *
 * <p>Created: 2016. 01. 15.</p>
 */
public class TemplateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6904998412140480762L;

    /**
     * Instantiates a new TemplateException.
     */
    public TemplateException() {
        super();
    }

    /**
     * Instantiates a new TemplateException with the specified detail message.
     * @param msg the detail message
     */
    public TemplateException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new TemplateException with the specified cause.
     * @param cause the root cause
     */
    public TemplateException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new TemplateException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the root cause
     */
    public TemplateException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
