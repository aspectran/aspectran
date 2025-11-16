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
 * Common constants and symbols used by the APON (Aspectran Parameters Object Notation)
 * reader/writer implementations.
 * <p>
 * APON is a human-friendly data notation inspired by JSON but optimized for
 * configuration and structured data exchange in Aspectran. It uses newlines as
 * separators instead of commas and supports an additional text value type for
 * multi-line strings. See the project guide for details.
 * </p>
 */
public abstract class AponFormat {

    /** Opening curly bracket used to start a parameters block: '{'. */
    protected static final char BLOCK_OPEN = '{';

    /** Closing curly bracket used to end a parameters block: '}'. */
    protected static final char BLOCK_CLOSE = '}';

    /** String representation of an empty parameters block: "{}". */
    protected static final String EMPTY_BLOCK = "{}";

    /** Opening square bracket used to start an array value: '['. */
    protected static final char ARRAY_OPEN = '[';

    /** Closing square bracket used to end an array value: ']'. */
    protected static final char ARRAY_CLOSE = ']';

    /** String representation of an empty array: "[]". */
    protected static final String EMPTY_ARRAY = "[]";

    /** Opening round bracket used to start a multi-line text value: '('. */
    protected static final char TEXT_OPEN = '(';

    /** Closing round bracket used to end a multi-line text value: ')'. */
    protected static final char TEXT_CLOSE = ')';

    /** Prefix character for each line within a multi-line text value. */
    public static final char TEXT_LINE_START = '|';

    /** Separator between name and value in a parameter: ':'. */
    protected static final char NAME_VALUE_SEPARATOR = ':';

    /** Start-of-line character that denotes a comment line. */
    protected static final char COMMENT_LINE_START = '#';

    /** Special marker meaning 'no control char' in parser state. */
    protected static final char NO_CONTROL_CHAR = 0;

    /** Double quote character used for quoting string values. */
    protected static final char DOUBLE_QUOTE_CHAR = '"';

    /** Single quote character optionally used for quoting string values. */
    protected static final char SINGLE_QUOTE_CHAR = '\'';

    /** Escape character used to escape quotes and special characters. */
    protected static final char ESCAPE_CHAR = '\\';

    /** Newline character used by APON as a delimiter between entries. */
    public static final char NEW_LINE_CHAR = '\n';

    /** Newline string constant (LF). */
    public static final String NEW_LINE = "\n";

    /** System-dependent newline sequence. */
    public static final String SYSTEM_NEW_LINE = System.lineSeparator();

    /** Default indentation string used when pretty-printing. */
    protected static final String DEFAULT_INDENT_STRING = "  ";

    /** A single space string convenience. */
    protected static final String SPACE = " ";

    /** A single space character convenience. */
    protected static final char SPACE_CHAR = ' ';

    /** Literal token for null textual representation. */
    protected static final String NULL = "null";

    /** Literal token for boolean true textual representation. */
    protected static final String TRUE = "true";

    /** Literal token for boolean false textual representation. */
    protected static final String FALSE = "false";

    /**
     * Determines if the given string needs to be enclosed in quotes when written to APON.
     * A string requires quoting if it contains special characters (double quotes, single quotes, newlines)
     * or if it starts or ends with a space, as these conditions could lead to parsing ambiguity
     * or loss of literal value in unquoted APON strings.
     * @param str the string to check
     * @return {@code true} if the string needs quoting; {@code false} otherwise
     */
    static boolean needsQuoting(String str) {
        return (str != null && (str.indexOf(DOUBLE_QUOTE_CHAR) >= 0 ||
                str.indexOf(SINGLE_QUOTE_CHAR) >= 0 ||
                str.startsWith(SPACE) ||
                str.endsWith(SPACE) ||
                str.contains(NEW_LINE)));
    }

    /**
     * Checks if the given string is enclosed in either double quotes ({@code "}) or single quotes ({@code '}).
     * This method considers a string "quoted" if it starts and ends with the same type of quote character
     * and has a length greater than 1.
     * @param str the string to check
     * @return {@code true} if the string is quoted; {@code false} otherwise
     */
    static boolean wasQuoted(String str) {
        return (str != null && str.length() > 1 &&
                ((str.charAt(0) == DOUBLE_QUOTE_CHAR && str.charAt(str.length() - 1) == DOUBLE_QUOTE_CHAR) ||
                        (str.charAt(0) == SINGLE_QUOTE_CHAR && str.charAt(str.length() - 1) == SINGLE_QUOTE_CHAR)));
    }

    /**
     * Escapes characters in a {@code String} to be APON-compliant.
     * @param str the string to escape, may be null
     * @return the escaped string, or null if the input was null
     */
    static String escape(String str) {
        if (str == null) {
            return null;
        }

        int len = str.length();
        if (len == 0) {
            return str;
        }

        StringBuilder sb = new StringBuilder(Math.min(len * 2, len + 16));
        char c;
        String t;
        for (int pos = 0; pos < len; pos++) {
            c = str.charAt(pos);
            switch (c) {
                case ESCAPE_CHAR:
                case DOUBLE_QUOTE_CHAR:
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Unescapes a string that contains APON-style escape sequences.
     * @param str the string to unescape, may be null
     * @return a new unescaped string, or the input if null or no escaping is needed
     * @throws IllegalArgumentException if the string contains an invalid escape sequence
     */
    public static String unescape(String str) throws IllegalArgumentException {
        if (str == null || str.indexOf(ESCAPE_CHAR) == -1) {
            return str;
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int pos = 0; pos < len;) {
            char c = str.charAt(pos++);
            if (c == ESCAPE_CHAR) {
                if (pos >= len) {
                    throw new IllegalArgumentException("Unterminated escape sequence");
                }
                c = str.charAt(pos++);
                switch (c) {
                    case ESCAPE_CHAR:
                    case DOUBLE_QUOTE_CHAR:
                    case SINGLE_QUOTE_CHAR:
                        sb.append(c);
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 'u':
                        if (pos + 4 > len) {
                            throw new IllegalArgumentException("Unterminated escape sequence");
                        }
                        String hex = str.substring(pos, pos + 4);
                        try {
                            sb.append((char)Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid unicode escape sequence: \\u" + hex, e);
                        }
                        pos += 4;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid escape sequence: " + c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
