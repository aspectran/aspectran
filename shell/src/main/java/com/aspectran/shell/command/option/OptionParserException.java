/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
 * Base for Exceptions thrown during parsing of a command-line.
 */
public class OptionParserException extends Exception {

    private static final long serialVersionUID = -8387597753242192122L;

    /**
     * Construct a new {@code OptionParseException}
     * with the specified detail message.
     * @param message the detail message
     */
    public OptionParserException(String message) {
        super(message);
    }

}
