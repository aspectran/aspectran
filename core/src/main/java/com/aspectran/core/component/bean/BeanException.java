/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.component.bean;

import java.io.Serial;

/**
 * This class is the basic exception that gets thrown from the bean package.
 *
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class BeanException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2598750999734896601L;

    /**
     * Creates a new BeanException without detail message.
     */
    public BeanException() {
        super();
    }

    /**
     * Constructs a BeanException with the specified detail message.
     * @param msg the detail message
     */
    public BeanException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     * @param cause the real cause of the exception
     */
    public BeanException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BeanException with the specified error message and
     * also the specified root cause exception.
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public BeanException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
