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
package com.aspectran.core.activity;

import java.io.Serial;

/**
 * Exception thrown when an error occurs during the performance phase of an
 * {@link Activity}.
 *
 * <p>This phase involves the execution of the actions defined within the translet,
 * such as invoking beans or transforming the response.
 *
 * <p>Created: 2019. 03. 25.</p>
 */
public class ActivityPerformException extends ActivityException {

    @Serial
    private static final long serialVersionUID = 2728451652587414622L;

    /**
     * Constructs a new ActivityPerformException with no detail message.
     */
    public ActivityPerformException() {
        super();
    }

    /**
     * Constructs a new ActivityPerformException with the specified detail message.
     * @param msg the detail message
     */
    public ActivityPerformException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new ActivityPerformException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the root cause
     */
    public ActivityPerformException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a new ActivityPerformException with the specified cause.
     * @param cause the root cause
     */
    public ActivityPerformException(Throwable cause) {
        super(cause);
    }

}
