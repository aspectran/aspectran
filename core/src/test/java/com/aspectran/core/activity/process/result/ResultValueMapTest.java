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
package com.aspectran.core.activity.process.result;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ResultValueMap}.
 */
class ResultValueMapTest {

    @Test
    void testConstructors() {
        ResultValueMap map1 = new ResultValueMap();
        assertTrue(map1.isEmpty());

        ResultValueMap map2 = new ResultValueMap(16);
        assertTrue(map2.isEmpty());

        ResultValueMap map3 = new ResultValueMap(Map.of("k1", "v1", "k2", 123));
        assertEquals(2, map3.size());
        assertEquals("v1", map3.get("k1"));
    }

    @Test
    void testTypedGetters() {
        ResultValueMap map = new ResultValueMap();
        map.put("str", "hello");
        map.put("num", 42);
        map.put("boolTrue", true);
        map.put("boolStr", "true");

        assertEquals("hello", map.getString("str"));
        assertEquals("42", map.getString("num"));
        assertNull(map.getString("nonExisting"));

        assertTrue(map.getBoolean("boolTrue"));
        assertTrue(map.getBoolean("boolStr"));
        assertNull(map.getBoolean("nonExisting"));

        assertEquals(42, map.get("num", Integer.class));
        assertNull(map.get("nonExisting", Integer.class));
        assertThrows(IllegalArgumentException.class, () -> map.get("str", Integer.class));

        assertEquals("hello", map.get("str", "default"));
        assertEquals("default", map.get("nonExisting", "default"));
    }

}
