/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-20.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils.json;

import java.io.IOException;
import java.io.Serial;

/**
 * Thrown when a {@link JsonReader} encounters malformed JSON.
 * <p>This class is a clone of {@code com.google.gson.stream.MalformedJsonException}.</p>
 * <p>Some syntax errors can be ignored by calling {@link JsonReader#setLenient(boolean)}.</p>
 */
public final class MalformedJsonException extends IOException {

    @Serial
    private static final long serialVersionUID = 7310479345877902705L;

    /**
     * Constructs a new MalformedJsonException with the specified detail message.
     * @param msg the detail message
     */
    public MalformedJsonException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new MalformedJsonException with the specified cause.
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *              (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public MalformedJsonException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new MalformedJsonException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *              (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public MalformedJsonException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
