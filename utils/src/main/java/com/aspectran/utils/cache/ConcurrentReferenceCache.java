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
package com.aspectran.utils.cache;

import com.aspectran.utils.Assert;
import com.aspectran.utils.ConcurrentReferenceHashMap;
import com.aspectran.utils.ConcurrentReferenceHashMap.ReferenceType;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A {@link Cache} implementation that uses {@link ConcurrentReferenceHashMap}
 * to store entries with {@link ReferenceType#SOFT soft} or {@linkplain ConcurrentReferenceHashMap.ReferenceType#WEAK weak} references.
 * <p>This cache is suitable for scenarios where cached objects can be garbage-collected
 * when memory is low, providing a memory-sensitive caching mechanism.</p>
 *
 * @param <K> the type of the key used for caching
 * @param <V> the type of the cached values
 * @since 6.6.7
 */
public class ConcurrentReferenceCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache;

    private final Function<K, V> generator;

    /**
     * Creates a new ConcurrentReferenceCache with default soft references.
     * @param generator a function to generate a value if it's not found in the cache
     */
    public ConcurrentReferenceCache(Function<K, V> generator) {
        Assert.notNull(generator, "Generator function must not be null");
        this.cache = new ConcurrentReferenceHashMap<>(16, ReferenceType.SOFT);
        this.generator = generator;
    }

    /**
     * Creates a new ConcurrentReferenceCache with the specified reference type.
     * @param referenceType the type of reference to use (SOFT or WEAK)
     * @param generator a function to generate a value if it's not found in the cache
     */
    public ConcurrentReferenceCache(ReferenceType referenceType, Function<K, V> generator) {
        Assert.notNull(referenceType, "Reference type must not be null");
        Assert.notNull(generator, "Generator function must not be null");
        this.cache = new ConcurrentReferenceHashMap<>(16, referenceType);
        this.generator = generator;
    }

    /**
     * Retrieves a value from the cache. If the value is not present, it is generated
     * using the provided {@code generator} function, added to the cache, and returned.
     * @param key the key of the value to retrieve
     * @return the cached or newly generated value
     */
    @Override
    public V get(K key) {
        V cached = cache.get(key);
        if (cached == null) {
            cached = generator.apply(key);
            V existing = cache.putIfAbsent(key, cached);
            if (existing != null) {
                cached = existing;
            }
        }
        return cached;
    }

    /**
     * Removes the mapping for a key from this cache if it is present.
     * @param key the key whose mapping is to be removed from the cache
     */
    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    /**
     * Removes all of the mappings from this cache.
     * The cache will be empty after this call returns.
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * Returns a {@link Set} view of the keys contained in this cache.
     * @return a set view of the keys contained in this cache
     */
    @Override
    public Set<K> keySet() {
        return cache.keySet();
    }

    /**
     * Returns the number of key-value mappings in this cache.
     * @return the number of elements in this cache
     */
    @Override
    public int size() {
        return cache.size();
    }

    /**
     * Returns {@code true} if this cache contains no key-value mappings.
     * @return {@code true} if this cache contains no key-value mappings, {@code false} otherwise
     */
    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

}
