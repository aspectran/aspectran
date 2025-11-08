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
import com.aspectran.utils.annotation.jsr305.NonNull;

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
     * @param line the character line
     * @param trimmedLine the trimmed character line
     * @param msg a message to associate with the exception
     */
    public MalformedAponException(int lineNumber, String line, String trimmedLine, String msg) {
        super(makeMessage(lineNumber, line, trimmedLine, msg));
    }

    /**
     * Constructor to create exception with a message.
     * @param lineNumber the line number
     * @param line the character line
     * @param trimmedLine the trimmed character line
     * @param parameterValue the actual value type
     * @param expectedValueType the expected value type
     */
    public MalformedAponException(
            int lineNumber, String line, String trimmedLine, ParameterValue parameterValue,
            ValueType expectedValueType) {
        super(makeMessage(lineNumber, line, trimmedLine, parameterValue, expectedValueType));
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
     * @param line the character line
     * @param trimmedLine the trimmed character line
     * @param msg the message
     * @return the detail message
     */
    @NonNull
    private static String makeMessage(int lineNumber, String line, String trimmedLine, String msg) {
        int columnNumber = (trimmedLine != null ? line.indexOf(trimmedLine) : 0);
        StringBuilder sb = new StringBuilder();
        if (msg != null) {
            sb.append(msg);
        }
        sb.append(" [lineNumber: ").append(lineNumber);
        if (columnNumber != -1) {
            String lspace = line.substring(0, columnNumber);
            int tabCnt = StringUtils.search(lspace, "\t");
            if (trimmedLine != null && trimmedLine.length() > 33) {
                trimmedLine = trimmedLine.substring(0, 30) + "...";
            }
            sb.append(", columnNumber: ").append(columnNumber + 1);
            if (tabCnt != 0) {
                sb.append(" (");
                sb.append("Tabs ").append(tabCnt);
                sb.append(", Spaces ").append(columnNumber - tabCnt);
                sb.append(")");
            }
            sb.append("] ").append(trimmedLine);
        }
        return sb.toString();
    }

    @NonNull
    private static String makeMessage(
            int lineNumber, String line, String trimmedLine, ParameterValue parameterValue,
            ValueType expectedValueType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Incompatible value type with expected value type '");
        sb.append(expectedValueType).append("'");
        if (parameterValue != null) {
            sb.append(" for the specified parameter ").append(parameterValue);
        }

        return makeMessage(lineNumber, line, trimmedLine, sb.toString());
    }

}
