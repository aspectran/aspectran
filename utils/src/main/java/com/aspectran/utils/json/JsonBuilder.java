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

import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.IOException;
import java.io.StringWriter;

/**
 * A builder for creating JSON strings using a fluent API.
 * <p>This class simplifies the process of constructing JSON objects and arrays
 * by providing a method-chaining interface. It internally uses a {@link JsonWriter}
 * to write the JSON structure.</p>
 * <p>Example usage:
 * <pre>
 *     String json = new JsonBuilder()
 *         .object()
 *             .put("name", "value")
 *             .object("nestedObject")
 *                 .put("key", "nestedValue")
 *             .endObject()
 *             .array("myArray")
 *                 .put("item1")
 *                 .put("item2")
 *             .endArray()
 *         .endObject()
 *         .toString();
 * </pre>
 * The resulting JSON string would be:
 * <pre>    {"name":"value","nestedObject":{"key":"nestedValue"},"myArray":["item1","item2"]}</pre>
 * </p>
 * <p>Note: {@code object()} and {@code endObject()} must always be used in pairs.
 * The same applies to {@code array()} and {@code endArray()}.</p>
 *
 * <p>Created: 2024. 12. 14.</p>
 */
public class JsonBuilder {

    private final JsonWriter jsonWriter = new JsonWriter(new StringWriter());

    /**
     * Sets the {@link StringifyContext} for the underlying {@link JsonWriter}.
     * This allows configuring pretty-printing, indentation, and null-writability.
     * @param stringifyContext the context for stringification options
     */
    public void setStringifyContext(StringifyContext stringifyContext) {
        jsonWriter.setStringifyContext(stringifyContext);
    }

    /**
     * Applies a {@link StringifyContext} to this builder in a fluent style.
     * @param stringifyContext the context for stringification options
     * @return this builder instance for chaining
     */
    public JsonBuilder apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return this;
    }

    /**
     * Enables or disables pretty-printing for the generated JSON.
     * @param prettyPrint {@code true} to enable pretty-printing; {@code false} for compact output
     * @return this builder instance for chaining
     */
    public JsonBuilder prettyPrint(boolean prettyPrint) {
        jsonWriter.prettyPrint(prettyPrint);
        return this;
    }

    /**
     * Sets the indentation string for pretty-printing.
     * @param indentString the string to use for indentation (e.g., "  " or "\t")
     * @return this builder instance for chaining
     */
    public JsonBuilder indentString(@Nullable String indentString) {
        jsonWriter.indentString(indentString);
        return this;
    }

    /**
     * Configures whether {@code null} values should be written to the JSON output.
     * @param nullWritable {@code true} to write null values; {@code false} to skip them
     * @return this builder instance for chaining
     */
    public JsonBuilder nullWritable(boolean nullWritable) {
        jsonWriter.nullWritable(nullWritable);
        return this;
    }

    /**
     * Begins encoding a new JSON object.
     * @return this builder instance for chaining
     */
    public JsonBuilder object() {
        try {
            jsonWriter.beginObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Begins encoding a new JSON object with the given name (key).
     * This is used when creating a nested object within another object.
     * @param name the name (key) of the object
     * @return this builder instance for chaining
     */
    public JsonBuilder object(String name) {
        try {
            jsonWriter.name(name).beginObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Ends encoding the current JSON object.
     * @return this builder instance for chaining
     */
    public JsonBuilder endObject() {
        try {
            jsonWriter.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Begins encoding a new JSON array.
     * @return this builder instance for chaining
     */
    public JsonBuilder array() {
        try {
            jsonWriter.beginArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Begins encoding a new JSON array with the given name (key).
     * This is used when creating a nested array within an object.
     * @param name the name (key) of the array
     * @return this builder instance for chaining
     */
    public JsonBuilder array(String name) {
        try {
            jsonWriter.name(name).beginArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Ends encoding the current JSON array.
     * @return this builder instance for chaining
     */
    public JsonBuilder endArray() {
        try {
            jsonWriter.endArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Puts a new value into the current JSON array or as a single value if not in an array context.
     * @param value the value to put
     * @return this builder instance for chaining
     */
    public JsonBuilder put(Object value) {
        try {
            jsonWriter.value(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Puts a new name-value pair into the current JSON object.
     * @param name the name (key) of the entry
     * @param value the value of the entry
     * @return this builder instance for chaining
     */
    public JsonBuilder put(String name, Object value) {
        try {
            jsonWriter.name(name).value(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Returns the final JSON string built by this builder.
     * @return the JSON string
     */
    @Nullable
    public String toString() {
        return jsonWriter.toString();
    }

    /**
     * Returns the final JSON string built by this builder as a {@link JsonString} object.
     * @return a {@link JsonString} object containing the JSON output
     */
    public JsonString toJsonString() {
        return new JsonString(jsonWriter.toString());
    }

}
