/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.json;

import com.aspectran.core.util.ArrayStack;
import com.aspectran.core.util.BeanUtils;
import com.aspectran.core.util.apon.Parameter;
import com.aspectran.core.util.apon.ParameterValue;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.statistic.CounterStatistic;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Converts an object to a JSON formatted string.
 * <p>If pretty-printing is enabled, the JsonWriter will add newlines and
 * indentation to the written data. Pretty-printing is disabled by default.</p>
 *
 * <p>Created: 2008. 06. 12 PM 8:20:54</p>
 * 
 * @author Juho Jeong
 */
public class JsonWriter implements Flushable, Closeable {

    private static final String DEFAULT_INDENT_STRING = "  ";

    private final Writer out;

    private boolean prettyPrint;

    private String indentString;

    private String dateFormat;

    private String dateTimeFormat;

    private boolean skipNull;

    private int indentDepth;

    private String pendedName;

    private final ArrayStack<AtomicInteger> countStack;

    /**
     * Instantiates a new JsonWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     */
    public JsonWriter() {
        this(new StringWriter());
    }

    /**
     * Instantiates a new JsonWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     *
     * @param out the character-output stream
     */
    public JsonWriter(Writer out) {
        this.out = out;

        setIndentString(DEFAULT_INDENT_STRING);

        countStack = new ArrayStack<>();
        countStack.push(new AtomicInteger());
    }

