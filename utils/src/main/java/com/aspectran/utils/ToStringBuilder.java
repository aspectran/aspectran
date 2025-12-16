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
package com.aspectran.utils;

import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.Parameters;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Assists in implementing an object's {@link Object#toString()} method by providing a flexible
 * way to build a string representation of an object's fields in a consistent format.
 * <p>This builder supports appending various types of data, including primitive values,
 * collections, maps, arrays, and even other objects. It can also integrate with
 * {@link StringifyContext} for custom formatting options like date/time representation.</p>
 */
public class ToStringBuilder {

    private final StringBuilder buffer;

    private StringifyContext stringifyContext;

    private boolean braced = false;

    private int start = -1;

    /**
     * Creates a new ToStringBuilder with a default initial capacity.
     */
    public ToStringBuilder() {
        this(null);
    }

    /**
     * Creates a new ToStringBuilder with a specified name and default capacity.
     * The name will be prepended to the string representation.
     * @param name the name to prepend
     */
    public ToStringBuilder(String name) {
        this(name, 64);
    }

    /**
     * Creates a new ToStringBuilder with a specified initial capacity.
     * @param capacity the initial capacity of the internal StringBuilder
     */
    public ToStringBuilder(int capacity) {
        this(null, capacity);
    }

    /**
     * Creates a new ToStringBuilder with a specified name and initial capacity.
     * @param name the name to prepend
     * @param capacity the initial capacity of the internal StringBuilder
     */
    public ToStringBuilder(String name, int capacity) {
        this.buffer = new StringBuilder(capacity);
        labeling(name, true);
    }

    /**
     * Private constructor used by static {@code toString} methods to initialize the builder
     * with an object and an optional {@code StringifyContext}.
     * @param value the object to append
     * @param stringifyContext the context for stringification options
     */
    private ToStringBuilder(Object value, StringifyContext stringifyContext) {
        this(null, value, stringifyContext);
    }

    /**
     * Private constructor used by static {@code toString} methods to initialize the builder
     * with a name, an object, and an optional {@code StringifyContext}.
     * @param name the name to prepend
     * @param value the object to append
     * @param stringifyContext the context for stringification options
     */
    private ToStringBuilder(String name, Object value, StringifyContext stringifyContext) {
        this.buffer = new StringBuilder(128);
        setStringifyContext(stringifyContext);
        if (name != null) {
            labeling(name, false);
        }
        if (value != null) {
            appendValue(value);
        }
    }

    private void labeling(String name, boolean braced) {
        if (name != null) {
            buffer.append(name).append(" ");
        }
        if (braced) {
            appendOpenBrace();
        }
        this.braced = braced;
        this.start = buffer.length();
    }

    /**
     * Checks if a {@link StringifyContext} has been set for this builder.
     * @return {@code true} if a StringifyContext is present, {@code false} otherwise
     */
    public boolean hasStringifyContext() {
        return (stringifyContext != null);
    }

    /**
     * Sets the {@link StringifyContext} for this builder.
     * @param stringifyContext the context for stringification options
     */
    public void setStringifyContext(StringifyContext stringifyContext) {
        this.stringifyContext = stringifyContext;
    }

    /**
     * Applies a {@link StringifyContext} to this builder in a fluent style.
     * @param stringifyContext the context for stringification options
     * @return this builder instance for chaining
     */
    public ToStringBuilder apply(StringifyContext stringifyContext) {
        setStringifyContext(stringifyContext);
        return this;
    }

    /**
     * Appends a named field and its value to the string representation.
     * The field is only appended if its value is not {@code null}.
     * @param name the name of the field
     * @param value the value of the field
     * @return this builder instance for chaining
     */
    public ToStringBuilder append(String name, Object value) {
        if (value != null) {
            appendName(name);
            appendValue(value);
        }
        return this;
    }

    /**
     * Appends a named field and its value, or a default value if the primary value is {@code null}.
     * @param name the name of the field
     * @param value the primary value of the field
     * @param defaultValue the default value to use if the primary value is {@code null}
     * @return this builder instance for chaining
     */
    public ToStringBuilder append(String name, Object value, Object defaultValue) {
        if (value != null) {
            return append(name, value);
        } else {
            return append(name, defaultValue);
        }
    }

    /**
     * Appends a named field and its value to the string representation, regardless of whether the value is {@code null}.
     * @param name the name of the field
     * @param value the value of the field
     * @return this builder instance for chaining
     */
    public ToStringBuilder appendForce(String name, Object value) {
        appendName(name);
        appendValue(value);
        return this;
    }

