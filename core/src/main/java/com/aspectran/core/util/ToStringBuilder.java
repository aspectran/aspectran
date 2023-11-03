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
package com.aspectran.core.util;

import com.aspectran.core.util.apon.Parameter;
import com.aspectran.core.util.apon.ParameterValue;
import com.aspectran.core.util.apon.Parameters;

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

    private final int start;

    public ToStringBuilder() {
        this(null);
    }

    public ToStringBuilder(String name) {
        this(name, 32);
    }

    public ToStringBuilder(int capacity) {
        this(null, capacity);
    }

    public ToStringBuilder(String name, int capacity) {
        buffer = new StringBuilder(capacity);
        if (name != null) {
            buffer.append(name).append(" ");
            this.start = buffer.length() + 1;
        } else {
            this.start = 1;
        }
        buffer.append("{");
    }

    public ToStringBuilder(String name, Parameters parameters) {
        this(name, 32);
        if (parameters != null) {
            append(parameters);
        }
    }

    public void append(String name, Object value) {
        if (value != null) {
            appendName(name);
            append(value);
        }
    }

    public void append(String name, Class<?> clazz) {
        if (clazz != null) {
            appendName(name);
            append(clazz.getTypeName());
        }
    }

    public void append(String name, Method method) {
        if (method != null) {
            appendName(name);
            append(method);
        }
    }

    public void appendForce(String name, Object value) {
        appendName(name);
        append(value);
    }

    public void append(String name, boolean value) {
        if (value) {
            appendName(name);
            buffer.append(true);
        }
    }

    public void appendForce(String name, boolean value) {
        appendName(name);
        buffer.append(value);
    }

    public void appendEqual(String name, Object value, Object compare) {
        if (value != null && value.equals(compare)) {
            appendName(name);
            append(value);
        }
    }

    public void appendNotEqual(String name, Object value, Object compare) {
        if (value != null && !value.equals(compare)) {
            appendName(name);
            append(value);
        }
    }

    public void appendSize(String name, Object object) {
        if (object != null) {
            appendName(name);
            if (object instanceof Map<?, ?>) {
                buffer.append(((Map<?, ?>)object).size());
            } else if (object instanceof Collection<?>) {
                buffer.append(((Collection<?>)object).size());
            } else if (object.getClass().isArray()) {
                buffer.append(Array.getLength(object));
            } else if (object instanceof CharSequence) {
                buffer.append(((CharSequence)object).length());
            }
        }
    }

    private void appendName(Object name) {
        appendName(name, this.start);
    }

    private void appendName(Object name, int start) {
        if (buffer.length() > start) {
            appendComma();
        }
        buffer.append(name).append("=");
    }

    private void appendComma() {
        buffer.append(", ");
    }

    private void append(Object object) {
        if (object == null) {
            buffer.append((Object)null);
        } else if (object instanceof CharSequence) {
            buffer.append(((CharSequence)object));
        } else if (object instanceof Map<?, ?>) {
            append((Map<?, ?>)object);
        } else if (object instanceof Collection<?>) {
            append((Collection<?>)object);
        } else if (object instanceof Enumeration<?>) {
            append((Enumeration<?>)object);
        } else if (object.getClass().isArray()) {
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
        } else if (object instanceof Parameters) {
            buffer.append("{");
            append((Parameters)object);
            buffer.append("}");
        } else if (object instanceof ToStringBuilder) {
            buffer.append(((ToStringBuilder)object).getBuffer());
        } else {
            buffer.append(object);
        }
    }

    private void append(Map<?, ?> map) {
        buffer.append("{");
        int len = buffer.length();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                appendName(key, len);
                append(value);
            }
        }
        buffer.append("}");
    }

    private void append(Collection<?> list) {
        buffer.append("[");
        int len = buffer.length();
        for (Object o : list) {
            if (buffer.length() > len) {
                appendComma();
            }
            append(o);
        }
        buffer.append("]");
    }

    private void append(Enumeration<?> en) {
        buffer.append("[");
        while (en.hasMoreElements()) {
            append(en.nextElement());
            if (en.hasMoreElements()) {
                appendComma();
            }
        }
        buffer.append("]");
    }

    private void append(Parameters parameters) {
        int len = buffer.length();
        Map<String, ParameterValue> params = parameters.getParameterValueMap();
        for (Parameter p : params.values()) {
            String name = p.getName();
            Object value = p.getValue();
            if (value != null) {
                appendName(name, len);
                append(value);
            }
        }
    }

    private void append(Method method) {
        buffer.append(method.getDeclaringClass().getTypeName());
        buffer.append('.');
        buffer.append(method.getName());
        buffer.append('(');
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                appendComma();
            }
            append(types[i].getTypeName());
        }
        buffer.append(')');
    }

    @Override
    public String toString() {
        buffer.append("}");
        return buffer.toString();
    }

    protected StringBuilder getBuffer() {
        return buffer;
    }

    private void checkCircularReference(Object wrapper, Object member) {
        if (wrapper.equals(member)) {
            throw new IllegalArgumentException("Serialization Failure: A circular reference was detected " +
                    "while converting a member object [" + member + "] in [" + wrapper + "]");
        }
    }

}
