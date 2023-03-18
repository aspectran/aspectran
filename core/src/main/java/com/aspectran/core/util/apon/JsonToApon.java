/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.util.apon;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.json.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Converts JSON to APON.
 *
 * @since 6.2.0
 */
public class JsonToApon {

    public static Parameters from(String json) throws IOException {
        return from(json, new VariableParameters());
    }

    public static <T extends Parameters> T from(String json, Class<T> requiredType) throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(json, container);
        return container;
    }

    public static <T extends Parameters> T from(String json, T container) throws IOException {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }
        return from(new StringReader(json), container);
    }

    public static Parameters from(Reader reader) throws IOException {
        return from(reader, new VariableParameters());
    }

    public static <T extends Parameters> T from(Reader reader, Class<T> requiredType) throws IOException {
        T container = ClassUtils.createInstance(requiredType);
        from(reader, container);
        return container;
    }

    public static <T extends Parameters> T from(Reader reader, T container) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        if (container == null) {
            throw new IllegalArgumentException("container must not be null");
        }

        try {
            JsonReader jsonReader = new JsonReader(reader);
            String name = (container instanceof ArrayParameters ? ArrayParameters.NONAME : null);
            read(jsonReader, container, name);
        } catch (Exception e) {
            throw new IOException("Failed to convert JSON to APON", e);
        }

        return container;
    }

    private static void read(JsonReader reader, Parameters container, String name) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                reader.beginObject();
                if (name != null) {
                    container = container.newParameters(name);
                }
                while (reader.hasNext()) {
                    read(reader, container, reader.nextName());
                }
                reader.endObject();
                return;
            case BEGIN_ARRAY:
                reader.beginArray();
                while (reader.hasNext()) {
                    read(reader, container, name);
                }
                reader.endArray();
                return;
            case STRING:
                container.putValue(name, reader.nextString());
                return;
            case BOOLEAN:
                container.putValue(name, reader.nextBoolean());
                return;
            case NUMBER:
                try {
                    container.putValue(name, reader.nextInt());
                } catch (NumberFormatException e0) {
                    try {
                        container.putValue(name, reader.nextLong());
                    } catch (NumberFormatException e1) {
                        container.putValue(name, reader.nextDouble());
                    }
                }
                return;
            case NULL:
                reader.nextNull();
                Parameter parameter = container.getParameter(name);
                if (parameter == null || parameter.getValueType() != ValueType.PARAMETERS) {
                    container.putValue(name, null);
                }
                return;
            default:
                throw new IllegalStateException();
        }
    }

}
