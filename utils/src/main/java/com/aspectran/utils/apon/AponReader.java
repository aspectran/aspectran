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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static com.aspectran.utils.apon.AponFormat.ARRAY_CLOSE;
import static com.aspectran.utils.apon.AponFormat.ARRAY_OPEN;
import static com.aspectran.utils.apon.AponFormat.BLOCK_CLOSE;
import static com.aspectran.utils.apon.AponFormat.BLOCK_OPEN;
import static com.aspectran.utils.apon.AponFormat.COMMENT_LINE_START;
import static com.aspectran.utils.apon.AponFormat.DOUBLE_QUOTE_CHAR;
import static com.aspectran.utils.apon.AponFormat.EMPTY_ARRAY;
import static com.aspectran.utils.apon.AponFormat.EMPTY_BLOCK;
import static com.aspectran.utils.apon.AponFormat.FALSE;
import static com.aspectran.utils.apon.AponFormat.NAME_VALUE_SEPARATOR;
import static com.aspectran.utils.apon.AponFormat.NO_CONTROL_CHAR;
import static com.aspectran.utils.apon.AponFormat.NULL;
import static com.aspectran.utils.apon.AponFormat.SINGLE_QUOTE_CHAR;
import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;
import static com.aspectran.utils.apon.AponFormat.TEXT_CLOSE;
import static com.aspectran.utils.apon.AponFormat.TEXT_LINE_START;
import static com.aspectran.utils.apon.AponFormat.TEXT_OPEN;
import static com.aspectran.utils.apon.AponFormat.TRUE;

/**
 * A streaming reader that parses APON (Aspectran Parameters Object Notation) text
 * into {@link Parameters} containers.
 * <p>
 * This class supports reading from strings, files, and generic {@link Reader} instances.
 * The parser handles various APON features such as nested objects, arrays, value type hints,
 * multi-line text blocks, and comments.
 * </p>
 */
public class AponReader {

    private final BufferedReader reader;

    private int lineNumber;

    private String originalLine;

    private String currentLine;

    /**
     * Creates a new AponReader for the given APON-formatted string.
     * @param apon the APON formatted string
     */
    public AponReader(String apon) {
        this(new StringReader(apon));
    }

