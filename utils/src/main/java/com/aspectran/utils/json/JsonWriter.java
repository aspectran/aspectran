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
package com.aspectran.utils.json;

import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.Assert;
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.Parameters;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
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

    private final Writer writer;

    private StringifyContext stringifyContext;

    private Map<Class<?>, JsonSerializer<?>> serializers;

    private boolean prettyPrint = true;

    private String indentString = DEFAULT_INDENT_STRING;

    private boolean nullWritable = true;

    private int indentDepth;

    private String pendingName;

    private Object upperObject;

    /**
     * Instantiates a new JsonWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     * @param writer the character-output stream
     */
    public JsonWriter(Writer writer) {
        Assert.notNull(writer, "out must not be null");
        this.writer = writer;
        writtenFlags.push(false);
    }

    public <T> void registerSerializer(Class<T> type, JsonSerializer<T> serializer) {
        if (serializers == null) {
            serializers = new HashMap<>();
        }
        serializers.put(type, serializer);
    }

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

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return (T)this;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        if (prettyPrint) {
            if (indentString == null) {
                indentString = DEFAULT_INDENT_STRING;
            }
        } else {
            indentString = null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T prettyPrint(boolean prettyPrint) {
        setPrettyPrint(prettyPrint);
        return (T)this;
    }

    public void setIndentString(String indentString) {
        this.indentString = indentString;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T indentString(String indentString) {
        setIndentString(indentString);
        return (T)this;
    }

    public void setNullWritable(boolean nullWritable) {
        this.nullWritable = nullWritable;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T nullWritable(boolean nullWritable) {
        setNullWritable(nullWritable);
        return (T)this;
    }

    /**
     * Begins encoding a new object.
     * @throws IOException if an I/O error has occurred
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T beginObject() throws IOException {
        writePendingName();
        writer.write("{");
        nextLine();
        indentDepth++;
        writtenFlags.push(false);
        return (T)this;
    }

    /**
     * Ends encoding the current object.
     * @throws IOException if an I/O error has occurred
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T endObject() throws IOException {
        indentDepth--;
        if (writtenFlags.pop()) {
            nextLine();
        }
        indent();
        writer.write("}");
        writtenFlags.update(true);
        return (T)this;
    }

    /**
     * Begins encoding a new array.
     * @throws IOException if an I/O error has occurred
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T beginArray() throws IOException {
        writePendingName();
        writer.write("[");
        nextLine();
        indentDepth++;
        writtenFlags.push(false);
        return (T)this;
    }

    /**
     * Ends encoding the current array.
     * @throws IOException if an I/O error has occurred
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T endArray() throws IOException {
        indentDepth--;
        if (writtenFlags.pop()) {
            nextLine();
        }
        indent();
        writer.write("]");
        writtenFlags.update(true);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T name(String name) throws IOException {
        writeName(name);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public <T extends JsonWriter> T value(Object value) throws IOException {
        writeValue(value);
        return (T)this;
    }

    /**
     * Writes a key name to the writer.
     * @param name the string to write to the writer
     */
    public void writeName(String name) {
        pendingName = name;
    }

    private void writePendingName() throws IOException {
        if (writtenFlags.peek()) {
            writeComma();
        }
        if (pendingName != null) {
            indent();
            writer.write(escape(pendingName));
            writer.write(":");
            if (prettyPrint) {
                writer.write(" ");
            }
            pendingName = null;
        } else {
            indent();
        }
    }

    private void clearPendingName() {
        pendingName = null;
    }

    /**
     * Writes an object to the writer.
     * @param object the object to write to the writer.
     * @throws IOException if an I/O error has occurred.
     */
    public void writeValue(Object object) throws IOException {
        if (object == null) {
            writeNull();
            return;
        }

        if (serializers != null) {
            @SuppressWarnings("unchecked")
            JsonSerializer<Object> serializer = (JsonSerializer<Object>)serializers.get(object.getClass());
            if (serializer != null) {
                serializer.serialize(object, this);
                return;
            }
        }

        if (object instanceof String string) {
            writeString(string);
        } else if (object instanceof JsonString) {
            String json = object.toString();
            if (json != null) {
                if (json.trim().isEmpty()) {
                    writeNull();
                    return;
                }
                try {
                    Object parsed = JsonParser.parse(json);
                    writeValue(parsed);
                } catch (IOException e) {
                    throw new IOException("Failed to re-parse JsonString", e);
                }
            } else {
                writeNull();
            }
        } else if (object instanceof Character) {
            writeString(String.valueOf(object));
        } else if (object instanceof Boolean bool) {
            writeBool(bool);
        } else if (object instanceof Number number) {
            writeNumber(number);
        } else if (object instanceof Parameters parameters) {
            beginObject();
            for (Parameter p : parameters.getParameterValues()) {
                String name = p.getName();
                Object value = p.getValue();
                writeName(name);
                writeValue(value, object);
            }
            endObject();
        } else if (object instanceof Map<?, ?> map) {
            beginObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                writeName(name);
                writeValue(value, object);
            }
            endObject();
        } else if (object instanceof Collection<?> collection) {
            beginArray();
            for (Object value : collection) {
                if (value != null) {
                    writeValue(value, object);
                } else {
                    writeNull(true);
                }
            }
            endArray();
        } else if (object instanceof Iterator<?> iterator) {
            beginArray();
            while (iterator.hasNext()) {
                Object value = iterator.next();
                if (value != null) {
                    writeValue(value, object);
                } else {
                    writeNull(true);
                }
            }
            endArray();
        } else if (object instanceof Enumeration<?> enumeration) {
            beginArray();
            while (enumeration.hasMoreElements()) {
                Object value = enumeration.nextElement();
                if (value != null) {
                    writeValue(value, object);
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
                    writeValue(value, object);
                } else {
                    writeNull(true);
                }
            }
            endArray();
        } else if (object instanceof LocalDateTime localDateTime) {
            if (stringifyContext != null) {
                writeString(stringifyContext.toString(localDateTime));
            } else {
                writeString(localDateTime.toString());
            }
        } else if (object instanceof LocalDate localDate) {
            if (stringifyContext != null) {
                writeString(stringifyContext.toString(localDate));
            } else {
                writeString(localDate.toString());
            }
        } else if (object instanceof LocalTime localTime) {
            if (stringifyContext != null) {
                writeString(stringifyContext.toString(localTime));
            } else {
                writeString(localTime.toString());
            }
        } else if (object instanceof Date date) {
            if (stringifyContext != null) {
                writeString(stringifyContext.toString(date));
            } else {
                writeString(date.toString());
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
                    writeValue(value, object);
                }
                endObject();
            } else {
                writeString(object.toString());
            }
        }
    }

    private void writeValue(Object object, Object container) throws IOException {
        checkCircularReference(container, object);
        this.upperObject = container;
        writeValue(object);
        this.upperObject = null;
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
            writePendingName();
            writer.write(NULL_STRING);
            writtenFlags.update(true);
        } else {
            clearPendingName();
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
            writePendingName();
            if (json != null) {
                BufferedReader reader = new BufferedReader(new StringReader(json));
                boolean first = true;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!first) {
                        nextLine();
                        indent();
                    }
                    writer.write(line);
                    first = false;
                }
            } else {
                writer.write(NULL_STRING);
            }
            writtenFlags.update(true);
        } else {
            clearPendingName();
        }
    }

    /**
     * Writes a string to the writer.
     * If {@code value} is null, write a null string ("").
     * @param value the string to write to the writer
     * @throws IOException if an I/O error has occurred
     */
    protected void writeString(String value) throws IOException {
        if (nullWritable || value != null) {
            writePendingName();
            writer.write(escape(value));
            writtenFlags.update(true);
        } else {
            clearPendingName();
        }
    }

    /**
     * Writes a {@code Boolean} object to the writer.
     * @param value a {@code Boolean} object to write to the writer
     * @throws IOException if an I/O error has occurred
     */
    protected void writeBool(Boolean value) throws IOException {
        if (nullWritable || value != null) {
            writePendingName();
            writer.write(value.toString());
            writtenFlags.update(true);
        } else {
            clearPendingName();
        }
    }

    /**
     * Writes a {@code Number} object to the writer.
     * @param value a {@code Number} object to write to the writer
     * @throws IOException if an I/O error has occurred
     */
    protected void writeNumber(Number value) throws IOException {
        if (nullWritable || value != null) {
            writePendingName();
            writer.write(value.toString());
            writtenFlags.update(true);
        } else {
            clearPendingName();
        }
    }

    /**
     * Writes a comma character to the writer.
     * @throws IOException if an I/O error has occurred
     */
    private void writeComma() throws IOException {
        writer.write(",");
        nextLine();
    }

    /**
     * Writes a tab character to the writer.
     * @throws IOException if an I/O error has occurred
     */
    private void indent() throws IOException {
        if (prettyPrint && indentString != null && !indentString.isEmpty()) {
            for (int i = 0; i < indentDepth; i++) {
                writer.write(indentString);
            }
        }
    }

    /**
     * Writes a new line character to the writer.
     * @throws IOException if an I/O error has occurred
     */
    private void nextLine() throws IOException {
        if (prettyPrint) {
            writer.write("\n");
        }
    }

    /**
     * Ensures all buffered data is written to the underlying
     * {@link Writer} and flushes that writer.
     * @throws IOException if an I/O error has occurred
     */
    public void flush() throws IOException {
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }

    @Override
    public String toString() {
        return writer.toString();
    }

    private void checkCircularReference(Object object, Object member) throws IOException {
        if (object == member || (upperObject != null && upperObject == member)) {
            String what;
            if (pendingName != null) {
                what = "member '" + pendingName + "'";
            } else {
                what = "a member";
            }
            throw new IOException("JSON Serialization Failure: Circular reference detected for " +
                    what + " in object " + ObjectUtils.identityToString(object));
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

        StringBuilder sb = new StringBuilder(Math.min(len * 2, len + 16));
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
