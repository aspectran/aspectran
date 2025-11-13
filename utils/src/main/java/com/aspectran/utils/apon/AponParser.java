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

import com.aspectran.utils.Assert;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.aspectran.utils.apon.AponFormat.ARRAY_CLOSE;
import static com.aspectran.utils.apon.AponFormat.ARRAY_OPEN;
import static com.aspectran.utils.apon.AponFormat.BLOCK_CLOSE;
import static com.aspectran.utils.apon.AponFormat.BLOCK_OPEN;
import static com.aspectran.utils.apon.AponFormat.COMMENT_LINE_START;
import static com.aspectran.utils.apon.AponFormat.DOUBLE_QUOTE_CHAR;
import static com.aspectran.utils.apon.AponFormat.EMPTY_ARRAY;
import static com.aspectran.utils.apon.AponFormat.EMPTY_BLOCK;
import static com.aspectran.utils.apon.AponFormat.ESCAPE_CHAR;
import static com.aspectran.utils.apon.AponFormat.NAME_VALUE_SEPARATOR;
import static com.aspectran.utils.apon.AponFormat.SINGLE_QUOTE_CHAR;
import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;
import static com.aspectran.utils.apon.AponFormat.TEXT_CLOSE;
import static com.aspectran.utils.apon.AponFormat.TEXT_LINE_START;
import static com.aspectran.utils.apon.AponFormat.TEXT_OPEN;

/**
 * A modern, structure-based parser for APON (Aspectran Parameters Object Notation).
 *
 * <p>This class provides a more efficient and intuitive alternative to AponReader,
 * especially for handling multi-dimensional arrays. It parses APON text by
 * identifying structural tokens ({}, [], etc.) and recursively building the
 * corresponding object graph. Nested arrays are parsed directly into nested
 * {@code List} objects.</p>
 *
 * @since 9.x.x
 */
public class AponParser {

    private final BufferedReader reader;

    private int lineNumber = 0;

    private String currentLine;

    /**
     * Creates a new AponParser for the given APON-formatted string.
     * @param apon the APON formatted string
     */
    public AponParser(String apon) {
        this(new StringReader(apon));
    }

    /**
     * Creates a new AponParser for the given {@link Reader}.
     * @param reader the character stream to read from
     */
    public AponParser(Reader reader) {
        Assert.notNull(reader, "reader must not be null");
        if (reader instanceof BufferedReader bufferedReader) {
            this.reader = bufferedReader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    /**
     * Parses an APON document and populates the given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public <T extends Parameters> T parse(T parameters) throws AponParseException {
        Assert.notNull(parameters, "parameters must not be null");
        try {
            while (nextLine() != null) {
                if (currentLine.length() == 1 && currentLine.charAt(0) == BLOCK_CLOSE) {
                    throw new AponParseException("Unexpected closing brace '}' at top level at line " + lineNumber);
                }

                int separatorIndex = currentLine.indexOf(NAME_VALUE_SEPARATOR);
                if (separatorIndex == -1) {
                    throw new AponParseException("Invalid line format; a parameter must be in 'name: value' format at line " + lineNumber);
                }

                String name = currentLine.substring(0, separatorIndex).trim();
                String valueStr = currentLine.substring(separatorIndex + 1).trim();

                Object value = parseValue(valueStr);
                parameters.putValue(name, value);
            }
        } catch (Exception e) {
            throw new AponParseException("Failed to parse APON document into " + parameters.getClass().getName(), e);
        }
        return parameters;
    }

    /**
     * Parses a nested parameter block (content within braces).
     * @param container the current parameters object to populate
     * @throws IOException if an I/O error occurs
     */
    private void parseNestedObject(Parameters container) throws IOException {
        while (nextLine() != null) {
            if (currentLine.length() == 1 && currentLine.charAt(0) == BLOCK_CLOSE) {
                return; // End of current object
            }

            int separatorIndex = currentLine.indexOf(NAME_VALUE_SEPARATOR);
            if (separatorIndex == -1) {
                throw new AponParseException("Invalid line format; a parameter must be in 'name: value' format at line " + lineNumber);
            }

            String name = currentLine.substring(0, separatorIndex).trim();
            String valueStr = currentLine.substring(separatorIndex + 1).trim();

            Object value = parseValue(valueStr);
            container.putValue(name, value);
        }
        throw new AponParseException("Unclosed object block at line " + lineNumber);
    }

    /**
     * Parses an array.
     * @return a new List containing the parsed elements
     * @throws IOException if an I/O error occurs
     */
    private List<Object> parseArray() throws IOException {
        List<Object> list = new ArrayList<>();
        while (nextLine() != null) {
            if (currentLine.length() == 1 && currentLine.charAt(0) == ARRAY_CLOSE) {
                return list; // End of current array
            }
            Object value = parseValue(currentLine);
            list.add(value);
        }
        throw new AponParseException("Unclosed array bracket at line " + lineNumber);
    }

    /**
     * Parses the next value from the stream, which could be a scalar,
     * an object, or an array.
     * @param valueStr the string representation of the value
     * @return the parsed value as an Object
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    private Object parseValue(@NonNull String valueStr) throws IOException {
        if (valueStr.isEmpty()) {
            return "";
        }

        if (EMPTY_BLOCK.equals(valueStr)) {
            return new VariableParameters();
        }
        if (EMPTY_ARRAY.equals(valueStr)) {
            return new ArrayList<>();
        }

        char firstChar = valueStr.charAt(0);
        int length = valueStr.length();

        if (length == 1) {
            if (firstChar == BLOCK_OPEN) {
                Parameters nestedParams = new VariableParameters();
                parseNestedObject(nestedParams);
                return nestedParams;
            }
            if (firstChar == ARRAY_OPEN) {
                return parseArray();
            }
            if (firstChar == TEXT_OPEN) {
                return parseTextBlock();
            }
        }

        if ("null".equals(valueStr)) {
            return null;
        }
        if ("true".equals(valueStr)) {
            return Boolean.TRUE;
        }
        if ("false".equals(valueStr)) {
            return Boolean.FALSE;
        }

        if ((firstChar == DOUBLE_QUOTE_CHAR && valueStr.charAt(length - 1) == DOUBLE_QUOTE_CHAR) ||
                (firstChar == SINGLE_QUOTE_CHAR && valueStr.charAt(length - 1) == SINGLE_QUOTE_CHAR)) {
            return unescape(valueStr.substring(1, length - 1));
        }

        // Try parsing as a number
        try {
            if (valueStr.indexOf('.') > -1 || valueStr.indexOf('e') > -1 || valueStr.indexOf('E') > -1) {
                return Double.parseDouble(valueStr);
            } else {
                long longValue = Long.parseLong(valueStr);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int)longValue;
                } else {
                    return longValue;
                }
            }
        } catch (NumberFormatException e) {
            // Not a number, treat as an unquoted string
        }

        return valueStr;
    }

    /**
     * Parses a multi-line text block.
     * @return the parsed text block as a single string
     * @throws IOException if an I/O error occurs
     */
    @NonNull
    private String parseTextBlock() throws IOException {
        StringBuilder sb = null;
        while (nextLine() != null) {
            if (currentLine.length() == 1 && currentLine.charAt(0) == TEXT_CLOSE) {
                return (sb != null ? sb.toString() : "");
            }

            if (!currentLine.isEmpty() && currentLine.charAt(0) == TEXT_LINE_START) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(SYSTEM_NEW_LINE);
                }
                String line = currentLine.substring(1);
                if (!line.isEmpty()) {
                    sb.append(line);
                }
            } else {
                throw new AponParseException("Text block lines must start with a '|' character at line " + lineNumber);
            }
        }
        throw new AponParseException("Unclosed text block at line " + lineNumber);
    }

