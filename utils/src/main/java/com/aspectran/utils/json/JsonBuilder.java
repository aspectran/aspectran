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

import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.IOException;
import java.io.StringWriter;

/**
 * A builder for creating JSON string.
 * <p>ex)
 * <pre>
 *     String json = new JsonBuilder();
 *         .object()
 *             .put("name", "value")
 *             .object("object")
 *                 .put("name", "value")
 *             .endObject()
 *             .array("array")
 *                 .put("value-1")
 *                 .put("value-2")
 *             .endArray()
 *         .endObject()
 *         .toString();
 * </pre></p>
 * <p>In the above example, the value of the json variable is:
 * <pre>    {"name": "value", "object": {"name": "value"}, "array": ["value-1", "value-2"]}</pre></p>
 * <p>Note: object() &amp; endObject() are a pair of methods, which means that they
 * should show up at the same time. So does array() &amp; endArray()</p>
 *
 * <p>Created: 2024. 12. 14.</p>
 */
public class JsonBuilder {

    private final JsonWriter jsonWriter = new JsonWriter(new StringWriter());

    public void setStringifyContext(StringifyContext stringifyContext) {
        jsonWriter.setStringifyContext(stringifyContext);
    }

    public JsonBuilder apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return this;
    }

    public JsonBuilder prettyPrint(boolean prettyPrint) {
        jsonWriter.prettyPrint(prettyPrint);
        return this;
    }

    public JsonBuilder indentString(@Nullable String indentString) {
        jsonWriter.indentString(indentString);
        return this;
    }

    public JsonBuilder nullWritable(boolean nullWritable) {
        jsonWriter.nullWritable(nullWritable);
        return this;
    }

    /**
     * Begins encoding a new object.
     * @return this builder
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
     * Begins encoding a new object with the given name.
     * @return this builder
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
     * Ends encoding the current object.
     * @return this builder
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
     * Begins encoding a new array.
     * @return this builder
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
     * Begins encoding a new array with the given name.
     * @return this builder
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
     * Ends encoding the current array.
     * @return this builder
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
     * Put a new value into an array.
     * If it's not in an array, just put it as a single value.
     * @param value the new value
     * @return this builder
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
     * Put a new name-value pair in an object.
     * @param name the name of the entry to put in the object
     * @param value the value of the entry to put in the object
     * @return this builder
     */
    public JsonBuilder put(String name, Object value) {
        try {
            jsonWriter.name(name).value(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Nullable
    public String toString() {
        return jsonWriter.toString();
    }

    public JsonString toJsonString() {
        return new JsonString(jsonWriter.toString());
    }

}
