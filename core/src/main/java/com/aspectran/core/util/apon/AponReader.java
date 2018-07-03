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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Objects;

/**
 * Converts a string in APON format to a Parameters object.
 */
public class AponReader extends AponFormat {

    private BufferedReader reader;

    private boolean addable;

    private int lineNumber;

    /**
     * Instantiates a new AponReader.
     *
     * @param text the APON formatted string
     */
    public AponReader(String text) {
        this(new StringReader(text));
    }

    /**
     * Instantiates a new AponReader.
     *
     * @param reader the character stream whose contents can be parsed as APON
     */
    public AponReader(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Argument 'reader' must not be null");
        }
        if (reader instanceof BufferedReader) {
            this.reader = (BufferedReader)reader;
        } else {
            this.reader = new BufferedReader(reader);
        }
    }

    /**
     * Reads an APON document into a {@link VariableParameters} object.
     *
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public Parameters read() throws AponParseException {
        Parameters parameters = new VariableParameters();
        return read(parameters);
    }

    /**
     * Reads an APON formatted document into the specified {@link Parameters} object.
     *
     * @param <T> the generic type
     * @param parameters the Parameters object
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public <T extends Parameters> T read(T parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Argument 'parameters' must not be null");
        }
        addable = parameters.isAddable();
        try {
            valuelize(parameters, NO_CONTROL_CHAR, null, null, null, false);
        } catch (Exception e) {
            throw new AponParseException("Could not read an APON formatted document into a Parameters object", e);
        }
        return parameters;
    }

    /**
     * Creates a {@link Parameters} object by parsing the content of the specified character stream as APON.
     *
     * @param parameters the Parameters object
     * @param openBracket the left bracket character
     * @param name the parameter name
     * @param parameterValue the parameter value
     * @param parameterValueType the value type of the parameter
     * @param valueTypeHinted whether a value type hinted
     * @throws IOException if an I/O error occurs
     * @throws AponParseException if an invalid parameter is detected
     */
    private void valuelize(Parameters parameters, char openBracket, String name, ParameterValue parameterValue,
            ParameterValueType parameterValueType, boolean valueTypeHinted)
            throws IOException, AponParseException {
        Map<String, ParameterValue> parameterValueMap = parameters.getParameterValueMap();

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

            if (tlen == 0 || (tline.charAt(0) == COMMENT_LINE_START && openBracket != SQUARE_BRACKET_OPEN)) {
                continue;
            }

            if (openBracket == SQUARE_BRACKET_OPEN) {
                value = tline;
                vlen = value.length();
                cchar = (vlen == 1 ? value.charAt(0) : NO_CONTROL_CHAR);
                if (SQUARE_BRACKET_CLOSE == cchar) {
                    return;
                }
            } else {
                if (tlen == 1) {
                    if (openBracket == CURLY_BRACKET_OPEN && CURLY_BRACKET_CLOSE == tline.charAt(0)) {
                        return;
                    }
                }

                int index = tline.indexOf(NAME_VALUE_SEPARATOR);
                if (index == -1) {
                    throw new AponSyntaxException(lineNumber, line, tline,
                            "Failed to break up string of name/value pairs");
                }
                if (index == 0) {
                    throw new AponSyntaxException(lineNumber, line, tline,
                            "Unrecognized parameter name");
                }

                name = tline.substring(0, index).trim();
                value = tline.substring(index + 1).trim();
                vlen = value.length();
                cchar = (vlen == 1 ? value.charAt(0) : NO_CONTROL_CHAR);

                parameterValue = parameterValueMap.get(name);

                if (parameterValue != null) {
                    parameterValueType = parameterValue.getParameterValueType();
                } else {
                    if (!addable) {
                        throw new InvalidParameterException(lineNumber, line, tline, "Parameter '" +
                                name + "' is not predefined; Note that only predefined parameters are allowed");
                    }
                    parameterValueType = ParameterValueType.resolveByHint(name);
                    if (parameterValueType != null) {
                        valueTypeHinted = true;
                        name = ParameterValueType.stripValueTypeHint(name);
                        parameterValue = parameterValueMap.get(name);
                        if (parameterValue != null) {
                            parameterValueType = parameterValue.getParameterValueType();
                        }
                    } else {
                        valueTypeHinted = false;
                    }
                }

                if (parameterValueType == ParameterValueType.VARIABLE) {
                    parameterValueType = null;
                }
                if (parameterValueType != null) {
                    if (parameterValue != null && !parameterValue.isArray() && SQUARE_BRACKET_OPEN == cchar) {
                        throw new IncompatibleParameterValueTypeException(lineNumber, line, tline,
                                "Parameter value is not an array type");
                    }
                    if (parameterValueType != ParameterValueType.PARAMETERS && CURLY_BRACKET_OPEN == cchar) {
                        throw new IncompatibleParameterValueTypeException(lineNumber, line, tline, parameterValue, parameterValueType);
                    }
                    if (parameterValueType != ParameterValueType.TEXT && ROUND_BRACKET_OPEN == cchar) {
                        throw new IncompatibleParameterValueTypeException(lineNumber, line, tline, parameterValue, parameterValueType);
                    }
                }
            }

            if (parameterValue != null && !parameterValue.isArray()) {
                if (parameterValueType == ParameterValueType.PARAMETERS && CURLY_BRACKET_OPEN != cchar) {
                    throw new IncompatibleParameterValueTypeException(lineNumber, line, tline, parameterValue, parameterValueType);
                }
                if (parameterValueType == ParameterValueType.TEXT && ROUND_BRACKET_OPEN != cchar) {
                    throw new IncompatibleParameterValueTypeException(lineNumber, line, tline, parameterValue, parameterValueType);
                }
            }

            if (parameterValue == null || parameterValue.isArray() || parameterValueType == null) {
                if (SQUARE_BRACKET_OPEN == cchar) {
                    valuelize(parameters, SQUARE_BRACKET_OPEN, name, parameterValue, parameterValueType, valueTypeHinted);
                    continue;
                }
            }
            if (parameterValueType == null) {
                if (CURLY_BRACKET_OPEN == cchar) {
                    parameterValueType = ParameterValueType.PARAMETERS;
                } else if (ROUND_BRACKET_OPEN == cchar) {
                    parameterValueType = ParameterValueType.TEXT;
                }
            }

            if (parameterValueType == ParameterValueType.PARAMETERS) {
                if (parameterValue == null) {
                    parameterValue = parameters.newParameterValue(name, parameterValueType, (openBracket == SQUARE_BRACKET_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                }
                Parameters parameters2 = parameters.newParameters(parameterValue.getName());
                addable = parameters2.isAddable();
                valuelize(parameters2, CURLY_BRACKET_OPEN, null, null, null, valueTypeHinted);
            } else if (parameterValueType == ParameterValueType.TEXT) {
                if (parameterValue == null) {
                    parameterValue = parameters.newParameterValue(name, parameterValueType, (openBracket == SQUARE_BRACKET_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                }
                if (ROUND_BRACKET_OPEN == cchar) {
                    parameterValue.putValue(valuelizeText());
                } else if (NULL.equals(value)) {
                    parameterValue.putValue(null);
                } else {
                    parameterValue.putValue(value);
                }
            } else {
                if (vlen == 0) {
                    value = null;
                    if (parameterValueType == null) {
                        parameterValueType = ParameterValueType.STRING;
                    }
                } else if (parameterValueType == null) {
                    if (NULL.equals(value)) {
                        value = null;
                        parameterValueType = ParameterValueType.STRING;
                    } else if (TRUE.equals(value) || FALSE.equals(value)) {
                        parameterValueType = ParameterValueType.BOOLEAN;
                    } else if (value.charAt(0) == DOUBLE_QUOTE_CHAR) {
                        if (vlen == 1 || value.charAt(vlen - 1) != DOUBLE_QUOTE_CHAR) {
                            throw new AponSyntaxException(lineNumber, line, tline,
                                    "Unclosed quotation mark after the character string " + value);
                        }
                        parameterValueType = ParameterValueType.STRING;
                    } else if (value.charAt(0) == SINGLE_QUOTE_CHAR) {
                        if (vlen == 1 || value.charAt(vlen - 1) != SINGLE_QUOTE_CHAR) {
                            throw new AponSyntaxException(lineNumber, line, tline,
                                    "Unclosed quotation mark after the character string " + value);
                        }
                        parameterValueType = ParameterValueType.STRING;
                    } else {
                        try {
                            Integer.parseInt(value);
                            parameterValueType = ParameterValueType.INT;
                        } catch (NumberFormatException e1) {
                            try {
                                Long.parseLong(value);
                                parameterValueType = ParameterValueType.LONG;
                            } catch (NumberFormatException e2) {
                                try {
                                    Float.parseFloat(value);
                                    parameterValueType = ParameterValueType.FLOAT;
                                } catch (NumberFormatException e3) {
                                    try {
                                        Double.parseDouble(value);
                                        parameterValueType = ParameterValueType.DOUBLE;
                                    } catch (NumberFormatException e4) {
                                        parameterValueType = ParameterValueType.STRING;
                                    }
                                }
                            }
                        }
                    }
                } else if (NULL.equals(value)) {
                    value = null;
                }

                if (parameterValue == null) {
                    parameterValue = parameters.newParameterValue(name, parameterValueType, (openBracket == SQUARE_BRACKET_OPEN));
                    parameterValue.setValueTypeHinted(valueTypeHinted);
                } else {
                    if (parameterValue.getParameterValueType() == ParameterValueType.VARIABLE) {
                        parameterValue.setParameterValueType(parameterValueType);
                    } else if (parameterValue.getParameterValueType() != parameterValueType) {
                        throw new IncompatibleParameterValueTypeException(
                                lineNumber, line, tline, parameterValue, parameterValue.getParameterValueType());
                    }
                }

                if (value == null) {
                    parameterValue.putValue(null);
                } else {
                    if (parameterValueType == ParameterValueType.STRING) {
                        if (value.charAt(0) == DOUBLE_QUOTE_CHAR || value.charAt(0) == SINGLE_QUOTE_CHAR) {
                            value = unescape(value.substring(1, vlen - 1), lineNumber, line, tline);
                        }
                        parameterValue.putValue(value);
                    } else if (parameterValueType == ParameterValueType.BOOLEAN) {
                        parameterValue.putValue(Boolean.valueOf(value));
                    } else if (parameterValueType == ParameterValueType.INT) {
                        parameterValue.putValue(Integer.valueOf(value));
                    } else if (parameterValueType == ParameterValueType.LONG) {
                        parameterValue.putValue(Long.valueOf(value));
                    } else if (parameterValueType == ParameterValueType.FLOAT) {
                        parameterValue.putValue(Float.valueOf(value));
                    } else if (parameterValueType == ParameterValueType.DOUBLE) {
                        parameterValue.putValue(Double.valueOf(value));
                    }
                }
            }

            if (parameterValue.isArray() && parameterValue.isBracketed()) {
                if (openBracket != SQUARE_BRACKET_OPEN) {
                    parameterValue.setBracketed(false);
                }
            }
        }

        if (openBracket == CURLY_BRACKET_OPEN) {
            throw new MissingClosingBracketException("curly", name, parameterValue);
        } else if (openBracket == SQUARE_BRACKET_OPEN) {
            throw new MissingClosingBracketException("square", name, parameterValue);
        }
    }

    private String valuelizeText() throws IOException, AponSyntaxException {
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
                    sb.append(NEW_LINE_CHAR);
                }
                str = line.substring(line.indexOf(TEXT_LINE_START) + 1);
                if (str.length() > 0) {
                    sb.append(str);
                }
            } else if (tlen > 0) {
                throw new AponSyntaxException(lineNumber, line, tline,
                        "The closing round bracket was missing or Each text line is must start with a '|'");
            }
        }

        throw new AponSyntaxException(lineNumber, "", tline,
                "The end of the text line was reached with no closing round bracket found");
    }

    private String unescape(String value, int lineNumber, String line, String ltrim) throws AponSyntaxException {
        String s = unescape(value);
        if (Objects.equals(value, s)) {
            return value;
        }
        if (s == null) {
            throw new AponSyntaxException(lineNumber, line, ltrim,
                    "Invalid escape sequence (valid ones are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\\ )");
        }
        return s;
    }

    /**
     * Closes the reader.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        reader = null;
    }

    /**
     * Converts an APON formatted string into a Parameters object.
     *
     * @param text the APON formatted string
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(String text) throws AponParseException {
        Parameters parameters = new VariableParameters();
        return parse(text, parameters);
    }

    /**
     * Converts an APON formatted string into a given Parameters object.
     *
     * @param <T> the generic type
     * @param text the APON formatted string
     * @param parameters the Parameters object
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static <T extends Parameters> T parse(String text, T parameters) throws AponParseException {
        if (text == null) {
            throw new IllegalArgumentException("Argument 'text' must not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Argument 'parameters' must not be null");
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
     *
     * @param file the file to parse
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(File file) throws AponParseException {
        return parse(file, (String)null);
    }

    /**
     * Converts to a Parameters object from a file.
     *
     * @param file the file to parse
     * @param encoding the character encoding
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(File file, String encoding) throws AponParseException {
        if (file == null) {
            throw new IllegalArgumentException("Argument 'file' must not be null");
        }
        Parameters parameters = new VariableParameters();
        return parse(file, encoding, parameters);
    }

    /**
     * Converts into a given Parameters object from a file.
     *
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
     *
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
            throw new IllegalArgumentException("Argument 'file' must not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Argument 'parameters' must not be null");
        }
        AponReader aponReader = null;
        try {
            if (encoding == null) {
                aponReader = new AponReader(new FileReader(file));
            } else {
                aponReader = new AponReader(new InputStreamReader(new FileInputStream(file), encoding));
            }
            return aponReader.read(parameters);
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
     *
     * @param reader the character-input stream
     * @return the Parameters object
     * @throws AponParseException if reading APON format document fails
     */
    public static Parameters parse(Reader reader) throws AponParseException {
        if (reader == null) {
            throw new IllegalArgumentException("Argument 'reader' must not be null");
        }
        AponReader aponReader = new AponReader(reader);
        return aponReader.read();
    }

    /**
     * Converts into a given Parameters object from a character-input stream.
     *
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
