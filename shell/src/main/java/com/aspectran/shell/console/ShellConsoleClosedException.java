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
package com.aspectran.shell.console;

import java.io.Serial;

/**
 * Exception thrown when an operation is attempted on a closed console.
 * <p>This typically occurs when trying to read from a console stream that has been
 * terminated, for example, by a user pressing Ctrl-D or Ctrl-Z.</p>
 */
public class ShellConsoleClosedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8812494142589655857L;

    /**
     * Instantiates a new ShellConsoleClosedException.
     */
    public ShellConsoleClosedException() {
        super();
    }

    /**
     * Instantiates a new ShellConsoleClosedException with the specified detail message.
     * @param msg a message to associate with the exception
     */
    public ShellConsoleClosedException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new ShellConsoleClosedException with the specified cause.
     * @param cause the real cause of the exception
     */
    public ShellConsoleClosedException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new ShellConsoleClosedException with the specified detail message and cause.
     * @param msg the message
     * @param cause the real cause of the exception
     */
    public ShellConsoleClosedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
