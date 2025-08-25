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
package com.aspectran.core.component.translet;

import java.io.Serial;

/**
 * General exception thrown when a Translet-related error occurs.
 * This serves as the base class for more specific translet exceptions.
 *
 * @since 2008. 01. 07
 */
public class TransletException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3736262494374232352L;

    /**
     * Creates a new TransletException without a detail message.
     */
    public TransletException() {
        super();
    }

    /**
     * Constructs a new TransletException with the specified detail message.
     * @param msg the detail message
     */
    public TransletException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new TransletException with the specified cause.
     * @param cause the underlying cause of the exception
     */
    public TransletException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new TransletException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the underlying cause of the exception
     */
    public TransletException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
