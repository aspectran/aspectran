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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>This class is a clone of io.undertow.util.CopyOnWriteMap</p>
 *
 * A basic copy on write map. It simply delegates to an underlying map, that is swapped out
 * every time the map is updated.
 * <em>Note: this is not a secure map. It should not be used in situations where the map is populated
 * from user input.</em>
 *
 * @author Stuart Douglas
 */
public class CopyOnWriteMap<K,V> implements ConcurrentMap<K, V> {

    private volatile Map<K, V> delegate = Collections.emptyMap();

    public CopyOnWriteMap() {
    }

    public CopyOnWriteMap(Map<K, V> existing) {
        this.delegate = new HashMap<>(existing);
    }

    @Override
    public synchronized V putIfAbsent(@NonNull K key, V value) {
        Map<K, V> delegate = this.delegate;
        V existing = delegate.get(key);
        if (existing != null) {
            return existing;
        }
        putInternal(key, value);
        return null;
    }

    @Override
    public synchronized boolean remove(@NonNull Object key, Object value) {
        Map<K, V> delegate = this.delegate;
        V existing = delegate.get(key);
        if (existing.equals(value)) {
            removeInternal(key);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean replace(@NonNull K key, @NonNull V oldValue, @NonNull V newValue) {
        Map<K, V> delegate = this.delegate;
        V existing = delegate.get(key);
        if (existing.equals(oldValue)) {
            putInternal(key, newValue);
            return true;
        }
        return false;
    }

    @Override
    public synchronized V replace(@NonNull K key, @NonNull V value) {
        Map<K, V> delegate = this.delegate;
        V existing = delegate.get(key);
        if (existing != null) {
            putInternal(key, value);
            return existing;
        }
        return null;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public synchronized V put(K key, V value) {
        return putInternal(key, value);
    }

    @Override
    public synchronized V remove(Object key) {
        return removeInternal(key);
    }

    @Override
    public synchronized void putAll(@NonNull Map<? extends K, ? extends V> map) {
        Map<K, V> delegate = new HashMap<>(this.delegate);
        delegate.putAll(map);
        this.delegate = delegate;
    }

    @Override
    public synchronized void clear() {
        delegate = Collections.emptyMap();
    }

    @Override
    @NonNull
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    @NonNull
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    @NonNull
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    // must be called under lock
    private V putInternal(K key, V value) {
        Map<K, V> delegate = new HashMap<>(this.delegate);
        V existing = delegate.put(key, value);
        this.delegate = delegate;
        return existing;
    }

    public V removeInternal(Object key) {
        Map<K, V> delegate = new HashMap<>(this.delegate);
        V existing = delegate.remove(key);
        this.delegate = delegate;
        return existing;
    }

}
