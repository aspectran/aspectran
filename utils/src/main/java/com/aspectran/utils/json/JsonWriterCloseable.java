/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import java.io.Closeable;
import java.io.Writer;

/**
 * Converts an object to a JSON formatted string.
 * <p>If pretty-printing is enabled, the JsonWriter will add newlines and
 * indentation to the written data. Pretty-printing is disabled by default.</p>
 * <p>Useful with Java 7 for example :
 * <pre>{@code
 *   try(JsonWriterCloseable jsonWriter = JsonWriterCloseable(out)) {
 *     ....
 *   }
 * }</pre></p>
 *
 * <p>Created: 2008. 06. 12 PM 8:20:54</p>
 *
 * @author Juho Jeong
 */
public class JsonWriterCloseable extends JsonWriter implements Closeable {

    /**
     * Instantiates a new JsonWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     * @param writer the character-output stream
     */
    public JsonWriterCloseable(Writer writer) {
        super(writer);
    }

}
