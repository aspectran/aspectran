/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@link LinkedHashMap} variant that stores String keys in a case-insensitive
 * manner, for example for key-based access in a results table.
 *
 * <p>Preserves the original order as well as the original casing of keys,
 * while allowing for contains, get and remove calls with any case of key.
 *
 * <p>Does <i>not</i> support {@code null} keys.
 *
 * @since 3.0.0
 */
public class LinkedCaseInsensitiveMultiValueMap<V> implements MultiValueMap<String, V>, Serializable {

    private static final long serialVersionUID = 2505523262093891621L;

    private final Map<String, List<V>> values;

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     */
    public LinkedCaseInsensitiveMultiValueMap() {
        this.values = new LinkedCaseInsensitiveMap<>(Locale.ENGLISH);
    }

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     *
     * @param initialCapacity the initial capacity
     */
    public LinkedCaseInsensitiveMultiValueMap(int initialCapacity) {
        this.values = new LinkedCaseInsensitiveMap<>(initialCapacity, Locale.ENGLISH);
    }

    @Override
    public V getFirst(String key) {
        List<V> headerValues = this.values.get(key);
        return (headerValues != null ? headerValues.get(0) : null);
    }

    @Override
    public void add(String key, V value) {
        List<V> headerValues = this.values.computeIfAbsent(key, k -> new LinkedList<>());
        headerValues.add(value);
    }

    @Override
    public void set(String key, V value) {
        List<V> headerValues = new LinkedList<>();
        headerValues.add(value);
        this.values.put(key, headerValues);
    }

    @Override
    public void setAll(Map<String, V> values) {
        for (Entry<String, V> entry : values.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void put(String key, V[] values) {
        List<V> list = new LinkedList<>();
        Collections.addAll(list, values);
        put(key, list);
    }

    @Override
    public Map<String, V> toSingleValueMap() {
        LinkedHashMap<String, V> singleValueMap = new LinkedHashMap<>(this.values.size());
        for (Entry<String, List<V>> entry : this.values.entrySet()) {
            singleValueMap.put(entry.getKey(), entry.getValue().get(0));
        }
        return singleValueMap;
    }

    // Map implementation

    @Override
    public int size() {
        return this.values.size();
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.values.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.values.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
        return this.values.get(key);
    }

    @Override
    public List<V> put(String key, List<V> value) {
        return this.values.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return this.values.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<V>> map) {
        this.values.putAll(map);
    }

    @Override
    public void clear() {
        this.values.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.values.keySet();
    }

    @Override
    public Collection<List<V>> values() {
        return this.values.values();
    }

    @Override
    public Set<Entry<String, List<V>>> entrySet() {
        return this.values.entrySet();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LinkedCaseInsensitiveMultiValueMap)) {
            return false;
        }
        LinkedCaseInsensitiveMultiValueMap<?> otherValues = (LinkedCaseInsensitiveMultiValueMap<?>)other;
        return this.values.equals(otherValues.values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

}