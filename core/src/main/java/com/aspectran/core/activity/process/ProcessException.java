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
package com.aspectran.core.activity.process;

import com.aspectran.core.activity.ActivityException;

import java.io.Serial;

/**
 * A generic base exception for errors that occur during the processing phase of an
 * {@link com.aspectran.core.activity.Activity}.
 *
 * <p>This exception is thrown when a problem occurs while executing the defined
 * process flow, such as running actions from an {@link ActionList}.</p>
 *
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class ProcessException extends ActivityException {

    @Serial
    private static final long serialVersionUID = 7290974002627109441L;

    /**
     * Creates a new ProcessException with a specified detail message.
     * @param msg a message to associate with the exception
     */
    public ProcessException(String msg) {
        super(msg);
    }

    /**
     * Creates a new ProcessException with a specified detail message and a nested cause.
     * @param msg the detail message
     * @param cause the nested exception
     */
    public ProcessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
