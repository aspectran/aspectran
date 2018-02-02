/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.context.AspectranCheckedException;

/**
 * This exception is thrown when ActivityContext building fails.
 * 
 * <p>Created: 2017. 04. 08 PM 8:17:55</p>
 */
public class ActivityContextBuilderException extends AspectranCheckedException {

    /** @serial */
    private static final long serialVersionUID = -276971044899838358L;

    /**
     * Simple constructor.
     */
    public ActivityContextBuilderException() {
        super();
    }

    /**
     * Constructor to create exception with a message.
     *
     * @param msg the specific message
     */
    public ActivityContextBuilderException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public ActivityContextBuilderException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param msg the specific message
     * @param cause the real cause of the exception
     */
    public ActivityContextBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }

}