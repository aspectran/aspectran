/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-20.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils.cache;

import java.util.Set;

/**
 * A generic cache interface that stores key-value pairs.
 * <p>This interface defines the basic operations for a cache, similar to a {@link java.util.Map},
 * but typically implies a temporary storage mechanism where entries might be evicted based on
 * various policies (e.g., LRU, time-based).</p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @since 6.6.7
 */
public interface Cache<K, V> {

    /**
     * Retrieves the value associated with the specified key from the cache.
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or {@code null} if the key is not found in the cache
     */
    V get(K key);

    /**
     * Removes the mapping for a key from this cache if it is present.
     * @param key the key whose mapping is to be removed from the cache
     */
    void remove(K key);

    /**
     * Removes all of the mappings from this cache.
     * The cache will be empty after this call returns.
     */
    void clear();

    /**
     * Returns a {@link Set} view of the keys contained in this cache.
     * The set is backed by the cache, so changes to the cache are reflected in the set, and vice-versa.
     * @return a set view of the keys contained in this cache
     */
    Set<K> keySet();

    /**
     * Returns the number of key-value mappings in this cache.
     * @return the number of elements in this cache
     */
    int size();

    /**
     * Returns {@code true} if this cache contains no key-value mappings.
     * @return {@code true} if this cache contains no key-value mappings, {@code false} otherwise
     */
    boolean isEmpty();

}
