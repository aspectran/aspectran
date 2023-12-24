/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * A {@link Cache} that uses {@link  ReferenceType#SOFT soft} or
 * {@linkplain ReferenceType#WEAK weak} references for both {@code keys} and {@code values}.
 *
 * @param <K> the type of the key used for caching
 * @param <V> the type of the cached values
 * @since 6.6.7
 */
public class ConcurrentReferenceCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache;

    private final Function<K, V> generator;

    public ConcurrentReferenceCache(Function<K, V> generator) {
        Assert.notNull(generator, "Generator function must not be null");
        this.cache = new ConcurrentReferenceHashMap<>(16, ReferenceType.SOFT);
        this.generator = generator;
    }

    public ConcurrentReferenceCache(ReferenceType referenceType, Function<K, V> generator) {
        Assert.notNull(referenceType, "Reference type must not be null");
        Assert.notNull(generator, "Generator function must not be null");
        this.cache = new ConcurrentReferenceHashMap<>(16, referenceType);
        this.generator = generator;
    }

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

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Set<K> keySet() {
        return cache.keySet();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

}
