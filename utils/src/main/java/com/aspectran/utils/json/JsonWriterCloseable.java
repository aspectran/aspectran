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

import java.io.Closeable;
import java.io.Writer;

/**
 * A {@link JsonWriter} subclass that implements the {@link Closeable} interface.
 * <p>This allows {@code JsonWriterCloseable} instances to be used in a
 * try-with-resources statement, ensuring that the underlying {@link Writer} is
 * automatically closed when the block is exited.</p>
 * <p>Example usage:
 * <pre>{@code
 *   try(JsonWriterCloseable jsonWriter = new JsonWriterCloseable(new StringWriter())) {
 *     jsonWriter.beginObject().name("key").value("value").endObject();
 *     String json = jsonWriter.toString();
 *   }
 * }</pre></p>
 *
 * <p>Created: 2008. 06. 12 PM 8:20:54</p>
 *
 * @author Juho Jeong
 */
public class JsonWriterCloseable extends JsonWriter implements Closeable {

    /**
     * Creates a new JsonWriterCloseable that writes to the given {@link Writer}.
     * @param writer the character-output stream
     */
    public JsonWriterCloseable(Writer writer) {
        super(writer);
    }

}