    private void setIndentString(String indentString) {
        this.prettyPrint = (indentString != null);
        this.indentString = indentString;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T prettyPrint(boolean prettyPrint) {
        if (prettyPrint) {
            setIndentString(DEFAULT_INDENT_STRING);
        } else {
            setIndentString(null);
        }
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T indentString(String indentString) {
        setIndentString(indentString);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T dateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T dateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T nullWritable(boolean nullWritable) {
        this.skipNull = !nullWritable;
        return (T)this;
    }

    /**
     * Write an object to a character stream.
     *
     * @param object the object to write to a character-output stream.
     * @throws IOException if an I/O error has occurred.
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T write(Object object) throws IOException {
        if (object == null) {
            writeNull();
        } else if (object instanceof String) {
            writeValue(object.toString());
        } else if (object instanceof Character) {
            writeValue(String.valueOf(((char)object)));
        } else if (object instanceof Boolean) {
            writeValue((Boolean)object);
        } else if (object instanceof Number) {
            writeValue((Number)object);
        } else if (object instanceof Parameters) {
            beginBlock();

            Map<String, ParameterValue> params = ((Parameters)object).getParameterValueMap();
            for (Parameter p : params.values()) {
                String name = p.getName();
                Object value = p.getValue();
                checkCircularReference(object, value);

                writeName(name);
                write(value);
            }

            endBlock();
        } else if (object instanceof Map<?, ?>) {
            beginBlock();

            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>)object).entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(object, value);

                writeName(name);
                write(value);
            }

            endBlock();
        } else if (object instanceof Collection<?>) {
            beginArray();

            for (Object value : (Collection<Object>)object) {
                checkCircularReference(object, value);

                if (value != null) {
                    write(value);
                } else {
                    writeNull(true);
                }
            }

            endArray();
        } else if (object.getClass().isArray()) {
            beginArray();

            int len = Array.getLength(object);
            for (int i = 0; i < len; i++) {
                Object value = Array.get(object, i);
                checkCircularReference(object, value);

                if (value != null) {
                    write(value);
                } else {
                    writeNull(true);
                }
            }

            endArray();
        } else if (object instanceof Date) {
            if (dateTimeFormat != null) {
                SimpleDateFormat dt = new SimpleDateFormat(dateTimeFormat);
                writeValue(dt.format((Date)object));
            } else {
                writeValue(object.toString());
            }
        } else if (object instanceof LocalDate) {
            if (dateFormat != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                writeValue(((LocalDate)object).format(formatter));
            } else {
                writeValue(object.toString());
            }
        } else if (object instanceof LocalDateTime) {
            if (dateTimeFormat != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
                writeValue(((LocalDateTime)object).format(formatter));
            } else {
                writeValue(object.toString());
            }
        } else {
            String[] readablePropertyNames = BeanUtils.getReadablePropertyNamesWithoutNonSerializable(object);
            if (readablePropertyNames != null && readablePropertyNames.length > 0) {
                beginBlock();

                for (String propertyName : readablePropertyNames) {
                    Object value;
                    try {
                        value = BeanUtils.getProperty(object, propertyName);
                    } catch (InvocationTargetException e) {
                        throw new IOException(e);
                    }
                    checkCircularReference(object, value);

                    writeName(propertyName);
                    write(value);
                }

                endBlock();
            } else {
                writeValue(object.toString());
            }
        }
        return (T)this;
    }

    /**
     * Writes a key name to a character stream.
     *
     * @param name the string to write to a character-output stream
     */
    public void writeName(String name) {
        pendedName = name;
    }

    private void writePendedName() throws IOException {
        if (countStack.peek().get() > 0) {
            writeComma();
        }
        if (pendedName != null) {
            indent();
            out.write(escape(pendedName));
            out.write(":");
            if (prettyPrint) {
                out.write(" ");
            }
            pendedName = null;
        } else {
            indent();
        }
    }

    /**
     * Writes a string to a character stream.
     * If {@code value} is null, write a null string ("").
     *
     * @param value the string to write to a character-output stream
     * @throws IOException if an I/O error has occurred
     */
    public void writeValue(String value) throws IOException {
        if (!skipNull || value != null) {
            writePendedName();
            out.write(escape(value));
            countStack.peek().incrementAndGet();
        }
    }

    /**
     *  Writes a {@code Boolean} object to a character stream.
     *
     * @param value a {@code Boolean} object to write to a character-output stream
     * @throws IOException if an I/O error has occurred
     */
    public void writeValue(Boolean value) throws IOException {
        if (!skipNull || value != null) {
            writePendedName();
            out.write(value.toString());
            countStack.peek().incrementAndGet();
        }
    }

    /**
     *  Writes a {@code Number} object to a character stream.
     *
     * @param value a {@code Number} object to write to a character-output stream
     * @throws IOException if an I/O error has occurred
     */
    public void writeValue(Number value) throws IOException {
        if (!skipNull || value != null) {
            writePendedName();
            out.write(value.toString());
            countStack.peek().incrementAndGet();
        }
    }

    /**
     * Write a string "null" to a character stream.
     *
     * @throws IOException if an I/O error has occurred
     */
    public void writeNull() throws IOException {
        writeNull(false);
    }

    public void writeNull(boolean force) throws IOException {
        if (!skipNull || force) {
            writePendedName();
            out.write("null");
            countStack.peek().incrementAndGet();
        }
    }

    /**
     * Write a comma character to a character stream.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void writeComma() throws IOException {
        out.write(",");
        nextLine();
    }

    /**
     * Open a single curly bracket.
     *
     * @throws IOException if an I/O error has occurred
     */
    public void beginBlock() throws IOException {
        writePendedName();
        out.write("{");
        nextLine();
        indentDepth++;
        countStack.push(new AtomicInteger());
    }

    /**
     * Close the open curly bracket.
     *
     * @throws IOException if an I/O error has occurred
     */
    public void endBlock() throws IOException {
        indentDepth--;
        if (countStack.pop().get() > 0) {
            nextLine();
        }
        indent();
        out.write("}");
        countStack.peek().incrementAndGet();
    }

    /**
     * Open a single square bracket.
     *
     * @throws IOException if an I/O error has occurred
     */
    public void beginArray() throws IOException {
        writePendedName();
        out.write("[");
        nextLine();
        indentDepth++;
        countStack.push(new AtomicInteger());
    }

    /**
     * Close the open square bracket.
     *
     * @throws IOException if an I/O error has occurred
     */
    public void endArray() throws IOException {
        indentDepth--;
        if (countStack.pop().get() > 0) {
            nextLine();
        }
        indent();
        out.write("]");
        countStack.peek().incrementAndGet();
    }

    /**
     * Write a tab character to a character stream.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void indent() throws IOException {
        if (prettyPrint) {
            for (int i = 0; i < indentDepth; i++) {
                out.write(indentString);
            }
        }
    }

    /**
     * Write a new line character to a character stream.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void nextLine() throws IOException {
        if (prettyPrint) {
            out.write("\n");
        }
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            out.close();
        }
    }

    @Override
    public String toString() {
        return out.toString();
    }

    private void checkCircularReference(Object wrapper, Object member) throws IOException {
        if (wrapper.equals(member)) {
            throw new IOException("JSON Serialization Failure: A circular reference was detected " +
                    "while converting a member object [" + member + "] in [" + wrapper + "]");
        }
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within &lt;/, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.
     *
     * @param string the input String, may be null
     * @return a String correctly formatted for insertion in a JSON text
     */
    private static String escape(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        int len = string.length();
        char b;
        char c = 0;
        String t;

        StringBuilder sb = new StringBuilder(len + 4);
        sb.append('"');
        for (int i = 0; i < len; i++) {
            b = c;
            c = string.charAt(i);

            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
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
        sb.append('"');
        return sb.toString();
    }

}