    /**
     * Appends a named field and its {@link Class} to the string representation.
     * The field is only appended if the class is not {@code null}.
     * @param name the name of the field
     * @param clazz the class to append
     * @return this builder instance for chaining
     */
    public ToStringBuilder append(String name, Class<?> clazz) {
        if (clazz != null) {
            appendName(name);
            appendValue(clazz.getTypeName());
        }
        return this;
    }

    /**
     * Appends a named field and its {@link Method} signature to the string representation.
     * The field is only appended if the method is not {@code null}.
     * @param name the name of the field
     * @param method the method to append
     * @return this builder instance for chaining
     */
    public ToStringBuilder append(String name, Method method) {
        if (method != null) {
            appendName(name);
            appendValue(method);
        }
        return this;
    }

    /**
     * Appends a named boolean field and its value to the string representation.
     * The field is only appended if its value is {@code true}.
     * @param name the name of the field
     * @param value the boolean value of the field
     * @return this builder instance for chaining
     */
    public ToStringBuilder append(String name, boolean value) {
        if (value) {
            appendName(name);
            buffer.append(true);
        }
        return this;
    }

    /**
     * Appends a named boolean field and its value to the string representation, regardless of its value.
     * @param name the name of the field
     * @param value the boolean value of the field
     * @return this builder instance for chaining
     */
    public ToStringBuilder appendForce(String name, boolean value) {
        appendName(name);
        buffer.append(value);
        return this;
    }

    /**
     * Appends a named field and its value if the value is equal to the compare object.
     * @param name the name of the field
     * @param value the value of the field
     * @param compare the object to compare against
     * @return this builder instance for chaining
     */
    public ToStringBuilder appendEqual(String name, Object value, Object compare) {
        if (value != null && value.equals(compare)) {
            appendName(name);
            appendValue(value);
        }
        return this;
    }

    /**
     * Appends a named field and its value if the value is not equal to the compare object.
     * @param name the name of the field
     * @param value the value of the field
     * @param compare the object to compare against
     * @return this builder instance for chaining
     */
    public ToStringBuilder appendNotEqual(String name, Object value, Object compare) {
        if (value != null && !value.equals(compare)) {
            appendName(name);
            appendValue(value);
        }
        return this;
    }

    /**
     * Appends the size of a collection, map, array, iterator, or enumeration to the string representation.
     * @param name the name of the field
     * @param object the object whose size is to be appended
     * @return this builder instance for chaining
     */
    public ToStringBuilder appendSize(String name, Object object) {
        if (object != null) {
            appendName(name);
            if (object instanceof Map<?, ?> map) {
                buffer.append(map.size());
            } else if (object instanceof Collection<?> collection) {
                buffer.append(collection.size());
            } else if (object instanceof Iterator<?> iterator) {
                int count = 0;
                while (iterator.hasNext()) {
                    count++;
                    iterator.next();
                }
                buffer.append(count);
            } else if (object instanceof Enumeration<?> enumeration) {
                int count = 0;
                while (enumeration.hasMoreElements()) {
                    count++;
                    enumeration.nextElement();
                }
                buffer.append(count);
            } else if (object.getClass().isArray()) {
                buffer.append(Array.getLength(object));
            } else {
                buffer.append(object.toString().length());
            }
        }
        return this;
    }

    private void appendOpenBrace() {
        buffer.append("{");
    }

    private void appendCloseBrace() {
        buffer.append("}");
    }

    private void appendOpenBracket() {
        buffer.append("[");
    }

    private void appendCloseBracket() {
        buffer.append("]");
    }

