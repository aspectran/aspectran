/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>This class is a clone of org.springframework.util.LinkedMultiValueMapTests</p>
 *
 * Tests for {@link LinkedMultiValueMapTest}.
 *
 * <p>Created: 2020/03/20</p>
 */
class LinkedMultiValueMapTest {

    private final LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();

    @Test
    void add() {
        map.add("key", "value1");
        map.add("key", "value2");
        assertEquals(1, map.size());
        assertLinesMatch(map.get("key"), Arrays.asList("value1", "value2"));
    }

    @Test
    void addIfAbsentWhenAbsent() {
        map.addIfAbsent("key", "value1");
        assertLinesMatch(map.get("key"), Collections.singletonList("value1"));
    }

    @Test
    void addIfAbsentWhenPresent() {
        map.add("key", "value1");
        map.addIfAbsent("key", "value2");
        assertLinesMatch(map.get("key"), Collections.singletonList("value1"));
    }

    @Test
    void set() {
        map.set("key", "value1");
        map.set("key", "value2");
        assertLinesMatch(map.get("key"), Collections.singletonList("value2"));
    }

    @Test
    void setWithArray() {
        map.set("key", new String[] {"value1", "value2"});
        assertLinesMatch(map.get("key"), Arrays.asList("value1", "value2"));
    }

    @Test
    void setWithNullArray() {
        map.set("key", (String[])null);
        assertNotNull(map.get("key"));
        assertEquals(1, map.size());
        assertNull(map.getFirst("key"));
    }

    @Test
    void addAll() {
        map.add("key", "value1");
        map.addAll("key", Arrays.asList("value2", "value3"));
        assertEquals(1, map.size());
        assertLinesMatch(map.get("key"), Arrays.asList("value1", "value2", "value3"));
    }

    @Test
    void addAllWithEmptyList() {
        map.addAll("key", Collections.emptyList());
        assertEquals(1, map.size());
        assertTrue(Objects.requireNonNull(map.get("key")).isEmpty());
        assertNull(map.getFirst("key"));
    }

    @Test
    void getFirst() {
        List<String> values = new ArrayList<>(2);
        values.add("value1");
        values.add("value2");
        map.put("key", values);
        assertEquals("value1", map.getFirst("key"));
        assertNull(map.getFirst("other"));
    }

    @Test
    void getFirstWithEmptyList() {
        map.put("key", Collections.emptyList());
        assertNull(map.getFirst("key"));
        assertNull(map.getFirst("other"));
    }

    @Test
    void toSingleValueMap() {
        List<String> values = new ArrayList<>(2);
        values.add("value1");
        values.add("value2");
        map.put("key", values);
        Map<String, String> singleValueMap = map.toSingleValueMap();
        assertEquals(1, singleValueMap.size());
        assertEquals("value1", singleValueMap.get("key"));
    }

    @Test
    void toSingleValueMapWithEmptyList() {
        map.put("key", Collections.emptyList());
        Map<String, String> singleValueMap = map.toSingleValueMap();
        assertTrue(singleValueMap.isEmpty());
        assertNull(singleValueMap.get("key"));
    }

    @Test
    void equals() {
        map.set("key1", "value1");
        assertEquals(map, map);
        MultiValueMap<String, String> o1 = new LinkedMultiValueMap<>();
        o1.set("key1", "value1");
        assertEquals(map, o1);
        assertEquals(o1, map);
        Map<String, List<String>> o2 = new HashMap<>();
        o2.put("key1", Collections.singletonList("value1"));
        assertEquals(map, o2);
        assertEquals(o2, map);
    }

}
