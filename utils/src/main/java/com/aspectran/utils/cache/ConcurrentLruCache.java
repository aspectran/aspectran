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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * A thread-safe LRU (Least Recently Used) {@link Cache} with a fixed capacity.
 * <p>If the cache reaches its capacity, it discards the least recently used entry first.
 * This implementation is backed by a {@code ConcurrentHashMap} for storing the cached values
 * and a {@code ConcurrentLinkedDeque} for ordering the keys and choosing the least recently
 * used key when the cache is at full capacity.</p>
 *
 * @param <K> the type of the key used for caching
 * @param <V> the type of the cached values
 * @since 6.6.7
 */
public class ConcurrentLruCache<K, V> implements Cache<K, V> {

    private final ConcurrentLinkedDeque<K> queue = new ConcurrentLinkedDeque<>();

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    private final int capacity;

    private final Function<K, V> generator;

    private final ReadWriteLock lock;

    private volatile int size = 0;

    /**
     * Creates a new ConcurrentLruCache with the specified capacity and value generator.
     * @param capacity the maximum number of entries the cache can hold (must be positive)
     * @param generator a function to generate a value if it's not found in the cache
     */
    public ConcurrentLruCache(int capacity, Function<K, V> generator) {
        Assert.isTrue(capacity > 0, "capacity must be positive");
        Assert.notNull(generator, "Generator function must not be null");
        this.capacity = capacity;
        this.generator = generator;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Retrieves a value from the cache. If the value is not present, it is generated
     * using the provided {@code generator} function, added to the cache, and returned.
     * The retrieved or generated entry becomes the most recently used.
     * @param key the key of the value to retrieve
     * @return the cached or newly generated value
     */
    @Override
    public V get(K key) {
        V cached = cache.get(key);
        if (cached != null) {
            if (size < capacity) {
                return cached;
            }
            lock.readLock().lock();
            try {
                if (queue.removeLastOccurrence(key)) {
                    queue.offer(key);
                }
                return cached;
            } finally {
                lock.readLock().unlock();
            }
        }
        lock.writeLock().lock();
        try {
            // Retrying in case of concurrent reads on the same key
            cached = cache.get(key);
            if (cached != null) {
                if (queue.removeLastOccurrence(key)) {
                    queue.offer(key);
                }
                return cached;
            }
            // Generate value first, to prevent size inconsistency
            V value = generator.apply(key);
            int cacheSize = size;
            if (cacheSize == capacity) {
                K leastUsed = queue.poll();
                if (leastUsed != null) {
                    cache.remove(leastUsed);
                    cacheSize--;
                }
            }
            queue.offer(key);
            cache.put(key, value);
            size = cacheSize + 1;
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes the mapping for a key from this cache if it is present.
     * @param key the key whose mapping is to be removed from the cache
     */
    @Override
    public void remove(K key) {
        if (!isEmpty()) {
            lock.writeLock().lock();
            try {
                queue.remove(key);
                cache.remove(key);
                size = cache.size();
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    /**
     * Removes all of the mappings from this cache.
     * The cache will be empty after this call returns.
     */
    @Override
    public void clear() {
        if (!isEmpty()) {
            lock.writeLock().lock();
            try {
                queue.clear();
                cache.clear();
                size = 0;
            } finally {
                lock.writeLock().unlock();
            }
        }
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
        return size;
    }

    /**
     * Returns {@code true} if this cache contains no key-value mappings.
     * @return {@code true} if this cache contains no key-value mappings, {@code false} otherwise
     */
    @Override
    public boolean isEmpty() {
        return (size == 0);
    }

}
