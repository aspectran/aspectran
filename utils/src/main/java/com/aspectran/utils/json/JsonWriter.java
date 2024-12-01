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
package com.aspectran.utils.json;

import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.Assert;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.ParameterValue;
import com.aspectran.utils.apon.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Converts an object to a JSON formatted string.
 * <p>If pretty-printing is enabled, the JsonWriter will add newlines and
 * indentation to the written data. Pretty-printing is disabled by default.</p>
 *
 * <p>Created: 2008. 06. 12 PM 8:20:54</p>
 *
 * @author Juho Jeong
 */
public class JsonWriter {

    private static final String DEFAULT_INDENT_STRING = "  ";

    private static final String NULL_STRING = "null";

    private final ArrayStack<Boolean> writtenFlags = new ArrayStack<>();

    private final Writer out;

    private StringifyContext stringifyContext;

    private boolean prettyPrint = true;

    private String indentString = DEFAULT_INDENT_STRING;

    private boolean nullWritable = true;

    private int indentDepth;

    private String pendedName;

    private Object upperObject;

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
     * @param out the character-output stream
     */
    public JsonWriter(Writer out) {
        Assert.notNull(out, "out must not be null");
        this.out = out;
        writtenFlags.push(false);
    }

    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
        if (stringifyContext != null) {
            if (stringifyContext.hasPretty()) {
                prettyPrint(stringifyContext.isPretty());
            }
            if (stringifyContext.hasIndentSize()) {
                indentString(stringifyContext.getIndentString());
            }
            if (stringifyContext.hasNullWritable()) {
                nullWritable(stringifyContext.isNullWritable());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T prettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        if (prettyPrint) {
            if (this.indentString == null) {
                this.indentString = DEFAULT_INDENT_STRING;
            }
        } else {
            this.indentString = null;
        }
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T indentString(@Nullable String indentString) {
        this.indentString = indentString;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T nullWritable(boolean nullWritable) {
        this.nullWritable = nullWritable;
        return (T)this;
    }

    /**
     * Writes an object to the writer.
     * @param object the object to write to the writer.
     * @throws IOException if an I/O error has occurred.
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T write(Object object) throws IOException {
        if (object == null) {
            writeNull();
        } else if (object instanceof String string) {
            writeValue(string);
        } else if (object instanceof JsonString) {
            writeJson(object.toString());
        } else if (object instanceof Character) {
            writeValue(String.valueOf(object));
        } else if (object instanceof Boolean bool) {
            writeValue(bool);
        } else if (object instanceof Number number) {
            writeValue(number);
        } else if (object instanceof Parameters parameters) {
            beginObject();
            Map<String, ParameterValue> params = parameters.getParameterValueMap();
            for (Parameter p : params.values()) {
                String name = p.getName();
                Object value = p.getValue();
                writeName(name);
                write(object, value);
            }
            endObject();
        } else if (object instanceof Map<?, ?> map) {
            beginObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                writeName(name);
                write(object, value);
            }
            endObject();
        } else if (object instanceof Collection<?> collection) {
            beginArray();
            for (Object value : collection) {
                if (value != null) {
                    write(object, value);
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
                if (value != null) {
                    write(object, value);
                } else {
                    writeNull(true);
                }
            }
            endArray();
        } else if (object instanceof LocalDateTime localDateTime) {
            if (stringifyContext != null) {
                writeValue(stringifyContext.toString(localDateTime));
            } else {
                writeValue(localDateTime.toString());
            }
        } else if (object instanceof LocalDate localDate) {
            if (stringifyContext != null) {
                writeValue(stringifyContext.toString(localDate));
            } else {
                writeValue(localDate.toString());
            }
        } else if (object instanceof LocalTime localTime) {
            if (stringifyContext != null) {
                writeValue(stringifyContext.toString(localTime));
            } else {
                writeValue(localTime.toString());
            }
        } else if (object instanceof Date date) {
            if (stringifyContext != null) {
                writeValue(stringifyContext.toString(date));
            } else {
                writeValue(date.toString());
            }
        } else {
            String[] readablePropertyNames = BeanUtils.getReadablePropertyNamesWithoutNonSerializable(object);
            if (readablePropertyNames != null && readablePropertyNames.length > 0) {
                beginObject();
                for (String propertyName : readablePropertyNames) {
                    Object value;
                    try {
                        value = BeanUtils.getProperty(object, propertyName);
                    } catch (InvocationTargetException e) {
                        throw new IOException(e);
                    }
                    writeName(propertyName);
                    write(object, value);
                }
                endObject();
            } else {
                writeValue(object.toString());
            }
        }
        return (T)this;
    }

    private void write(Object object, Object member) throws IOException {
        checkCircularReference(object, member);
        this.upperObject = object;
        write(member);
        this.upperObject = null;
    }

    /**
     * Writes a key name to the writer.
     * @param name the string to write to the writer
     */
    public JsonWriter writeName(String name) {
        pendedName = name;
        return this;
    }

    private void writePendedName() throws IOException {
        if (writtenFlags.peek()) {
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

    private void clearPendedName() {
        pendedName = null;
    }

    /**
     * Writes a string to the writer.
     * If {@code value} is null, write a null string ("").
     * @param value the string to write to the writer
     * @throws IOException if an I/O error has occurred
     */
    public void writeValue(String value) throws IOException {
        if (nullWritable || value != null) {
            writePendedName();
            out.write(escape(value));
            writtenFlags.update(true);
        } else {
            clearPendedName();
        }
    }

    /**
     *  Writes a {@code Boolean} object to the writer.
     * @param value a {@code Boolean} object to write to the writer
     * @throws IOException if an I/O error has occurred
     */
    public void writeValue(Boolean value) throws IOException {
        if (nullWritable || value != null) {
            writePendedName();
            out.write(value.toString());
            writtenFlags.update(true);
        } else {
            clearPendedName();
        }
    }

    /**
     *  Writes a {@code Number} object to the writer.
     * @param value a {@code Number} object to write to the writer
     * @throws IOException if an I/O error has occurred
     */
    public void writeValue(Number value) throws IOException {
        if (nullWritable || value != null) {
            writePendedName();
            out.write(value.toString());
            writtenFlags.update(true);
        } else {
            clearPendedName();
        }
    }

    /**
     * Writes a "null" string to the writer.
     * @throws IOException if an I/O error has occurred
     */
    public void writeNull() throws IOException {
        writeNull(false);
    }

    /**
     * Writes a "null" string to the writer.
     * @param force true if forces should be written null value
     * @throws IOException if an I/O error has occurred
     */
    public void writeNull(boolean force) throws IOException {
        if (nullWritable || force) {
            writePendedName();
            out.write(NULL_STRING);
            writtenFlags.update(true);
        } else {
            clearPendedName();
        }
    }

    /**
     * Writes a string directly to the writer stream without
     * quoting or escaping.
     * @param json the string to write to the writer
     * @throws IOException if an I/O error has occurred
     */
    public void writeJson(String json) throws IOException {
        if (nullWritable || json != null) {
            writePendedName();
            if (json != null) {
                BufferedReader reader = new BufferedReader(new StringReader(json));
                boolean first = true;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!first) {
                        nextLine();
                        indent();
                    }
                    out.write(line);
                    first = false;
                }
            } else {
                out.write(NULL_STRING);
            }
            writtenFlags.update(true);
        } else {
            clearPendedName();
        }
    }

    /**
     * Writes a comma character to the writer.
     * @throws IOException if an I/O error has occurred
     */
    private void writeComma() throws IOException {
        out.write(",");
        nextLine();
    }

    /**
     * Begins encoding a new object.
     * @throws IOException if an I/O error has occurred
     */
    public void beginObject() throws IOException {
        writePendedName();
        out.write("{");
        nextLine();
        indentDepth++;
        writtenFlags.push(false);
    }

    /**
     * Ends encoding the current object.
     * @throws IOException if an I/O error has occurred
     */
    public void endObject() throws IOException {
        indentDepth--;
        if (writtenFlags.pop()) {
            nextLine();
        }
        indent();
        out.write("}");
        writtenFlags.update(true);
    }

    /**
     * Begins encoding a new array.
     * @throws IOException if an I/O error has occurred
     */
    public void beginArray() throws IOException {
        writePendedName();
        out.write("[");
        nextLine();
        indentDepth++;
        writtenFlags.push(false);
    }

    /**
     * Ends encoding the current array.
     * @throws IOException if an I/O error has occurred
     */
    public void endArray() throws IOException {
        indentDepth--;
        if (writtenFlags.pop()) {
            nextLine();
        }
        indent();
        out.write("]");
        writtenFlags.update(true);
    }

    /**
     * Writes a tab character to the writer.
     * @throws IOException if an I/O error has occurred
     */
    private void indent() throws IOException {
        if (prettyPrint && indentString != null && !indentString.isEmpty()) {
            for (int i = 0; i < indentDepth; i++) {
                out.write(indentString);
            }
        }
    }

    /**
     * Writes a new line character to the writer.
     * @throws IOException if an I/O error has occurred
     */
    private void nextLine() throws IOException {
        if (prettyPrint) {
            out.write("\n");
        }
    }

    /**
     * Ensures all buffered data is written to the underlying
     * {@link Writer} and flushes that writer.
     * @throws IOException if an I/O error has occurred
     */
    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }

    @Override
    public String toString() {
        return out.toString();
    }

    private void checkCircularReference(Object object, Object member) throws IOException {
        if (object == member || (upperObject != null && upperObject == member)) {
            String what;
            if (pendedName != null) {
                what = "member '" + pendedName + "'";
            } else {
                what = "a member";
            }
            throw new IOException("JSON Serialization Failure: " +
                    "A circular reference was detected while converting " + what);
        }
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within &lt;/, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.
     * @param string the input String, may be null
     * @return a String correctly formatted for insertion in a JSON text
     */
    @NonNull
    private static String escape(String string) {
        if (string == null || string.isEmpty()) {
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
