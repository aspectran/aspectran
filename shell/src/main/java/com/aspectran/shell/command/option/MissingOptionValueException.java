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
package com.aspectran.shell.command.option;

import org.jspecify.annotations.NonNull;

import java.io.Serial;

/**
 * Thrown when an option requiring a value is not provided with a value.
 */
public class MissingOptionValueException extends OptionParserException {

    @Serial
    private static final long serialVersionUID = 3097819241980741135L;

    /** The option requiring additional values */
    private Option option;

    /**
     * Construct a new {@code MissingOptionValueException}
     * with the specified detail message.
     * @param message the detail message
     */
    public MissingOptionValueException(String message) {
        super(message);
    }

    /**
     * Construct a new {@code MissingOptionValueException} for the specified option.
     * @param option the option requiring an argument
     */
    public MissingOptionValueException(@NonNull Option option) {
        this("Missing value for option: " + option.getKey());
        this.option = option;
    }

    /**
     * Return the option requiring a value that wasn't provided
     * on the command line.
     * @return the related option
     */
    public Option getOption() {
        return option;
    }

}
