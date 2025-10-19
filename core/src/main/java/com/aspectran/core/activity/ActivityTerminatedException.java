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
 * Exception thrown when an activity is forcefully terminated during its lifecycle.
 *
 * <p>This exception indicates that the activity did not complete its normal execution
 * path, often due to external factors such as a timeout, a system shutdown signal,
 * or other forms of interruption.
 */
public class ActivityTerminatedException extends ActivityPerformException {

    @Serial
    private static final long serialVersionUID = 6615572357964634821L;

    /**
     * Constructs a new ActivityTerminatedException with no detail message.
     */
    public ActivityTerminatedException() {
        super();
    }

    /**
     * Constructs a new ActivityTerminatedException with the specified detail message.
     * @param msg a message to associate with the exception
     */
    public ActivityTerminatedException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new ActivityTerminatedException with the specified cause.
     * @param cause the root cause of the termination
     */
    public ActivityTerminatedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ActivityTerminatedException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the root cause of the termination
     */
    public ActivityTerminatedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
