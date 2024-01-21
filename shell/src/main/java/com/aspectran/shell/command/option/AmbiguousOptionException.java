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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.Collection;
import java.util.Iterator;

/**
 * Exception thrown when an option can't be identified from a partial name.
 */
public class AmbiguousOptionException extends UnrecognizedOptionException {

    @Serial
    private static final long serialVersionUID = 7582734904376616120L;

    /** The list of options matching the partial name specified */
    private final Collection<String> matchingOptions;

    /**
     * Constructs a new AmbiguousOptionException.
     * @param option the partial option name
     * @param matchingOptions the options matching the name
     */
    public AmbiguousOptionException(String option, Collection<String> matchingOptions) {
        super(createMessage(option, matchingOptions), option);
        this.matchingOptions = matchingOptions;
    }

    /**
     * Returns the options matching the partial name.
     * @return a collection of options matching the name
     */
    public Collection<String> getMatchingOptions() {
        return matchingOptions;
    }

    /**
     * Build the exception message from the specified list of options.
     *
     * @param option the option name
     * @param matchingOptions the options matching the name
     * @return the exception message
     */
    @NonNull
    private static String createMessage(String option, @NonNull Collection<String> matchingOptions) {
        StringBuilder buf = new StringBuilder("Ambiguous option: '");
        buf.append(option);
        buf.append("'  (could be: ");
        Iterator<String> it = matchingOptions.iterator();
        while (it.hasNext()) {
            buf.append("'");
            buf.append(it.next());
            buf.append("'");
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(")");
        return buf.toString();
    }

}
