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

import java.util.Iterator;
import java.util.List;

/**
 * Thrown when a required option has not been provided.
 */
public class MissingOptionException extends OptionParserException {

    /** @serial */
    private static final long serialVersionUID = -3648528884089184961L;

    /** The list of missing options and groups */
    private List<?> missingOptions;

    /**
     * Construct a new {@code MissingSelectedException}
     * with the specified detail message.
     *
     * @param message the detail message
     */
    public MissingOptionException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code MissingSelectedException} with the
     * specified list of missing options.
     *
     * @param missingOptions the list of missing options and groups
     */
    public MissingOptionException(List<?> missingOptions) {
        this(createMessage(missingOptions));
        this.missingOptions = missingOptions;
    }

    /**
     * Returns the list of options or option groups missing in the command line parsed.
     *
     * @return the missing options, consisting of String instances for simple
     *         options, and OptionGroup instances for required option groups.
     */
    public List<?> getMissingOptions() {
        return missingOptions;
    }

    /**
     * Build the exception message from the specified list of options.
     *
     * @param missingOptions the list of missing options and groups
     */
    private static String createMessage(final List<?> missingOptions) {
        StringBuilder buf = new StringBuilder("Missing required option");
        buf.append(missingOptions.size() == 1 ? "" : "s");
        buf.append(": ");
        Iterator<?> it = missingOptions.iterator();
        while (it.hasNext()) {
            buf.append(it.next());
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

}
