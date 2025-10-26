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
package com.aspectran.core.context.builder;

import java.io.Serial;

/**
 * The top-level exception thrown when an {@link com.aspectran.core.context.ActivityContext}
 * fails to build.
 *
 * @since 2017. 4. 8.
 */
public class ActivityContextBuilderException extends Exception {

    @Serial
    private static final long serialVersionUID = -276971044899838358L;

    /**
     * Instantiates a new ActivityContextBuilderException.
     */
    public ActivityContextBuilderException() {
        super();
    }

    /**
     * Instantiates a new ActivityContextBuilderException with the specified detail message.
     * @param msg the specific message
     */
    public ActivityContextBuilderException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new ActivityContextBuilderException with the specified cause.
     * @param cause the real cause of the exception
     */
    public ActivityContextBuilderException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new ActivityContextBuilderException with the specified detail message and cause.
     * @param msg the specific message
     * @param cause the real cause of the exception
     */
    public ActivityContextBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
