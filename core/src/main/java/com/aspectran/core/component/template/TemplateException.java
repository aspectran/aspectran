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
package com.aspectran.core.component.template;

import com.aspectran.core.context.AspectranRuntimeException;

/**
 * This class is the basic exception that gets thrown from the template package.
 * 
 * <p>Created: 2016. 01. 15.</p>
 */
public class TemplateException extends AspectranRuntimeException {

    /** @serial */
    private static final long serialVersionUID = -6904998412140480762L;

    /**
     * Creates a new TemplateException without detail message.
     */
    public TemplateException() {
        super();
    }

    /**
     * Constructs a TemplateException with the specified detail message.
     *
     * @param msg a message to associate with the exception
     */
    public TemplateException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public TemplateException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a TemplateException with the specified error message and
     * also the specified root cause exception.
     * The root cause exception is generally for TypeConversionException's root cause
     * or something that might have caused a TemplateException.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public TemplateException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
