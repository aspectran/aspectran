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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.process.ProcessException;

import java.io.Serial;

/**
 * Exception thrown when an error occurs during the execution of an action in the process.
 */
public class ActionExecutionException extends ProcessException {

    @Serial
    private static final long serialVersionUID = 3568162614053964319L;

    /**
     * Stores the executable action that caused this exception.
     */
    private Executable action;

    /**
     * Constructs a new ActionExecutionException with the specified detail message.
     * @param msg the detail message
     */
    public ActionExecutionException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new ActionExecutionException with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method)
     */
    public ActionExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a new ActionExecutionException with the specified executable action and cause.
     * @param action the executable action that caused the exception
     * @param cause the cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method)
     */
    public ActionExecutionException(Executable action, Throwable cause) {
        super("Failed to execute action " + action + "; Cause: " +
                (cause.getMessage() != null ? cause.getMessage() : cause.toString()),
                cause);
        this.action = action;
    }

    public Executable getAction() {
        return action;
    }

}
