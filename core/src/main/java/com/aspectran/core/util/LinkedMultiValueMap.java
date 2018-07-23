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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of {@link MultiValueMap} that wraps a {@link LinkedHashMap},
 * storing multiple values in a {@link LinkedList}.
 *
 * <p>This Map implementation is generally not thread-safe. It is primarily designed
 * for data structures exposed from request objects, for use in a single thread only.
 *
 * @since 3.0.0
 */
public class LinkedMultiValueMap<K, V> extends LinkedHashMap<K, List<V>> implements MultiValueMap<K, V>, Serializable {

    private static final long serialVersionUID = -9081293088810769730L;

    /**
     * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}.
     */
    public LinkedMultiValueMap() {
        super();
    }

    /**
     * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}
     * with the given initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public LinkedMultiValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Copy constructor: Create a new LinkedMultiValueMap with the same mappings as
     * the specified Map. Note that this will be a shallow copy; its value-holding
     * List entries will get reused and therefore cannot get modified independently.
     *
     * @param otherMap the Map whose mappings are to be placed in this Map
     * @see #clone()
     * @see #deepCopy()
     */
    public LinkedMultiValueMap(Map<K, List<V>> otherMap) {
        super(otherMap);
    }

    @Override
    public void add(K key, V value) {
        List<V> values = this.computeIfAbsent(key, k -> new LinkedList<>());
        values.add(value);
    }

    @Override
    public V getFirst(K key) {
        List<V> values = get(key);
        return (values != null ? values.get(0) : null);
    }

    @Override
    public void set(K key, V value) {
        List<V> values = new LinkedList<>();
        values.add(value);
        put(key, values);
    }

    @Override
    public void setAll(Map<K, V> values) {
        values.forEach(this::set);
    }

    @Override
    public void put(K key, V[] values) {
        List<V> list = new LinkedList<>();
        Collections.addAll(list, values);
        put(key, list);
    }

    @Override
    public Map<K, V> toSingleValueMap() {
        LinkedHashMap<K, V> singleValueMap = new LinkedHashMap<>(this.size());
        this.forEach((key, value) -> singleValueMap.put(key, value.get(0)));
        return singleValueMap;
    }

    /**
     * Create a regular copy of this Map.
     *
     * @return a shallow copy of this Map, reusing this Map's value-holding List entries
     * @see LinkedMultiValueMap#LinkedMultiValueMap(Map)
     * @see #deepCopy()
     */
    @Override
    public LinkedMultiValueMap<K, V> clone() {
        return new LinkedMultiValueMap<>(this);
    }

    /**
     * Create a deep copy of this Map.
     *
     * @return a copy of this Map, including a copy of each value-holding List entry
     * @see #clone()
     */
    public LinkedMultiValueMap<K, V> deepCopy() {
        LinkedMultiValueMap<K, V> copy = new LinkedMultiValueMap<>(this.size());
        this.forEach((key, value) -> copy.put(key, new LinkedList<>(value)));
        return copy;
    }

}