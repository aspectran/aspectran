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
package com.aspectran.utils.json;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for JsonParser.
 *
 * <p>Created: 2025-10-15</p>
 */
class JsonParserTest {

    @Test
    void testParseSimpleObject() throws IOException {
        String json = "{\"name\":\"John Doe\",\"age\":30,\"isStudent\":false}";
        Object result = JsonParser.parse(json);
        assertInstanceOf(Map.class, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>)result;
        assertEquals("John Doe", map.get("name"));
        assertEquals(30, map.get("age"));
        assertEquals(false, map.get("isStudent"));
    }

    @Test
    void testParseSimpleArray() throws IOException {
        String json = "[1, \"hello\", true, null]";
        Object result = JsonParser.parse(json);
        assertInstanceOf(List.class, result);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>)result;
        assertEquals(1, list.get(0));
        assertEquals("hello", list.get(1));
        assertEquals(true, list.get(2));
        assertNull(list.get(3));
    }

    @Test
    void testParseNestedObject() throws IOException {
        String json = "{\"person\":{\"name\":\"Jane Doe\",\"age\":25},\"city\":\"New York\"}";
        Object result = JsonParser.parse(json);
        assertInstanceOf(Map.class, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>)result;
        assertEquals("New York", map.get("city"));
        assertInstanceOf(Map.class, map.get("person"));
        @SuppressWarnings("unchecked")
        Map<String, Object> personMap = (Map<String, Object>)map.get("person");
        assertEquals("Jane Doe", personMap.get("name"));
        assertEquals(25, personMap.get("age"));
    }

    @Test
    void testParseNestedArray() throws IOException {
        String json = "{\"data\":[{\"id\":1},{\"id\":2}]}";
        Object result = JsonParser.parse(json);
        assertInstanceOf(Map.class, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>)result;
        assertInstanceOf(List.class, map.get("data"));
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>)map.get("data");
        assertEquals(2, list.size());
        assertInstanceOf(Map.class, list.getFirst());
        @SuppressWarnings("unchecked")
        Map<String, Object> item1 = (Map<String, Object>)list.getFirst();
        assertEquals(1, item1.get("id"));
    }

    @Test
    void testParseEmptyObject() throws IOException {
        String json = "{}";
        Object result = JsonParser.parse(json);
        assertInstanceOf(Map.class, result);
        assertTrue(((Map<?, ?>)result).isEmpty());
    }

    @Test
    void testParseEmptyArray() throws IOException {
        String json = "[]";
        Object result = JsonParser.parse(json);
        assertInstanceOf(List.class, result);
        assertTrue(((List<?>)result).isEmpty());
    }

    @Test
    void testParseNullInput() throws IOException {
        assertNull(JsonParser.parse(null));
    }

    @Test
    void testParseNumberTypes() throws IOException {
        String json = "{\"int\":123,\"long\":1234567890123,\"double\":123.45}";
        Object result = JsonParser.parse(json);
        assertInstanceOf(Map.class, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>)result;
        assertEquals(123, map.get("int"));
        assertEquals(1234567890123L, map.get("long"));
        assertEquals(123.45, map.get("double"));
    }

    void testWhitespaceHandling() throws IOException {
        String json = "  { \n \"key\" \t : \r \"value\" \n }  ";
        Object result = JsonParser.parse(json);
        assertInstanceOf(Map.class, result);
        Map<?, ?> map = (Map<?, ?>)result;
        assertEquals("value", map.get("key"));
        assertEquals(1, map.size());
    }

    @Test
    void testComplexStructure() throws IOException {
        String json = """
            {
              "id": "001",
              "type": "donut",
              "name": "Cake",
              "ppu": 0.55,
              "batters": {
                "batter": [
                  { "id": "1001", "type": "Regular" },
                  { "id": "1002", "type": "Chocolate" },
                  { "id": "1003", "type": "Blueberry" },
                  { "id": "1004", "type": "Devil's Food" }
                ]
              },
              "topping": [
                { "id": "5001", "type": "None" },
                { "id": "5002", "type": "Glazed" },
                { "id": "5005", "type": "Sugar" },
                { "id": "5007", "type": "Powdered Sugar" },
                { "id" : "5006", "type": "Chocolate with Sprinkles" },
                { "id": "5003", "type": "Chocolate" },
                { "id": "5004", "type": "Maple" }
              ]
            }""";
        Object result = JsonParser.parse(json);
        assertInstanceOf(Map.class, result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("001", map.get("id"));
        assertEquals("donut", map.get("type"));
        assertInstanceOf(Map.class, map.get("batters"));
        Map<?, ?> batters = (Map<?, ?>) map.get("batters");
        assertInstanceOf(List.class, batters.get("batter"));
        List<?> batterList = (List<?>) batters.get("batter");
        assertEquals(4, batterList.size());
        assertInstanceOf(List.class, map.get("topping"));
        List<?> toppingList = (List<?>) map.get("topping");
        assertEquals(7, toppingList.size());
    }

    @Test
    void testMalformedJson() {
        String json = "{\"name\":\"John Doe\",,}";
        assertThrows(MalformedJsonException.class, () -> JsonParser.parse(json));
    }

    @Test
    void testIncompleteJson() {
        String json = "{\"name\":\"John Doe\"";
        assertThrows(java.io.EOFException.class, () -> JsonParser.parse(json));
    }

}
