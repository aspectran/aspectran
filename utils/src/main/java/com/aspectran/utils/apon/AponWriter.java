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
import com.aspectran.utils.StringifyContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static com.aspectran.utils.apon.AponFormat.ARRAY_CLOSE;
import static com.aspectran.utils.apon.AponFormat.ARRAY_OPEN;
import static com.aspectran.utils.apon.AponFormat.BLOCK_CLOSE;
import static com.aspectran.utils.apon.AponFormat.BLOCK_OPEN;
import static com.aspectran.utils.apon.AponFormat.COMMA_CHAR;
import static com.aspectran.utils.apon.AponFormat.COMMENT_LINE_START;
import static com.aspectran.utils.apon.AponFormat.DEFAULT_INDENT_STRING;
import static com.aspectran.utils.apon.AponFormat.DOUBLE_QUOTE_CHAR;
import static com.aspectran.utils.apon.AponFormat.EMPTY_ARRAY;
import static com.aspectran.utils.apon.AponFormat.EMPTY_BLOCK;
import static com.aspectran.utils.apon.AponFormat.NAME_VALUE_SEPARATOR;
import static com.aspectran.utils.apon.AponFormat.NULL;
import static com.aspectran.utils.apon.AponFormat.SPACE_CHAR;
import static com.aspectran.utils.apon.AponFormat.SYSTEM_NEW_LINE;
import static com.aspectran.utils.apon.AponFormat.TEXT_CLOSE;
import static com.aspectran.utils.apon.AponFormat.TEXT_LINE_START;
import static com.aspectran.utils.apon.AponFormat.TEXT_OPEN;

/**
 * A writer that outputs APON (Aspectran Parameters Object Notation) formatted text
 * from {@link Parameters} objects.
 * <p>Supports multiple rendering styles: PRETTY, SINGLE_LINE, and COMPACT.</p>
 */
public class AponWriter implements Flushable {

    private final Writer writer;

    private StringWriter stringWriter;

    private StringifyContext stringifyContext;

    private boolean prettyPrint = true;

    private String indentString = DEFAULT_INDENT_STRING;

    private boolean nullWritable = true;

    private boolean enableValueTypeHints;

    private boolean autoFlush;

    private AponRenderStyle currentStyle;

    private boolean pendingSeparator;

    private int indentDepth;

    private boolean atStartOfLine = true;

    /**
     * Creates a new AponWriter that writes to an internal buffer.
     * Use {@link #toString()} to retrieve the formatted text.
     */
    public AponWriter() {
        this.stringWriter = new StringWriter();
        this.writer = this.stringWriter;
    }

    /**
     * Creates a new AponWriter that writes to the specified file.
     * @param file the file to write to
     * @throws IOException if an I/O error occurs
     */
    public AponWriter(@NonNull File file) throws IOException {
        this(Files.newBufferedWriter(file.toPath()));
    }

    /**
     * Creates a new AponWriter that writes to the specified file using the given charset.
     * @param file the file to write to
     * @param charset the charset to use
     * @throws IOException if an I/O error occurs
     */
    public AponWriter(@NonNull File file, Charset charset) throws IOException {
        this(Files.newBufferedWriter(file.toPath(), charset));
    }

    /**
     * Creates a new AponWriter that wraps the given {@link Writer}.
     * @param writer the writer to wrap
     */
    public AponWriter(Writer writer) {
        Assert.notNull(writer, "writer must not be null");
        this.writer = writer;
        if (writer instanceof StringWriter sw) {
            this.stringWriter = sw;
        }
    }

