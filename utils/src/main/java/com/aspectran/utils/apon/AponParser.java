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
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
import static com.aspectran.utils.apon.AponFormat.COMMA_CHAR;
import static com.aspectran.utils.apon.AponFormat.COMMENT_CHAR;
import static com.aspectran.utils.apon.AponFormat.DOUBLE_QUOTE_CHAR;
import static com.aspectran.utils.apon.AponFormat.EMPTY_ARRAY;
import static com.aspectran.utils.apon.AponFormat.EMPTY_BLOCK;
import static com.aspectran.utils.apon.AponFormat.ESCAPE_CHAR;
import static com.aspectran.utils.apon.AponFormat.FALSE;
import static com.aspectran.utils.apon.AponFormat.NAME_VALUE_SEPARATOR;
import static com.aspectran.utils.apon.AponFormat.NEW_LINE_CHAR;
import static com.aspectran.utils.apon.AponFormat.NO_CONTROL_CHAR;
import static com.aspectran.utils.apon.AponFormat.NULL;
import static com.aspectran.utils.apon.AponFormat.SINGLE_QUOTE_CHAR;
import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;
import static com.aspectran.utils.apon.AponFormat.TEXT_CLOSE;
import static com.aspectran.utils.apon.AponFormat.TEXT_LINE_START;
import static com.aspectran.utils.apon.AponFormat.TEXT_OPEN;
import static com.aspectran.utils.apon.AponFormat.TRUE;

/**
 * A character-level parser for APON (Aspectran Parameters Object Notation).
 * <p>
 * APON is a simplified text format designed for representing hierarchical
 * parameter objects, supporting properties, arrays, and nested structures.
 * </p>
 */
public class AponParser {

    private final BufferedReader reader;

    private int lineNumber = 0;

    private int linePos = 0;

    private String originalLine;

    private String currentLine;

    /**
     * Constructs a new parser using the specified APON format string.
     * @param apon the APON format string to parse
     */
    public AponParser(String apon) {
        this(new StringReader(apon));
    }

    /**
     * Constructs a new parser using the specified Reader.
     * @param reader the reader to read the APON character stream from
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
     * Parses the APON document and populates the given parameters object.
     * @param <T> the type of the {@code Parameters} object
     * @param parameters the {@code Parameters} object to populate
     * @return the populated parameters object
     * @throws AponParseException if a parsing error occurs due to syntax or format issues
     */
    public <T extends Parameters> T parse(T parameters) throws AponParseException {
        Assert.notNull(parameters, "parameters must not be null");
        try {
            skipWhitespaceAndCommas();
            char firstChar = peekChar();
            if (firstChar == NO_CONTROL_CHAR) {
                return parameters;
            }

            if (firstChar == BLOCK_OPEN) {
                readChar();
                parameters.setBraceless(false);
                parseNestedObject(parameters);
                skipWhitespaceAndCommas();
                if (peekChar() != NO_CONTROL_CHAR) {
                    throw syntaxError("Unexpected content after closing brace '}' of root object");
                }
            } else if (firstChar == ARRAY_OPEN) {
                readChar();
                List<Object> list = parseArray(parameters, ArrayParameters.NONAME);
                parameters.putValue(ArrayParameters.NONAME, list);
                skipWhitespaceAndCommas();
                if (peekChar() != NO_CONTROL_CHAR) {
                    throw syntaxError("Unexpected content after closing bracket ']' of root array");
                }
            } else {
                parameters.setBraceless(true);
                while (peekChar() != NO_CONTROL_CHAR) {
                    if (peekChar() == BLOCK_CLOSE) {
                        throw syntaxError("Unexpected closing brace '}' at top level");
                    }
                    parseItem(parameters);
                    skipWhitespaceAndCommas();
                }
            }
        } catch (AponParseException e) {
            throw e;
        } catch (IOException e) {
            throw new AponParseException("Failed to parse APON document", e);
        }
        return parameters;
    }

