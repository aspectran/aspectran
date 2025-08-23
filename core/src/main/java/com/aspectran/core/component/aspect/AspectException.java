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
package com.aspectran.core.component.aspect;

import java.io.Serial;

/**
 * The base runtime exception for errors that occur during aspect processing.
 */
public class AspectException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3778865608683444815L;

    /**
     * Creates a new AspectException.
     */
    public AspectException() {
        super();
    }

    /**
     * Creates a new AspectException with the specified detail message.
     * @param msg the detail message
     */
    public AspectException(String msg) {
        super(msg);
    }

    /**
     * Creates a new AspectException with the specified cause.
     * @param cause the root cause
     */
    public AspectException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new AspectException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the root cause
     */
    public AspectException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
