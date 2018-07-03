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
package com.aspectran.core.util.apon;

import com.aspectran.core.util.StringUtils;

/**
 * This exception is raised when attempting to read (or write) a malformed APON element.
 */
public class AponSyntaxException extends AponParseException {

    /** @serial */
    private static final long serialVersionUID = -2012813522496665651L;

    /**
     * Simple constructor.
     */
    public AponSyntaxException() {
        super();
    }

    /**
     * Constructor to create exception with a message.
     *
     * @param msg a message to associate with the exception
     */
    public AponSyntaxException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create exception with a message.
     *
     * @param lineNumber the line number
     * @param line the character line
     * @param tline the trimmed character line
     * @param msg a message to associate with the exception
     */
    public AponSyntaxException(int lineNumber, String line, String tline, String msg) {
        super(createMessage(lineNumber, line, tline, msg));
    }

    /**
     * Constructor to create exception to wrap another exception.
     *
     * @param cause the real cause of the exception
     */
    public AponSyntaxException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create exception to wrap another exception and pass a message.
     *
     * @param msg the message
     * @param cause the real cause of the exception
     */
    public AponSyntaxException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a detail message.
     *
     * @param lineNumber the line number
     * @param line the character line
     * @param tline the trimmed character line
     * @param msg the message
     * @return the detail message
     */
    protected static String createMessage(int lineNumber, String line, String tline, String msg) {
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

}
