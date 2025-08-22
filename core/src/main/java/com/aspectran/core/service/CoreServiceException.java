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
package com.aspectran.core.service;

import java.io.Serial;

/**
 * Generic exception class for errors occurring within the Aspectran core service layer.
 * <p>This exception is a {@code RuntimeException}, meaning it is typically not checked
 * and indicates a serious problem that prevents the normal operation of the service.
 *
 * @since 2008-01-07
 */
public class CoreServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7460804495296696284L;

    /**
     * Constructs a new {@code CoreServiceException} with no detail message.
     */
    public CoreServiceException() {
        super();
    }

    /**
     * Constructs a new {@code CoreServiceException} with the specified detail message.
     * @param msg the detail message
     */
    public CoreServiceException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code CoreServiceException} with the specified cause and a
     * detail message of {@code (cause == null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *      (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CoreServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code CoreServiceException} with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *      (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CoreServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
