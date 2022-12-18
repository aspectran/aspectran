/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.util.BeanUtils;

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
            if (o instanceof Parameters) {
                container.putAll((Parameters)o);
            }
        } else if (value == null) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            container.putValue(name, null);
        } else if (value instanceof Collection<?>) {
            if (container.hasParameter(name)) {
                container.removeValue(name);
            }
            for (Object o : ((Collection<?>)value)) {
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
        } else if (object instanceof Map<?, ?>) {
            Parameters ps = new VariableParameters();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet()) {
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                checkCircularReference(object, value);
                putValue(ps, name, value);
            }
            return ps;
        } else if (object instanceof Collection<?> ||
                object.getClass().isArray()) {
            return object.toString();
        } else if (object instanceof Date) {
            if (dateTimeFormat != null) {
                SimpleDateFormat dt = new SimpleDateFormat(dateTimeFormat);
                return dt.format((Date)object);
            } else {
                return object.toString();
            }
        } else if (object instanceof LocalDate) {
            if (dateFormat != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                return ((LocalDate)object).format(formatter);
            } else {
                return object.toString();
            }
        } else if (object instanceof LocalDateTime) {
            if (dateTimeFormat != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
                return ((LocalDateTime)object).format(formatter);
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

    private void checkCircularReference(Object wrapper, Object member) {
        if (wrapper.equals(member)) {
            throw new InvalidParameterValueException("APON Serialization Failure: A circular reference was detected " +
                    "while converting a member object [" + member + "] in [" + wrapper + "]");
        }
    }

}