    /**
     * Parses a single parameter name-value pair item and adds it to the parameters.
     * @param parameters the {@code Parameters} object to add the parsed item to
     * @throws IOException if an I/O error occurs
     */
    private void parseItem(Parameters parameters) throws IOException {
        skipWhitespaceAndCommas();
        char peek = peekChar();
        if (peek == NO_CONTROL_CHAR) {
            return;
        }
        if (peek == BLOCK_CLOSE || peek == ARRAY_CLOSE) {
            throw syntaxError("Unexpected character '" + peek + "'");
        }

        int startLinePos = linePos;
        String name = readName();
        if (StringUtils.isEmpty(name)) {
            throw syntaxError("Parameter name is missing");
        }

        skipWhitespace();
        if (peekChar() != NAME_VALUE_SEPARATOR) {
            linePos = startLinePos;
            throw syntaxError("Invalid line format; a parameter must be in 'name: value' format");
        }
        readChar(); // consume ':'
        skipWhitespace();

        ValueType valueType = ValueType.resolveByHint(name);
        boolean valueTypeHinted = false;
        if (valueType != null) {
            valueTypeHinted = true;
            name = ValueType.stripHint(name);
            if (valueType == ValueType.VARIABLE || valueType == ValueType.PARAMETERS) {
                valueType = null;
            }
        }

        // Before putting the value, check if this key is already assigned.
        // If it is, this is a repeated key (no brackets).
        boolean repeatedKey = parameters.isAssigned(name);

        Object value;
        if (valueType != null) {
            value = parseHintedValue(parameters, name, valueType);
        } else {
            value = parseValue(parameters, name, false);
        }

        parameters.putValue(name, value);

        Parameter parameter = parameters.getParameter(name);
        if (parameter != null) {
            if (valueTypeHinted) {
                parameter.setValueTypeHinted(true);
            }
            // If the key was already assigned, it means this was a repeated key.
            // We force bracketed = false only if the parameter is NOT fixed.
            if (repeatedKey && parameter.isArray() && !parameter.isValueTypeFixed()) {
                parameter.setBracketed(false);
            }
        }
    }

