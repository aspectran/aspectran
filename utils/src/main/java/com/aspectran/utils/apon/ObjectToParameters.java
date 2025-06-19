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
import com.aspectran.utils.annotation.jsr305.NonNull;

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
 * Converts Object to APON.
 *
 * <p>Created: 2015. 03. 16 PM 11:14:29</p>
 */
public class ObjectToParameters {

    private final Class<? extends Parameters> requiredType;

    private StringifyContext stringifyContext;

    public ObjectToParameters() {
        this.requiredType = null;
    }

    public ObjectToParameters(final Class<? extends Parameters> requiredType) {
        Assert.notNull(requiredType, "requiredType must not be null");
        this.requiredType = requiredType;
    }

    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
    }

    public ObjectToParameters apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return this;
    }

    public <T extends Parameters> T read(Object object) {
        return createContainer(object);
    }

    public <T extends Parameters> T read(String name, Object object) {
        T container = createContainer();
        return read(name, object, container);
    }

    public <T extends Parameters> T read(String name, Object object, T container) {
        Assert.notNull(name, "name must not be null");
        Assert.notNull(object, "object must not be null");
        Assert.notNull(container, "container must not be null");
        putValue(container, name, object);
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

    protected void putValue(@NonNull Parameters container, @NonNull String name, Object value) {
        if (isNullWritable()) {
            container.putValue(name, normalize(value));
        } else {
            container.putValueIfNotNull(name, normalize(value));
        }
    }

    protected void putValue(@NonNull Parameters container, Object value) {
        Object obj = normalize(value);
        if (obj instanceof Parameters parameters) {
            container.putAll(parameters);
        }
    }

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

    private boolean isNullWritable() {
        return (stringifyContext == null || stringifyContext.isNullWritable());
    }

    private void checkCircularReference(@NonNull Object wrapper, Object member) {
        if (wrapper == member) {
            throw new IllegalArgumentException("Serialization Failure: Circular reference was detected " +
                    "while serializing object " + ObjectUtils.identityToString(wrapper) + " " + wrapper);
        }
    }

    @NonNull
    public static Parameters from(Object object) throws IOException {
        return new ObjectToParameters().read(object);
    }

    @NonNull
    public static Parameters from(Object object, StringifyContext stringifyContext) throws IOException {
        return new ObjectToParameters().apply(stringifyContext).read(object);
    }

    @NonNull
    public static <T extends Parameters> T from(Object object, Class<? extends Parameters> requiredType)
            throws IOException {
        return new ObjectToParameters(requiredType).read(object);
    }

    @NonNull
    public static <T extends Parameters> T from(
            Object object, Class<? extends Parameters> requiredType, StringifyContext stringifyContext)
            throws IOException {
        return new ObjectToParameters(requiredType).apply(stringifyContext).read(object);
    }

    @NonNull
    public static Parameters from(String name, Object object) throws IOException {
        return new ObjectToParameters().read(name, object);
    }

    @NonNull
    public static Parameters from(String name, Object object, StringifyContext stringifyContext) throws IOException {
        return new ObjectToParameters().apply(stringifyContext).read(name, object);
    }

    @NonNull
    public static <T extends Parameters> T from(
            String name, Object object, Class<? extends Parameters> requiredType) throws IOException {
        return new ObjectToParameters(requiredType).read(name, object);
    }

    @NonNull
    public static <T extends Parameters> T from(
            String name, Object object, Class<? extends Parameters> requiredType, StringifyContext stringifyContext)
            throws IOException {
        return new ObjectToParameters(requiredType).apply(stringifyContext).read(name, object);
    }

}
