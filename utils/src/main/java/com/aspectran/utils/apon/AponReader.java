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

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * Converts a string in APON format to a Parameters object.
 */
public class AponReader extends AponFormat implements Closeable {

    private final BufferedReader reader;

    private int lineNumber;

    /**
     * Instantiates a new AponReader.
     * @param text the APON formatted string
     */
    public AponReader(String text) {
        this(new StringReader(text));
    }

    /**
     * Instantiates a new AponReader.
     * @param reader the character stream capable of parsing content into APON
     */
    public AponReader(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader)reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    /**
     * Reads an APON document into a {@code VariableParameters} object.
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public Parameters read() throws AponParseException {
        Parameters parameters = new VariableParameters();
        return read(parameters);
    }

    /**
     * Reads an APON formatted document into the specified {@link Parameters} object.
     * @param <T> the generic type
     * @param parameters the Parameters object
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public <T extends Parameters> T read(T parameters) throws AponParseException {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        try {
            if (parameters instanceof ArrayParameters) {
                readArray(parameters);
            } else {
                read(parameters, NO_CONTROL_CHAR, null, null, null, false);
            }
        } catch (AponParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AponParseException("Failed to read APON document with specified parameters object " +
                    parameters.getClass().getName(), e);
        }
        return parameters;
    }

    private void readArray(Parameters container) throws IOException {
        String line;
        String value;
        String tline;
        int tlen;
        int vlen;
        char cchar;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            tline = line.trim();
            tlen = tline.length();

            if (tlen == 0 || (tline.charAt(0) == COMMENT_LINE_START)) {
                continue;
            }

            value = tline;
            vlen = value.length();
            cchar = (vlen == 1 ? value.charAt(0) : NO_CONTROL_CHAR);
            if (cchar != CURLY_BRACKET_OPEN) {
                throw syntaxError(line, tline, "Expected to open curly brackets, " +
                        "but encounter string: " + value);
            }
            Parameters ps = container.newParameters(ArrayParameters.NONAME);
            read(ps, CURLY_BRACKET_OPEN, null, null, null, false);
        }
    }

    /**
     * Creates a {@link Parameters} object by parsing the content of the specified character stream as APON.
     * @param container the Parameters object
     * @param openedBracket the opened bracket character
     * @param name the parameter name
     * @param parameterValue the parameter value
     * @param valueType the value type of the parameter
     * @param valueTypeHinted whether the value type is hinted
     * @throws IOException if an invalid parameter is detected or I/O error occurs
     */
    private void read(Parameters container, char openedBracket, String name, ParameterValue parameterValue,
                      ValueType valueType, boolean valueTypeHinted) throws IOException {
        String line;
        String value;
        String tline;
        int tlen;
        int vlen;
        char cchar;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            tline = line.trim();
            tlen = tline.length();

            if (tlen == 0 || tline.charAt(0) == COMMENT_LINE_START) {
                continue;
            }

            if (openedBracket == SQUARE_BRACKET_OPEN) {
                value = tline;
                vlen = value.length();
                cchar = (vlen == 1 ? value.charAt(0) : NO_CONTROL_CHAR);
                if (SQUARE_BRACKET_CLOSE == cchar) {
                    return;
                }
            } else {
                if (tlen == 1) {
                    cchar = tline.charAt(0);
                    if (openedBracket == CURLY_BRACKET_OPEN && CURLY_BRACKET_CLOSE == cchar) {
                        return;
                    }
                    if (CURLY_BRACKET_OPEN == cchar) {
                        if (!container.hasParameter(ArrayParameters.NONAME)) {
                            container.newParameterValue(ArrayParameters.NONAME, ValueType.PARAMETERS, true);
                        }
                        Parameters ps = container.newParameters(ArrayParameters.NONAME);
                        read(ps, CURLY_BRACKET_OPEN, null, null, null, false);
                        continue;
                    }
                }

                int index = tline.indexOf(NAME_VALUE_SEPARATOR);
                if (index == -1) {
                    throw syntaxError(line, tline, "Failed to break up string of name/value pairs");
                }
                if (index == 0) {
                    throw syntaxError(line, tline, "Unrecognized parameter name");
                }

                name = tline.substring(0, index).trim();
                value = tline.substring(index + 1).trim();
                vlen = value.length();
                cchar = (vlen == 1 ? value.charAt(0) : NO_CONTROL_CHAR);

                parameterValue = container.getParameterValueMap().get(name);

                if (parameterValue != null) {
                    valueType = parameterValue.getValueType();
                } else {
                    valueType = ValueType.resolveByHint(name);
                    if (valueType != null) {
                        valueTypeHinted = true;
                        name = ValueType.stripHint(name);
                        parameterValue = container.getParameterValueMap().get(name);
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
                    if (parameterValue != null && !parameterValue.isArray() && SQUARE_BRACKET_OPEN == cchar) {
                        throw syntaxError(line, tline,
                                "Parameter value is not an array type");
                    }
                    if (valueType != ValueType.PARAMETERS && CURLY_BRACKET_OPEN == cchar) {
                        throw syntaxError(line, tline, parameterValue, valueType);
                    }
                    if (valueType != ValueType.TEXT && ROUND_BRACKET_OPEN == cchar) {
                        throw syntaxError(line, tline, parameterValue, valueType);
                    }
                }
            }

            if (parameterValue != null && !parameterValue.isArray()) {
                if (valueType == ValueType.PARAMETERS && CURLY_BRACKET_OPEN != cchar) {
                    throw syntaxError(line, tline, parameterValue, valueType);
                }
                if (valueType == ValueType.TEXT && !NULL.equals(value) && ROUND_BRACKET_OPEN != cchar) {
                    throw syntaxError(line, tline, parameterValue, valueType);
                }
            }

            if (parameterValue == null || parameterValue.isArray() || valueType == null) {
                if (SQUARE_BRACKET_OPEN == cchar) {
                    read(container, SQUARE_BRACKET_OPEN, name, parameterValue, valueType, valueTypeHinted);
                    continue;
                }
            }
            if (valueType == null) {
                if (CURLY_BRACKET_OPEN == cchar) {
                    valueType = ValueType.PARAMETERS;
                } else if (ROUND_BRACKET_OPEN == cchar) {
                    valueType = ValueType.TEXT;
                }
            }

            if (valueType == ValueType.PARAMETERS) {
                if (parameterValue == null) {
                    parameterValue = container.newParameterValue(name, valueType, (openedBracket == SQUARE_BRACKET_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                }
                Parameters ps = container.newParameters(name);
                read(ps, CURLY_BRACKET_OPEN, null, null, null, valueTypeHinted);
            } else if (valueType == ValueType.TEXT) {
                if (parameterValue == null) {
                    parameterValue = container.newParameterValue(name, valueType, (openedBracket == SQUARE_BRACKET_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                }
                if (ROUND_BRACKET_OPEN == cchar) {
                    parameterValue.putValue(readText());
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
                            throw syntaxError(line, tline,
                                    "Unclosed quotation mark after the character string " + value);
                        }
                        valueType = ValueType.STRING;
                    } else if (value.charAt(0) == SINGLE_QUOTE_CHAR) {
                        if (vlen == 1 || value.charAt(vlen - 1) != SINGLE_QUOTE_CHAR) {
                            throw syntaxError(line, tline,
                                    "Unclosed quotation mark after the character string " + value);
                        }
                        valueType = ValueType.STRING;
                    } else {
                        try {
                            Integer.parseInt(value);
                            valueType = ValueType.INT;
                        } catch (NumberFormatException e1) {
                            try {
                                Long.parseLong(value);
                                valueType = ValueType.LONG;
                            } catch (NumberFormatException e2) {
                                try {
                                    Float.parseFloat(value);
                                    valueType = ValueType.FLOAT;
                                } catch (NumberFormatException e3) {
                                    try {
                                        Double.parseDouble(value);
                                        valueType = ValueType.DOUBLE;
                                    } catch (NumberFormatException e4) {
                                        valueType = ValueType.STRING;
                                    }
                                }
                            }
                        }
                    }
                } else if (NULL.equals(value)) {
                    value = null;
                }

                if (parameterValue == null) {
                    parameterValue = container.newParameterValue(name, valueType, (openedBracket == SQUARE_BRACKET_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                } else {
                    if (parameterValue.getValueType() == ValueType.VARIABLE) {
                        parameterValue.setValueType(valueType);
                    } else if (parameterValue.getValueType() != valueType) {
                        throw syntaxError(line, tline, parameterValue, parameterValue.getValueType());
                    }
                }

                if (value == null) {
                    parameterValue.putValue(null);
                } else {
                    if (valueType == ValueType.STRING) {
                        if (value.charAt(0) == DOUBLE_QUOTE_CHAR || value.charAt(0) == SINGLE_QUOTE_CHAR) {
                            value = unescape(value.substring(1, vlen - 1), line, tline);
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
                if (openedBracket != SQUARE_BRACKET_OPEN) {
                    parameterValue.setBracketed(false);
                }
            }
        }

        if (openedBracket == CURLY_BRACKET_OPEN) {
            throw new MissingClosingBracketException("curly", name, parameterValue);
        } else if (openedBracket == SQUARE_BRACKET_OPEN) {
            throw new MissingClosingBracketException("square", name, parameterValue);
        }
    }

    private String readText() throws IOException {
        String line;
        String tline = null;
        String str;
        int tlen;
        char tchar;
        StringBuilder sb = null;

        while ((line = reader.readLine()) != null) {
            lineNumber++;

            tline = line.trim();
            tlen = tline.length();
            tchar = (tlen > 0 ? tline.charAt(0) : NO_CONTROL_CHAR);

            if (tlen == 1 && ROUND_BRACKET_CLOSE == tchar) {
                return (sb != null ? sb.toString() : StringUtils.EMPTY);
            }

            if (TEXT_LINE_START == tchar) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(SYSTEM_NEW_LINE);
                }
                str = line.substring(line.indexOf(TEXT_LINE_START) + 1);
                if (!str.isEmpty()) {
                    sb.append(str);
                }
            } else if (tlen > 0) {
                throw syntaxError(line, tline,
                        "The closing round bracket was missing or Each text line is must start with a '|'");
            }
        }

        throw syntaxError("", tline,
                "The end of the text line was reached with no closing round bracket found");
    }

    private String unescape(String str, String line, String ltrim) throws AponParseException {
        if (str == null) {
            return null;
        }

        int len = str.length();
        if (len == 0 || str.indexOf(ESCAPE_CHAR) == -1) {
            return str;
        }

        StringBuilder sb = new StringBuilder(len);
        char c;
        for (int pos = 0; pos < len;) {
            c = str.charAt(pos++);
            if (c == ESCAPE_CHAR) {
                if (pos >= len) {
                    throw syntaxError(line, ltrim, "Unterminated escape sequence");
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
                            throw syntaxError(line, ltrim, "Unterminated escape sequence");
                        }
                        // Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
                        char result = 0;
                        int i = pos, end = i + 4;
                        for (; i < end; i++) {
                            c = str.charAt(i);
                            result <<= 4;
                            if (c >= '0' && c <= '9') {
                                result += (char)(c - '0');
                            } else if (c >= 'a' && c <= 'f') {
                                result += (char)(c - 'a' + 10);
                            } else if (c >= 'A' && c <= 'F') {
                                result += (char)(c - 'A' + 10);
                            } else {
                                throw syntaxError(line, ltrim, "Invalid number format: \\u" +
                                        str.substring(pos, pos + 4));
                            }
                        }
                        pos = end;
                        sb.append(result);
                        break;
                    default:
                        // throw error when none of the above cases are matched
                        throw syntaxError(line, ltrim, "Invalid escape sequence");
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    private AponParseException syntaxError(String line,  String tline, String message) throws AponParseException {
        throw new MalformedAponException(lineNumber, line, tline, message);
    }

    private AponParseException syntaxError(String line,  String tline, ParameterValue parameterValue,
                                    ValueType expectedValueType) throws AponParseException {
        throw new MalformedAponException(lineNumber, line, tline, parameterValue, expectedValueType);
    }

    /**
     * Converts an APON formatted string into a Parameters object.
     * @param text the APON formatted string
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(String text) throws AponParseException {
        Parameters parameters = new VariableParameters();
        return parse(text, parameters);
    }

    public static <T extends Parameters> T parse(String text, Class<T> requiredType) throws AponParseException {
        T parameters = ClassUtils.createInstance(requiredType);
        return parse(text, parameters);
    }

    /**
     * Converts an APON formatted string into a given Parameters object.
     * @param <T> the generic type
     * @param text the APON formatted string
     * @param parameters the Parameters object
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static <T extends Parameters> T parse(String text, T parameters) throws AponParseException {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        if (StringUtils.isEmpty(text)) {
            return parameters;
        }
        try {
            AponReader aponReader = new AponReader(text);
            aponReader.read(parameters);
            aponReader.close();
            return parameters;
        } catch (AponParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AponParseException("Failed to parse string with APON format", e);
        }
    }

    /**
     * Converts to a Parameters object from a file.
     * @param file the file to parse
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(File file) throws AponParseException {
        return parse(file, (String)null);
    }

    /**
     * Converts to a Parameters object from a file.
     * @param file the file to parse
     * @param encoding the character encoding
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(File file, String encoding) throws AponParseException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        Parameters parameters = new VariableParameters();
        return parse(file, encoding, parameters);
    }

    /**
     * Converts into a given Parameters object from a file.
     * @param <T> the generic type
     * @param file the file to parse
     * @param parameters the Parameters object
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static <T extends Parameters> T parse(File file, T parameters) throws AponParseException {
        return parse(file, null, parameters);
    }

    /**
     * Converts into a given Parameters object from a file.
     * @param <T> the generic type
     * @param file the file to parse
     * @param encoding the character encoding
     * @param parameters the Parameters object
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static <T extends Parameters> T parse(File file, String encoding, T parameters)
            throws AponParseException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
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
            throw new AponParseException("Failed to parse string with APON format", e);
        } finally {
            if (aponReader != null) {
                try {
                    aponReader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Converts to a Parameters object from a character-input stream.
     * @param reader the character-input stream
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(Reader reader) throws AponParseException {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        AponReader aponReader = new AponReader(reader);
        return aponReader.read();
    }

    /**
     * Converts into a given Parameters object from a character-input stream.
     * @param <T> the generic type
     * @param reader the character-input stream
     * @param parameters the Parameters object
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static <T extends Parameters> T parse(Reader reader, T parameters) throws AponParseException {
        AponReader aponReader = new AponReader(reader);
        aponReader.read(parameters);
        return parameters;
    }

}
