/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

/**
 * Thrown when an error occurs while executing an action.
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class ActionExecutionException extends ProcessException {

    /** @serial */
    private static final long serialVersionUID = 3568162614053964319L;

    private Executable action;

    /**
     * Constructor to create exception with a message.
     *
     * @param msg a message to associate with the exception
     */
    public ActionExecutionException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a
     * message.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public ActionExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ActionExecutionException(Executable action, Throwable cause) {
        super("Failed to execute action " + action + "; nested exception is " + cause, cause);
        this.action = action;
    }

    public Executable getAction() {
        return action;
    }

}
