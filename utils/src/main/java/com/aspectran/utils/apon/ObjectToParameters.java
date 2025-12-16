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
import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringifyContext;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility that converts arbitrary Java objects to {@link Parameters}.
 * <p>
 * Supports {@link Map}, JavaBean-style objects (via reflection), collections,
 * arrays, and common primitives/wrappers. Dates and time types can be formatted
 * through an optional {@link com.aspectran.utils.StringifyContext}.
 * </p>
 */
public class ObjectToParameters {

    private final Class<? extends Parameters> requiredType;

    private StringifyContext stringifyContext;

    /**
     * Create a converter that produces a default {@link VariableParameters} container.
     */
    public ObjectToParameters() {
        this.requiredType = null;
    }

    /**
     * Create a converter that will instantiate the given {@code requiredType}
     * for the target {@link Parameters} container.
     * @param requiredType the concrete Parameters implementation to instantiate (not null)
     * @throws IllegalArgumentException if {@code requiredType} is null
     */
    public ObjectToParameters(Class<? extends Parameters> requiredType) {
        Assert.notNull(requiredType, "requiredType must not be null");
        this.requiredType = requiredType;
    }

    /**
     * Set an optional {@link StringifyContext} used to format date/time and other values
     * during conversion.
     * @param stringifyContext the formatting context to apply (may be null)
     */
    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
    }

    /**
     * Fluent variant of {@link #setStringifyContext(StringifyContext)}.
     * @param stringifyContext the formatting context
     * @return this converter instance for chaining
     */
    public ObjectToParameters apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return this;
    }

    /**
     * Convert the given object into a new {@link Parameters} container.
     * For {@link java.util.Map} inputs, entries are copied as-is; for beans, readable
     * properties are reflected and added.
     * @param <T> the container type
     * @param object the source object (not null)
     * @return a populated container instance
     */
    public <T extends Parameters> T read(Object object) {
        return createContainer(object);
    }

    /**
     * Convert the given object into a new {@link Parameters} container and place it under
     * the specified parameter {@code name} inside the new container.
     * @param <T> the container type
     * @param name the parameter name to assign
     * @param object the source object (not null)
     * @return the populated container
     */
    public <T extends Parameters> T read(String name, Object object) {
        T container = createContainer();
        return read(name, object, container);
    }

    /**
     * Convert the given object and put the value under {@code name} in the provided
     * {@code container}.
     * @param <T> the container type
     * @param name the parameter name (not null)
     * @param object the source object (not null)
     * @param container the target container (not null)
     * @return the same {@code container} instance for chaining
     */
    public <T extends Parameters> T read(String name, Object object, T container) {
        Assert.notNull(name, "name must not be null");
        Assert.notNull(object, "object must not be null");
        Assert.notNull(container, "container must not be null");
        putValue(container, name, object);
        return container;
    }

    /**
     * Create a new container instance of {@code requiredType} if provided,
     * otherwise a {@link VariableParameters}.
     * @param <T> the container type
     * @return a new container instance
     */
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

    /**
     * Create a container suitable for the given source object and populate it
     * if the object is a {@link Map}.
     * @param <T> the container type
     * @param object the source object (not null)
     * @return a new container, possibly pre-populated
     */
    @NonNull
    protected <T extends Parameters> T createContainer(Object object) {
        Assert.notNull(object, "object must not be null");
        if (object instanceof Map<?, ?> map) {
            T ps = createContainer();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(map, value);
                putValue(ps, name, value);
            }
            return ps;
        } else {
            return createContainer();
        }
    }

    /**
     * Put a value into the {@code container} under {@code name}, honoring
     * null-handling settings.
     * @param container the target Parameters (not null)
     * @param name the parameter name (not null)
     * @param value the value to put (may be null)
     */
    protected void putValue(@NonNull Parameters container, @NonNull String name, Object value) {
        if (isNullWritable()) {
            container.putValue(name, normalize(value));
        } else {
            container.putValueIfNotNull(name, normalize(value));
        }
    }

    /**
     * Merge the given value into the target container if it is itself a
     * Parameters instance.
     * @param container the target Parameters (not null)
     * @param value the value to merge
     */
    protected void putValue(@NonNull Parameters container, Object value) {
        Object obj = normalize(value);
        if (obj instanceof Parameters parameters) {
            container.mergeParameterValues(parameters);
        }
    }

    /**
     * Normalize the given source value into a representation storable in {@link Parameters}:
     * Maps become nested Parameters, collections and arrays are kept as-is, common primitives
     * and wrappers are passed through, date/time types can be formatted via {@link StringifyContext},
     * and arbitrary beans are reflected into a Parameters structure.
     * @param object the source value (may be null)
     * @return a normalized value suitable for storage
     */
    private Object normalize(Object object) {
        if (object == null ||
                object instanceof Parameters ||
                object instanceof String ||
                object instanceof Number ||
                object instanceof Boolean ||
                object instanceof Character ||
                object instanceof Collection<?> ||
                object instanceof Iterator<?> ||
                object instanceof Enumeration<?> ||
                object.getClass().isArray()) {
            return object;
        } else if (object instanceof Map<?, ?> map) {
            Parameters ps = new VariableParameters();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(map, value);
                putValue(ps, name, value);
            }
            return ps;
        } else if (object instanceof LocalDateTime localDateTime) {
            if (stringifyContext != null) {
                return stringifyContext.toString(localDateTime);
            } else {
                return localDateTime.toString();
            }
        } else if (object instanceof LocalDate localDate) {
            if (stringifyContext != null) {
                return stringifyContext.toString(localDate);
            } else {
                return localDate.toString();
            }
        } else if (object instanceof LocalTime localTime) {
            if (stringifyContext != null) {
                return stringifyContext.toString(localTime);
            } else {
                return localTime.toString();
            }
        } else if (object instanceof Date date) {
            if (stringifyContext != null) {
                return stringifyContext.toString(date);
            } else {
                return date.toString();
            }
        } else {
            String[] readablePropertyNames = BeanUtils.getReadablePropertyNamesWithoutNonSerializable(object);
            if (readablePropertyNames != null && readablePropertyNames.length > 0) {
                Parameters ps = new VariableParameters();
                for (String name : readablePropertyNames) {
                    Object value;
                    try {
                        value = BeanUtils.getProperty(object, name);
                    } catch (InvocationTargetException e) {
                        throw new InvalidParameterValueException(e);
                    }
                    checkCircularReference(object, value);
                    putValue(ps, name, value);
                }
                return ps;
            } else {
                return object.toString();
            }
        }
    }

    /**
     * Whether null values should be included when writing into the container.
     * @return true if nulls are allowed to be written; false to skip nulls
     */
    private boolean isNullWritable() {
        return (stringifyContext == null || stringifyContext.isNullWritable());
    }

    /**
     * Detect direct self-references to avoid infinite recursion when converting nested structures.
     * @param wrapper the enclosing object
     * @param member the member about to be added
     * @throws InvalidParameterValueException if a circular reference is detected
     */
    private void checkCircularReference(@NonNull Object wrapper, Object member) {
        if (wrapper == member) {
            throw new IllegalArgumentException("Serialization Failure: Circular reference was detected " +
                    "while serializing object " + ObjectUtils.identityToString(wrapper) + " " + wrapper);
        }
    }

    /**
     * Convenience factory to convert an object into a new {@link VariableParameters} container.
     * @param object the source object to convert
     * @return a populated Parameters instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static Parameters from(Object object) throws IOException {
        return new ObjectToParameters().read(object);
    }

    /**
     * Convenience factory to convert an object using the given {@link StringifyContext}.
     * @param object the source object to convert
     * @param stringifyContext formatting rules for date/time and others
     * @return a populated Parameters instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static Parameters from(Object object, StringifyContext stringifyContext) throws IOException {
        return new ObjectToParameters().apply(stringifyContext).read(object);
    }

    /**
     * Convenience factory to convert an object into a new container of the given type.
     * @param <T> the container type to create
     * @param object the source object to convert
     * @param requiredType the concrete Parameters implementation to instantiate
     * @return a populated container instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(Object object, Class<? extends Parameters> requiredType)
            throws IOException {
        return new ObjectToParameters(requiredType).read(object);
    }

    /**
     * Convenience factory to convert an object into a typed container with a {@link StringifyContext}.
     * @param <T> the container type to create
     * @param object the source object to convert
     * @param requiredType the concrete Parameters implementation to instantiate
     * @param stringifyContext formatting rules for date/time and others
     * @return a populated container instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(
            Object object, Class<? extends Parameters> requiredType, StringifyContext stringifyContext)
            throws IOException {
        return new ObjectToParameters(requiredType).apply(stringifyContext).read(object);
    }

    /**
     * Convenience factory to convert a named object into a new {@link VariableParameters} container.
     * The value is placed under the given parameter name.
     * @param name the parameter name to use
     * @param object the source object
     * @return a populated Parameters instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static Parameters from(String name, Object object) throws IOException {
        return new ObjectToParameters().read(name, object);
    }

    /**
     * Convenience factory to convert a named object into a new container using the given context.
     * @param name the parameter name to use
     * @param object the source object
     * @param stringifyContext formatting rules for date/time and others
     * @return a populated Parameters instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static Parameters from(String name, Object object, StringifyContext stringifyContext) throws IOException {
        return new ObjectToParameters().apply(stringifyContext).read(name, object);
    }

    /**
     * Convenience factory to convert a named object into a new typed container.
     * @param <T> the container type to create
     * @param name the parameter name to use
     * @param object the source object
     * @param requiredType the concrete Parameters implementation to instantiate
     * @return a populated container instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(
            String name, Object object, Class<? extends Parameters> requiredType) throws IOException {
        return new ObjectToParameters(requiredType).read(name, object);
    }

    /**
     * Convenience factory to convert a named object into a new typed container using the given context.
     * @param <T> the container type to create
     * @param name the parameter name to use
     * @param object the source object
     * @param requiredType the concrete Parameters implementation to instantiate
     * @param stringifyContext formatting rules for date/time and others
     * @return a populated container instance
     * @throws IOException if conversion fails
     */
    @NonNull
    public static <T extends Parameters> T from(
            String name, Object object, Class<? extends Parameters> requiredType, StringifyContext stringifyContext)
            throws IOException {
        return new ObjectToParameters(requiredType).apply(stringifyContext).read(name, object);
    }

}
