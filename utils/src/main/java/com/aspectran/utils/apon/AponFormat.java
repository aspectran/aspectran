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
    protected static final char CURLY_BRACKET_OPEN = '{';

    /** Closing curly bracket used to end a parameters block: '}'. */
    protected static final char CURLY_BRACKET_CLOSE = '}';

    /** Opening square bracket used to start an array value: '['. */
    protected static final char SQUARE_BRACKET_OPEN = '[';

    /** Closing square bracket used to end an array value: ']'. */
    protected static final char SQUARE_BRACKET_CLOSE = ']';

    /** Opening round bracket used to start a multi-line text value: '('. */
    protected static final char ROUND_BRACKET_OPEN = '(';

    /** Closing round bracket used to end a multi-line text value: ')'. */
    protected static final char ROUND_BRACKET_CLOSE = ')';

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

}
