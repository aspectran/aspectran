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

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>This class is a clone of org.springframework.util.LinkedCaseInsensitiveMapTests</p>
 *
 * Tests for {@link LinkedCaseInsensitiveMapTest}.
 *
 * <p>Created: 2020/03/20</p>
 */
class LinkedCaseInsensitiveMapTest {

    private final LinkedCaseInsensitiveMap<String> map = new LinkedCaseInsensitiveMap<>();

    @Test
    void putAndGet() {
        assertNull(map.put("key", "value1"));
        assertEquals("value1", map.put("key", "value2"));
        assertEquals("value2", map.put("key", "value3"));
        assertEquals(1, map.size());
        assertEquals("value3", map.get("key"));
        assertEquals("value3", map.get("KEY"));
        assertEquals("value3", map.get("Key"));
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("KEY"));
        assertTrue(map.containsKey("Key"));
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("KEY"));
        assertTrue(map.containsKey("Key"));
    }

    @Test
    void putWithOverlappingKeys() {
        assertNull(map.put("key", "value1"));
        assertEquals("value1", map.put("KEY", "value2"));
        assertEquals("value2", map.put("Key", "value3"));
        assertEquals(1, map.size());
        assertEquals("value3", map.get("key"));
        assertEquals("value3", map.get("KEY"));
        assertEquals("value3", map.get("Key"));
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("KEY"));
        assertTrue(map.containsKey("Key"));
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("KEY"));
        assertTrue(map.containsKey("Key"));
    }

    @Test
    void getOrDefault() {
        assertNull(map.put("key", "value1"));
        assertEquals("value1", map.put("KEY", "value2"));
        assertEquals("value2", map.put("Key", "value3"));
        assertEquals("value3", map.getOrDefault("key", "N"));
        assertEquals("value3", map.getOrDefault("KEY", "N"));
        assertEquals("value3", map.getOrDefault("Key", "N"));
        assertEquals("N", map.getOrDefault("keeeey", "N"));
        assertEquals("N", map.getOrDefault(new Object(), "N"));
    }

    @Test
    void getOrDefaultWithNullValue() {
        assertNull(map.put("key", null));
        assertNull(map.put("KEY", null));
        assertNull(map.put("Key", null));
        assertNull(map.getOrDefault("key", "N"));
        assertNull(map.getOrDefault("KEY", "N"));
        assertNull(map.getOrDefault("Key", "N"));
        assertEquals("N", map.getOrDefault("keeeey", "N"));
        assertEquals("N", map.getOrDefault(new Object(), "N"));
    }

    @Test
    void computeIfAbsentWithExistingValue() {
        assertNull(map.putIfAbsent("key", "value1"));
        assertEquals("value1", map.putIfAbsent("KEY", "value2"));
        assertEquals("value1", map.put("Key", "value3"));
        assertEquals("value3", map.computeIfAbsent("key", key2 -> "value1"));
        assertEquals("value3", map.computeIfAbsent("KEY", key1 -> "value2"));
        assertEquals("value3", map.computeIfAbsent("Key", key -> "value3"));
    }

    @Test
    void computeIfAbsentWithComputedValue() {
        assertEquals("value1", map.computeIfAbsent("key", key2 -> "value1"));
        assertEquals("value1", map.computeIfAbsent("KEY", key1 -> "value2"));
        assertEquals("value1", map.computeIfAbsent("Key", key -> "value3"));
    }

    @Test
    void mapClone() {
        assertNull(map.put("key", "value1"));
        LinkedCaseInsensitiveMap<String> copy = map.clone();

        assertEquals(map.getLocale(), copy.getLocale());
        assertEquals("value1", map.get("key"));
        assertEquals("value1", map.get("KEY"));
        assertEquals("value1", map.get("Key"));
        assertEquals("value1", copy.get("key"));
        assertEquals("value1", copy.get("KEY"));
        assertEquals("value1", copy.get("Key"));

        copy.put("Key", "value2");
        assertEquals(1, map.size());
        assertEquals(1, copy.size());
        assertEquals("value1", map.get("key"));
        assertEquals("value1", map.get("KEY"));
        assertEquals("value1", map.get("Key"));
        assertEquals("value2", copy.get("key"));
        assertEquals("value2", copy.get("KEY"));
        assertEquals("value2", copy.get("Key"));
    }

    @Test
    void clearFromKeySet() {
        map.put("key", "value");
        map.keySet().clear();
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void removeFromKeySet() {
        map.put("key", "value");
        map.keySet().remove("key");
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void removeFromKeySetViaIterator() {
        map.put("key", "value");
        nextAndRemove(map.keySet().iterator());
        assertEquals(0, map.size());
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void clearFromValues() {
        map.put("key", "value");
        map.values().clear();
        assertEquals(0, map.size());
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void removeFromValues() {
        map.put("key", "value");
        map.values().remove("value");
        assertEquals(0, map.size());
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void removeFromValuesViaIterator() {
        map.put("key", "value");
        nextAndRemove(map.values().iterator());
        assertEquals(0, map.size());
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void clearFromEntrySet() {
        map.put("key", "value");
        map.entrySet().clear();
        assertEquals(0, map.size());
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void removeFromEntrySet() {
        map.put("key", "value");
        map.entrySet().remove(map.entrySet().iterator().next());
        assertEquals(0, map.size());
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    @Test
    void removeFromEntrySetViaIterator() {
        map.put("key", "value");
        nextAndRemove(map.entrySet().iterator());
        assertEquals(0, map.size());
        map.computeIfAbsent("key", k -> "newvalue");
        assertEquals("newvalue", map.get("key"));
    }

    private void nextAndRemove(Iterator<?> iterator) {
        iterator.next();
        iterator.remove();
    }

}