    private void appendComma() {
        buffer.append(", ");
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

    private void appendValue(Object object) {
        if (object == null) {
            buffer.append((Object)null);
            return;
        }
        if (object instanceof CharSequence charSequence) {
            buffer.append(charSequence);
        } else if (object instanceof Map<?, ?> map) {
            appendValue(map);
        } else if (object instanceof Collection<?> collection) {
            appendValue(collection);
        } else if (object instanceof Iterator<?> iterator) {
            appendValue(iterator);
        } else if (object instanceof Enumeration<?> enumeration) {
            appendValue(enumeration);
        } else if (object instanceof Parameters parameters) {
            appendValue(parameters);
        } else if (object.getClass().isArray()) {
            appendArrayValue(object);
        } else if (object instanceof LocalDateTime localDateTime) {
            if (stringifyContext != null) {
                buffer.append(stringifyContext.toString(localDateTime));
            } else {
                buffer.append(localDateTime);
            }
        } else if (object instanceof LocalDate localDate) {
            if (stringifyContext != null) {
                buffer.append(stringifyContext.toString(localDate));
            } else {
                buffer.append(localDate);
            }
        } else if (object instanceof LocalTime localTime) {
            if (stringifyContext != null) {
                buffer.append(stringifyContext.toString(localTime));
            } else {
                buffer.append(localTime);
            }
        } else if (object instanceof Date date) {
            if (stringifyContext != null) {
                buffer.append(stringifyContext.toString(date));
            } else {
                buffer.append(date);
            }
        } else {
            buffer.append(object);
        }
    }

    private void appendValue(@NonNull Map<?, ?> map) {
        appendOpenBrace();
        int index = buffer.length();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                checkCircularReference(map, value);
                appendName(key, index);
                appendValue(value);
            }
        }
        appendCloseBrace();
    }

    private void appendValue(@NonNull Collection<?> list) {
        appendOpenBracket();
        int index = buffer.length();
        for (Object value : list) {
            checkCircularReference(list, value);
            if (buffer.length() > index) {
                appendComma();
            }
            appendValue(value);
        }
        appendCloseBracket();
    }

    private void appendValue(@NonNull Iterator<?> iterator) {
        appendOpenBracket();
        while (iterator.hasNext()) {
            Object value = iterator.next();
            checkCircularReference(iterator, value);
            appendValue(value);
            if (iterator.hasNext()) {
                appendComma();
            }
        }
        appendCloseBracket();
    }

    private void appendValue(@NonNull Enumeration<?> enumeration) {
        appendOpenBracket();
        while (enumeration.hasMoreElements()) {
            Object value = enumeration.nextElement();
            checkCircularReference(enumeration, value);
            appendValue(value);
            if (enumeration.hasMoreElements()) {
                appendComma();
            }
        }
        appendCloseBracket();
    }

    private void appendValue(@NonNull Parameters parameters) {
        appendOpenBrace();
        int index = buffer.length();
        for (Parameter p : parameters.getParameterValues()) {
            String name = p.getName();
            Object value = p.getValue();
            if (value != null) {
                checkCircularReference(parameters, value);
                appendName(name, index);
                appendValue(value);
            }
        }
        appendCloseBrace();
    }

    private void appendArrayValue(Object object) {
        appendOpenBracket();
        int len = Array.getLength(object);
        for (int i = 0; i < len; i++) {
            Object value = Array.get(object, i);
            checkCircularReference(object, value);
            if (i > 0) {
                appendComma();
            }
            appendValue(value);
        }
        appendCloseBracket();
    }

    private void appendValue(@NonNull Method method) {
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

    /**
     * Returns the final string representation built by this builder.
     * @return the string representation
     */
    @Override
    public String toString() {
        if (braced) {
            return buffer + "}";
        } else {
            return buffer.toString();
        }
    }

    private static void checkCircularReference(Object wrapper, Object member) {
        if (wrapper == member) {
            throw new IllegalArgumentException("Serialization Failure: Circular reference was detected " +
                "while serializing object " + ObjectUtils.identityToString(wrapper) + " " + wrapper);
        }
    }

    /**
     * Creates a string representation of an object using a default builder.
     * @param object the object to stringify
     * @return the string representation
     */
    public static String toString(Object object) {
        return toString(object, null);
    }

    /**
     * Creates a string representation of an object using a default builder and a specified name.
     * @param name the name to prepend
     * @param object the object to stringify
     * @return the string representation
     */
    public static String toString(String name, Object object) {
        return toString(name, object, null);
    }

    /**
     * Creates a string representation of an object using a specified {@link StringifyContext}.
     * @param object the object to stringify
     * @param stringifyContext the context for stringification options
     * @return the string representation
     */
    public static String toString(Object object, StringifyContext stringifyContext) {
        return new ToStringBuilder(object, stringifyContext).toString();
    }

    /**
     * Creates a string representation of an object using a specified name and {@link StringifyContext}.
     * @param name the name to prepend
     * @param object the object to stringify
     * @param stringifyContext the context for stringification options
     * @return the string representation
     */
    public static String toString(String name, Object object, StringifyContext stringifyContext) {
        return new ToStringBuilder(name, object, stringifyContext).toString();
    }

}
