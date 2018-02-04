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
package com.aspectran.core.activity;

import com.aspectran.core.context.AspectranCheckedException;

/**
 * The Class AdapterException.
 * 
 * <p>Created: 2016. 2. 14.</p>
 */
public class AdapterException extends AspectranCheckedException {

    /** @serial */
    private static final long serialVersionUID = -4222246026009174749L;

    /**
     * Simple constructor.
     */
    public AdapterException() {
        super();
    }

    /**
     * Constructor to create exception with a message.
     *
     * @param msg a message to associate with the exception
     */
    public AdapterException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public AdapterException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public AdapterException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
