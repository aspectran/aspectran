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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple JSON parser that converts a JSON string into a Java object
 * (a hierarchy of Maps and Lists).
 *
 * <p>Created: 2025-10-14</p>
 */
public class JsonParser {

    /**
     * Parses a JSON string into a Java object.
     * @param json the JSON string to parse
     * @return a Java object (Map, List, String, Number, Boolean, or null)
     * @throws IOException if an I/O error occurs during parsing
     */
    public static Object parse(String json) throws IOException {
        return parse(json, false);
    }

    /**
     * Parses a JSON string into a Java object.
     * @param json the JSON string to parse
     * @param lenient {@code true} to enable lenient parsing, {@code false} for strict parsing
     * @return a Java object (Map, List, String, Number, Boolean, or null)
     * @throws IOException if an I/O error occurs during parsing
     */
    public static Object parse(String json, boolean lenient) throws IOException {
        if (json == null) {
            return null;
        }
        JsonReader reader = new JsonReader(json);
        reader.setLenient(lenient);
        return readObject(reader);
    }

    @Nullable
    private static Object readObject(@NonNull JsonReader reader) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                Map<String, Object> map = new LinkedHashMap<>();
                reader.beginObject();
                while (reader.hasNext()) {
                    map.put(reader.nextName(), readObject(reader));
                }
                reader.endObject();
                return map;
            case BEGIN_ARRAY:
                List<Object> list = new ArrayList<>();
                reader.beginArray();
                while (reader.hasNext()) {
                    list.add(readObject(reader));
                }
                reader.endArray();
                return list;
            case STRING:
                return reader.nextString();
            case NUMBER:
                try {
                    return reader.nextInt();
                } catch (NumberFormatException e0) {
                    try {
                        return reader.nextLong();
                    } catch (NumberFormatException e1) {
                        return reader.nextDouble();
                    }
                }
            case BOOLEAN:
                return reader.nextBoolean();
            case NULL:
                reader.nextNull();
                return null;
            default:
                throw new MalformedJsonException("Unexpected token: " + reader.peek());
        }
    }

}
