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
package com.aspectran.utils.apon;

import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;

import java.io.Serial;

/**
 * Thrown when an APON string is syntactically incorrect or its value types
 * are incompatible with the expected schema.
 * <p>
 * Includes helpers to compose detailed error messages with line/column information
 * for easier troubleshooting.
 * </p>
 */
public class MalformedAponException extends AponParseException {

    @Serial
    private static final long serialVersionUID = -2012813522496665651L;

    /**
     * Constructor to create exception with a message.
     * @param msg a message to associate with the exception
     */
    public MalformedAponException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception with a message.
     * @param lineNumber the line number
     * @param columnNumber the column number
     * @param line the character line
     * @param msg a message to associate with the exception
     */
    public MalformedAponException(int lineNumber, int columnNumber, String line, String msg) {
        super(makeMessage(lineNumber, columnNumber, line, msg));
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     * @param msg the message
     * @param cause the real cause of the exception
     */
    public MalformedAponException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a detail message.
     * @param lineNumber the line number
     * @param columnNumber the column number
     * @param line the character line
     * @param msg the message
     * @return the detail message
     */
    @NonNull
    private static String makeMessage(int lineNumber, int columnNumber, String line, String msg) {
        StringBuilder sb = new StringBuilder();
        if (msg != null) {
            sb.append(msg);
        }
        sb.append(" [lineNumber: ").append(lineNumber);
        sb.append(", columnNumber: ").append(columnNumber > 0 ? columnNumber : 1);
        sb.append("]");
        if (line != null) {
            String context = getContext(line, columnNumber);
            if (StringUtils.hasLength(context)) {
                sb.append(" ").append(context);
            }
        }
        return sb.toString();
    }

    @NonNull
    private static String getContext(String line, int columnNumber) {
        if (line == null || line.isEmpty()) {
            return StringUtils.EMPTY;
        }
        // Find the first non-whitespace character to adjust display
        int firstNonWhsp = 0;
        while (firstNonWhsp < line.length() && Character.isWhitespace(line.charAt(firstNonWhsp))) {
            firstNonWhsp++;
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return StringUtils.EMPTY;
        }

        // If the line is too long, crop it around the columnNumber
        if (trimmed.length() > 103) {
            int start = Math.max(0, (columnNumber > 0 ? columnNumber - 1 : 0) - firstNonWhsp - 40);
            int end = Math.min(trimmed.length(), start + 100);
            if (start > 0) {
                trimmed = "..." + trimmed.substring(start, end);
            } else {
                trimmed = trimmed.substring(0, end);
            }
            if (end < line.trim().length()) {
                trimmed += "...";
            }
        }
        return trimmed;
    }

}
