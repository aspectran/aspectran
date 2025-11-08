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
import com.aspectran.utils.json.MalformedJsonException;

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

    private final boolean lenient;

    /**
     * Create a converter that produces a default {@link VariableParameters} container.
     */
    public JsonToParameters() {
        this(null, false);
    }

    /**
     * Create a converter that will instantiate the given {@code requiredType}
     * for the target {@link Parameters} container.
     * @param requiredType the concrete Parameters implementation to instantiate (not null)
     * @throws IllegalArgumentException if {@code requiredType} is null
     */
    public JsonToParameters(final Class<? extends Parameters> requiredType) {
        this(requiredType, false);
    }

    /**
     * Create a converter that will instantiate the given {@code requiredType}
     * for the target {@link Parameters} container.
     * @param requiredType the concrete Parameters implementation to instantiate
     * @param lenient {@code true} to enable lenient parsing, {@code false} for strict parsing
     */
    public JsonToParameters(final Class<? extends Parameters> requiredType, boolean lenient) {
        this.requiredType = requiredType;
        this.lenient = lenient;
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
            if (lenient) {
                jsonReader.setLenient(true);
            }
            read(jsonReader, container, name, false);
        } catch (MalformedJsonException e) {
            throw new MalformedAponException("Failed to convert JSON to APON", e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new AponParseException("Failed to convert JSON to APON", e);
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

    private void read(@NonNull JsonReader reader, Parameters container, String name, boolean array) throws IOException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                reader.beginObject();
                if (name != null) {
                    Parameters parameters = container.newParameters(name);
                    if (array) {
                        Parameter parameter = container.getParameter(name);
                        if (!parameter.isArray()) {
                            parameter.arraylize();
                        }
                    }
                    container = parameters;
                }
                while (reader.hasNext()) {
                    read(reader, container, reader.nextName(), false);
                }
                reader.endObject();
                return;
            case BEGIN_ARRAY:
                reader.beginArray();
                if (reader.hasNext()) {
                    do {
                        read(reader, container, name, !container.isStructureFixed());
                    } while (reader.hasNext());
                } else {
                    container.newParameterValue(name, ValueType.VARIABLE, true);
                }
                reader.endArray();
                return;
            case STRING:
                Parameter parameter = container.getParameter(name);
                if (parameter == null) {
                    parameter = container.newParameterValue(name, ValueType.STRING, array);
                }
                parameter.putValue(reader.nextString());
                return;
            case BOOLEAN:
                touchParameter(container, name, ValueType.BOOLEAN, array).putValue(reader.nextBoolean());
                return;
            case NUMBER:
                Parameter param = container.getParameter(name);
                if (param != null && param.getValueType() != ValueType.VARIABLE) {
                    ValueType valueType = param.getValueType();
                    if (valueType == ValueType.FLOAT) {
                        param.putValue(Float.parseFloat(reader.nextString()));
                        return;
                    } else if (valueType == ValueType.INT) {
                        param.putValue(reader.nextInt());
                        return;
                    } else if (valueType == ValueType.LONG) {
                        param.putValue(reader.nextLong());
                        return;
                    } else if (valueType == ValueType.DOUBLE) {
                        param.putValue(reader.nextDouble());
                        return;
                    }
                }
                
                // Fallback for VariableParameters or if type is not specified
                ValueType valueType;
                Object number;
                try {
                    number = reader.nextInt();
                    valueType = ValueType.INT;
                } catch (NumberFormatException e0) {
                    try {
                        number = reader.nextLong();
                        valueType = ValueType.LONG;
                    } catch (NumberFormatException e1) {
                        number = reader.nextDouble();
                        valueType = ValueType.DOUBLE;
                    }
                }
                touchParameter(container, name, valueType, array).putValue(number);
                return;
            case NULL:
                reader.nextNull();
                touchParameter(container, name, ValueType.VARIABLE, array).putValue(null);
                return;
            default:
                throw new MalformedJsonException("Unexpected token: " + reader.peek());
        }
    }

    @NonNull
    private Parameter touchParameter(@NonNull Parameters container, String name, ValueType valueType, boolean array) {
        Parameter parameter = container.getParameter(name);
        if (parameter != null) {
            return parameter;
        } else {
            return container.newParameterValue(name, valueType, array);
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
     * Convenience factory to parse JSON text into a new {@link VariableParameters} container.
     * @param json the JSON content
     * @param lenient {@code true} to enable lenient parsing, {@code false} for strict parsing
     * @return a populated Parameters instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static Parameters from(String json, boolean lenient) throws IOException {
        return new JsonToParameters(null, lenient).read(json);
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
     * Convenience factory to parse JSON text into a new container of the given type.
     * @param <T> the container type
     * @param json the JSON content
     * @param requiredType the concrete Parameters implementation to instantiate
     * @param lenient {@code true} to enable lenient parsing, {@code false} for strict parsing
     * @return a populated container instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(String json, Class<? extends Parameters> requiredType, boolean lenient)
            throws IOException {
        return new JsonToParameters(requiredType, lenient).read(json);
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
     * Convenience factory to parse JSON content from a reader into a new {@link VariableParameters} container.
     * @param reader the JSON reader
     * @param lenient {@code true} to enable lenient parsing, {@code false} for strict parsing
     * @return a populated Parameters instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static Parameters from(Reader reader, boolean lenient) throws IOException {
        return new JsonToParameters(null, lenient).read(reader);
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

    /**
     * Convenience factory to parse JSON content from a reader into a new container of the given type.
     * @param <T> the container type
     * @param reader the JSON reader
     * @param requiredType the concrete Parameters implementation to instantiate
     * @param lenient {@code true} to enable lenient parsing, {@code false} for strict parsing
     * @return a populated container instance
     * @throws IOException if reading or conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(Reader reader, Class<? extends Parameters> requiredType, boolean lenient)
            throws IOException {
        return new JsonToParameters(requiredType, lenient).read(reader);
    }

}
