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
package com.aspectran.core.util.apon;

/**
 * Exception thrown when an invalid parameter is encountered.
 */
public class InvalidParameterException extends AponSyntaxException {

    /** @serial */
    private static final long serialVersionUID = -459157992330424751L;

    /**
     * Simple constructor.
     */
    public InvalidParameterException() {
        super();
    }

    /**
     * Constructor to create exception with a message.
     *
     * @param msg a message to associate with the exception
     */
    public InvalidParameterException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception with a message.
     *
     * @param lineNumber the line number
     * @param line the character line
     * @param tline the trimmed character line
     * @param msg a message to associate with the exception
     */
    public InvalidParameterException(int lineNumber, String line, String tline, String msg) {
        super(lineNumber, line, tline, msg);
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public InvalidParameterException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param msg the message
     * @param cause the real cause of the exception
     */
    public InvalidParameterException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
