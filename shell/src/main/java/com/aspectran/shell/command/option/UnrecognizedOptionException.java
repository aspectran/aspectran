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
package com.aspectran.shell.command.option;

/**
 * Exception thrown during parsing signalling an unrecognized
 * option was seen.
 */
public class UnrecognizedOptionException extends OptionParserException {

    /** @serial */
    private static final long serialVersionUID = 4662457863100554595L;

    /** The unrecognized option */
    private String option;

    /**
     * Construct a new {@code UnrecognizedArgumentException}
     * with the specified detail message.
     *
     * @param message the detail message
     */
    public UnrecognizedOptionException(String message) {
        super(message);
    }

    /**
     * Construct a new {@code UnrecognizedArgumentException}
     * with the specified option and detail message.
     *
     * @param message the detail message
     * @param option the unrecognized option
     */
    public UnrecognizedOptionException(String message, String option) {
        this(message);
        this.option = option;
    }

    /**
     * Returns the unrecognized option.
     *
     * @return the related option
     */
    public String getOption() {
        return option;
    }

}
