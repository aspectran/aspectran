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
 * Exception thrown when reading a command from the console fails.
 * <p>This may be caused by an underlying I/O error or other interruption
 * during the input reading process.</p>
 */
public class CommandReadFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6982213056770858079L;

    /**
     * Instantiates a new CommandReadFailedException.
     */
    public CommandReadFailedException() {
        super();
    }

    /**
     * Instantiates a new CommandReadFailedException with the specified detail message.
     * @param msg a message to associate with the exception
     */
    public CommandReadFailedException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new CommandReadFailedException with the specified cause.
     * @param cause the real cause of the exception
     */
    public CommandReadFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new CommandReadFailedException with the specified detail message and cause.
     * @param msg the message
     * @param cause the real cause of the exception
     */
    public CommandReadFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