    /**
     * Sets the context for serialization, including date/time formats and indentation.
     * @param stringifyContext the context to use
     */
    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
        if (stringifyContext != null) {
            if (stringifyContext.hasPrettyPrint()) {
                setPrettyPrint(stringifyContext.isPrettyPrint());
            }
            if (stringifyContext.hasIndentSize()) {
                setIndentString(stringifyContext.getIndentString());
            }
            if (stringifyContext.hasNullWritable()) {
                setNullWritable(stringifyContext.isNullWritable());
            }
        }
    }

    /**
     * Fluent API for applying a {@link StringifyContext}.
     * @param stringifyContext the context to apply
     * @return this AponWriter
     * @param <T> the type of AponWriter
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return (T)this;
    }

    /**
     * Sets whether to enable pretty printing.
     * @param prettyPrint true to enable pretty printing, false for compact output
     */
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * Fluent API for enabling or disabling pretty printing.
     * @param prettyPrint true to enable pretty printing, false for compact output
     * @return this AponWriter
     * @param <T> the type of AponWriter
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T prettyPrint(boolean prettyPrint) {
        setPrettyPrint(prettyPrint);
        return (T)this;
    }

    /**
     * Sets the indentation string.
     * @param indentString the indentation string to use
     */
    public void setIndentString(String indentString) {
        this.indentString = indentString;
    }

    /**
     * Fluent API for setting the indentation string.
     * @param indentString the indentation string to use
     * @return this AponWriter
     * @param <T> the type of AponWriter
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T indentString(String indentString) {
        setIndentString(indentString);
        return (T)this;
    }

    /**
     * Sets whether null values should be written.
     * @param nullWritable true to write null values as 'null', false to omit them
     */
    public void setNullWritable(boolean nullWritable) {
        this.nullWritable = nullWritable;
    }

    /**
     * Fluent API for specifying whether null values should be written.
     * @param nullWritable true to write null values as 'null', false to omit them
     * @return this AponWriter
     * @param <T> the type of AponWriter
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T nullWritable(boolean nullWritable) {
        setNullWritable(nullWritable);
        return (T)this;
    }

    /**
     * Sets whether to include value type hints in the output.
     * @param enableValueTypeHints true to include type hints like (String) or (Integer)
     */
    public void setEnableValueTypeHints(boolean enableValueTypeHints) {
        this.enableValueTypeHints = enableValueTypeHints;
    }

    /**
     * Fluent API for enabling or disabling value type hints in the output.
     * @param enableValueTypeHints true to include type hints like (String) or (Integer)
     * @return this AponWriter
     * @param <T> the type of AponWriter
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T enableValueTypeHints(boolean enableValueTypeHints) {
        setEnableValueTypeHints(enableValueTypeHints);
        return (T)this;
    }

    /**
     * Sets whether to enable automatic flushing after each write.
     * @param autoFlush true to enable auto-flush, false otherwise
     */
    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    /**
     * Fluent API for enabling or disabling automatic flushing after each write.
     * @param autoFlush true to enable auto-flush, false otherwise
     * @return this AponWriter
     * @param <T> the type of AponWriter
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T autoFlush(boolean autoFlush) {
        setAutoFlush(autoFlush);
        return (T)this;
    }

    /**
     * Writes a comment to the output. Comments are only written in PRETTY mode.
     * @param message the comment message
     * @return this AponWriter
     * @throws IOException if an I/O error occurs
     * @param <T> the type of AponWriter
     */
    @SuppressWarnings("unchecked")
    public <T extends AponWriter> T comment(String message) throws IOException {
        if (prettyPrint && (currentStyle == null || currentStyle == AponRenderStyle.PRETTY) && message != null) {
            String line;
            int start = 0;
            while ((line = readLine(message, start)) != null) {
                if (atStartOfLine && indentString != null && !indentString.isEmpty()) {
                    for (int i = 0; i < indentDepth; i++) {
                        writer.write(indentString);
                    }
                }
                writer.write(COMMENT_LINE_START);
                if (!line.isEmpty()) {
                    writer.write(SPACE_CHAR);
                    writer.write(line);
                }
                writer.write(SYSTEM_NEW_LINE);
                atStartOfLine = true;
                start += line.length();
                start = skipNewLineChar(message, start);
                if (start == -1) break;
            }
            if (start != -1) {
                if (atStartOfLine && indentString != null && !indentString.isEmpty()) {
                    for (int i = 0; i < indentDepth; i++) {
                        writer.write(indentString);
                    }
                }
                writer.write(COMMENT_LINE_START);
                writer.write(SYSTEM_NEW_LINE);
                atStartOfLine = true;
            }
        }
        return (T)this;
    }

    /**
     * Serializes the given {@link Parameters} object to APON format.
     * @param parameters the parameters to write
     * @return this AponWriter
     * @throws IOException if an I/O error occurs
     */
    public AponWriter write(Parameters parameters) throws IOException {
        return write(parameters, indentDepth == 0);
    }

    /**
     * Internal recursive write method for {@link Parameters}.
     * @param parameters the parameters to write
     * @param isRoot true if this is the root-level object
     * @return this AponWriter
     * @throws IOException if an I/O error occurs
     */
    private AponWriter write(Parameters parameters, boolean isRoot) throws IOException {
        Assert.notNull(parameters, "parameters must not be null");

        AponRenderStyle savedStyle = this.currentStyle;
        AponRenderStyle targetStyle = parameters.getRenderStyle();

        if (!prettyPrint) {
            targetStyle = AponRenderStyle.COMPACT;
        } else if (targetStyle == null) {
            targetStyle = AponRenderStyle.PRETTY;
        }

        // Inherit style if already in a one-line mode
        if (savedStyle != null && savedStyle != AponRenderStyle.PRETTY) {
            this.currentStyle = savedStyle;
        } else {
            this.currentStyle = targetStyle;
        }

        if (parameters instanceof ArrayParameters arrayParameters) {
            writeValue(arrayParameters.getValueList());
        } else {
            // ONLY root parameters can be braceless
            if (isRoot && parameters.isBraceless()) {
                writeParameters(parameters);
            } else {
                beginBlock();
                writeParameters(parameters);
                endBlock();
            }
        }

        this.currentStyle = savedStyle;
        return this;
    }

    /**
     * Iterates over and writes all parameters within a block.
     * @param parameters the parameters to iterate
     * @throws IOException if an I/O error occurs
     */
    private void writeParameters(@NonNull Parameters parameters) throws IOException {
        for (Parameter parameter : parameters.getParameterValues()) {
            if (parameter.getValueType() == ValueType.PARAMETERS) {
                if (parameter.isArray()) {
                    @SuppressWarnings("unchecked")
                    List<Parameters> list = (List<Parameters>)parameter.getValueList();
                    if (list != null) {
                        if (parameter.isBracketed()) {
                            writeName(parameter);
                            if (list.isEmpty()) {
                                emptyArray();
                            } else {
                                beginArray();
                                for (Parameters p : list) {
                                    if (nullWritable || p != null) {
                                        writeValue(p);
                                    }
                                }
                                endArray();
                            }
                        } else {
                            for (Parameters p : list) {
                                if (nullWritable || p != null) {
                                    writeName(parameter);
                                    writeValue(p);
                                }
                            }
                        }
                    }
                } else {
                    Parameters p = parameter.getValueAsParameters();
                    if (nullWritable || p != null) {
                        writeName(parameter);
                        writeValue(p);
                    }
                }
            } else if (parameter.getValueType() == ValueType.TEXT) {
                if (parameter.isArray()) {
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>)parameter.getValueList();
                    if (list != null) {
                        if (parameter.isBracketed()) {
                            writeName(parameter);
                            if (list.isEmpty()) {
                                emptyArray();
                            } else {
                                beginArray();
                                for (String text : list) {
                                    if (nullWritable || text != null) {
                                        beginText();
                                        writeText(text);
                                        endText();
                                    }
                                }
                                endArray();
                            }
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
                            if (list.isEmpty()) {
                                emptyArray();
                            } else {
                                beginArray();
                                for (Object value : list) {
                                    writeValue(value);
                                }
                                endArray();
                            }
                        } else {
                            for (Object value : list) {
                                if (nullWritable || value != null) {
                                    writeName(parameter);
                                    writeValue(value);
                                }
                            }
                        }
                    }
                } else {
                    if (nullWritable || parameter.getValue() != null) {
                        writeName(parameter);
                        writeValue(parameter.getValue());
                    }
                }
            }
        }
    }

    /**
     * Writes a single value of any supported type.
     * @param value the value to write
     * @throws IOException if an I/O error occurs
     */
    private void writeValue(Object value) throws IOException {
        if (value != null) {
            if (value instanceof ArrayParameters arrayParameters) {
                writeValue(arrayParameters.getValueList());
            } else if (value instanceof Parameters parameters) {
                if (parameters.isEmpty()) {
                    emptyBlock();
                } else {
                    // write(Parameters) handles style inheritance and braces
                    write(parameters, false);
                }
            } else if (value instanceof List<?> list) {
                if (list.isEmpty()) {
                    emptyArray();
                } else {
                    beginArray();
                    for (Object obj : list) {
                        writeValue(obj);
                    }
                    endArray();
                }
            } else if (value instanceof LocalDateTime localDateTime) {
                if (stringifyContext != null) {
                    writeString(stringifyContext.toString(localDateTime));
                } else {
                    writeString(localDateTime.toString());
                }
            } else if (value instanceof LocalDate localDate) {
                if (stringifyContext != null) {
                    writeString(stringifyContext.toString(localDate));
                } else {
                    writeString(localDate.toString());
                }
            } else if (value instanceof LocalTime localTime) {
                if (stringifyContext != null) {
                    writeString(stringifyContext.toString(localTime));
                } else {
                    writeString(localTime.toString());
                }
            } else if (value instanceof Date date) {
                if (stringifyContext != null) {
                    writeString(stringifyContext.toString(date));
                } else {
                    writeString(date.toString());
                }
            } else if (value instanceof String str) {
                writeString(str);
            } else {
                writeString(value.toString());
            }
        } else {
            writeNull();
        }
    }

    /**
     * Writes the name of a parameter, optionally including a type hint.
     * @param parameter the parameter whose name to write
     * @throws IOException if an I/O error occurs
     */
    private void writeName(@NonNull Parameter parameter) throws IOException {
        indent();
        String name = parameter.getName();
        if (AponFormat.needsQuoting(name)) {
            write(DOUBLE_QUOTE_CHAR);
            write(AponFormat.escape(name));
            write(DOUBLE_QUOTE_CHAR);
        } else {
            write(name);
        }
        if (enableValueTypeHints || parameter.isValueTypeHinted()) {
            write(TEXT_OPEN);
            write(parameter.getValueType().toString());
            write(TEXT_CLOSE);
        }
        write(NAME_VALUE_SEPARATOR);
        if (currentStyle != AponRenderStyle.COMPACT) {
            write(SPACE_CHAR);
        }
    }

    /**
     * Writes a string value, wrapping it in quotes if necessary.
     * @param value the string value to write
     * @throws IOException if an I/O error occurs
     */
    private void writeString(String value) throws IOException {
        if (value != null) {
            indent();
            if (AponFormat.needsQuoting(value)) {
                write(DOUBLE_QUOTE_CHAR);
                write(AponFormat.escape(value));
                write(DOUBLE_QUOTE_CHAR);
            } else {
                write(value);
            }
            newLine();
        } else {
            writeNull();
        }
    }

    /**
     * Writes a multi-line text block.
     * @param text the text to write
     * @throws IOException if an I/O error occurs
     */
    private void writeText(String text) throws IOException {
        String line;
        int start = 0;
        while ((line = readLine(text, start)) != null) {
            indent();
            write(TEXT_LINE_START);
            write(line);
            writer.write(SYSTEM_NEW_LINE);
            atStartOfLine = true;
            start += line.length();
            start = skipNewLineChar(text, start);
            if (start == -1) break;
        }
        if (start != -1) {
            indent();
            write(TEXT_LINE_START);
            writer.write(SYSTEM_NEW_LINE);
            atStartOfLine = true;
        }
    }

    /** Writes 'null' literal. */
    private void writeNull() throws IOException {
        indent();
        write(NULL);
        newLine();
    }

    /** Starts a new parameter block with '{'. */
    private void beginBlock() throws IOException {
        indent();
        write(BLOCK_OPEN);
        if (currentStyle == AponRenderStyle.PRETTY) {
            writer.write(SYSTEM_NEW_LINE);
            atStartOfLine = true;
        }
        indentDepth++;
        pendingSeparator = false;
    }

    /** Ends a parameter block with '}'. */
    private void endBlock() throws IOException {
        indentDepth--;
        if (pendingSeparator) {
            pendingSeparator = false;
        }
        if (currentStyle == AponRenderStyle.PRETTY) {
            if (!atStartOfLine) {
                writer.write(SYSTEM_NEW_LINE);
            }
            atStartOfLine = true;
        }
        indent();
        write(BLOCK_CLOSE);
        newLine();
    }

    /** Writes an empty block '{}'. */
    private void emptyBlock() throws IOException {
        indent();
        write(EMPTY_BLOCK);
        newLine();
    }

    /** Starts a new array with '['. */
    private void beginArray() throws IOException {
        indent();
        write(ARRAY_OPEN);
        if (currentStyle == AponRenderStyle.PRETTY) {
            writer.write(SYSTEM_NEW_LINE);
            atStartOfLine = true;
        }
        indentDepth++;
        pendingSeparator = false;
    }

    /** Ends an array with ']'. */
    private void endArray() throws IOException {
        indentDepth--;
        if (pendingSeparator) {
            pendingSeparator = false;
        }
        if (currentStyle == AponRenderStyle.PRETTY) {
            if (!atStartOfLine) {
                writer.write(SYSTEM_NEW_LINE);
            }
            atStartOfLine = true;
        }
        indent();
        write(ARRAY_CLOSE);
        newLine();
    }

    /** Writes an empty array '[]'. */
    private void emptyArray() throws IOException {
        indent();
        write(EMPTY_ARRAY);
        newLine();
    }

    /** Starts a multi-line text block with '('. */
    private void beginText() throws IOException {
        indent();
        write(TEXT_OPEN);
        writer.write(SYSTEM_NEW_LINE);
        atStartOfLine = true;
        indentDepth++;
    }

    /** Ends a multi-line text block with ')'. */
    private void endText() throws IOException {
        indentDepth--;
        indent();
        write(TEXT_CLOSE);
        newLine();
    }

    /** Writes a single character to the underlying writer. */
    private void write(char c) throws IOException {
        writer.write(c);
        atStartOfLine = false;
    }

    /** Writes a string to the underlying writer. */
    private void write(String str) throws IOException {
        writer.write(str);
        atStartOfLine = false;
    }

    /**
     * Handles newline or entry separation based on the current rendering style.
     */
    private void newLine() throws IOException {
        if (currentStyle == AponRenderStyle.PRETTY) {
            writer.write(SYSTEM_NEW_LINE);
            atStartOfLine = true;
        } else {
            pendingSeparator = true;
        }
        if (autoFlush) {
            flush();
        }
    }

    /**
     * Handles indentation and comma separation between entries.
     */
    private void indent() throws IOException {
        if (pendingSeparator) {
            writer.write(COMMA_CHAR);
            if (currentStyle == AponRenderStyle.SINGLE_LINE) {
                writer.write(SPACE_CHAR);
            }
            pendingSeparator = false;
            atStartOfLine = false;
        }
        if (atStartOfLine && currentStyle == AponRenderStyle.PRETTY && indentString != null && !indentString.isEmpty()) {
            for (int i = 0; i < indentDepth; i++) {
                writer.write(indentString);
            }
            atStartOfLine = false;
        }
    }

    /** Helper to read a line from a string. */
    @Nullable
    private String readLine(@NonNull String value, int start) {
        if (start >= value.length()) return null;
        int end = start;
        for (; end < value.length(); end++) {
            char c = value.charAt(end);
            if (c == '\n' || c == '\r') break;
        }
        return (end > start ? value.substring(start, end) : StringUtils.EMPTY);
    }

    /** Helper to skip newline characters in a string. */
    private int skipNewLineChar(@NonNull String value, int start) {
        int end = start;
        boolean cr = false;
        boolean lf = false;
        for (; end < value.length(); end++) {
            char c = value.charAt(end);
            if (c != '\n' && c != '\r') break;
            if ((lf && c == '\n') || (cr && c == '\r')) break;
            if (c == '\n') lf = true;
            if (c == '\r') cr = true;
        }
        return (end > start ? end : -1);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }

    @Override
    public String toString() {
        if (stringWriter != null) {
            try {
                flush();
            } catch (IOException e) {
                // ignore
            }
            return stringWriter.toString();
        }
        return super.toString();
    }

}
