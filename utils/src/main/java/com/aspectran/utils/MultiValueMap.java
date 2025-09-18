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

import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A specialized {@link Map} interface that can store multiple values for a single key.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface MultiValueMap<K, V> extends Map<K, List<V>> {

    /**
     * Return the first value for the given key.
     * @param key the key
     * @return the first value for the specified key, or {@code null} if none
     */
    @Nullable
    V getFirst(K key);

    /**
     * Add the given single value to the current list of values for the given key.
     * @param key the key
     * @param value the value to be added
     */
    void add(K key, @Nullable V value);

    /**
     * Add all the values of the given list to the current list of values for the given key.
     * @param key they key
     * @param values the values to be added
     */
    void addAll(K key, List<? extends V> values);

    /**
     * Add all the values of the given {@code MultiValueMap} to the current values.
     * @param values the values to be added
     */
    void addAll(MultiValueMap<K, V> values);

    /**
     * Add the given value, only if the map does not already contain the given key.
     * @param key the key
     * @param value the value to be added
     */
    void addIfAbsent(K key, @Nullable V value);

    /**
     * Set the given single value under the given key.
     * @param key the key
     * @param value the value to set
     */
    void set(K key, @Nullable V value);

    /**
     * Set the given values under the given key.
     * @param key the key
     * @param values the values to set
     */
    void set(K key, @Nullable V[] values);

    /**
     * Set the given values under.
     * @param values the values
     */
    void setAll(Map<K, V> values);

    /**
     * Return a {@code Map} with the first values contained in this {@code MultiValueMap}.
     * @return a single value representation of this map
     */
    Map<K, V> toSingleValueMap();

}
