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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.ParameterValue;
import com.aspectran.utils.apon.Parameters;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 * This class enables a good and consistent toString() to be built for any class or object.
 *
 * @author Juho Jeong
 * @since 2016. 2. 11.
 */
public class ToStringBuilder {

    private final StringBuilder buffer;

    private boolean braced = false;

    private int start = -1;

    public ToStringBuilder() {
        this(null);
    }

    public ToStringBuilder(String name) {
        this(name, 64);
    }

    public ToStringBuilder(int capacity) {
        this(null, capacity);
    }

    public ToStringBuilder(String name, int capacity) {
        this.buffer = new StringBuilder(capacity);
        labeling(name, true);
    }

    public ToStringBuilder(Object value) {
        this(null, value);
    }

    public ToStringBuilder(String name, Object value) {
        this.buffer = new StringBuilder(128);
        if (name != null) {
            labeling(name, false);
        }
        if (value != null) {
            append(value);
        }
    }

    private void labeling(String name, boolean braced) {
        if (name != null) {
            buffer.append(name).append(" ");
        }
        if (braced) {
            buffer.append("{");
        }
        this.braced = braced;
        this.start = buffer.length();
    }

    public ToStringBuilder append(String name, Object value) {
        if (value != null) {
            appendName(name);
            append(value);
        }
        return this;
    }

    public ToStringBuilder append(String name, Class<?> clazz) {
        if (clazz != null) {
            appendName(name);
            append(clazz.getTypeName());
        }
        return this;
    }

    public ToStringBuilder append(String name, Method method) {
        if (method != null) {
            appendName(name);
            append(method);
        }
        return this;
    }

    public ToStringBuilder appendForce(String name, Object value) {
        appendName(name);
        append(value);
        return this;
    }

    public ToStringBuilder append(String name, boolean value) {
        if (value) {
            appendName(name);
            buffer.append(true);
        }
        return this;
    }

    public ToStringBuilder appendForce(String name, boolean value) {
        appendName(name);
        buffer.append(value);
        return this;
    }

    public ToStringBuilder appendEqual(String name, Object value, Object compare) {
        if (value != null && value.equals(compare)) {
            appendName(name);
            append(value);
        }
        return this;
    }

    public ToStringBuilder appendNotEqual(String name, Object value, Object compare) {
        if (value != null && !value.equals(compare)) {
            appendName(name);
            append(value);
        }
        return this;
    }

    public ToStringBuilder appendSize(String name, Object object) {
        if (object != null) {
            appendName(name);
            if (object instanceof Map<?, ?> map) {
                buffer.append(map.size());
            } else if (object instanceof Collection<?> collection) {
                buffer.append(collection.size());
            } else if (object.getClass().isArray()) {
                buffer.append(Array.getLength(object));
            } else if (object instanceof CharSequence charSequence) {
                buffer.append(charSequence.length());
            }
        }
        return this;
    }

    private void appendName(Object name) {
        appendName(name, start);
    }

    private void appendName(Object name, int index) {
        if (buffer.length() > index) {
            appendComma();
        }
        buffer.append(name).append("=");
    }

    private void appendComma() {
        buffer.append(", ");
    }

    private void append(Object object) {
        if (object instanceof CharSequence charSequence) {
            buffer.append(charSequence);
        } else if (object instanceof Map<?, ?> map) {
            append(map);
        } else if (object instanceof Collection<?> collection) {
            append(collection);
        } else if (object instanceof Enumeration<?> enumeration) {
            append(enumeration);
        } else if (object instanceof Parameters parameters) {
            append(parameters);
        } else if (object.getClass().isArray()) {
            appendArray(object);
        } else {
            buffer.append(object);
        }
    }

    private void append(@NonNull Map<?, ?> map) {
        buffer.append("{");
        int index = buffer.length();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                checkCircularReference(map, value);
                appendName(key, index);
                append(value);
            }
        }
        buffer.append("}");
    }

    private void append(@NonNull Collection<?> list) {
        buffer.append("[");
        int index = buffer.length();
        for (Object value : list) {
            checkCircularReference(list, value);
            if (buffer.length() > index) {
                appendComma();
            }
            append(value);
        }
        buffer.append("]");
    }

    private void append(@NonNull Enumeration<?> enumeration) {
        buffer.append("[");
        while (enumeration.hasMoreElements()) {
            Object value = enumeration.nextElement();
            checkCircularReference(enumeration, value);
            append(value);
            if (enumeration.hasMoreElements()) {
                appendComma();
            }
        }
        buffer.append("]");
    }

    private void append(@NonNull Parameters parameters) {
        buffer.append("{");
        int index = buffer.length();
        Map<String, ParameterValue> params = parameters.getParameterValueMap();
        for (Parameter p : params.values()) {
            String name = p.getName();
            Object value = p.getValue();
            if (value != null) {
                checkCircularReference(parameters, value);
                appendName(name, index);
                append(value);
            }
        }
        buffer.append("}");
    }

    private void appendArray(Object object) {
        buffer.append("[");
        int len = Array.getLength(object);
        for (int i = 0; i < len; i++) {
            Object value = Array.get(object, i);
            checkCircularReference(object, value);
            if (i > 0) {
                appendComma();
            }
            append(value);
        }
        buffer.append("]");
    }

    private void append(@NonNull Method method) {
        buffer.append(method.getDeclaringClass().getTypeName());
        buffer.append('.');
        buffer.append(method.getName());
        buffer.append('(');
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                appendComma();
            }
            buffer.append(types[i].getTypeName());
        }
        buffer.append(')');
    }

    @Override
    public String toString() {
        if (braced) {
            return buffer + "}";
        } else {
            return buffer.toString();
        }
    }

    private static void checkCircularReference(@NonNull Object wrapper, Object member) {
        if (wrapper == member) {
            throw new IllegalArgumentException("Serialization Failure: Circular reference was detected " +
                "while serializing object " + ObjectUtils.identityToString(wrapper) + " " + wrapper);
        }
    }

    public static String toString(Object object) {
        return toString(null, object);
    }

    public static String toString(String name, Object object) {
        return new ToStringBuilder(name, object).toString();
    }

}
