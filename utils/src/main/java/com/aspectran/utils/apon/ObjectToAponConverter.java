/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

/**
 * Converts Object to APON.
 *
 * <p>Created: 2015. 03. 16 PM 11:14:29</p>
 */
public class ObjectToAponConverter {

    private StringifyContext stringifyContext;

    public ObjectToAponConverter() {
    }

    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
    }

    public ObjectToAponConverter apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return this;
    }

    public Parameters toParameters(Object object) {
        Parameters container = new VariableParameters();
        putValue(container, object);
        return container;
    }

    public Parameters toParameters(String name, Object object) {
        Assert.notNull(name, "name must not be null");
        Parameters container = new VariableParameters();
        putValue(container, name, object);
        return container;
    }

    private void putValue(Parameters container, Object value) {
        Object o = valuelize(value);
        if (o instanceof Parameters parameters) {
            container.putAll(parameters);
        }
    }

    protected void putValue(@NonNull Parameters container, @NonNull String name, Object value) {
        if (value == null) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            container.putValue(name, null);
        } else if (value instanceof Collection<?> collection) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            container.putValue(name, collection);
        } else if (value instanceof Enumeration<?> enumeration) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            container.putValue(name, enumeration);
        } else if (value.getClass().isArray()) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            container.putValue(name, value);
        } else {
            container.putValue(name, valuelize(value));
        }
    }

    private Object valuelize(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Parameters ||
                object instanceof String ||
                object instanceof Number ||
                object instanceof Boolean ) {
            return object;
        } else if (object instanceof Character) {
           return String.valueOf(((char)object));
        } else if (object instanceof Map<?, ?> map) {
            Parameters ps = new VariableParameters();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(map, value);
                putValue(ps, name, value);
            }
            return ps;
        } else if (object instanceof Collection<?> || object.getClass().isArray()) {
            return object.toString();
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

    private void checkCircularReference(@NonNull Object wrapper, Object member) {
        if (wrapper == member) {
            throw new IllegalArgumentException("Serialization Failure: Circular reference was detected " +
                    "while serializing object " + ObjectUtils.identityToString(wrapper) + " " + wrapper);
        }
    }

}
