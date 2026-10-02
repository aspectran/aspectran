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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopyOnWriteMapTest {

    @Test
    void testBasicPutAndGet() {
        CopyOnWriteMap<String, Integer> map = new CopyOnWriteMap<>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        assertNull(map.put("one", 1));
        assertEquals(1, map.size());
        assertEquals(1, map.get("one"));
        assertTrue(map.containsKey("one"));
        assertTrue(map.containsValue(1));

        assertEquals(1, map.put("one", 10));
        assertEquals(10, map.get("one"));

        assertEquals(10, map.remove("one"));
        assertNull(map.get("one"));
        assertTrue(map.isEmpty());
    }

    @Test
    void testRemoveWithValueWhenKeyMissing() {
        CopyOnWriteMap<String, String> map = new CopyOnWriteMap<>();
        // Should not throw NullPointerException when key does not exist
        assertFalse(map.remove("nonexistent", "value"));

        map.put("key", "val1");
        assertFalse(map.remove("key", "wrongVal"));
        assertTrue(map.remove("key", "val1"));
        assertNull(map.get("key"));
    }

    @Test
    void testReplaceWhenKeyMissing() {
        CopyOnWriteMap<String, String> map = new CopyOnWriteMap<>();
        // Should not throw NullPointerException when key does not exist
        assertFalse(map.replace("nonexistent", "oldVal", "newVal"));
        assertNull(map.replace("nonexistent", "newVal"));

        map.put("key", "old");
        assertFalse(map.replace("key", "wrongOld", "new"));
        assertEquals("old", map.get("key"));

        assertTrue(map.replace("key", "old", "new"));
        assertEquals("new", map.get("key"));

        assertEquals("new", map.replace("key", "updated"));
        assertEquals("updated", map.get("key"));
    }

    @Test
    void testPutIfAbsent() {
        CopyOnWriteMap<String, String> map = new CopyOnWriteMap<>();
        assertNull(map.putIfAbsent("k1", "v1"));
        assertEquals("v1", map.get("k1"));

        assertEquals("v1", map.putIfAbsent("k1", "v2"));
        assertEquals("v1", map.get("k1"));
    }

    @Test
    void testComputeMethods() {
        CopyOnWriteMap<String, Integer> map = new CopyOnWriteMap<>();

        assertEquals(10, map.computeIfAbsent("a", k -> 10));
        assertEquals(10, map.computeIfAbsent("a", k -> 20));

        assertNull(map.computeIfPresent("b", (k, v) -> v + 1));
        assertEquals(11, map.computeIfPresent("a", (k, v) -> v + 1));
        assertNull(map.computeIfPresent("a", (k, v) -> null));
        assertNull(map.get("a"));

        assertEquals(100, map.compute("x", (k, v) -> (v == null ? 100 : v + 50)));
        assertEquals(150, map.compute("x", (k, v) -> (v == null ? 100 : v + 50)));
        assertNull(map.compute("x", (k, v) -> null));
        assertFalse(map.containsKey("x"));
    }

    @Test
    void testMergeAndReplaceAll() {
        CopyOnWriteMap<String, String> map = new CopyOnWriteMap<>();
        map.merge("a", "hello", String::concat);
        assertEquals("hello", map.get("a"));

        map.merge("a", " world", String::concat);
        assertEquals("hello world", map.get("a"));

        map.put("b", "foo");
        map.replaceAll((k, v) -> v.toUpperCase());
        assertEquals("HELLO WORLD", map.get("a"));
        assertEquals("FOO", map.get("b"));
    }

    @Test
    void testUnmodifiableViews() {
        CopyOnWriteMap<String, String> map = new CopyOnWriteMap<>();
        map.put("k1", "v1");

        Set<String> keySet = map.keySet();
        assertEquals(1, keySet.size());
        assertThrows(UnsupportedOperationException.class, () -> keySet.remove("k1"));

        assertThrows(UnsupportedOperationException.class, () -> map.values().remove("v1"));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().clear());
    }

    @Test
    void testEqualsAndHashCode() {
        CopyOnWriteMap<String, String> map1 = new CopyOnWriteMap<>();
        map1.put("a", "1");
        map1.put("b", "2");

        Map<String, String> map2 = new HashMap<>();
        map2.put("a", "1");
        map2.put("b", "2");

        assertEquals(map1, map2);
        assertEquals(map2, map1);
        assertEquals(map1.hashCode(), map2.hashCode());
        assertNotNull(map1.toString());

        map2.put("c", "3");
        assertNotEquals(map1, map2);
    }

    @Test
    void testCopyConstructorAndPutAll() {
        Map<String, String> init = Map.of("k1", "v1", "k2", "v2");
        CopyOnWriteMap<String, String> map = new CopyOnWriteMap<>(init);
        assertEquals(2, map.size());
        assertEquals("v1", map.get("k1"));

        map.putAll(Map.of("k3", "v3", "k4", "v4"));
        assertEquals(4, map.size());

        map.clear();
        assertTrue(map.isEmpty());
    }

}
