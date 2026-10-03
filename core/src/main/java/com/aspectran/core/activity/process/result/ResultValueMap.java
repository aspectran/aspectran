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
package com.aspectran.core.activity.process.result;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A specialized {@link java.util.LinkedHashMap} used to hold action results, particularly
 * when an action produces a nested or map-like structure.
 *
 * <p>This class serves as a standard map implementation for storing key-value results
 * within an {@link ActionResult}, allowing for complex, structured data to be returned
 * from a single action execution. It maintains insertion order of elements.</p>
 *
 * @since 2008
 */
public class ResultValueMap extends LinkedHashMap<String, Object> {

    @Serial
    private static final long serialVersionUID = 1904311925549971136L;

    /**
     * Constructs an empty ResultValueMap with a default initial capacity.
     */
    public ResultValueMap() {
        super(1 << 3);
    }

    /**
     * Constructs an empty ResultValueMap with the specified initial capacity.
     * @param initialCapacity the initial capacity
     */
    public ResultValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs a new ResultValueMap with the same mappings as the specified map.
     * @param m the map whose mappings are to be placed in this map
     */
    public ResultValueMap(Map<? extends String, ?> m) {
        super(m);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or the default value if this map contains no mapping for the key.
     * @param <T> the type of the value
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default value to return if no mapping is found
     * @return the mapped value, or {@code defaultValue} if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(@Nullable String key, @Nullable T defaultValue) {
        Object value = super.get(key);
        return (value != null ? (T)value : defaultValue);
    }

    /**
     * Returns the value to which the specified key is mapped cast to the required type,
     * or {@code null} if this map contains no mapping for the key.
     * @param <T> the required type
     * @param key the key whose associated value is to be returned
     * @param requiredType the required class type
     * @return the value mapped to the key, or {@code null} if not found
     */
    @Nullable
    public <T> T get(@Nullable String key, @NonNull Class<T> requiredType) {
        Object value = super.get(key);
        if (value == null) {
            return null;
        }
        if (!requiredType.isInstance(value)) {
            throw new IllegalArgumentException("Value [" + value + "] is not of required type [" +
                    requiredType.getName() + "]");
        }
        return requiredType.cast(value);
    }

    /**
     * Returns the string representation of the value mapped to the specified key,
     * or {@code null} if this map contains no mapping for the key.
     * @param key the key whose associated value is to be returned
     * @return the string value, or {@code null} if not found
     */
    @Nullable
    public String getString(@Nullable String key) {
        Object value = super.get(key);
        return (value != null ? value.toString() : null);
    }

    /**
     * Returns the boolean value mapped to the specified key,
     * or {@code null} if this map contains no mapping for the key.
     * @param key the key whose associated value is to be returned
     * @return the boolean value, or {@code null} if not found
     */
    @Nullable
    public Boolean getBoolean(@Nullable String key) {
        Object value = super.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.valueOf(value.toString());
    }

}
