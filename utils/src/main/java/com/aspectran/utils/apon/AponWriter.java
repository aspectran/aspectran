/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.utils.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Writes an APON object to an output source.
 *
 * <p>By default, the indentation string is "  " (two blanks)</p>
 */
public class AponWriter extends AponFormat implements Flushable, Closeable {

    private final Writer writer;

    private boolean autoFlush;

    private boolean prettyPrint;

    private String indentString;

    private boolean nullWritable = true;

    private boolean valueTypeHintEnabled;

    private int indentDepth;

    /**
     * Instantiates a new AponWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     */
    public AponWriter() {
        this(new StringWriter());
    }

    /**
     * Instantiates a new AponWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     * @param writer the character-output stream
     */
    public AponWriter(Writer writer) {
        this.writer = writer;
        this.prettyPrint = true;
        setIndentString(DEFAULT_INDENT_STRING);
    }

    /**
     * Instantiates a new AponWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     * @param file a File object to write to
     * @throws IOException if an I/O error occurs
     */
    public AponWriter(File file) throws IOException {
        this(new FileWriter(file));
    }

    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T autoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T prettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        if (prettyPrint) {
            setIndentString(DEFAULT_INDENT_STRING);
        } else {
            setIndentString(null);
        }
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T indentString(String indentString) {
        setIndentString(indentString);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T nullWritable(boolean nullWritable) {
        this.nullWritable = nullWritable;
        return (T)this;
    }

    /**
     * Sets whether write a type hint for values.
     * @param valueTypeHintEnabled true, write a type hint for values
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T enableValueTypeHints(boolean valueTypeHintEnabled) {
        this.valueTypeHintEnabled = valueTypeHintEnabled;
        return (T)this;
    }

    /**
     * Specifies the indent string.
     * @param indentString the indentation string, by default "  " (two blanks).
     */
    public void setIndentString(String indentString) {
        this.indentString = indentString;
    }

    /**
     * Write a Parameters object to the character-output stream.
     * @param parameters the Parameters object to be converted
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T write(Parameters parameters) throws IOException {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        if (parameters instanceof ArrayParameters) {
            for (Parameters ps : ((ArrayParameters)parameters).getParametersList()) {
                beginBlock();
                for (Parameter pv : ps.getParameterValueMap().values()) {
                    if (nullWritable || pv.isAssigned()) {
                        write(pv);
                    }
                }
                endBlock();
            }
        } else {
            for (Parameter pv : parameters.getParameterValueMap().values()) {
                if (nullWritable || pv.isAssigned()) {
                    write(pv);
                }
            }
        }
        return (T)this;
    }

    /**
     * Write a Parameter object to the character-output stream.
     * @param parameter the Parameter object to be converted
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T write(Parameter parameter) throws IOException {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter must not be null");
        }
        if (parameter.getValueType() == ValueType.PARAMETERS) {
            if (parameter.isArray()) {
                List<Parameters> list = parameter.getValueAsParametersList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        beginArray();
                        for (Parameters ps : list) {
                            indent();
                            beginBlock();
                            if (ps != null) {
                                write(ps);
                            }
                            endBlock();
                        }
                        endArray();
                    } else {
                        for (Parameters ps : list) {
                            if (ps != null) {
                                writeName(parameter, ps.getActualName());
                                beginBlock();
                                write(ps);
                                endBlock();
                            }
                        }
                    }
                }
            } else {
                Parameters ps = parameter.getValueAsParameters();
                if (nullWritable || ps != null) {
                    writeName(parameter, (ps != null ? ps.getActualName() : null));
                    beginBlock();
                    if (ps != null) {
                        write(ps);
                    }
                    endBlock();
                }
            }
        } else if (parameter.getValueType() == ValueType.VARIABLE) {
            if (parameter.isArray()) {
                List<?> list = parameter.getValueList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        beginArray();
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
                        endArray();
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
                if (nullWritable || value != null) {
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
        } else if (parameter.getValueType() == ValueType.STRING) {
            if (parameter.isArray()) {
                List<String> list = parameter.getValueAsStringList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        beginArray();
                        for (String value : list) {
                            indent();
                            writeString(value);
                        }
                        endArray();
                    } else {
                        for (String value : list) {
                            if (nullWritable || value != null) {
                                writeName(parameter);
                                writeString(value);
                            }
                        }
                    }
                }
            } else {
                String value = parameter.getValueAsString();
                if (nullWritable || value != null) {
                    writeName(parameter);
                    writeString(value);
                }
            }
        } else if (parameter.getValueType() == ValueType.TEXT) {
            if (parameter.isArray()) {
                List<String> list = parameter.getValueAsStringList();
                if (list != null) {
                    if (parameter.isBracketed()) {
                        writeName(parameter);
                        beginArray();
                        for (String text : list) {
                            indent();
                            beginText();
                            writeText(text);
                            endText();
                        }
                        endArray();
                    } else {
                        for (String text : list) {
                            if (nullWritable || text != null) {
                                writeName(parameter);
                                beginText();
                                writeText(text);
                                endText();
                            }
                        }
                    }
                }
            } else {
                String text = parameter.getValueAsString();
                if (text != null) {
                    writeName(parameter);
                    beginText();
                    writeText(text);
                    endText();
                } else if (nullWritable) {
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
                        beginArray();
                        for (Object value : list) {
                            indent();
                            write(value);
                        }
                        endArray();
                    } else {
                        for (Object value : list) {
                            if (nullWritable || value != null) {
                                writeName(parameter);
                                write(value);
                            }
                        }
                    }
                }
            } else {
                if (nullWritable || parameter.getValue() != null) {
                    writeName(parameter);
                    write(parameter.getValue());
                }
            }
        }
        return (T)this;
    }

    /**
     * Writes a comment to the character-output stream.
     * @param message the comment to write to a character-output stream
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T comment(String message) throws IOException {
        if (message != null) {
            if (message.indexOf(NEW_LINE_CHAR) != -1) {
                String line;
                int start = 0;
                while ((line = readLine(message, start)) != null) {
                    writer.write(COMMENT_LINE_START);
                    writer.write(SPACE_CHAR);
                    writer.write(line);
                    newLine();

                    start += line.length();
                    start = skipNewLineChar(message, start);
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
                writer.write(message);
                newLine();
            }
        }
        return (T)this;
    }

    private void writeName(Parameter parameter) throws IOException {
        writeName(parameter, null);
    }

    private void writeName(Parameter parameter, String actualName) throws IOException {
        indent();
        writer.write(actualName != null ? actualName : parameter.getName());
        if (valueTypeHintEnabled || parameter.isValueTypeHinted()) {
            writer.write(ROUND_BRACKET_OPEN);
            writer.write(parameter.getValueType().toString());
            writer.write(ROUND_BRACKET_CLOSE);
        }
        writer.write(NAME_VALUE_SEPARATOR);
        if (prettyPrint) {
            writer.write(SPACE_CHAR);
        }
    }

    private void writeString(String value) throws IOException {
        if (value == null) {
            writeNull();
            return;
        }
        if (value.isEmpty()) {
            writer.write(DOUBLE_QUOTE_CHAR);
            writer.write(DOUBLE_QUOTE_CHAR);
            newLine();
            return;
        }
        if (value.indexOf(DOUBLE_QUOTE_CHAR) >= 0 ||
                value.indexOf(SINGLE_QUOTE_CHAR) >= 0 ||
                value.startsWith(SPACE) ||
                value.endsWith(SPACE) ||
                value.contains(AponFormat.NEW_LINE)) {
            writer.write(DOUBLE_QUOTE_CHAR);
            writer.write(escape(value));
            writer.write(DOUBLE_QUOTE_CHAR);
        } else {
            writer.write(value);
        }
        newLine();
    }

    private void writeText(String text) throws IOException {
        String line;
        int start = 0;
        while ((line = readLine(text, start)) != null) {
            indent();
            writer.write(TEXT_LINE_START);
            writer.write(line);
            newLine();

            start += line.length();
            start = skipNewLineChar(text, start);
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

    private void beginBlock() throws IOException {
        writer.write(CURLY_BRACKET_OPEN);
        newLine();
        increaseIndent();
    }

    private void endBlock() throws IOException {
        decreaseIndent();
        indent();
        writer.write(CURLY_BRACKET_CLOSE);
        newLine();
    }

    private void beginArray() throws IOException {
        writer.write(SQUARE_BRACKET_OPEN);
        newLine();
        increaseIndent();
    }

    private void endArray() throws IOException {
        decreaseIndent();
        indent();
        writer.write(SQUARE_BRACKET_CLOSE);
        newLine();
    }

    private void beginText() throws IOException {
        writer.write(ROUND_BRACKET_OPEN);
        newLine();
        increaseIndent();
    }

    private void endText() throws IOException {
        decreaseIndent();
        indent();
        writer.write(ROUND_BRACKET_CLOSE);
        newLine();
    }

    private void newLine() throws IOException {
        writer.write(SYSTEM_NEW_LINE);
        if (autoFlush) {
            flush();
        }
    }

    private void indent() throws IOException {
        if (indentString != null) {
            for (int i = 0; i < indentDepth; i++) {
                writer.write(indentString);
            }
        }
    }

    private void increaseIndent() {
        if (indentString != null) {
            indentDepth++;
        }
    }

    private void decreaseIndent() {
        if (indentString != null) {
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

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public String toString() {
        return writer.toString();
    }

    public static String escape(String str) {
        if (str == null) {
            return null;
        }

        int len = str.length();
        if (len == 0) {
            return str;
        }

        StringBuilder sb = new StringBuilder(len);
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

}
