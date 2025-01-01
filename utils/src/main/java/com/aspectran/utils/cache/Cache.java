/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.utils.cache;

import java.util.Set;

/**
 * A Map-like data structure that stores key-value pairs and provides temporary
 * access to it.
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 * @since 6.6.7
 */
public interface Cache<K, V> {

    /**
     * Gets an entry from the cache.
     * @param key the key whose associated value is to be returned
     * @return the element, or {@code null}, if it does not exist
     */
    V get(K key);

    /**
     * Removes the specified element from this cache if it is present.
     * @param key key with which the specified value is to be associated
     */
    void remove(K key);

    /**
     * Removes all of the elements from this cache.
     */
    void clear();

    /**
     * Returns a {@link Set} view of the keys contained in this cache.
     * @return the set view
     */
    Set<K> keySet();

    /**
     * Returns the number of elements in this cache.
     * @return the number of elements in this cache
     */
    int size();

    /**
     * Returns {@code true} if this cache contains no key-value mappings.
     * @return {@code true} if this cache contains no key-value mappings
     */
    boolean isEmpty();

}
