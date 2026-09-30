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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
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
public class LinkedCaseInsensitiveMultiValueMap<V> implements MultiValueMap<String, V>, Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 2505523262093891621L;

    private final Map<String, List<V>> targetMap;

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     */
    public LinkedCaseInsensitiveMultiValueMap() {
        this((Locale)null);
    }

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     * @param initialCapacity the initial capacity
     */
    public LinkedCaseInsensitiveMultiValueMap(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     * @param locale the Locale to use for case-insensitive key conversion
     */
    public LinkedCaseInsensitiveMultiValueMap(@Nullable Locale locale) {
        this.targetMap = new LinkedCaseInsensitiveMap<>(locale);
    }

    /**
     * Constructs a new, empty instance of the {@code LinkedCaseInsensitiveMultiValueMap} object.
     * @param initialCapacity the initial capacity
     * @param locale the Locale to use for case-insensitive key conversion
     */
    public LinkedCaseInsensitiveMultiValueMap(int initialCapacity, @Nullable Locale locale) {
        this.targetMap = new LinkedCaseInsensitiveMap<>(initialCapacity, locale);
    }

    /**
     * Copy constructor: Create a new LinkedCaseInsensitiveMultiValueMap with the same mappings as
     * the specified Map.
     * @param otherMap the Map whose mappings are to be placed in this Map
     * @see #clone()
     * @see #deepCopy()
     */
    public LinkedCaseInsensitiveMultiValueMap(@NonNull Map<String, List<V>> otherMap) {
        this.targetMap = new LinkedCaseInsensitiveMap<>(otherMap.size());
        this.targetMap.putAll(otherMap);
    }

    @Override
    @Nullable
    public V getFirst(String key) {
        List<V> values = this.targetMap.get(key);
        return (values != null && !values.isEmpty() ? values.getFirst() : null);
    }

    @Override
    public void add(String key, @Nullable V value) {
        List<V> values = this.targetMap.computeIfAbsent(key, k -> new ArrayList<>(2));
        values.add(value);
    }

    @Override
    public void addAll(String key, List<? extends V> values) {
        List<V> currentValues = this.targetMap.computeIfAbsent(key, k -> new ArrayList<>(values.size()));
        currentValues.addAll(values);
    }

    @Override
    public void addAll(@NonNull MultiValueMap<String, V> values) {
        for (Entry<String, List<V>> entry : values.entrySet()) {
            addAll(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void set(String key, @Nullable V value) {
        List<V> values = new ArrayList<>(1);
        values.add(value);
        this.targetMap.put(key, values);
    }

    @Override
    public void set(String key, @Nullable V[] values) {
        List<V> list = (values != null ? new ArrayList<>(Arrays.asList(values)) : new ArrayList<>(0));
        put(key, list);
    }

    @Override
    public void setAll(@NonNull Map<String, V> values) {
        values.forEach(this::set);
    }

    @Override
    public Map<String, V> toSingleValueMap() {
        LinkedCaseInsensitiveMap<V> singleValueMap = new LinkedCaseInsensitiveMap<>(this.targetMap.size());
        this.targetMap.forEach((key, values) -> {
            if (values != null && !values.isEmpty()) {
                singleValueMap.put(key, values.getFirst());
            }
        });
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
    @Nullable
    public List<V> get(Object key) {
        return this.targetMap.get(key);
    }

    @Override
    @Nullable
    public List<V> put(String key, List<V> value) {
        return this.targetMap.put(key, value);
    }

    @Override
    @Nullable
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

    /**
     * Create a deep copy of this Map.
     * @return a copy of this Map, including a copy of each value-holding List entry
     *      (consistently using an independent modifiable {@link ArrayList} for each entry)
     *      along the lines of {@code MultiValueMap.addAll} semantics
     * @see #addAll(MultiValueMap)
     * @see #clone()
     */
    public LinkedCaseInsensitiveMultiValueMap<V> deepCopy() {
        LinkedCaseInsensitiveMultiValueMap<V> copy = new LinkedCaseInsensitiveMultiValueMap<>(this.targetMap.size());
        this.targetMap.forEach((key, values) -> copy.put(key, (values != null ? new ArrayList<>(values) : null)));
        return copy;
    }

    /**
     * Create a regular copy of this Map.
     * @return a shallow copy of this Map, reusing this Map's value-holding List entries
     *      (even if some entries are shared or unmodifiable) along the lines of standard
     *      {@code Map.put} semantics
     * @see #put(String, List)
     * @see #putAll(Map)
     * @see LinkedCaseInsensitiveMultiValueMap#LinkedCaseInsensitiveMultiValueMap(Map)
     * @see #deepCopy()
     */
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public LinkedCaseInsensitiveMultiValueMap<V> clone() {
        return new LinkedCaseInsensitiveMultiValueMap<>(this);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(@Nullable Object other) {
        return (this == other || this.targetMap.equals(other));
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
