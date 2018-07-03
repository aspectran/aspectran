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

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Converts a Parameters object to an APON formatted string.
 *
 * <p>If pretty-printing is enabled, includes spaces, tabs to make the format more readable.
 * By default, pretty-printing is enabled, and the indent string is a tab character.</p>
 */
public class AponWriter extends AponFormat implements Flushable, Closeable {

    private Writer writer;

    private boolean prettyPrint;

    private String indentString;

    private int indentDepth;

    private boolean noQuotes;

    private boolean nullWrite;

    private boolean typeHintWrite;

    /**
     * Instantiates a new AponWriter.
     * By default, pretty printing is enabled, and the indent string is a tab character.
     *
     * @param writer the character-output stream
     */
    public AponWriter(Writer writer) {
        this(writer, true);
    }

    /**
     * Instantiates a new AponWriter.
     * If pretty-printing is enabled, includes spaces, tabs to make the format more readable.
     * By default, the indent string is a tab character.
     *
     * @param writer the character-output stream
     * @param prettyPrint enables or disables pretty-printing
     */
    public AponWriter(Writer writer, boolean prettyPrint) {
        this.writer = writer;
        this.prettyPrint = prettyPrint;
        this.indentString = (prettyPrint ? INDENT_STRING : null);
    }

    /**
     * Instantiates a new AponWriter.
     * If pretty-printing is enabled, includes spaces, tabs to make the format more readable.
     *
     * @param writer the character-output stream
     * @param indentString the string that should be used for indentation when pretty-printing is enabled
     */
    public AponWriter(Writer writer, String indentString) {
        this.writer = writer;
        this.prettyPrint = (indentString != null);
        this.indentString = indentString;
    }

    /**
     * Instantiates a new AponWriter.
     *
     * @param file  a File object to write to
     * @throws IOException if an I/O error occurs
     */
    public AponWriter(File file) throws IOException {
        this(file, false);
    }

