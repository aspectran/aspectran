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
package com.aspectran.core.activity.response;

import com.aspectran.core.activity.ActivityException;

import java.io.Serial;

/**
 * A generic base exception for errors that occur during the response generation phase of an
 * {@link com.aspectran.core.activity.Activity}.
 *
 * <p>This exception serves as the root for all response-related exceptions within Aspectran,
 * providing a common superclass for more specific issues like transformation failures
 * or dispatch errors.</p>
 *
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class ResponseException extends ActivityException {

    @Serial
    private static final long serialVersionUID = -7446545758142913255L;

    /**
     * Constructs a new {@code ResponseException} with no specified detail message.
     */
    public ResponseException() {
        super();
    }

    /**
     * Constructs a new {@code ResponseException} with the specified detail message.
     * @param msg the detail message
     */
    public ResponseException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code ResponseException} with the specified cause and a
     * detail message of {@code (cause == null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method). (A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ResponseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code ResponseException} with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method). (A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ResponseException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
