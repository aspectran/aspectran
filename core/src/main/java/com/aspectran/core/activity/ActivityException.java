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
 * The base class for checked exceptions thrown when an error occurs during the
 * lifecycle of an {@link Activity}.
 *
 * <p>This exception and its subclasses indicate failures that can occur during
 * the preparation or performance phases of an activity. See
 * {@link ActivityPrepareException} and {@link ActivityPerformException} for exceptions
 * that are specific to those phases.
 *
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class ActivityException extends Exception {

    @Serial
    private static final long serialVersionUID = -4400747654771758521L;

    /**
     * Constructs a new ActivityException with no detail message.
     */
    public ActivityException() {
        super();
    }

    /**
     * Constructs a new ActivityException with the specified detail message.
     * @param msg the detail message
     */
    public ActivityException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new ActivityException with the specified cause.
     * @param cause the root cause
     */
    public ActivityException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ActivityException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the root cause
     */
    public ActivityException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