    /**
     * Creates a new AponReader for the given {@link Reader}.
     * @param reader the character stream to read from
     */
    public AponReader(Reader reader) {
        Assert.notNull(reader, "reader must not be null");
        if (reader instanceof BufferedReader bufferedReader) {
            this.reader = bufferedReader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    /**
     * Reads an APON document and populates a new {@link VariableParameters} instance.
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public Parameters read() throws AponParseException {
        Parameters parameters = new VariableParameters();
        return read(parameters);
    }

    /**
     * Reads an APON document and populates the given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public <T extends Parameters> T read(T parameters) throws AponParseException {
        Assert.notNull(parameters, "parameters must not be null");
        try {
            // Mark the beginning of the stream to allow reset
            reader.mark(1024);
            lineNumber = 0; // Reset line number for this parsing operation

            String firstLine = readAndTrimLine();
            if (firstLine == null) { // Empty input
                return parameters;
            }

            char firstChar = (firstLine.length() == 1 ? firstLine.charAt(0) : NO_CONTROL_CHAR);

            if (firstChar == ARRAY_OPEN) {
                readArray(parameters);
                String trailingLine = readAndTrimLine();
                if (trailingLine != null) {
                    throw syntaxError("Unexpected content after closing bracket ']' of root array");
                }
            } else if (firstChar == BLOCK_OPEN) {
                readBlock(parameters);
                String trailingLine = readAndTrimLine();
                if (trailingLine != null) {
                    throw syntaxError("Unexpected content after closing brace '}' of root object");
                }
            } else {
                // Non-braced root (compact style)
                parameters.setCompactStyle(true);
                reader.reset(); // Rewind the reader to the beginning
                lineNumber = 0; // Reset line number after rewinding
                readLoop(parameters, NO_CONTROL_CHAR, null, null, null, false);
            }
        } catch (AponParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AponParseException("Failed to read APON document into " + parameters.getClass().getName(), e);
        }
        return parameters;
    }

    private void readArray(@NonNull Parameters container) throws IOException {
        ParameterValue parameterValue = container.getParameterValue(ArrayParameters.NONAME);
        readLoop(container, ARRAY_OPEN, ArrayParameters.NONAME, parameterValue, null, false);
    }

    private void readBlock(@NonNull Parameters container) throws IOException {
        container.setCompactStyle(false);
        readLoop(container, BLOCK_OPEN, null, null, null, false);
    }

    /**
     * The core recursive method for parsing APON content into a {@link Parameters} object.
     * @param container the current parameters object to populate
     * @param openedBracket the type of bracket that opened the current scope (e.g., '{', '[')
     * @param name the current parameter name being processed
     * @param parameterValue the current parameter value object being processed
     * @param valueType the determined value type for the current parameter
     * @param valueTypeHinted whether the value type was determined by a hint in the name
     * @throws IOException if an I/O error occurs
     * @throws AponParseException if a syntax error is found
     */
    private void readLoop(
            Parameters container, char openedBracket, String name, ParameterValue parameterValue,
            ValueType valueType, boolean valueTypeHinted) throws IOException {
        String value;
        int vlen;
        char cchar;

        while (readAndTrimLine() != null) {
            if (openedBracket == ARRAY_OPEN) {
                value = currentLine;
                vlen = value.length();
                cchar = (vlen == 1 ? value.charAt(0) : NO_CONTROL_CHAR);
                if (ARRAY_CLOSE == cchar) {
                    return;
                }
                if (BLOCK_OPEN == cchar) {
                    if (parameterValue == null) {
                        parameterValue = container.attachParameterValue(name, ValueType.PARAMETERS, true);
                        parameterValue.setValueTypeHinted(valueTypeHinted);
                    }
                    Parameters ps = parameterValue.attachParameters(parameterValue);
                    readLoop(ps, BLOCK_OPEN, null, null, null, false);
                    continue;
                } else if (ARRAY_OPEN == cchar) {
                    // Create a temporary container to hold the nested array's content
                    ArrayParameters tempContainer = new ArrayParameters();
                    // The nested array will be parsed as a parameter named "" (ArrayParameters.NONAME)
                    readLoop(tempContainer, ARRAY_OPEN, ArrayParameters.NONAME, null, null, false);
                    // Extract the parsed list from the temporary container
                    List<?> nestedList = tempContainer.getValueList();
                    // Now, add this extracted list to the *real* parameterValue
                    if (parameterValue == null) {
                        parameterValue = container.attachParameterValue(name, ValueType.VARIABLE, true);
                        parameterValue.setValueTypeHinted(valueTypeHinted);
                    }
                    parameterValue.putValue(nestedList);
                    continue;
                } else if (EMPTY_ARRAY.equals(currentLine) || EMPTY_BLOCK.equals(currentLine)) {
                    if (parameterValue == null) {
                        parameterValue = container.attachParameterValue(name, ValueType.PARAMETERS, true);
                        parameterValue.setValueTypeHinted(valueTypeHinted);
                    }
                    parameterValue.attachParameters(parameterValue);
                    continue;
                }
            } else {
                if (currentLine.length() == 1) {
                    cchar = currentLine.charAt(0);
                    if (openedBracket == BLOCK_OPEN && BLOCK_CLOSE == cchar) {
                        return;
                    }
                    if (BLOCK_OPEN == cchar) {
                        if (!container.hasParameter(ArrayParameters.NONAME)) {
                            container.attachParameterValue(ArrayParameters.NONAME, ValueType.PARAMETERS, true);
                        }
                        Parameters ps = container.attachParameters(ArrayParameters.NONAME);
                        readLoop(ps, BLOCK_OPEN, null, null, null, false);
                        continue;
                    }
                }

                int index = currentLine.indexOf(NAME_VALUE_SEPARATOR);
                if (index == -1) {
                    throw syntaxError("Invalid line format; a parameter must be in the 'name: value' format");
                }
                if (index == 0) {
                    throw syntaxError("Missing parameter name; a parameter must be in the 'name: value' format");
                }

                name = currentLine.substring(0, index).trim();
                value = currentLine.substring(index + 1).trim();
                vlen = value.length();
                cchar = (vlen == 1 ? value.charAt(0) : NO_CONTROL_CHAR);

                if (EMPTY_BLOCK.equals(value)) {
                    parameterValue = container.attachParameterValue(name, ValueType.PARAMETERS, false);
                    parameterValue.putValue(new VariableParameters());
                    continue;
                }

                parameterValue = container.getParameterValue(name);

                if (parameterValue != null) {
                    valueType = parameterValue.getValueType();
                } else {
                    valueType = ValueType.resolveByHint(name);
                    if (valueType != null) {
                        valueTypeHinted = true;
                        name = ValueType.stripHint(name);
                        parameterValue = container.getParameterValue(name);
                        if (parameterValue != null) {
                            valueType = parameterValue.getValueType();
                        }
                    } else {
                        valueTypeHinted = false;
                    }
                }

                if (valueType == ValueType.VARIABLE) {
                    valueType = null;
                }
                if (valueType != null) {
                    if (parameterValue != null && !parameterValue.isArray() && ARRAY_OPEN == cchar) {
                        throw syntaxError("The parameter '" + parameterValue.getQualifiedName() + "' is not an array type");
                    }
                    if (valueType != ValueType.PARAMETERS && BLOCK_OPEN == cchar) {
                        throw syntaxError(parameterValue, valueType);
                    }
                    if (valueType != ValueType.TEXT && TEXT_OPEN == cchar) {
                        throw syntaxError(parameterValue, valueType);
                    }
                }
            }

            if (parameterValue != null && !parameterValue.isArray()) {
                if (valueType == ValueType.PARAMETERS && BLOCK_OPEN != cchar) {
                    throw syntaxError(parameterValue, valueType);
                }
                if (valueType == ValueType.TEXT && !NULL.equals(value) && TEXT_OPEN != cchar) {
                    throw syntaxError(parameterValue, valueType);
                }
            }

            if (parameterValue == null || parameterValue.isArray() || valueType == null) {
                if (ARRAY_OPEN == cchar) {
                    readLoop(container, ARRAY_OPEN, name, parameterValue, valueType, valueTypeHinted);
                    continue;
                }
            }
            if (valueType == null) {
                if (BLOCK_OPEN == cchar) {
                    valueType = ValueType.PARAMETERS;
                } else if (TEXT_OPEN == cchar) {
                    valueType = ValueType.TEXT;
                }
            }

            if (valueType == ValueType.PARAMETERS) {
                if (parameterValue == null) {
                    parameterValue = container.attachParameterValue(name, valueType, (openedBracket == ARRAY_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                }
                Parameters ps = container.attachParameters(name);
                readLoop(ps, BLOCK_OPEN, null, null, null, valueTypeHinted);
            } else if (valueType == ValueType.TEXT) {
                if (parameterValue == null) {
                    parameterValue = container.attachParameterValue(name, valueType, (openedBracket == ARRAY_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                }
                if (TEXT_OPEN == cchar) {
                    parameterValue.putValue(readTextBlock());
                } else if (NULL.equals(value)) {
                    parameterValue.putValue(null);
                } else {
                    parameterValue.putValue(value);
                }
            } else {
                if (vlen == 0) {
                    value = null;
                    if (valueType == null) {
                        valueType = ValueType.STRING;
                    }
                } else if (valueType == null) {
                    if (NULL.equals(value)) {
                        value = null;
                        valueType = ValueType.STRING;
                    } else if (TRUE.equals(value) || FALSE.equals(value)) {
                        valueType = ValueType.BOOLEAN;
                    } else if (value.charAt(0) == DOUBLE_QUOTE_CHAR) {
                        if (vlen == 1 || value.charAt(vlen - 1) != DOUBLE_QUOTE_CHAR) {
                            throw syntaxError("Unclosed quotation mark");
                        }
                        valueType = ValueType.STRING;
                    } else if (value.charAt(0) == SINGLE_QUOTE_CHAR) {
                        if (vlen == 1 || value.charAt(vlen - 1) != SINGLE_QUOTE_CHAR) {
                            throw syntaxError("Unclosed quotation mark");
                        }
                        valueType = ValueType.STRING;
                    } else {
                        if (value.indexOf('.') > -1 || value.indexOf('e') > -1 || value.indexOf('E') > -1) {
                            try {
                                Double.parseDouble(value);
                                valueType = ValueType.DOUBLE;
                            } catch (NumberFormatException e) {
                                valueType = ValueType.STRING;
                            }
                        } else {
                            try {
                                long longValue = Long.parseLong(value);
                                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                                    valueType = ValueType.INT;
                                } else {
                                    valueType = ValueType.LONG;
                                }
                            } catch (NumberFormatException e) {
                                valueType = ValueType.STRING;
                            }
                        }
                    }
                } else if (NULL.equals(value)) {
                    value = null;
                }

                if (parameterValue == null) {
                    parameterValue = container.attachParameterValue(name, valueType, (openedBracket == ARRAY_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                } else {
                    if (parameterValue.getValueType() == ValueType.VARIABLE) {
                        parameterValue.setValueType(valueType);
                    } else if (parameterValue.getValueType() != valueType) {
                        throw syntaxError(parameterValue, parameterValue.getValueType());
                    }
                }

                if (value == null) {
                    parameterValue.putValue(null);
                } else {
                    boolean wasQuoted = AponFormat.wasQuoted(value);

                    // Validation: Non-string, non-structural types cannot be quoted.
                    if (wasQuoted && valueType != ValueType.STRING) {
                        throw syntaxError("Value for a parameter of type '" + valueType + "' cannot be quoted: '" + value + "'");
                    }

                    if (valueType == ValueType.STRING) {
                        if (wasQuoted) {
                            value = unescape(value.substring(1, value.length() - 1));
                        }
                        parameterValue.putValue(value);
                    } else if (valueType == ValueType.BOOLEAN) {
                        parameterValue.putValue(Boolean.valueOf(value));
                    } else if (valueType == ValueType.INT) {
                        parameterValue.putValue(Integer.valueOf(value));
                    } else if (valueType == ValueType.LONG) {
                        parameterValue.putValue(Long.valueOf(value));
                    } else if (valueType == ValueType.FLOAT) {
                        parameterValue.putValue(Float.valueOf(value));
                    } else if (valueType == ValueType.DOUBLE) {
                        parameterValue.putValue(Double.valueOf(value));
                    }
                }
            }

            if (parameterValue.isArray() && parameterValue.isBracketed()) {
                if (openedBracket != ARRAY_OPEN) {
                    parameterValue.setBracketed(false);
                }
            }
        }

        if (openedBracket == BLOCK_OPEN) {
            throw new MissingClosingBracketException("curly", name, parameterValue);
        } else if (openedBracket == ARRAY_OPEN) {
            throw new MissingClosingBracketException("square", name, parameterValue);
        }
    }

    private String readTextBlock() throws IOException {
        String str;
        char tchar;
        StringBuilder sb = null;

        while (readAndTrimLine() != null) {
            tchar = (!currentLine.isEmpty() ? currentLine.charAt(0) : NO_CONTROL_CHAR);
            if (currentLine.length() == 1 && TEXT_CLOSE == tchar) {
                return (sb != null ? sb.toString() : StringUtils.EMPTY);
            }
            if (TEXT_LINE_START == tchar) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(SYSTEM_NEW_LINE);
                }
                str = originalLine.substring(originalLine.indexOf(TEXT_LINE_START) + 1);
                if (!str.isEmpty()) {
                    sb.append(str);
                }
            } else if (!currentLine.isEmpty()) {
                throw syntaxError("Text block lines must start with a '|' character");
            }
        }
        throw syntaxError("Missing closing round bracket ')' for the text block");
    }

    @Nullable
    private String readAndTrimLine() throws IOException {
        while ((originalLine = reader.readLine()) != null) {
            lineNumber++;
            currentLine = originalLine.trim();
            if (!currentLine.isEmpty() && currentLine.charAt(0) != COMMENT_LINE_START) {
                return currentLine;
            }
        }
        return null;
    }

    /**
     * Closes the reader.
     */
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private String unescape(String str) throws AponParseException {
        try {
            return AponFormat.unescape(str);
        } catch (IllegalArgumentException e) {
            throw syntaxError(e.getMessage());
        }
    }

    @NonNull
    private AponParseException syntaxError(String message) {
        return new MalformedAponException(lineNumber, originalLine, currentLine, message);
    }

    @NonNull
    private AponParseException syntaxError(ParameterValue parameterValue, ValueType expectedValueType) {
        return new MalformedAponException(lineNumber, originalLine, currentLine, parameterValue, expectedValueType);
    }

    /**
     * A static utility method that parses an APON-formatted string into a new {@link VariableParameters} object.
     * @param apon the APON-formatted string
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(String apon) throws AponParseException {
        Parameters parameters = new VariableParameters();
        return read(apon, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted string into a new container of the given type.
     * @param <T> the type of the new container
     * @param apon the APON-formatted string
     * @param requiredType the concrete {@link Parameters} implementation to instantiate
     * @return a new, populated container instance
     * @throws AponParseException if parsing fails or the type cannot be instantiated
     */
    public static <T extends Parameters> T read(String apon, Class<T> requiredType) throws AponParseException {
        T parameters = ClassUtils.createInstance(requiredType);
        return read(apon, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted string into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param apon the APON-formatted string
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(String apon, T parameters) throws AponParseException {
        Assert.notNull(apon, "apon must not be null");
        Assert.notNull(parameters, "parameters must not be null");
        if (StringUtils.isEmpty(apon)) {
            return parameters;
        }
        try {
            AponReader aponReader = new AponReader(apon);
            aponReader.read(parameters);
            aponReader.close();
            return parameters;
        } catch (AponParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AponParseException("Failed to parse the APON-formatted string", e);
        }
    }

    /**
     * A static utility method that parses an APON-formatted file into a new {@link VariableParameters} object.
     * @param file the file to parse
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(File file) throws AponParseException {
        return read(file, (String)null);
    }

    /**
     * A static utility method that parses an APON-formatted file into a new {@link VariableParameters} object.
     * @param file the file to parse
     * @param encoding the character encoding of the file
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(File file, String encoding) throws AponParseException {
        Assert.notNull(file, "file must not be null");
        Parameters parameters = new VariableParameters();
        return read(file, encoding, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted file into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param file the file to parse
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(File file, T parameters) throws AponParseException {
        return read(file, null, parameters);
    }

    /**
     * A static utility method that parses an APON-formatted file into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param file the file to parse
     * @param encoding the character encoding of the file
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(File file, String encoding, T parameters)
            throws AponParseException {
        Assert.notNull(file, "file must not be null");
        Assert.notNull(parameters, "parameters must not be null");
        AponReader aponReader = null;
        try {
            if (encoding == null) {
                aponReader = new AponReader(new FileReader(file));
            } else {
                aponReader = new AponReader(new InputStreamReader(new FileInputStream(file), encoding));
            }
            return aponReader.read(parameters);
        } catch (AponParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AponParseException("Failed to read APON from file: " + file, e);
        } finally {
            if (aponReader != null) {
                aponReader.close();
            }
        }
    }

    /**
     * A static utility method that parses an APON-formatted stream into a new {@link VariableParameters} object.
     * @param reader the character stream to read from
     * @return a new {@code Parameters} object containing the parsed data
     * @throws AponParseException if an error occurs during parsing
     */
    public static Parameters read(Reader reader) throws AponParseException {
        Assert.notNull(reader, "reader must not be null");
        try (AponReaderCloseable aponReader = new AponReaderCloseable(reader)) {
            return aponReader.read();
        }
    }

    /**
     * A static utility method that parses an APON-formatted stream into a given {@link Parameters} object.
     * @param <T> the type of the parameters object
     * @param reader the character stream to read from
     * @param parameters the {@code Parameters} object to populate
     * @return the populated {@code Parameters} object
     * @throws AponParseException if an error occurs during parsing
     */
    public static <T extends Parameters> T read(Reader reader, T parameters) throws AponParseException {
        try (AponReaderCloseable aponReader = new AponReaderCloseable(reader)) {
            return aponReader.read(parameters);
        }
    }

}