    /**
     * Reads the next meaningful line, skipping empty lines and comments.
     * @return the next non-empty, non-comment line, or null if end of stream
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    private String nextLine() throws IOException {
        while ((currentLine = reader.readLine()) != null) {
            lineNumber++;
            currentLine = currentLine.trim();
            if (!currentLine.isEmpty() && currentLine.charAt(0) != COMMENT_LINE_START) {
                return currentLine;
            }
        }
        return null;
    }

    private String unescape(String str) throws AponParseException {
        if (str == null || str.indexOf(ESCAPE_CHAR) == -1) {
            return str;
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int pos = 0; pos < len;) {
            char c = str.charAt(pos++);
            if (c == ESCAPE_CHAR) {
                if (pos >= len) {
                    throw new AponParseException("Unterminated escape sequence at line " + lineNumber);
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
                            throw new AponParseException("Unterminated escape sequence at line " + lineNumber);
                        }
                        String hex = str.substring(pos, pos + 4);
                        try {
                            sb.append((char)Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            throw new AponParseException("Invalid unicode escape sequence: \\u" + hex + " at line " + lineNumber, e);
                        }
                        pos += 4;
                        break;
                    default:
                        throw new AponParseException("Invalid escape sequence: " + c + " at line " + lineNumber);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * A static utility method that parses an APON-formatted string into a new {@link VariableParameters} object.
     * @param apon the APON-formatted string
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters parse(String apon) throws AponParseException {
        if (StringUtils.isEmpty(apon)) {
            return new VariableParameters();
        }
        return new AponParser(apon).parse(new VariableParameters());
    }

    /**
     * A static utility method that parses an APON-formatted string into a new container of the given type.
     * @param <T> the type of the new container
     * @param apon the APON-formatted string
     * @param requiredType the concrete {@link Parameters} implementation to instantiate
     * @return a new, populated container instance
     * @throws AponParseException if parsing fails or the type cannot be instantiated
     */
    public static <T extends Parameters> T parse(String apon, Class<T> requiredType) throws AponParseException {
        T parameters = ClassUtils.createInstance(requiredType);
        if (StringUtils.isEmpty(apon)) {
            return parameters;
        }
        return new AponParser(apon).parse(parameters);
    }

}
