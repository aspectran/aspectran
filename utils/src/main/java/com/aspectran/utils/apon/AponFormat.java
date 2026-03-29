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

/**
 * Defines the format for APON (Aspectran Parameters Object Notation).
 * <p>APON is a data serialization format designed for both readability and efficiency.</p>
 */
public abstract class AponFormat {

    /** The character used to open a parameter block. */
    public static final char BLOCK_OPEN = '{';

    /** The character used to close a parameter block. */
    public static final char BLOCK_CLOSE = '}';

    /** The character used to open an array. */
    public static final char ARRAY_OPEN = '[';

    /** The character used to close an array. */
    public static final char ARRAY_CLOSE = ']';

    /** The character used to start a multi-line text block or wrap value type hints. */
    public static final char TEXT_OPEN = '(';

    /** The character used to end a multi-line text block or wrap value type hints. */
    public static final char TEXT_CLOSE = ')';

    /** The character used to separate a parameter name and its value. */
    public static final char NAME_VALUE_SEPARATOR = ':';

    /** The character used to separate multiple entries in a single line. */
    public static final char COMMA_CHAR = ',';

    /** The character used to start a comment line. */
    public static final char COMMENT_LINE_START = '#';

    /** The character used at the beginning of each line in a multi-line text block. */
    public static final char TEXT_LINE_START = '|';

    /** The double quote character used for wrapping string values. */
    public static final char DOUBLE_QUOTE_CHAR = '"';

    /** The single quote character used for wrapping string values. */
    public static final char SINGLE_QUOTE_CHAR = '\'';

    /** The escape character used within quoted strings. */
    public static final char ESCAPE_CHAR = '\\';

    /** The space character. */
    public static final char SPACE_CHAR = ' ';

    /** The newline character. */
    public static final char NEW_LINE_CHAR = '\n';

    /** A null character representation. */
    public static final char NO_CONTROL_CHAR = '\0';

    /** Literal string for a null value. */
    public static final String NULL = "null";

    /** Literal string for a true boolean value. */
    public static final String TRUE = "true";

    /** Literal string for a false boolean value. */
    public static final String FALSE = "false";

    /** Representation of an empty block. */
    public static final String EMPTY_BLOCK = "{}";

    /** Representation of an empty array. */
    public static final String EMPTY_ARRAY = "[]";

    /** Default indentation string (two spaces). */
    public static final String DEFAULT_INDENT_STRING = "  ";

    /** String representation of a single space. */
    public static final String SPACE = " ";

    /** String representation of a newline. */
    public static final String NEW_LINE = "\n";

    /** The system-dependent newline separator. */
    public static final String SYSTEM_NEW_LINE = System.lineSeparator();

    /**
     * Determines whether the given string needs to be wrapped in quotes.
     * <p>A string needs quoting if it:</p>
     * <ul>
     *   <li>Is an empty string.</li>
     *   <li>Starts with whitespace or structural characters like '{', '[', '(', '#', etc.</li>
     *   <li>Contains quotes, commas, colons, or control characters.</li>
     *   <li>Ends with whitespace.</li>
     * </ul>
     * @param str the string to check
     * @return true if the string needs quoting, false otherwise
     */
    public static boolean needsQuoting(String str) {
        if (str == null) {
            return false;
        }
        if (str.isEmpty()) {
            return true;
        }
        char firstChar = str.charAt(0);
        if (Character.isWhitespace(firstChar) ||
                firstChar == BLOCK_OPEN ||
                firstChar == BLOCK_CLOSE ||
                firstChar == ARRAY_OPEN ||
                firstChar == ARRAY_CLOSE ||
                firstChar == TEXT_OPEN ||
                firstChar == TEXT_CLOSE ||
                firstChar == COMMENT_LINE_START) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == DOUBLE_QUOTE_CHAR ||
                    c == SINGLE_QUOTE_CHAR ||
                    c == COMMA_CHAR ||
                    c == NAME_VALUE_SEPARATOR ||
                    c == NEW_LINE_CHAR ||
                    c == '\r') {
                return true;
            }
        }
        return Character.isWhitespace(str.charAt(str.length() - 1));
    }

    /**
     * Checks if the given string was already wrapped in double or single quotes.
     * @param str the string to check
     * @return true if the string is quoted, false otherwise
     */
    static boolean wasQuoted(String str) {
        return (str != null && str.length() > 1 &&
                ((str.charAt(0) == DOUBLE_QUOTE_CHAR && str.charAt(str.length() - 1) == DOUBLE_QUOTE_CHAR) ||
                        (str.charAt(0) == SINGLE_QUOTE_CHAR && str.charAt(str.length() - 1) == SINGLE_QUOTE_CHAR)));
    }

    /**
     * Escapes special characters within a string for APON serialization.
     * @param str the string to escape
     * @return the escaped string
     */
    public static String escape(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == DOUBLE_QUOTE_CHAR) sb.append("\\\"");
            else if (c == ESCAPE_CHAR) sb.append("\\\\");
            else if (c == '\b') sb.append("\\b");
            else if (c == '\t') sb.append("\\t");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\f') sb.append("\\f");
            else if (c == '\r') sb.append("\\r");
            else sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Unescapes escaped characters within an APON string.
     * @param str the string to unescape
     * @return the unescaped string
     */
    public static String unescape(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ESCAPE_CHAR && i + 1 < str.length()) {
                char next = str.charAt(++i);
                switch (next) {
                    case DOUBLE_QUOTE_CHAR: sb.append(DOUBLE_QUOTE_CHAR); break;
                    case SINGLE_QUOTE_CHAR: sb.append(SINGLE_QUOTE_CHAR); break;
                    case ESCAPE_CHAR: sb.append(ESCAPE_CHAR); break;
                    case 'b': sb.append('\b'); break;
                    case 't': sb.append('\t'); break;
                    case 'n': sb.append('\n'); break;
                    case 'f': sb.append('\f'); break;
                    case 'r': sb.append('\r'); break;
                    case 'u':
                        if (i + 4 < str.length()) {
                            String hex = str.substring(i + 1, i + 5);
                            try {
                                sb.append((char)Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append(ESCAPE_CHAR).append(next);
                            }
                        } else {
                            sb.append(ESCAPE_CHAR).append(next);
                        }
                        break;
                    default:
                        sb.append(ESCAPE_CHAR).append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
