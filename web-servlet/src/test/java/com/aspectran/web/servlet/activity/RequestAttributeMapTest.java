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
package com.aspectran.web.servlet.activity;

import com.aspectran.test.web.servlet.mock.MockHttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link RequestAttributeMap}.
 */
class RequestAttributeMapTest {

    @Test
    void testEmptyAndNullRequest() {
        RequestAttributeMap map = new RequestAttributeMap();
        assertNull(map.getRequest());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey("key"));
        assertFalse(map.containsValue("value"));
        assertNull(map.get("key"));
        assertTrue(map.keySet().isEmpty());
        assertTrue(map.values().isEmpty());
        assertTrue(map.entrySet().isEmpty());

        assertThrows(IllegalStateException.class, () -> map.put("k", "v"));
        assertThrows(IllegalStateException.class, () -> map.remove("k"));
        assertThrows(IllegalStateException.class, () -> map.putAll(Map.of("k", "v")));
    }

    @Test
    void testBasicOperations() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestAttributeMap map = new RequestAttributeMap(request);

        assertEquals(request, map.getRequest());
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        map.put("attr1", "val1");
        map.put("attr2", 123);

        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("attr1"));
        assertTrue(map.containsKey("attr2"));
        assertFalse(map.containsKey("attr3"));
        assertFalse(map.containsKey(null));

        assertTrue(map.containsValue("val1"));
        assertTrue(map.containsValue(123));
        assertFalse(map.containsValue("nonExistent"));

        assertEquals("val1", map.get("attr1"));
        assertEquals(123, map.get("attr2"));
        assertNull(map.get("attr3"));
        assertNull(map.get(null));

        // Test keySet
        Set<String> keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("attr1"));
        assertTrue(keys.contains("attr2"));

        // Test values
        Collection<Object> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("val1"));
        assertTrue(values.contains(123));

        // Test entrySet
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        assertEquals(2, entries.size());

        // Test remove
        Object removed = map.remove("attr1");
        assertEquals("val1", removed);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("attr1"));
        assertNull(map.remove("nonExistent"));
        assertNull(map.remove(null));

        // Test putAll
        Map<String, Object> newAttrs = new HashMap<>();
        newAttrs.put("attr3", "val3");
        newAttrs.put("attr4", "val4");
        map.putAll(newAttrs);
        assertEquals(3, map.size());
        assertTrue(map.containsKey("attr3"));
        assertTrue(map.containsKey("attr4"));

        // Test clear
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

}
