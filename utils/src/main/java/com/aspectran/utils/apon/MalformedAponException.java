/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

/**
 * This exception is raised when attempting to read (or write) a malformed APON element.
 */
public class MalformedAponException extends AponParseException {

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
     * @param tline the trimmed character line
     * @param msg a message to associate with the exception
     */
    public MalformedAponException(int lineNumber, String line, String tline, String msg) {
        super(makeMessage(lineNumber, line, tline, msg));
    }

    /**
     * Constructor to create exception with a message.
     * @param lineNumber the line number
     * @param line the character line
     * @param tline the trimmed character line
     * @param parameterValue the actual value type
     * @param expectedValueType the expected value type
     */
    public MalformedAponException(int lineNumber, String line, String tline, ParameterValue parameterValue,
                                  ValueType expectedValueType) {
        super(makeMessage(lineNumber, line, tline, parameterValue, expectedValueType));
    }

    /**
     * Create a detail message.
     * @param lineNumber the line number
     * @param line the character line
     * @param tline the trimmed character line
     * @param msg the message
     * @return the detail message
     */
    private static String makeMessage(int lineNumber, String line, String tline, String msg) {
        int columnNumber = (tline != null ? line.indexOf(tline) : 0);
        StringBuilder sb = new StringBuilder();
        if (msg != null) {
            sb.append(msg);
        }
        sb.append(" [lineNumber: ").append(lineNumber);
        if (columnNumber != -1) {
            String lspace = line.substring(0, columnNumber);
            int tabCnt = StringUtils.search(lspace, "\t");
            if (tline != null && tline.length() > 33) {
                tline = tline.substring(0, 30) + "...";
            }
            sb.append(", columnNumber: ").append(columnNumber + 1);
            if (tabCnt != 0) {
                sb.append(" (");
                sb.append("Tabs ").append(tabCnt);
                sb.append(", Spaces ").append(columnNumber - tabCnt);
                sb.append(")");
            }
            sb.append("] ").append(tline);
        }
        return sb.toString();
    }

    private static String makeMessage(int lineNumber, String line, String tline, ParameterValue parameterValue,
                                      ValueType expectedValueType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Incompatible value type with expected value type '");
        sb.append(expectedValueType).append("'");
        if (parameterValue != null) {
            sb.append(" for the specified parameter ").append(parameterValue);
        }

        return makeMessage(lineNumber, line, tline, sb.toString());
    }

}
