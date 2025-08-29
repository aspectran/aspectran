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

/**
 * A structure, name, or value type in a JSON-encoded string.
 * <p>This enum is a clone of {@code com.google.gson.stream.JsonToken}.</p>
 *
 * @author Jesse Wilson
 */
public enum JsonToken {

    /** The opening of a JSON array ({@code [}). */
    BEGIN_ARRAY,

    /** The closing of a JSON array ({@code ]}). */
    END_ARRAY,

    /** The opening of a JSON object ({@code {}}). */
    BEGIN_OBJECT,

    /** The closing of a JSON object ({@code }}). */
    END_OBJECT,

    /** A JSON property name. Within objects, tokens alternate between names and their values. */
    NAME,

    /** A JSON string value. */
    STRING,

    /** A JSON number value, represented in this API by a Java {@code double}, {@code long}, or {@code int}. */
    NUMBER,

    /** A JSON boolean value ({@code true} or {@code false}). */
    BOOLEAN,

    /** A JSON null value ({@code null}). */
    NULL,

    /** The end of the JSON stream. This sentinel value is returned by {@link
     * JsonReader#peek()} to signal that the JSON-encoded value has no more
     * tokens. */
    END_DOCUMENT

}
