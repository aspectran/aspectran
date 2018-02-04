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

public class AponFormat {

    protected static final char CURLY_BRACKET_OPEN = '{';

    protected static final char CURLY_BRACKET_CLOSE = '}';

    protected static final char SQUARE_BRACKET_OPEN = '[';

    protected static final char SQUARE_BRACKET_CLOSE = ']';

    protected static final char ROUND_BRACKET_OPEN = '(';

    protected static final char ROUND_BRACKET_CLOSE = ')';

    public static final char TEXT_LINE_START = '|';

    protected static final char NAME_VALUE_SEPARATOR = ':';

    protected static final char COMMENT_LINE_START = '#';

    protected static final char NO_CONTROL_CHAR = 0;

    protected static final char DOUBLE_QUOTE_CHAR = '"';

    protected static final char SINGLE_QUOTE_CHAR = '\'';

    protected static final char ESCAPE_CHAR = '\\';

    public static final char NEW_LINE_CHAR = '\n';

    protected static final String INDENT_STRING = "\t";

    protected static final String SPACE = " ";

    protected static final char SPACE_CHAR = ' ';

    protected static final String NULL = "null";

    protected static final String TRUE = "true";

    protected static final String FALSE = "false";

    public static String escape(String value, boolean noQuote) {
        if (value == null) {
            return null;
        }

        int vlen = value.length();
        if (vlen == 0) {
            return value;
        }

        StringBuilder sb = new StringBuilder(vlen);
        char c;
        String t;

        for (int i = 0; i < vlen; i++) {
            c = value.charAt(i);

            switch (c) {
            case '\\':
            case '"':
                if (!noQuote) {
                    sb.append('\\');
                }
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

    public static String unescape(String value) {
        if (value == null) {
            return null;
        }

        int vlen = value.length();
        if (vlen == 0 || value.indexOf(ESCAPE_CHAR) == -1) {
            return value;
        }

        StringBuilder sb = new StringBuilder(vlen);
        char c;

        for (int i = 0; i < vlen; i++) {
            c = value.charAt(i);

            if (c == ESCAPE_CHAR) {
                if (++i < vlen) {
                    c = value.charAt(i);
                } else {
                    c = 0;
                }

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
                default:
                    return null;
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}
