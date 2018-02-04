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
package com.aspectran.core.component.aspect;

import com.aspectran.core.context.AspectranRuntimeException;

/**
 * This class is the basic exception that gets thrown from the aspect package.
 */
public class AspectException extends AspectranRuntimeException {

    /** @serial */
    private static final long serialVersionUID = 3778865608683444815L;

    /**
     * Creates a new AspectException without detail message.
     */
    public AspectException() {
        super();
    }

    /**
     * Constructs a AspectException with the specified detail message.
     *
     * @param msg a message to associate with the exception
     */
    public AspectException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public AspectException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a AspectException with the specified error message and also the specified root cause exception.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public AspectException(String msg, Throwable cause) {
        super(msg, cause);
    }

}