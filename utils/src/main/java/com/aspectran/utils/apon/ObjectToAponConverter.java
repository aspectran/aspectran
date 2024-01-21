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

import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Converts Object to APON.
 *
 * <p>Created: 2015. 03. 16 PM 11:14:29</p>
 */
public class ObjectToAponConverter {

    private String dateFormat;

    private String dateTimeFormat;

    public ObjectToAponConverter() {
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public ObjectToAponConverter dateFormat(String dateFormat) {
        setDateFormat(dateFormat);
        return this;
    }

    public ObjectToAponConverter dateTimeFormat(String dateTimeFormat) {
        setDateTimeFormat(dateTimeFormat);
        return this;
    }

    public Parameters toParameters(Object object) {
        return toParameters(null, object);
    }

    public Parameters toParameters(String name, Object object) {
        Parameters container = new VariableParameters();
        putValue(container, name, object);
        return container;
    }

    public void putValue(Parameters container, Object value) {
        putValue(container, null, value);
    }

    public void putValue(Parameters container, String name, Object value) {
        if (name == null) {
            Object o = valuelize(value);
            if (o instanceof Parameters parameters) {
                container.putAll(parameters);
            }
        } else if (value == null) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            container.putValue(name, null);
        } else if (value instanceof Collection<?> collection) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            for (Object o : collection) {
                if (o != null) {
                    putValue(container, name, o);
                }
            }
            if (!container.hasParameter(name)) {
                container.putValue(name, null);
            }
        } else if (value.getClass().isArray()) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(value, i);
                if (o != null) {
                    putValue(container, name, o);
                }
            }
            if (!container.hasParameter(name)) {
                container.putValue(name, null);
            }
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
        } else if (object instanceof Collection<?> ||
                object.getClass().isArray()) {
            return object.toString();
        } else if (object instanceof Date date) {
            if (dateTimeFormat != null) {
                SimpleDateFormat dt = new SimpleDateFormat(dateTimeFormat);
                return dt.format(date);
            } else {
                return object.toString();
            }
        } else if (object instanceof LocalDate localDate) {
            if (dateFormat != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                return localDate.format(formatter);
            } else {
                return object.toString();
            }
        } else if (object instanceof LocalDateTime localDateTime) {
            if (dateTimeFormat != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
                return localDateTime.format(formatter);
            } else {
                return object.toString();
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
        if (wrapper.equals(member)) {
            throw new InvalidParameterValueException("APON Serialization Failure: A circular reference was detected " +
                    "while converting a member object [" + member + "] in [" + wrapper + "]");
        }
    }

}