    /**
     * Instantiates a new AponWriter.
     *
     * @param file a File object to write to
     * @param append if {@code true}, then bytes will be written
     *               to the end of the file rather than the beginning
     * @throws IOException if an I/O error occurs
     */
    public AponWriter(File file, boolean append) throws IOException {
        this.writer = new FileWriter(file, append);
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void setIndentString(String indentString) {
        this.indentString = indentString;
    }

    /**
     * Sets whether wrap a string in quotes.
     *
     * @param noQuotes true, wrap a string in quotes
     */
    public void setNoQuotes(boolean noQuotes) {
        this.noQuotes = noQuotes;
    }

    /**
     * Sets whether to write a null parameter.
     *
     * @param nullWrite true, write a null parameter
     */
    public void setNullWrite(boolean nullWrite) {
        this.nullWrite = nullWrite;
    }

    /**
     * Sets whether write a type hint for values.
     *
     * @param typeHintWrite true, write a type hint for values
     */
    public void setTypeHintWrite(boolean typeHintWrite) {
        this.typeHintWrite = typeHintWrite;
    }

    /**
     * Write a Parameters object to the character-output stream.
     *
     * @param parameters the Parameters object to be converted
     * @throws IOException if an I/O error occurs
     */
    public void write(Parameters parameters) throws IOException {
        if (parameters != null) {
            Map<String, ParameterValue> parameterValueMap = parameters.getParameterValueMap();
            for (Parameter pv : parameterValueMap.values()) {
                if (pv.isAssigned()) {
                    write(pv);
                }
            }
        }
    }

    /**
     * Write a Parameter object to the character-output stream.
     *
     * @param parameter the Parameter object to be converted
     * @throws IOException if an I/O error occurs
     */
    public void write(Parameter parameter) throws IOException {
        if (parameter.getParameterValueType() == ParameterValueType.PARAMETERS) {
            if (parameter.isArray()) {
                List<Parameters> list = parameter.getValueAsParametersList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        openSquareBracket();
                        for (Parameters p : list) {
                            indent();
                            openCurlyBracket();
                            write(p);
                            closeCurlyBracket();
                        }
                        closeSquareBracket();
                    } else {
                        for (Parameters p : list) {
                            writeName(parameter);
                            openCurlyBracket();
                            write(p);
                            closeCurlyBracket();
                        }
                    }
                }
            } else {
                if (nullWrite || parameter.getValueAsParameters() != null) {
                    writeName(parameter);
                    openCurlyBracket();
                    write(parameter.getValueAsParameters());
                    closeCurlyBracket();
                }
            }
        } else if (parameter.getParameterValueType() == ParameterValueType.VARIABLE) {
            if (parameter.isArray()) {
                List<?> list = parameter.getValueList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        openSquareBracket();
                        for (Object value : list) {
                            indent();
                            if (value instanceof Parameters) {
                                write((Parameters)value);
                            } else if (value != null) {
                                writeString(value.toString());
                            } else {
                                writeNull();
                            }
                        }
                        closeSquareBracket();
                    } else {
                        for (Object value : list) {
                            writeName(parameter);
                            if (value instanceof Parameters) {
                                write((Parameters)value);
                            } else if (value != null) {
                                writeString(value.toString());
                            } else {
                                writeNull();
                            }
                        }
                    }
                }
            } else {
                Object value = parameter.getValue();
                if (nullWrite || value != null) {
                    writeName(parameter);
                    openCurlyBracket();
                    if (value instanceof Parameters) {
                        write((Parameters)value);
                    } else if (value != null) {
                        writeString(value.toString());
                    } else {
                        writeNull();
                    }
                    closeCurlyBracket();
                }
            }
        } else if (parameter.getParameterValueType() == ParameterValueType.STRING) {
            if (parameter.isArray()) {
                List<String> list = parameter.getValueAsStringList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        openSquareBracket();
                        for (String value : list) {
                            indent();
                            writeString(value);
                        }
                        closeSquareBracket();
                    } else {
                        for (String value : list) {
                            writeName(parameter);
                            writeString(value);
                        }
                    }
                }
            } else {
                String s = parameter.getValueAsString();
                if (nullWrite || s != null) {
                    writeName(parameter);
                    writeString(s);
                }
            }
        } else if (parameter.getParameterValueType() == ParameterValueType.TEXT) {
            if (parameter.isArray()) {
                List<String> list = parameter.getValueAsStringList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        openSquareBracket();
                        for (String value : list) {
                            indent();
                            openRoundBracket();
                            writeText(value);
                            closeRoundBracket();
                        }
                        closeSquareBracket();
                    } else {
                        for (String value : list) {
                            writeName(parameter);
                            openRoundBracket();
                            writeText(value);
                            closeRoundBracket();
                        }
                    }
                }
            } else {
                String s = parameter.getValueAsString();
                if (s != null) {
                    writeName(parameter);
                    openRoundBracket();
                    writeText(s);
                    closeRoundBracket();
                } else if (nullWrite) {
                    writeName(parameter);
                    writeNull();
                }
            }
        } else {
            if (parameter.isArray()) {
                List<?> list = parameter.getValueList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        openSquareBracket();
                        for (Object value : list) {
                            indent();
                            write(value);
                        }
                        closeSquareBracket();
                    } else {
                        for (Object value : list) {
                            writeName(parameter);
                            write(value);
                        }
                    }
                }
            } else {
                if (nullWrite || parameter.getValue() != null) {
                    writeName(parameter);
                    write(parameter.getValue());
                }
            }
        }
    }

    /**
     * Writes a comment to the character-output stream.
     *
     * @param describe the comment to write to a character-output stream
     * @throws IOException if an I/O error occurs
     */
    public void comment(String describe) throws IOException {
        if (describe.indexOf(NEW_LINE_CHAR) != -1) {
            String line;
            int start = 0;
            while ((line = readLine(describe, start)) != null) {
                writer.write(COMMENT_LINE_START);
                writer.write(SPACE_CHAR);
                writer.write(line);
                newLine();

                start += line.length();
                start = skipNewLineChar(describe, start);
                if (start == -1) {
                    break;
                }
            }
            if (start != -1) {
                writer.write(COMMENT_LINE_START);
                newLine();
            }
        } else {
            writer.write(COMMENT_LINE_START);
            writer.write(SPACE_CHAR);
            writer.write(describe);
            newLine();
        }
    }

    private void writeName(Parameter parameter) throws IOException {
        indent();
        writer.write(parameter.getName());
        if (typeHintWrite || parameter.isValueTypeHinted()) {
            writer.write(ROUND_BRACKET_OPEN);
            writer.write(parameter.getParameterValueType().toString());
            writer.write(ROUND_BRACKET_CLOSE);
        }
        writer.write(NAME_VALUE_SEPARATOR);
        writer.write(SPACE_CHAR);
    }

    private void writeString(String value) throws IOException {
        if (value != null) {
            if (noQuotes && !NULL.equals(value)) {
                writer.write(escape(value, true));
            } else {
                if (value.startsWith(SPACE) || value.endsWith(SPACE)) {
                    writer.write(DOUBLE_QUOTE_CHAR);
                    writer.write(escape(value, false));
                    writer.write(DOUBLE_QUOTE_CHAR);
                } else {
                    writer.write(escape(value, true));
                }
            }
            newLine();
        } else {
            writeNull();
        }
    }

    private void writeText(String value) throws IOException {
        String line;
        int start = 0;
        while ((line = readLine(value, start)) != null) {
            indent();
            writer.write(TEXT_LINE_START);
            writer.write(line);
            newLine();

            start += line.length();
            start = skipNewLineChar(value, start);
            if (start == -1) {
                break;
            }
        }
        if (start != -1) {
            indent();
            writer.write(TEXT_LINE_START);
            newLine();
        }
    }

    private void write(Object value) throws IOException {
        if (value != null) {
            writer.write(value.toString());
            newLine();
        } else {
            writeNull();
        }
    }

    private void writeNull() throws IOException {
        writer.write(NULL);
        newLine();
    }

    private void openCurlyBracket() throws IOException {
        writer.write(CURLY_BRACKET_OPEN);
        newLine();
        increaseIndent();
    }

    private void closeCurlyBracket() throws IOException {
        decreaseIndent();
        indent();
        writer.write(CURLY_BRACKET_CLOSE);
        newLine();
    }

    private void openSquareBracket() throws IOException {
        writer.write(SQUARE_BRACKET_OPEN);
        newLine();
        increaseIndent();
    }

    private void closeSquareBracket() throws IOException {
        decreaseIndent();
        indent();
        writer.write(SQUARE_BRACKET_CLOSE);
        newLine();
    }

    private void openRoundBracket() throws IOException {
        writer.write(ROUND_BRACKET_OPEN);
        newLine();
        increaseIndent();
    }

    private void closeRoundBracket() throws IOException {
        decreaseIndent();
        indent();
        writer.write(ROUND_BRACKET_CLOSE);
        newLine();
    }

    private void newLine() throws IOException {
        writer.write(NEW_LINE_CHAR);
    }

    private void indent() throws IOException {
        if (prettyPrint && indentString != null) {
            for (int i = 0; i < indentDepth; i++) {
                writer.write(indentString);
            }
        }
    }

    private void increaseIndent() {
        if (prettyPrint) {
            indentDepth++;
        }
    }

    private void decreaseIndent() {
        if (prettyPrint) {
            indentDepth--;
        }
    }

    private String readLine(String value, int start) {
        if (start >= value.length()) {
            return null;
        }
        int end = start;
        for (; end < value.length(); end++) {
            char c = value.charAt(end);
            if (c == '\n' || c == '\r') {
                break;
            }
        }
        return (end > start ? value.substring(start, end) : StringUtils.EMPTY);
    }

    private int skipNewLineChar(String value, int start) {
        int end = start;
        boolean cr = false;
        boolean lf = false;
        for (; end < value.length(); end++) {
            char c = value.charAt(end);
            if (c != '\n' && c != '\r') {
                break;
            }
            if ((lf && c == '\n') || (cr && c == '\r')) {
                break;
            }
            if (c == '\n') {
                lf = true;
            }
            if (c == '\r') {
                cr = true;
            }
        }
        return (end > start ? end : -1);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Closes the writer.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
        writer = null;
    }

    /**
     * Converts a Parameters object to an APON formatted string.
     * By default, pretty printing is enabled, and the indent string is a tab character.
     *
     * @param parameters the Parameters object to be converted
     * @return a string that contains the APON text
     */
    public static String stringify(Parameters parameters) {
        return stringify(parameters, INDENT_STRING);
    }

    /**
     * Converts a Parameters object to an APON formatted string.
     * If pretty-printing is enabled, includes spaces, tabs to make the format more readable.
     * The default indentation string is a tab character.
     *
     * @param parameters the Parameters object to be converted
     * @param prettyPrint enables or disables pretty-printing
     * @return a string that contains the APON text
     */
    public static String stringify(Parameters parameters, boolean prettyPrint) {
        if (prettyPrint) {
            return stringify(parameters, INDENT_STRING);
        } else {
            return stringify(parameters, null);
        }
    }

    /**
     * Converts a Parameters object to an APON formatted string.
     * If pretty-printing is enabled, includes spaces, tabs to make the format more readable.
     *
     * @param parameters the Parameters object to be converted
     * @param indentString the string that should be used for indentation when pretty-printing is enabled
     * @return a string that contains the APON text
     */
    public static String stringify(Parameters parameters, String indentString) {
        if (parameters == null) {
            return null;
        }
        try {
            Writer writer = new StringWriter();
            AponWriter aponWriter = new AponWriter(writer, indentString);
            aponWriter.write(parameters);
            aponWriter.close();
            return writer.toString();
        } catch (Exception e) {
            throw new AponException("Could not convert to APON formatted string", e);
        }
    }

}