    /**
     * Reads a quoted string from the current position, handling escape sequences.
     * @param quoteChar the character used for quoting (e.g., single or double quote)
     * @return the decoded string value
     * @throws IOException if an I/O error occurs or the string is unclosed
     */
    @NonNull
    private String readQuotedString(char quoteChar) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = readRawChar();
            if (c == NO_CONTROL_CHAR || c == NEW_LINE_CHAR) {
                throw syntaxError("Unclosed quotation mark");
            }
            if (c == quoteChar) {
                break;
            }
            if (c == ESCAPE_CHAR) {
                char next = readRawChar();
                if (next == NO_CONTROL_CHAR) {
                    throw syntaxError("Unterminated escape sequence");
                }
                if (next == 'b') sb.append('\b');
                else if (next == 't') sb.append('\t');
                else if (next == 'n') sb.append('\n');
                else if (next == 'f') sb.append('\f');
                else if (next == 'r') sb.append('\r');
                else if (next == 'u') {
                    StringBuilder hex = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        char h = readRawChar();
                        if (h == NO_CONTROL_CHAR) throw syntaxError("Unterminated escape sequence");
                        hex.append(h);
                    }
                    try {
                        sb.append((char)Integer.parseInt(hex.toString(), 16));
                    } catch (NumberFormatException e) {
                        throw syntaxError("Invalid unicode escape sequence: \\u" + hex, e);
                    }
                } else sb.append(next);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Reads the parameter name. If the name is quoted, handles it as a quoted string;
     * otherwise, reads it as an unquoted token.
     * @return the parameter name
     * @throws IOException if an I/O error occurs
     */
    private String readName() throws IOException {
        skipWhitespace();
        char firstChar = peekChar();
        if (firstChar == DOUBLE_QUOTE_CHAR || firstChar == SINGLE_QUOTE_CHAR) {
            readChar();
            return readQuotedString(firstChar);
        } else {
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = peekChar();
                if (c == NO_CONTROL_CHAR || c == NEW_LINE_CHAR || c == NAME_VALUE_SEPARATOR ||
                        c == COMMENT_CHAR) {
                    break;
                }
                // Structural characters only break token if they are the FIRST character
                if (sb.isEmpty()) {
                    if (c == COMMA_CHAR || c == BLOCK_OPEN || c == BLOCK_CLOSE ||
                            c == ARRAY_OPEN || c == ARRAY_CLOSE) {
                        break;
                    }
                }
                if (Character.isWhitespace(c)) {
                    int savedPos = linePos;
                    skipWhitespace();
                    char next = peekChar();
                    if (next == NAME_VALUE_SEPARATOR) {
                        break;
                    }
                    linePos = savedPos;
                }
                sb.append(readChar());
            }
            return sb.toString().trim();
        }
    }

    /**
     * Parses a nested block of parameters enclosed in curly braces '{' and '}'.
     * @param container the {@code Parameters} object to hold the nested parameters
     * @throws IOException if an I/O error occurs or the block is unclosed
     */
    private void parseNestedObject(Parameters container) throws IOException {
        while (true) {
            skipWhitespaceAndCommas();
            char firstChar = peekChar();
            if (firstChar == NO_CONTROL_CHAR) break;
            if (firstChar == BLOCK_CLOSE) {
                readChar();
                return;
            }
            parseItem(container);
        }
        throw syntaxError("Unclosed object block; no closing curly bracket '}' was found");
    }

    /**
     * Parses an array of values enclosed in square brackets '[' and ']'.
     * @param container the parent {@code Parameters} object
     * @param name the name of the parameter representing the array
     * @return the list of parsed values
     * @throws IOException if an I/O error occurs or the array is unclosed
     */
    private List<Object> parseArray(Parameters container, String name) throws IOException {
        List<Object> list = new ArrayList<>();
        while (true) {
            skipWhitespaceAndCommas();
            char firstChar = peekChar();
            if (firstChar == NO_CONTROL_CHAR) {
                throw syntaxError("Unclosed array bracket; no closing square bracket ']' was found");
            }
            if (firstChar == ARRAY_CLOSE) {
                readChar();
                return list;
            }
            Object value = parseValue(container, name, true);
            list.add(value);
        }
    }

    /**
     * Parses an array of values with a specified type hint.
     * @param container the parent {@code Parameters} object
     * @param name the name of the parameter representing the array
     * @param valueType the expected type of the array elements
     * @return the list of parsed values
     * @throws IOException if an I/O error occurs
     */
    private List<Object> parseHintedArray(Parameters container, String name, ValueType valueType) throws IOException {
        List<Object> list = new ArrayList<>();
        while (true) {
            skipWhitespaceAndCommas();
            char firstChar = peekChar();
            if (firstChar == NO_CONTROL_CHAR) {
                throw syntaxError("Unclosed array bracket; no closing square bracket ']' was found");
            }
            if (firstChar == ARRAY_CLOSE) {
                readChar();
                return list;
            }
            Object value = parseHintedValue(container, name, valueType);
            list.add(value);
        }
    }

    /**
     * Parses a parameter value, determining its type dynamically based on its format.
     * @param container the parent {@code Parameters} object
     * @param name the name of the parameter
     * @param inArray whether the parsing is occurring within an array context
     * @return the parsed parameter value, or null
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    private Object parseValue(Parameters container, String name, boolean inArray) throws IOException {
        skipWhitespace();
        char firstChar = peekChar();
        if (firstChar == NO_CONTROL_CHAR) return null;

        if (firstChar == BLOCK_OPEN) {
            readChar();
            Parameters nestedParams = null;
            if (container instanceof ArrayParameters arrayParameters) {
                nestedParams = arrayParameters.createParameters(ArrayParameters.NONAME);
            } else if (container != null && name != null && container.hasParameter(name)) {
                nestedParams = container.createParameters(name);
            }
            if (nestedParams == null) nestedParams = new VariableParameters();
            parseNestedObject(nestedParams);
            return nestedParams;
        }
        if (firstChar == ARRAY_OPEN) {
            readChar();
            return parseArray(container, name);
        }
        if (firstChar == TEXT_OPEN) {
            int savedPos = linePos;
            readChar();
            skipWhitespaceOnlyOnLine();
            if (peekChar() == NEW_LINE_CHAR) {
                return parseTextBlock();
            }
            linePos = savedPos;
        }

        if (firstChar == BLOCK_CLOSE || firstChar == ARRAY_CLOSE) return null;

        String valueStr = readToken(inArray);
        if (valueStr == null) return null;

        switch (valueStr) {
            case EMPTY_BLOCK -> {
                Parameters nestedParams = null;
                if (container instanceof ArrayParameters arrayParameters) {
                    nestedParams = arrayParameters.createParameters(ArrayParameters.NONAME);
                } else if (container != null && name != null && container.hasParameter(name)) {
                    nestedParams = container.createParameters(name);
                }
                return (nestedParams != null ? nestedParams : new VariableParameters());
            }
            case EMPTY_ARRAY -> {
                return new ArrayList<>();
            }
            case NULL -> {
                return null;
            }
            case TRUE -> {
                return Boolean.TRUE;
            }
            case FALSE -> {
                return Boolean.FALSE;
            }
        }

        try {
            if (valueStr.indexOf('.') > -1 || valueStr.indexOf('e') > -1 || valueStr.indexOf('E') > -1) {
                return Double.parseDouble(valueStr);
            } else {
                long longValue = Long.parseLong(valueStr);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) return (int)longValue;
                return longValue;
            }
        } catch (NumberFormatException e) {
            return valueStr;
        }
    }

    /**
     * Reads a plain value token. Handles unquoted values, boolean values,
     * numeric literals, null literals, and escape sequences.
     * @param inArray whether the token is being read within an array
     * @return the read token as a string, or null if empty
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    private String readToken(boolean inArray) throws IOException {
        skipWhitespace();
        char firstChar = peekChar();
        if (firstChar == DOUBLE_QUOTE_CHAR || firstChar == SINGLE_QUOTE_CHAR) {
            readChar();
            return readQuotedString(firstChar);
        }
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = peekChar();
            if (c == NO_CONTROL_CHAR || c == NEW_LINE_CHAR || c == COMMENT_CHAR) {
                break;
            }
            // Structural characters break token only at the START of value
            if (sb.isEmpty()) {
                if (c == BLOCK_OPEN || c == BLOCK_CLOSE ||
                        c == ARRAY_OPEN || c == ARRAY_CLOSE) {
                    break;
                }
            } else {
                // But closing braces must ALWAYS break the token to terminate container
                if (c == BLOCK_CLOSE || c == ARRAY_CLOSE) {
                    break;
                }
            }
            if (c == COMMA_CHAR) {
                if (inArray || hasColonAheadOnLine()) {
                    break;
                }
            }
            if (Character.isWhitespace(c)) {
                int savedPos = linePos;
                skipWhitespaceOnlyOnLine();
                char next = peekChar();
                if (next == BLOCK_CLOSE || next == ARRAY_CLOSE || next == COMMA_CHAR ||
                        next == COMMENT_CHAR) {
                    linePos = savedPos;
                    break;
                }
                linePos = savedPos;
            }
            if (c == ESCAPE_CHAR) {
                readChar();
                char next = readRawChar();
                if (next == NO_CONTROL_CHAR) break;
                if (next == COMMA_CHAR || next == ESCAPE_CHAR) {
                    sb.append(next);
                } else {
                    sb.append(ESCAPE_CHAR).append(next);
                }
                continue;
            }
            sb.append(readChar());
        }
        String valueStr = sb.toString().trim();
        return (valueStr.isEmpty() ? null : valueStr);
    }

    /**
     * Checks if there is a colon (name-value separator) ahead on the current line.
     * Used to distinguish between plain values and nested key-value structures.
     * @return true if a colon is found ahead on the current line; false otherwise
     */
    private boolean hasColonAheadOnLine() {
        if (currentLine == null) return false;
        for (int i = linePos + 1; i < currentLine.length(); i++) {
            char next = currentLine.charAt(i);
            if (next == NAME_VALUE_SEPARATOR) return true;
            if (next == BLOCK_OPEN || next == BLOCK_CLOSE || next == ARRAY_OPEN || next == ARRAY_CLOSE ||
                    next == COMMENT_CHAR) break;
        }
        return false;
    }

    /**
     * Parses a parameter value according to a specified type hint.
     * @param container the parent {@code Parameters} object
     * @param name the name of the parameter
     * @param valueType the expected type of the value
     * @return the parsed parameter value matching the hinted type, or null
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    private Object parseHintedValue(Parameters container, String name, @NonNull ValueType valueType) throws IOException {
        skipWhitespace();
        char firstChar = peekChar();
        if (firstChar == NO_CONTROL_CHAR) return null;

        if (firstChar == BLOCK_OPEN) {
            readChar();
            Parameters nestedParams = null;
            if (container instanceof ArrayParameters arrayParameters) {
                nestedParams = arrayParameters.createParameters(ArrayParameters.NONAME);
            } else if (container != null && name != null && container.hasParameter(name)) {
                nestedParams = container.createParameters(name);
            }
            if (nestedParams == null) nestedParams = new VariableParameters();
            parseNestedObject(nestedParams);
            return nestedParams;
        }
        if (firstChar == ARRAY_OPEN) {
            readChar();
            return parseHintedArray(container, name, valueType);
        }

        if (valueType == ValueType.TEXT) {
            if (firstChar == TEXT_OPEN) {
                int savedPos = linePos;
                readChar();
                skipWhitespaceOnlyOnLine();
                if (peekChar() == NEW_LINE_CHAR) {
                    return parseTextBlock();
                }
                linePos = savedPos;
            }
            String valueStr = readToken(false);
            return (NULL.equals(valueStr) ? null : valueStr);
        }

        String valueStr = readToken(false);
        if (valueStr == null || NULL.equals(valueStr)) return null;

        if (EMPTY_BLOCK.equals(valueStr)) return new VariableParameters();
        if (EMPTY_ARRAY.equals(valueStr)) return new ArrayList<>();

        try {
            switch (valueType) {
                case STRING -> { return valueStr; }
                case INT -> { return Integer.parseInt(valueStr); }
                case LONG -> { return Long.parseLong(valueStr); }
                case FLOAT -> { return Float.parseFloat(valueStr); }
                case DOUBLE -> { return Double.parseDouble(valueStr); }
                case BOOLEAN -> { return Boolean.parseBoolean(valueStr); }
                default -> { return valueStr; }
            }
        } catch (NumberFormatException e) {
            throw syntaxError("Invalid value '" + valueStr + "' for type '" + valueType + "'", e);
        }
    }

    /**
     * Parses a multi-line text block starting with '(' and ending with ')',
     * where each line in the block must start with the '|' character.
     * @return the concatenated multi-line string content
     * @throws IOException if an I/O error occurs or the text block format is invalid
     */
    @NonNull
    private String parseTextBlock() throws IOException {
        StringBuilder sb = null;
        if (peekChar() == NEW_LINE_CHAR) {
            readChar();
        } else {
            throw syntaxError("Multi-line text block must start on a new line after '('");
        }

        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            lineNumber++;
            originalLine = line;
            currentLine = line;
            linePos = 0;

            skipWhitespaceOnlyOnLine();
            if (linePos < currentLine.length()) {
                char c = currentLine.charAt(linePos);
                if (c == TEXT_CLOSE) {
                    linePos++;
                    return (sb != null ? sb.toString() : "");
                }
                if (c == TEXT_LINE_START) {
                    if (sb == null) sb = new StringBuilder();
                    else sb.append(SYSTEM_NEW_LINE);
                    sb.append(currentLine.substring(linePos + 1));
                    linePos = currentLine.length();
                    continue;
                }
            } else {
                continue;
            }
            throw syntaxError("Text block lines must start with a '|' character or end with ')'");
        }
        throw syntaxError("Unclosed text block; no closing round bracket ')' was found");
    }

    /**
     * Peeks at the next character without consuming it.
     * @return the next character, or {@link AponFormat#NO_CONTROL_CHAR} if the end of stream is reached
     * @throws IOException if an I/O error occurs
     */
    private char peekChar() throws IOException {
        if (currentLine == null || linePos > currentLine.length()) {
            if (nextLine() == null) return NO_CONTROL_CHAR;
        }
        if (linePos == currentLine.length()) {
            return NEW_LINE_CHAR;
        }
        return currentLine.charAt(linePos);
    }

    /**
     * Reads and consumes the next character.
     * @return the next character, or {@link AponFormat#NO_CONTROL_CHAR} if the end of stream is reached
     * @throws IOException if an I/O error occurs
     */
    private char readChar() throws IOException {
        char c = peekChar();
        if (c != NO_CONTROL_CHAR) {
            linePos++;
        }
        return c;
    }

    /**
     * Reads the raw character at the current line position without performing line wrapping checks.
     * @return the character at the current position, or {@link AponFormat#NO_CONTROL_CHAR} if at the end of the line
     */
    private char readRawChar() {
        if (currentLine == null || linePos >= currentLine.length()) {
            return NO_CONTROL_CHAR;
        }
        return currentLine.charAt(linePos++);
    }

    /**
     * Reads the next line from the buffered reader, ignoring comment lines.
     * @return the next valid line content, or null if the end of the stream is reached
     * @throws IOException if an I/O error occurs
     */
    @Nullable
    private String nextLine() throws IOException {
        while ((originalLine = reader.readLine()) != null) {
            lineNumber++;
            currentLine = originalLine;
            linePos = 0;
            skipWhitespaceOnlyOnLine();
            if (linePos < currentLine.length() && currentLine.charAt(linePos) == COMMENT_CHAR) {
                continue;
            }
            if (linePos == currentLine.length()) {
                continue;
            }
            return currentLine;
        }
        currentLine = null;
        return null;
    }

    /**
     * Skips whitespace characters on the current line, ignoring comments.
     * @throws IOException if an I/O error occurs
     */
    private void skipWhitespace() throws IOException {
        while (true) {
            char c = peekChar();
            if (c == NO_CONTROL_CHAR) break;
            if (c == COMMENT_CHAR) {
                linePos = (currentLine != null ? currentLine.length() : 0);
                continue;
            }
            if (Character.isWhitespace(c) && c != NEW_LINE_CHAR) {
                readChar();
            } else {
                break;
            }
        }
    }

    /**
     * Skips only the horizontal whitespace characters on the current line.
     */
    private void skipWhitespaceOnlyOnLine() {
        if (currentLine != null) {
            while (linePos < currentLine.length() && Character.isWhitespace(currentLine.charAt(linePos))) {
                linePos++;
            }
        }
    }

    /**
     * Skips whitespace and comma characters, ignoring comments.
     * @throws IOException if an I/O error occurs
     */
    private void skipWhitespaceAndCommas() throws IOException {
        while (true) {
            char c = peekChar();
            if (c == NO_CONTROL_CHAR) break;
            if (c == COMMENT_CHAR) {
                linePos = (currentLine != null ? currentLine.length() : 0);
                continue;
            }
            if (Character.isWhitespace(c) || c == COMMA_CHAR) {
                readChar();
            } else {
                break;
            }
        }
    }

    /**
     * Creates an {@link AponParseException} representing a syntax error.
     * @param message the detail error message
     * @return the parsed exception instance
     */
    @NonNull
    private AponParseException syntaxError(String message) {
        return new MalformedAponException(lineNumber, linePos + 1, currentLine, message);
    }

    /**
     * Creates an {@link AponParseException} representing a syntax error with a cause.
     * @param message the detail error message
     * @param cause the underlying cause
     * @return the parsed exception instance
     */
    @NonNull
    private AponParseException syntaxError(String message, Throwable cause) {
        MalformedAponException e = new MalformedAponException(lineNumber, linePos + 1, currentLine, message);
        e.initCause(cause);
        return e;
    }

}
