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
package com.aspectran.utils.apon;

import com.aspectran.utils.Assert;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.json.JsonReader;
import com.aspectran.utils.json.JsonReaderCloseable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Utility that converts JSON into {@link Parameters}.
 * <p>
 * Accepts JSON from {@link String} or {@link Reader} and produces either a
 * default {@link VariableParameters} container or a caller-provided/typed container.
 * The conversion maps JSON objects to APON parameter blocks and arrays to
 * APON arrays, preserving primitive types where possible.
 * </p>
 *
 * @since 6.2.0
 */
public class JsonToParameters {

    private final Class<? extends Parameters> requiredType;

    /**
     * Create a converter that produces a default {@link VariableParameters} container.
     */
    public JsonToParameters() {
        this.requiredType = null;
    }

    /**
     * Create a converter that will instantiate the given {@code requiredType}
     * for the target {@link Parameters} container.
     * @param requiredType the concrete Parameters implementation to instantiate (not null)
     * @throws IllegalArgumentException if {@code requiredType} is null
     */
    public JsonToParameters(final Class<? extends Parameters> requiredType) {
        Assert.notNull(requiredType, "requiredType must not be null");
        this.requiredType = requiredType;
    }

    /**
     * Parse a JSON {@link String} into a newly created {@link Parameters} container.
     * @param <T> the container type
     * @param json the JSON text (not null)
     * @return the populated container
     * @throws IOException if reading or conversion fails
     */
    public <T extends Parameters> T read(String json) throws IOException {
        T container = createContainer();
        return read(json, container);
    }

    /**
     * Parse a JSON {@link String} into the supplied {@code container}.
     * @param <T> the container type
     * @param json the JSON text (not null)
     * @param container the target container to populate (not null)
     * @return the same {@code container} instance for chaining
     * @throws IOException if reading or conversion fails
     */
    public <T extends Parameters> T read(String json, T container) throws IOException {
        Assert.notNull(json, "json must not be null");
        read(new StringReader(json), container);
        return container;
    }

    /**
     * Parse JSON from a {@link Reader} into a newly created {@link Parameters} container.
     * @param <T> the container type
     * @param reader the character stream with JSON content (not null)
     * @return the populated container
     * @throws IOException if reading or conversion fails
     */
    public <T extends Parameters> T read(Reader reader) throws IOException {
        T container = createContainer();
        return read(reader, container);
    }

    /**
     * Parse JSON from a {@link Reader} into the supplied container.
     * @param <T> the container type
     * @param reader the character stream with JSON content (not null)
     * @param container the target container to populate (not null)
     * @return the same {@code container} instance
     * @throws IOException if reading or conversion fails
     */
    public <T extends Parameters> T read(Reader reader, T container) throws IOException {
        Assert.notNull(reader, "reader must not be null");
        Assert.notNull(container, "container must not be null");
        String name = (container instanceof ArrayParameters ? ArrayParameters.NONAME : null);
        try (JsonReaderCloseable jsonReader = new JsonReaderCloseable(reader)) {
            read(jsonReader, container, name);
        } catch (Exception e) {
            throw new IOException("Failed to convert JSON to APON", e);
        }
        return container;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T extends Parameters> T createContainer() {
        Parameters container;
        if (requiredType != null) {
            container = ClassUtils.createInstance(requiredType);
        } else {
            container = new VariableParameters();
        }
        return (T)container;
    }

    private void read(@NonNull JsonReader reader, Parameters container, String name) throws IOException {
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

    /**
     * Convenience factory to parse JSON text into a new {@link VariableParameters} container.
     * @param json the JSON content
     * @return a populated Parameters instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static Parameters from(String json) throws IOException {
        return new JsonToParameters().read(json);
    }

    /**
     * Convenience factory to parse JSON text into a new container of the given type.
     * @param <T> the container type
     * @param json the JSON content
     * @param requiredType the concrete Parameters implementation to instantiate
     * @return a populated container instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(String json, Class<? extends Parameters> requiredType)
            throws IOException {
        return new JsonToParameters(requiredType).read(json);
    }

    /**
     * Convenience factory to parse JSON content from a reader into a new {@link VariableParameters} container.
     * @param reader the JSON reader
     * @return a populated Parameters instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static Parameters from(Reader reader) throws IOException {
        return new JsonToParameters().read(reader);
    }

    /**
     * Convenience factory to parse JSON content from a reader into a new container of the given type.
     * @param <T> the container type
     * @param reader the JSON reader
     * @param requiredType the concrete Parameters implementation to instantiate
     * @return a populated container instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(Reader reader, Class<? extends Parameters> requiredType)
            throws IOException {
        return new JsonToParameters(requiredType).read(reader);
    }

}
