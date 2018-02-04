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
package com.aspectran.core.component.translet;

import com.aspectran.core.context.AspectranRuntimeException;

/**
 * The Class TransletException.
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class TransletException extends AspectranRuntimeException {

    /** @serial */
    private static final long serialVersionUID = 3736262494374232352L;

    /**
     * Creates a new TransletException without detail message.
     */
    public TransletException() {
        super();
    }

    /**
     * Constructs a TransletException with the specified detail message.
     *
     * @param msg a message to associate with the exception
     */
    public TransletException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public TransletException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public TransletException(String msg, Throwable cause) {
        super(msg, cause);
    }

}