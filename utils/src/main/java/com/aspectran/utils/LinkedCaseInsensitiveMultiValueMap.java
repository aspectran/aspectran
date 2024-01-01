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
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * <p>This class is a clone of org.springframework.util.LinkedCaseInsensitiveMultiValueMap</p>
 *
 * {@link LinkedHashMap} variant that stores String keys in a case-insensitive
 * manner, for example for key-based access in a results table.
 *
 * <p>Preserves the original order as well as the original casing of keys,
 * while allowing for contains, get and remove calls with any case of key.</p>
 *
 * <p>Does <i>not</i> support {@code null} keys.</p>
 *
 * @param <V> the value type
 */
public class LinkedCaseInsensitiveMultiValueMap<V> implements MultiValueMap<String, V>, Serializable {

    private static final long serialVersionUID = 2505523262093891621L;

    private final Map<String, List<V>> targetMap;

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     */
    public LinkedCaseInsensitiveMultiValueMap() {
        this.targetMap = new LinkedCaseInsensitiveMap<>(Locale.ENGLISH);
    }

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     * @param initialCapacity the initial capacity
     */
    public LinkedCaseInsensitiveMultiValueMap(int initialCapacity) {
        this.targetMap = new LinkedCaseInsensitiveMap<>(initialCapacity, Locale.ENGLISH);
    }

    @Override
    public V getFirst(String key) {
        List<V> values = this.targetMap.get(key);
        return (values != null ? values.get(0) : null);
    }

    @Override
    public void add(String key, V value) {
        List<V> values = this.targetMap.computeIfAbsent(key, k -> new LinkedList<>());
        values.add(value);
    }

    @Override
    public void addAll(String key, List<? extends V> values) {
        List<V> currentValues = this.targetMap.computeIfAbsent(key, k -> new LinkedList<>());
        currentValues.addAll(values);
    }

    @Override
    public void addAll(MultiValueMap<String, V> values) {
        for (Entry<String, List<V>> entry : values.entrySet()) {
            addAll(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void set(String key, V value) {
        List<V> values = new LinkedList<>();
        values.add(value);
        this.targetMap.put(key, values);
    }

    @Override
    public void set(String key, V[] values) {
        List<V> list = new LinkedList<>();
        if (values != null) {
            Collections.addAll(list, values);
        }
        put(key, list);
    }

    @Override
    public void setAll(Map<String, V> values) {
        for (Entry<String, V> entry : values.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<String, V> toSingleValueMap() {
        LinkedHashMap<String, V> singleValueMap = new LinkedHashMap<>(this.targetMap.size());
        for (Entry<String, List<V>> entry : this.targetMap.entrySet()) {
            singleValueMap.put(entry.getKey(), entry.getValue().get(0));
        }
        return singleValueMap;
    }

    // Map implementation

    @Override
    public int size() {
        return this.targetMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.targetMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.targetMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.targetMap.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
        return this.targetMap.get(key);
    }

    @Override
    public List<V> put(String key, List<V> value) {
        return this.targetMap.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return this.targetMap.remove(key);
    }

    @Override
    public void putAll(@NonNull Map<? extends String, ? extends List<V>> map) {
        this.targetMap.putAll(map);
    }

    @Override
    public void clear() {
        this.targetMap.clear();
    }

    @Override
    @NonNull
    public Set<String> keySet() {
        return this.targetMap.keySet();
    }

    @Override
    @NonNull
    public Collection<List<V>> values() {
        return this.targetMap.values();
    }

    @Override
    @NonNull
    public Set<Entry<String, List<V>>> entrySet() {
        return this.targetMap.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super List<V>> action) {
        this.targetMap.forEach(action);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LinkedCaseInsensitiveMultiValueMap)) {
            return false;
        }
        LinkedCaseInsensitiveMultiValueMap<?> otherValues = (LinkedCaseInsensitiveMultiValueMap<?>)other;
        return this.targetMap.equals(otherValues.targetMap);
    }

    @Override
    public int hashCode() {
        return this.targetMap.hashCode();
    }

    @Override
    public String toString() {
        return this.targetMap.toString();
    }

}
