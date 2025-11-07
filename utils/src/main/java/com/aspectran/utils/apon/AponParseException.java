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
package com.aspectran.utils.apon;

import java.io.IOException;
import java.io.Serial;

/**
 * The base class for all exceptions thrown when an error occurs while reading,
 * parsing, or interpreting APON (Aspectran Parameters Object Notation) text.
 * <p>
 * This exception extends {@link java.io.IOException} to allow callers to handle
 * both I/O errors and APON-specific parsing errors in a single catch block,
 * especially when working with {@link AponReader}.
 * </p>
 * @see AponReader
 * @see MalformedAponException
 * @see MissingClosingBracketException
 */
public class AponParseException extends IOException {

    @Serial
    private static final long serialVersionUID = -8511680666286307705L;

    /**
     * Constructs a new AponParseException with null as its detail message.
     */
    public AponParseException() {
        super();
    }

    /**
     * Constructs a new AponParseException with the specified detail message.
     * @param msg the detail message
     */
    public AponParseException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new AponParseException with the specified cause.
     * @param cause the cause
     */
    public AponParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new AponParseException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the cause
     */
    public AponParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
