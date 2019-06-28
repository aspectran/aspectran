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

/**
 * <p>(This class is a member of Google's Gson Library.)</p>
 *
 * A structure, name or value type in a JSON-encoded string.
 *
 * @author Jesse Wilson
 */
public enum JsonToken {

    /**
     * The opening of a JSON array.
     */
    BEGIN_ARRAY,

    /**
     * The closing of a JSON array.
     */
    END_ARRAY,

    /**
     * The opening of a JSON object block.
     */
    BEGIN_BLOCK,

    /**
     * The closing of a JSON object block.
     */
    END_BLOCK,

    /**
     * A JSON property name. Within objects, tokens alternate between names and
     * their values.
     */
    NAME,

    /**
     * A JSON string.
     */
    STRING,

    /**
     * A JSON number represented in this API by a Java {@code double}, {@code
     * long}, or {@code int}.
     */
    NUMBER,

    /**
     * A JSON {@code true} or {@code false}.
     */
    BOOLEAN,

    /**
     * A JSON {@code null}.
     */
    NULL,

    /**
     * The end of the JSON stream. This sentinel value is returned by {@link
     * JsonReader#peek()} to signal that the JSON-encoded value has no more
     * tokens.
     */
    END_DOCUMENT

}
